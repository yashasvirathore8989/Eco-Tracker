package com.ecotracker.dto;

import com.ecotracker.entity.PickupStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PickupRequestResponseDto {

    private Long id;
    private String referenceNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String wasteTypeCode;
    private String wasteTypeName;
    private String description;
    private BigDecimal quantity;
    private String unit;
    private String address;
    private String contactNumber;
    private LocalDateTime preferredAt;
    private PickupStatus status;
    private String statusDisplayName;
    private String statusBadgeClass;
    private LocalDateTime createdAt;
    private Long collectorId;
    private String collectorName;
    private String collectorPhone;
    private String collectorServiceArea;
    private List<StatusHistoryDto> history;
    private RecyclingRecordDto recyclingRecord;
    private Integer rewardPoints;
    private boolean cancellable;

    public PickupRequestResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getWasteTypeCode() {
        return wasteTypeCode;
    }

    public void setWasteTypeCode(String wasteTypeCode) {
        this.wasteTypeCode = wasteTypeCode;
    }

    public String getWasteTypeName() {
        return wasteTypeName;
    }

    public void setWasteTypeName(String wasteTypeName) {
        this.wasteTypeName = wasteTypeName;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDateTime getPreferredAt() {
        return preferredAt;
    }

    public void setPreferredAt(LocalDateTime preferredAt) {
        this.preferredAt = preferredAt;
    }

    public PickupStatus getStatus() {
        return status;
    }

    public void setStatus(PickupStatus status) {
        this.status = status;
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
            this.statusBadgeClass = status.getBadgeClass();
            this.cancellable = status.isCancellable();
        }
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

    public void setStatusBadgeClass(String statusBadgeClass) {
        this.statusBadgeClass = statusBadgeClass;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Long collectorId) {
        this.collectorId = collectorId;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public String getCollectorPhone() {
        return collectorPhone;
    }

    public void setCollectorPhone(String collectorPhone) {
        this.collectorPhone = collectorPhone;
    }

    public String getCollectorServiceArea() {
        return collectorServiceArea;
    }

    public void setCollectorServiceArea(String collectorServiceArea) {
        this.collectorServiceArea = collectorServiceArea;
    }

    public List<StatusHistoryDto> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistoryDto> history) {
        this.history = history;
    }

    public RecyclingRecordDto getRecyclingRecord() {
        return recyclingRecord;
    }

    public void setRecyclingRecord(RecyclingRecordDto recyclingRecord) {
        this.recyclingRecord = recyclingRecord;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }
}
