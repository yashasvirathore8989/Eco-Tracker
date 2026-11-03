package com.ecotracker.repository;

import com.ecotracker.entity.PickupRequest;
import com.ecotracker.entity.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {

    List<PickupRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PickupRequest> findAllByOrderByCreatedAtDesc();

    Page<PickupRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM PickupRequest r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:wasteTypeCode IS NULL OR LOWER(r.wasteType.code) = LOWER(:wasteTypeCode)) AND " +
           "(:startDate IS NULL OR r.preferredAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.preferredAt <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    Page<PickupRequest> findWithFilters(@Param("status") PickupStatus status,
                                        @Param("wasteTypeCode") String wasteTypeCode,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    long countByStatus(PickupStatus status);

    long countByStatusIn(List<PickupStatus> statuses);

    @Query("SELECT COUNT(r) FROM PickupRequest r WHERE r.user.id = :userId AND r.status IN ('PENDING', 'APPROVED', 'ASSIGNED')")
    long countActiveRequestsByUserId(@Param("userId") Long userId);
}
