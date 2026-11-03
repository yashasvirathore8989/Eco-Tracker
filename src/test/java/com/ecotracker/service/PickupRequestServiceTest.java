package com.ecotracker.service;

import com.ecotracker.dto.PickupRequestCreateDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.*;
import com.ecotracker.exception.InvalidStatusTransitionException;
import com.ecotracker.exception.UnauthorizedAccessException;
import com.ecotracker.exception.ValidationException;
import com.ecotracker.repository.PickupRequestRepository;
import com.ecotracker.repository.RequestStatusHistoryRepository;
import com.ecotracker.repository.UserRepository;
import com.ecotracker.repository.WasteTypeRepository;
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
class PickupRequestServiceTest {

    @Mock
    private PickupRequestRepository pickupRequestRepository;

    @Mock
    private WasteTypeRepository wasteTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestStatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private PickupRequestService pickupRequestService;

    private User user;
    private WasteType eWaste;

    @BeforeEach
    void setUp() {
        user = new User("Alice Green", "alice@example.com", "hash", "+15551234");
        user.setId(1L);

        eWaste = new WasteType("E_WASTE", "Electronic Waste", true);
    }

    @Test
    @DisplayName("FR-05 & FR-12: Create valid request in PENDING status and record initial history")
    void testCreateValidRequest() {
        LocalDateTime futureSlot = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        PickupRequestCreateDto dto = new PickupRequestCreateDto(
                "E_WASTE", "2 broken desktop towers", BigDecimal.valueOf(12.5),
                "kg", "123 Green Way", futureSlot, "+15551234"
        );

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(wasteTypeRepository.findByCodeIgnoreCase("E_WASTE")).thenReturn(Optional.of(eWaste));
        when(pickupRequestRepository.save(any(PickupRequest.class))).thenAnswer(i -> {
            PickupRequest saved = i.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        PickupRequestResponseDto response = pickupRequestService.createRequest(dto, "alice@example.com");

        assertNotNull(response);
        assertEquals(PickupStatus.PENDING, response.getStatus());
        assertEquals("REQ-00101", response.getReferenceNumber());
        verify(statusHistoryRepository, times(1)).save(any(RequestStatusHistory.class));
    }

    @Test
    @DisplayName("FR-06: Reject pickup request with past time")
    void testRejectPastPickupTime() {
        LocalDateTime pastSlot = LocalDateTime.now().minusHours(2);
        PickupRequestCreateDto dto = new PickupRequestCreateDto(
                "E_WASTE", "2 broken desktop towers", BigDecimal.valueOf(12.5),
                "kg", "123 Green Way", pastSlot, "+15551234"
        );

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(wasteTypeRepository.findByCodeIgnoreCase("E_WASTE")).thenReturn(Optional.of(eWaste));

        assertThrows(ValidationException.class, () ->
                pickupRequestService.createRequest(dto, "alice@example.com"));
    }

    @Test
    @DisplayName("FR-06: Reject pickup request outside service hours (e.g. 2:00 AM)")
    void testRejectOutsideServiceHours() {
        LocalDateTime nightSlot = LocalDateTime.now().plusDays(1).withHour(2).withMinute(0);
        PickupRequestCreateDto dto = new PickupRequestCreateDto(
                "E_WASTE", "Broken printers", BigDecimal.valueOf(5.0),
                "kg", "123 Green Way", nightSlot, "+15551234"
        );

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(wasteTypeRepository.findByCodeIgnoreCase("E_WASTE")).thenReturn(Optional.of(eWaste));

        assertThrows(ValidationException.class, () ->
                pickupRequestService.createRequest(dto, "alice@example.com"));
    }

    @Test
    @DisplayName("FR-07 & Ownership: Non-owner citizen cannot access another user's request")
    void testOwnershipSecurityCheck() {
        PickupRequest req = new PickupRequest();
        req.setId(50L);
        req.setUser(user); // owned by alice@example.com
        req.setStatus(PickupStatus.PENDING);

        when(pickupRequestRepository.findById(50L)).thenReturn(Optional.of(req));

        assertThrows(UnauthorizedAccessException.class, () ->
                pickupRequestService.getAuthorizedRequestDetail(50L, "hacker@example.com", false, false));
    }

    @Test
    @DisplayName("Cancellation Policy: Cannot cancel a COMPLETED request")
    void testCannotCancelCompletedRequest() {
        PickupRequest req = new PickupRequest();
        req.setId(50L);
        req.setUser(user);
        req.setStatus(PickupStatus.COMPLETED);

        when(pickupRequestRepository.findById(50L)).thenReturn(Optional.of(req));
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidStatusTransitionException.class, () ->
                pickupRequestService.cancelRequest(50L, "alice@example.com", "Changed mind", false));
    }
}
