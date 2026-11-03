package com.ecotracker.dto;

import java.math.BigDecimal;

public class PickupOutcomeDto {

    private String notes;
    private BigDecimal verifiedQuantity;

    public PickupOutcomeDto() {
    }

    public PickupOutcomeDto(String notes, BigDecimal verifiedQuantity) {
        this.notes = notes;
        this.verifiedQuantity = verifiedQuantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getVerifiedQuantity() {
        return verifiedQuantity;
    }

    public void setVerifiedQuantity(BigDecimal verifiedQuantity) {
        this.verifiedQuantity = verifiedQuantity;
    }
}
