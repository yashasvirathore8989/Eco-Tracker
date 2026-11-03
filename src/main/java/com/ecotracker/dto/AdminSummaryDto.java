package com.ecotracker.dto;

public class AdminSummaryDto {

    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long assignedRequests;
    private long pickedUpRequests;
    private long completedRequests;
    private long rejectedRequests;
    private long cancelledRequests;
    private long totalUsers;
    private long totalCollectors;
    private long totalPointsAwarded;

    public AdminSummaryDto() {
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getApprovedRequests() {
        return approvedRequests;
    }

    public void setApprovedRequests(long approvedRequests) {
        this.approvedRequests = approvedRequests;
    }

    public long getAssignedRequests() {
        return assignedRequests;
    }

    public void setAssignedRequests(long assignedRequests) {
        this.assignedRequests = assignedRequests;
    }

    public long getPickedUpRequests() {
        return pickedUpRequests;
    }

    public void setPickedUpRequests(long pickedUpRequests) {
        this.pickedUpRequests = pickedUpRequests;
    }

    public long getCompletedRequests() {
        return completedRequests;
    }

    public void setCompletedRequests(long completedRequests) {
        this.completedRequests = completedRequests;
    }

    public long getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(long rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }

    public long getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(long cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCollectors() {
        return totalCollectors;
    }

    public void setTotalCollectors(long totalCollectors) {
        this.totalCollectors = totalCollectors;
    }

    public long getTotalPointsAwarded() {
        return totalPointsAwarded;
    }

    public void setTotalPointsAwarded(long totalPointsAwarded) {
        this.totalPointsAwarded = totalPointsAwarded;
    }
}
