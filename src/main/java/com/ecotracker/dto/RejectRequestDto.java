package com.ecotracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectRequestDto {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 5, max = 255, message = "Rejection reason must be between 5 and 255 characters")
    private String reason;

    public RejectRequestDto() {
    }

    public RejectRequestDto(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
