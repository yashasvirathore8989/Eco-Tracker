package com.ecotracker.service;

import com.ecotracker.dto.PickupOutcomeDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.*;
import com.ecotracker.exception.InvalidStatusTransitionException;
import com.ecotracker.exception.ResourceNotFoundException;
import com.ecotracker.exception.UnauthorizedAccessException;
import com.ecotracker.repository.CollectorAssignmentRepository;
import com.ecotracker.repository.CollectorRepository;
import com.ecotracker.repository.PickupRequestRepository;
import com.ecotracker.repository.RequestStatusHistoryRepository;
import com.ecotracker.util.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectorService {

    private final CollectorRepository collectorRepository;
    private final CollectorAssignmentRepository assignmentRepository;
    private final PickupRequestRepository pickupRequestRepository;
    private final RequestStatusHistoryRepository statusHistoryRepository;

    public CollectorService(CollectorRepository collectorRepository,
                            CollectorAssignmentRepository assignmentRepository,
                            PickupRequestRepository pickupRequestRepository,
                            RequestStatusHistoryRepository statusHistoryRepository) {
        this.collectorRepository = collectorRepository;
        this.assignmentRepository = assignmentRepository;
        this.pickupRequestRepository = pickupRequestRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<PickupRequestResponseDto> getCollectorAssignments(String collectorEmail) {
        List<CollectorAssignment> assignments = assignmentRepository.findActiveByCollectorEmail(collectorEmail);
        return assignments.stream()
                .map(CollectorAssignment::getRequest)
                .map(DtoMapper::toPickupRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PickupRequestResponseDto> getAllCollectorHistory(String collectorEmail) {
        List<CollectorAssignment> assignments = assignmentRepository.findAllByCollectorEmail(collectorEmail);
        return assignments.stream()
                .map(CollectorAssignment::getRequest)
                .distinct()
                .map(DtoMapper::toPickupRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PickupRequestResponseDto recordPickup(Long requestId, String collectorEmail, PickupOutcomeDto dto) {
        PickupRequest request = pickupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup request #" + requestId + " not found."));

        Collector collector = collectorRepository.findByUserEmailIgnoreCase(collectorEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("You are not registered as an authorized collector."));

        // Verify assignment ownership: Must have active assignment for this collector
        CollectorAssignment assignment = assignmentRepository.findByRequestIdAndActiveTrue(requestId)
                .orElseThrow(() -> new UnauthorizedAccessException("This request is not currently assigned to any active collector."));

        if (!assignment.getCollector().getId().equals(collector.getId())) {
            throw new UnauthorizedAccessException("Forbidden: This pickup request is not assigned to you.");
        }

        if (request.getStatus() != PickupStatus.ASSIGNED) {
            throw new InvalidStatusTransitionException("Only ASSIGNED requests can be marked as PICKED_UP. Current status: " + request.getStatus());
        }

        PickupStatus oldStatus = request.getStatus();
        request.setStatus(PickupStatus.PICKED_UP);

        if (dto.getVerifiedQuantity() != null && dto.getVerifiedQuantity().signum() > 0) {
            request.setQuantity(dto.getVerifiedQuantity());
        }

        String note = "Pickup completed by collector " + collector.getUser().getFullName() + ".";
        if (dto.getNotes() != null && !dto.getNotes().isBlank()) {
            note += " Notes: " + dto.getNotes().trim();
        }

        RequestStatusHistory history = new RequestStatusHistory(
                request,
                oldStatus,
                PickupStatus.PICKED_UP,
                collector.getUser(),
                note
        );
        statusHistoryRepository.save(history);
        request.getStatusHistory().add(history);

        PickupRequest saved = pickupRequestRepository.save(request);
        return DtoMapper.toPickupRequestDto(saved);
    }

    @Transactional(readOnly = true)
    public Collector getCollectorProfile(String email) {
        return collectorRepository.findByUserEmailIgnoreCase(email)
                .orElse(null);
    }
}
