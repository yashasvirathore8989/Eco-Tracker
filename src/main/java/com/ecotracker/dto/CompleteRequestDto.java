package com.ecotracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CompleteRequestDto {

    @NotBlank(message = "Recycler name is required")
    private String recyclerName;

    @NotNull(message = "Material weight is required")
    @DecimalMin(value = "0.01", message = "Material weight must be greater than zero")
    private BigDecimal materialWeight;

    private String notes;

    public CompleteRequestDto() {
    }

    public CompleteRequestDto(String recyclerName, BigDecimal materialWeight, String notes) {
        this.recyclerName = recyclerName;
        this.materialWeight = materialWeight;
        this.notes = notes;
    }

    public String getRecyclerName() {
        return recyclerName;
    }

    public void setRecyclerName(String recyclerName) {
        this.recyclerName = recyclerName;
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
