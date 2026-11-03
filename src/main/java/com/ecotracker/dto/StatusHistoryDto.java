package com.ecotracker.dto;

import com.ecotracker.entity.PickupStatus;
import java.time.LocalDateTime;

public class StatusHistoryDto {

    private PickupStatus oldStatus;
    private PickupStatus newStatus;
    private String changedByName;
    private String note;
    private LocalDateTime changedAt;

    public StatusHistoryDto() {
    }

    public StatusHistoryDto(PickupStatus oldStatus, PickupStatus newStatus, String changedByName, String note, LocalDateTime changedAt) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByName = changedByName;
        this.note = note;
        this.changedAt = changedAt;
    }

    public PickupStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(PickupStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public PickupStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PickupStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
