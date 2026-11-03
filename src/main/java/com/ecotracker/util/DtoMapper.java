package com.ecotracker.util;

import com.ecotracker.dto.*;
import com.ecotracker.entity.*;

import java.util.stream.Collectors;

public class DtoMapper {

    public static PickupRequestResponseDto toPickupRequestDto(PickupRequest request) {
        if (request == null) return null;

        PickupRequestResponseDto dto = new PickupRequestResponseDto();
        dto.setId(request.getId());
        dto.setReferenceNumber(request.getReferenceNumber());
        if (request.getUser() != null) {
            dto.setUserId(request.getUser().getId());
            dto.setUserName(request.getUser().getFullName());
            dto.setUserEmail(request.getUser().getEmail());
            dto.setUserPhone(request.getUser().getPhone());
        }
        if (request.getWasteType() != null) {
            dto.setWasteTypeCode(request.getWasteType().getCode());
            dto.setWasteTypeName(request.getWasteType().getDisplayName());
        }
        dto.setDescription(request.getDescription());
        dto.setQuantity(request.getQuantity());
        dto.setUnit(request.getUnit());
        dto.setAddress(request.getAddress());
        dto.setContactNumber(request.getContactNumber());
        dto.setPreferredAt(request.getPreferredAt());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());

        CollectorAssignment activeAssignment = request.getActiveAssignment();
        if (activeAssignment != null && activeAssignment.getCollector() != null) {
            Collector collector = activeAssignment.getCollector();
            dto.setCollectorId(collector.getId());
            dto.setCollectorServiceArea(collector.getServiceArea());
            if (collector.getUser() != null) {
                dto.setCollectorName(collector.getUser().getFullName());
                dto.setCollectorPhone(collector.getUser().getPhone());
            }
        }

        if (request.getStatusHistory() != null) {
            dto.setHistory(request.getStatusHistory().stream()
                    .map(DtoMapper::toStatusHistoryDto)
                    .collect(Collectors.toList()));
        }

        if (request.getRecyclingRecord() != null) {
            dto.setRecyclingRecord(toRecyclingRecordDto(request.getRecyclingRecord()));
        }

        if (request.getRewardTransaction() != null) {
            dto.setRewardPoints(request.getRewardTransaction().getPoints());
        }

        return dto;
    }

    public static StatusHistoryDto toStatusHistoryDto(RequestStatusHistory history) {
        if (history == null) return null;
        StatusHistoryDto dto = new StatusHistoryDto();
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        if (history.getChangedBy() != null) {
            dto.setChangedByName(history.getChangedBy().getFullName());
        } else {
            dto.setChangedByName("System");
        }
        dto.setNote(history.getNote());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }

    public static RecyclingRecordDto toRecyclingRecordDto(RecyclingRecord record) {
        if (record == null) return null;
        RecyclingRecordDto dto = new RecyclingRecordDto();
        dto.setId(record.getId());
        dto.setRecyclerName(record.getRecyclerName());
        dto.setReceivedAt(record.getReceivedAt());
        dto.setMaterialWeight(record.getMaterialWeight());
        dto.setNotes(record.getNotes());
        return dto;
    }

    public static RewardTransactionDto toRewardTransactionDto(RewardTransaction transaction) {
        if (transaction == null) return null;
        RewardTransactionDto dto = new RewardTransactionDto();
        dto.setId(transaction.getId());
        if (transaction.getRequest() != null) {
            dto.setRequestId(transaction.getRequest().getId());
            dto.setRequestReference(transaction.getRequest().getReferenceNumber());
        }
        dto.setPoints(transaction.getPoints());
        dto.setReason(transaction.getReason());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}
