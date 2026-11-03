package com.ecotracker.service;

import com.ecotracker.dto.AdminSummaryDto;
import com.ecotracker.dto.CompleteRequestDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.*;
import com.ecotracker.exception.InvalidStatusTransitionException;
import com.ecotracker.exception.ResourceNotFoundException;
import com.ecotracker.exception.ValidationException;
import com.ecotracker.repository.*;
import com.ecotracker.util.DtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final PickupRequestRepository pickupRequestRepository;
    private final CollectorRepository collectorRepository;
    private final CollectorAssignmentRepository assignmentRepository;
    private final RequestStatusHistoryRepository statusHistoryRepository;
    private final RecyclingRecordRepository recyclingRecordRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public AdminService(PickupRequestRepository pickupRequestRepository,
                        CollectorRepository collectorRepository,
                        CollectorAssignmentRepository assignmentRepository,
                        RequestStatusHistoryRepository statusHistoryRepository,
                        RecyclingRecordRepository recyclingRecordRepository,
                        UserRepository userRepository,
                        RewardService rewardService) {
        this.pickupRequestRepository = pickupRequestRepository;
        this.collectorRepository = collectorRepository;
        this.assignmentRepository = assignmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.recyclingRecordRepository = recyclingRecordRepository;
        this.userRepository = userRepository;
        this.rewardService = rewardService;
    }

    @Transactional(readOnly = true)
    public AdminSummaryDto getAdminSummary() {
        AdminSummaryDto dto = new AdminSummaryDto();
        dto.setTotalRequests(pickupRequestRepository.count());
        dto.setPendingRequests(pickupRequestRepository.countByStatus(PickupStatus.PENDING));
        dto.setApprovedRequests(pickupRequestRepository.countByStatus(PickupStatus.APPROVED));
        dto.setAssignedRequests(pickupRequestRepository.countByStatus(PickupStatus.ASSIGNED));
        dto.setPickedUpRequests(pickupRequestRepository.countByStatus(PickupStatus.PICKED_UP));
        dto.setCompletedRequests(pickupRequestRepository.countByStatus(PickupStatus.COMPLETED));
        dto.setRejectedRequests(pickupRequestRepository.countByStatus(PickupStatus.REJECTED));
        dto.setCancelledRequests(pickupRequestRepository.countByStatus(PickupStatus.CANCELLED));

        dto.setTotalUsers(userRepository.count());
        dto.setTotalCollectors(collectorRepository.count());
        dto.setTotalPointsAwarded(rewardService.getTotalSystemPoints());
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<PickupRequestResponseDto> getFilteredRequests(PickupStatus status,
                                                             String wasteTypeCode,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate,
                                                             Pageable pageable) {
        return pickupRequestRepository.findWithFilters(status, wasteTypeCode, startDate, endDate, pageable)
                .map(DtoMapper::toPickupRequestDto);
    }

    @Transactional
    public PickupRequestResponseDto approveRequest(Long id, String adminEmail) {
        PickupRequest request = pickupRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + id + " not found."));

        if (request.getStatus() != PickupStatus.PENDING) {
            throw new InvalidStatusTransitionException("Only PENDING requests can be approved. Current status: " + request.getStatus());
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found."));

        PickupStatus oldStatus = request.getStatus();
        request.setStatus(PickupStatus.APPROVED);

        RequestStatusHistory history = new RequestStatusHistory(
                request,
                oldStatus,
                PickupStatus.APPROVED,
                admin,
                "Request reviewed and approved for collector assignment."
        );
        statusHistoryRepository.save(history);
        request.getStatusHistory().add(history);

        PickupRequest saved = pickupRequestRepository.save(request);
        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional
    public PickupRequestResponseDto rejectRequest(Long id, String adminEmail, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.trim().isBlank()) {
            throw new ValidationException("A clear rejection reason must be provided.");
        }

        PickupRequest request = pickupRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + id + " not found."));

        if (request.getStatus() != PickupStatus.PENDING && request.getStatus() != PickupStatus.APPROVED) {
            throw new InvalidStatusTransitionException("Only PENDING or APPROVED requests can be rejected.");
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found."));

        PickupStatus oldStatus = request.getStatus();
        request.setStatus(PickupStatus.REJECTED);

        RequestStatusHistory history = new RequestStatusHistory(
                request,
                oldStatus,
                PickupStatus.REJECTED,
                admin,
                "Rejected: " + rejectionReason.trim()
        );
        statusHistoryRepository.save(history);
        request.getStatusHistory().add(history);

        PickupRequest saved = pickupRequestRepository.save(request);
        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional
    public PickupRequestResponseDto assignCollector(Long requestId, Long collectorId, String adminEmail, String notes) {
        PickupRequest request = pickupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + requestId + " not found."));

        if (request.getStatus() != PickupStatus.APPROVED && request.getStatus() != PickupStatus.PENDING && request.getStatus() != PickupStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Cannot assign collector to a request in status: " + request.getStatus());
        }

        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collector #" + collectorId + " not found."));

        if (!collector.isActive()) {
            throw new ValidationException("Cannot assign an inactive collector.");
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found."));

        // Deactivate previous active assignment if exists
        assignmentRepository.findByRequestIdAndActiveTrue(requestId)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    assignmentRepository.save(existing);
                });

        CollectorAssignment newAssignment = new CollectorAssignment(request, collector, admin);
        assignmentRepository.save(newAssignment);

        PickupStatus oldStatus = request.getStatus();
        request.setStatus(PickupStatus.ASSIGNED);

        String noteText = String.format("Assigned to collector %s (%s). %s",
                collector.getUser().getFullName(),
                collector.getServiceArea(),
                notes != null && !notes.isBlank() ? "Note: " + notes.trim() : "");

        RequestStatusHistory history = new RequestStatusHistory(
                request,
                oldStatus,
                PickupStatus.ASSIGNED,
                admin,
                noteText
        );
        statusHistoryRepository.save(history);
        request.getStatusHistory().add(history);

        PickupRequest saved = pickupRequestRepository.save(request);
        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional
    public PickupRequestResponseDto completeRequest(Long requestId, String adminEmail, CompleteRequestDto dto) {
        PickupRequest request = pickupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + requestId + " not found."));

        if (request.getStatus() != PickupStatus.PICKED_UP && request.getStatus() != PickupStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Request must be in PICKED_UP or ASSIGNED status before completing. Current: " + request.getStatus());
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found."));

        // Create or update recycling record
        RecyclingRecord recyclingRecord = recyclingRecordRepository.findByRequestId(requestId)
                .orElse(new RecyclingRecord());
        recyclingRecord.setRequest(request);
        recyclingRecord.setRecyclerName(dto.getRecyclerName().trim());
        recyclingRecord.setMaterialWeight(dto.getMaterialWeight());
        recyclingRecord.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);
        recyclingRecord.setReceivedAt(LocalDateTime.now());
        recyclingRecordRepository.save(recyclingRecord);
        request.setRecyclingRecord(recyclingRecord);

        PickupStatus oldStatus = request.getStatus();
        request.setStatus(PickupStatus.COMPLETED);

        RequestStatusHistory history = new RequestStatusHistory(
                request,
                oldStatus,
                PickupStatus.COMPLETED,
                admin,
                String.format("Collection confirmed at %s (%s kg). Rewards issued.",
                        dto.getRecyclerName(), dto.getMaterialWeight())
        );
        statusHistoryRepository.save(history);
        request.getStatusHistory().add(history);

        PickupRequest saved = pickupRequestRepository.save(request);

        // Transactional, idempotent reward points issuance
        RewardTransaction rewardTx = rewardService.awardPointsForCompletedRequest(saved);
        saved.setRewardTransaction(rewardTx);

        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional(readOnly = true)
    public List<Collector> getAllActiveCollectors() {
        return collectorRepository.findByActiveTrue();
    }
}
