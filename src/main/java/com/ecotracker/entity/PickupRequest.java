package com.ecotracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pickup_requests", indexes = {
    @Index(name = "idx_pickup_requests_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_pickup_requests_status_preferred", columnList = "status, preferred_at")
})
public class PickupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "waste_type_id", nullable = false)
    private WasteType wasteType;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit; // "kg", "items", "bags"

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "preferred_at", nullable = false)
    private LocalDateTime preferredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PickupStatus status = PickupStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("assignedAt DESC")
    private List<CollectorAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC")
    private List<RequestStatusHistory> statusHistory = new ArrayList<>();

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RecyclingRecord recyclingRecord;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RewardTransaction rewardTransaction;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PickupStatus.PENDING;
        }
    }

    public PickupRequest() {
    }

    public PickupRequest(User user, WasteType wasteType, String description, BigDecimal quantity, String unit, String address, LocalDateTime preferredAt, String contactNumber) {
        this.user = user;
        this.wasteType = wasteType;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.address = address;
        this.preferredAt = preferredAt;
        this.contactNumber = contactNumber;
        this.status = PickupStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WasteType getWasteType() {
        return wasteType;
    }

    public void setWasteType(WasteType wasteType) {
        this.wasteType = wasteType;
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
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<CollectorAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<CollectorAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<RequestStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<RequestStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public RecyclingRecord getRecyclingRecord() {
        return recyclingRecord;
    }

    public void setRecyclingRecord(RecyclingRecord recyclingRecord) {
        this.recyclingRecord = recyclingRecord;
        if (recyclingRecord != null) {
            recyclingRecord.setRequest(this);
        }
    }

    public RewardTransaction getRewardTransaction() {
        return rewardTransaction;
    }

    public void setRewardTransaction(RewardTransaction rewardTransaction) {
        this.rewardTransaction = rewardTransaction;
        if (rewardTransaction != null) {
            rewardTransaction.setRequest(this);
        }
    }

    public CollectorAssignment getActiveAssignment() {
        if (assignments == null) return null;
        return assignments.stream().filter(CollectorAssignment::isActive).findFirst().orElse(null);
    }

    public String getReferenceNumber() {
        return String.format("REQ-%05d", id != null ? id : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PickupRequest that = (PickupRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
