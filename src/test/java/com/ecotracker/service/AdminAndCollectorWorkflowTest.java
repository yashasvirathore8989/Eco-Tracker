package com.ecotracker.service;

import com.ecotracker.dto.CompleteRequestDto;
import com.ecotracker.dto.PickupOutcomeDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.*;
import com.ecotracker.exception.InvalidStatusTransitionException;
import com.ecotracker.exception.UnauthorizedAccessException;
import com.ecotracker.exception.ValidationException;
import com.ecotracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAndCollectorWorkflowTest {

    @Mock
    private PickupRequestRepository pickupRequestRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private CollectorAssignmentRepository assignmentRepository;
    @Mock
    private RequestStatusHistoryRepository statusHistoryRepository;
    @Mock
    private RecyclingRecordRepository recyclingRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RewardService rewardService;

    @InjectMocks
    private AdminService adminService;

    private CollectorService collectorService;

    private User admin;
    private User collectorUser;
    private User citizen;
    private Collector collector;
    private WasteType eWaste;
    private PickupRequest request;

    @BeforeEach
    void setUp() {
        collectorService = new CollectorService(collectorRepository, assignmentRepository, pickupRequestRepository, statusHistoryRepository);

        admin = new User("Admin", "admin@ecotracker.com", "hash", "+15550100");
        admin.setId(1L);

        collectorUser = new User("Alex", "alex@collector.com", "hash", "+15550199");
        collectorUser.setId(2L);

        collector = new Collector(collectorUser, "North Zone", true);
        collector.setId(20L);

        citizen = new User("Citizen", "citizen@example.com", "hash", "+15550111");
        citizen.setId(3L);

        eWaste = new WasteType("E_WASTE", "Electronic Waste", true);

        request = new PickupRequest();
        request.setId(200L);
        request.setUser(citizen);
        request.setWasteType(eWaste);
        request.setQuantity(BigDecimal.valueOf(10.0));
        request.setUnit("kg");
        request.setAddress("456 Eco Blvd");
        request.setPreferredAt(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setStatus(PickupStatus.PENDING);
    }

    @Test
    @DisplayName("Admin Workflow: Approve PENDING request sets status to APPROVED")
    void testApproveRequest() {
        when(pickupRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmailIgnoreCase("admin@ecotracker.com")).thenReturn(Optional.of(admin));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenAnswer(i -> i.getArgument(0));

        PickupRequestResponseDto result = adminService.approveRequest(200L, "admin@ecotracker.com");

        assertEquals(PickupStatus.APPROVED, result.getStatus());
        verify(statusHistoryRepository, times(1)).save(any(RequestStatusHistory.class));
    }

    @Test
    @DisplayName("Admin Workflow: Reject requires a valid reason")
    void testRejectRequiresReason() {
        assertThrows(ValidationException.class, () ->
                adminService.rejectRequest(200L, "admin@ecotracker.com", ""));
    }

    @Test
    @DisplayName("Admin Workflow: Assign active collector transitions status to ASSIGNED")
    void testAssignCollector() {
        request.setStatus(PickupStatus.APPROVED);

        when(pickupRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(collectorRepository.findById(20L)).thenReturn(Optional.of(collector));
        when(userRepository.findByEmailIgnoreCase("admin@ecotracker.com")).thenReturn(Optional.of(admin));
        when(assignmentRepository.findByRequestIdAndActiveTrue(200L)).thenReturn(Optional.empty());
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenAnswer(i -> i.getArgument(0));

        PickupRequestResponseDto result = adminService.assignCollector(200L, 20L, "admin@ecotracker.com", "Urgent pickup");

        assertEquals(PickupStatus.ASSIGNED, result.getStatus());
        verify(assignmentRepository, times(1)).save(any(CollectorAssignment.class));
    }

    @Test
    @DisplayName("Collector Workflow: Assigned collector records PICKED_UP")
    void testCollectorRecordPickup() {
        request.setStatus(PickupStatus.ASSIGNED);

        CollectorAssignment assignment = new CollectorAssignment(request, collector, admin);
        assignment.setActive(true);

        when(pickupRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(collectorRepository.findByUserEmailIgnoreCase("alex@collector.com")).thenReturn(Optional.of(collector));
        when(assignmentRepository.findByRequestIdAndActiveTrue(200L)).thenReturn(Optional.of(assignment));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenAnswer(i -> i.getArgument(0));

        PickupOutcomeDto outcome = new PickupOutcomeDto("Collected 10kg from doorstep", BigDecimal.valueOf(10.0));
        PickupRequestResponseDto result = collectorService.recordPickup(200L, "alex@collector.com", outcome);

        assertEquals(PickupStatus.PICKED_UP, result.getStatus());
        verify(statusHistoryRepository, times(1)).save(any(RequestStatusHistory.class));
    }

    @Test
    @DisplayName("Collector Security: Unassigned collector cannot record pickup")
    void testUnassignedCollectorCannotRecordPickup() {
        request.setStatus(PickupStatus.ASSIGNED);

        Collector otherCollector = new Collector(new User("Bob", "bob@collector.com", "hash", "+15550222"), "South Zone", true);
        otherCollector.setId(99L);

        CollectorAssignment assignment = new CollectorAssignment(request, collector, admin); // assigned to Alex (id 20)

        when(pickupRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(collectorRepository.findByUserEmailIgnoreCase("bob@collector.com")).thenReturn(Optional.of(otherCollector));
        when(assignmentRepository.findByRequestIdAndActiveTrue(200L)).thenReturn(Optional.of(assignment));

        assertThrows(UnauthorizedAccessException.class, () ->
                collectorService.recordPickup(200L, "bob@collector.com", new PickupOutcomeDto()));
    }

    @Test
    @DisplayName("Admin Workflow: Complete request creates recycling record and awards points")
    void testCompleteRequest() {
        request.setStatus(PickupStatus.PICKED_UP);

        when(pickupRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmailIgnoreCase("admin@ecotracker.com")).thenReturn(Optional.of(admin));
        when(recyclingRecordRepository.findByRequestId(200L)).thenReturn(Optional.empty());
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenAnswer(i -> i.getArgument(0));

        CompleteRequestDto dto = new CompleteRequestDto("GreenCycle Ltd", BigDecimal.valueOf(10.0), "Batch #981");
        PickupRequestResponseDto result = adminService.completeRequest(200L, "admin@ecotracker.com", dto);

        assertEquals(PickupStatus.COMPLETED, result.getStatus());
        verify(recyclingRecordRepository, times(1)).save(any(RecyclingRecord.class));
        verify(rewardService, times(1)).awardPointsForCompletedRequest(any(PickupRequest.class));
    }

    @Test
    @DisplayName("Status Lifecycle Constraint: Cannot jump directly from PENDING to COMPLETED")
    void testCannotJumpDirectlyFromPendingToCompleted() {
        request.setStatus(PickupStatus.PENDING);
        when(pickupRequestRepository.findById(200L)).thenReturn(Optional.of(request));

        CompleteRequestDto dto = new CompleteRequestDto("Recycler", BigDecimal.valueOf(10.0), "Notes");
        assertThrows(InvalidStatusTransitionException.class, () ->
                adminService.completeRequest(200L, "admin@ecotracker.com", dto));
    }
}
