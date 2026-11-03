package com.ecotracker.dto;

import jakarta.validation.constraints.NotNull;

public class AssignCollectorDto {

    @NotNull(message = "Please select an active collector")
    private Long collectorId;

    private String notes;

    public AssignCollectorDto() {
    }

    public AssignCollectorDto(Long collectorId, String notes) {
        this.collectorId = collectorId;
        this.notes = notes;
    }

    public Long getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Long collectorId) {
        this.collectorId = collectorId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
