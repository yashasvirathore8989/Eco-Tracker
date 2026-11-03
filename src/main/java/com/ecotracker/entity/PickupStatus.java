package com.ecotracker.entity;

public enum PickupStatus {
    PENDING("Pending Review", "warning"),
    APPROVED("Approved", "info"),
    ASSIGNED("Collector Assigned", "primary"),
    PICKED_UP("Picked Up", "secondary"),
    COMPLETED("Completed", "success"),
    REJECTED("Rejected", "danger"),
    CANCELLED("Cancelled", "dark");

    private final String displayName;
    private final String badgeClass;

    PickupStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean isCancellable() {
        return this == PENDING || this == APPROVED;
    }
}
