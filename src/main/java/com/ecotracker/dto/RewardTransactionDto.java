package com.ecotracker.dto;

import java.time.LocalDateTime;

public class RewardTransactionDto {

    private Long id;
    private Long requestId;
    private String requestReference;
    private Integer points;
    private String reason;
    private LocalDateTime createdAt;

    public RewardTransactionDto() {
    }

    public RewardTransactionDto(Long id, Long requestId, String requestReference, Integer points, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.requestReference = requestReference;
        this.points = points;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
