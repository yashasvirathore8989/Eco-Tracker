package com.ecotracker.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PickupRequestCreateDto {

    @NotBlank(message = "Waste category is required (E_WASTE or DRY_WASTE)")
    private String wasteTypeCode;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required (e.g. kg, items, bags)")
    private String unit;

    @NotBlank(message = "Pickup address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @NotNull(message = "Preferred pickup date and time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime preferredAt;

    private String contactNumber;

    public PickupRequestCreateDto() {
    }

    public PickupRequestCreateDto(String wasteTypeCode, String description, BigDecimal quantity, String unit, String address, LocalDateTime preferredAt, String contactNumber) {
        this.wasteTypeCode = wasteTypeCode;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.address = address;
        this.preferredAt = preferredAt;
        this.contactNumber = contactNumber;
    }

    public String getWasteTypeCode() {
        return wasteTypeCode;
    }

    public void setWasteTypeCode(String wasteTypeCode) {
        this.wasteTypeCode = wasteTypeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getPreferredAt() {
        return preferredAt;
    }

    public void setPreferredAt(LocalDateTime preferredAt) {
        this.preferredAt = preferredAt;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
