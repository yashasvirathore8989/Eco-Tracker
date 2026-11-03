package com.ecotracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecyclingRecordDto {

    private Long id;
    private String recyclerName;
    private LocalDateTime receivedAt;
    private BigDecimal materialWeight;
    private String notes;

    public RecyclingRecordDto() {
    }

    public RecyclingRecordDto(Long id, String recyclerName, LocalDateTime receivedAt, BigDecimal materialWeight, String notes) {
        this.id = id;
        this.recyclerName = recyclerName;
        this.receivedAt = receivedAt;
        this.materialWeight = materialWeight;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecyclerName() {
        return recyclerName;
    }

    public void setRecyclerName(String recyclerName) {
        this.recyclerName = recyclerName;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public BigDecimal getMaterialWeight() {
        return materialWeight;
    }

    public void setMaterialWeight(BigDecimal materialWeight) {
        this.materialWeight = materialWeight;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
