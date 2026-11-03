package com.ecotracker.service;

import com.ecotracker.dto.PickupRequestCreateDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.*;
import com.ecotracker.exception.InvalidStatusTransitionException;
import com.ecotracker.exception.ResourceNotFoundException;
import com.ecotracker.exception.UnauthorizedAccessException;
import com.ecotracker.exception.ValidationException;
import com.ecotracker.repository.PickupRequestRepository;
import com.ecotracker.repository.RequestStatusHistoryRepository;
import com.ecotracker.repository.UserRepository;
import com.ecotracker.repository.WasteTypeRepository;
import com.ecotracker.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PickupRequestService {

    private final PickupRequestRepository pickupRequestRepository;
    private final WasteTypeRepository wasteTypeRepository;
    private final UserRepository userRepository;
    private final RequestStatusHistoryRepository statusHistoryRepository;

    public PickupRequestService(PickupRequestRepository pickupRequestRepository,
                                WasteTypeRepository wasteTypeRepository,
                                UserRepository userRepository,
                                RequestStatusHistoryRepository statusHistoryRepository) {
        this.pickupRequestRepository = pickupRequestRepository;
        this.wasteTypeRepository = wasteTypeRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public PickupRequestResponseDto createRequest(PickupRequestCreateDto dto, String userEmail) {
        // 1. Resolve and validate User from security context
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // 2. Validate waste type
        WasteType wasteType = wasteTypeRepository.findByCodeIgnoreCase(dto.getWasteTypeCode().trim())
                .orElseThrow(() -> new ValidationException("Unsupported waste category: " + dto.getWasteTypeCode()));

        if (!wasteType.isEnabled()) {
            throw new ValidationException("The selected waste category is currently not available for pickup.");
        }

        // 3. Validate preferred date and time
        LocalDateTime preferredAt = dto.getPreferredAt();
        if (preferredAt == null || preferredAt.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Preferred pickup time must be at least 1 hour in the future.",
                    Map.of("preferredAt", "Pickup time must be in the future"));
        }

        LocalTime time = preferredAt.toLocalTime();
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(19, 0))) {
            throw new ValidationException("Preferred pickup time must be within supported service hours (08:00 AM to 07:00 PM).",
                    Map.of("preferredAt", "Service hours are 08:00 AM to 07:00 PM"));
        }

        // 4. Create and save PickupRequest with PENDING status
        PickupRequest request = new PickupRequest();
        request.setUser(user);
        request.setWasteType(wasteType);
        request.setDescription(dto.getDescription().trim());
        request.setQuantity(dto.getQuantity());
        request.setUnit(dto.getUnit().trim());
        request.setAddress(dto.getAddress().trim());
        request.setPreferredAt(preferredAt);
        request.setContactNumber(dto.getContactNumber() != null && !dto.getContactNumber().isBlank()
                ? dto.getContactNumber().trim()
                : user.getPhone());
        request.setStatus(PickupStatus.PENDING);

        PickupRequest saved = pickupRequestRepository.save(request);

        // 5. Create initial RequestStatusHistory in the same transaction
        RequestStatusHistory history = new RequestStatusHistory(
                saved,
                null,
                PickupStatus.PENDING,
                user,
                "Pickup request submitted by user."
        );
        statusHistoryRepository.save(history);
        saved.getStatusHistory().add(history);

        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PickupRequestResponseDto> getUserRequests(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return pickupRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(DtoMapper::toPickupRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PickupRequestResponseDto getAuthorizedRequestDetail(Long id, String currentUserEmail, boolean isAdmin, boolean isCollector) {
        PickupRequest request = pickupRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + id + " not found."));

        // Verify authorization: Owner, Admin, or Assigned Collector
        boolean isOwner = request.getUser().getEmail().equalsIgnoreCase(currentUserEmail);
        boolean isAssignedCollector = false;

        CollectorAssignment activeAssignment = request.getActiveAssignment();
        if (activeAssignment != null && activeAssignment.getCollector() != null && activeAssignment.getCollector().getUser() != null) {
            isAssignedCollector = activeAssignment.getCollector().getUser().getEmail().equalsIgnoreCase(currentUserEmail);
        }

        if (!isOwner && !isAdmin && !isAssignedCollector) {
            throw new UnauthorizedAccessException("You are not authorized to view this pickup request.");
        }

        return DtoMapper.toPickupRequestDto(request);
    }

    @Transactional
    public PickupRequestResponseDto cancelRequest(Long id, String userEmail, String cancellationReason, boolean isAdmin) {
        PickupRequest request = pickupRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + id + " not found."));

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Ownership or Admin check
        boolean isOwner = request.getUser().getEmail().equalsIgnoreCase(userEmail);
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this request.");
        }

        if (!request.getStatus().isCancellable()) {
            throw new InvalidStatusTransitionException("Cannot cancel request in status: " + request.getStatus().getDisplayName());
        }

        PickupStatus oldStatus = request.getStatus();
        request.setStatus(PickupStatus.CANCELLED);

        // Inactivate any active collector assignment if assigned
        CollectorAssignment activeAssignment = request.getActiveAssignment();
        if (activeAssignment != null) {
            activeAssignment.setActive(false);
        }

        String note = (cancellationReason != null && !cancellationReason.isBlank())
                ? "Cancelled: " + cancellationReason
                : (isOwner ? "Cancelled by user." : "Cancelled by admin.");

        RequestStatusHistory history = new RequestStatusHistory(request, oldStatus, PickupStatus.CANCELLED, user, note);
        statusHistoryRepository.save(history);
        request.getStatusHistory().add(history);

        PickupRequest saved = pickupRequestRepository.save(request);
        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional(readOnly = true)
    public long getActiveRequestCount(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);
        if (user == null) return 0;
        return pickupRequestRepository.countActiveRequestsByUserId(user.getId());
    }
}
