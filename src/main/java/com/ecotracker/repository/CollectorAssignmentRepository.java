package com.ecotracker.repository;

import com.ecotracker.entity.CollectorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorAssignmentRepository extends JpaRepository<CollectorAssignment, Long> {

    List<CollectorAssignment> findByCollectorIdAndActiveTrue(Long collectorId);

    @Query("SELECT a FROM CollectorAssignment a WHERE LOWER(a.collector.user.email) = LOWER(:email) AND a.active = true ORDER BY a.assignedAt DESC")
    List<CollectorAssignment> findActiveByCollectorEmail(@Param("email") String email);

    @Query("SELECT a FROM CollectorAssignment a WHERE LOWER(a.collector.user.email) = LOWER(:email) ORDER BY a.assignedAt DESC")
    List<CollectorAssignment> findAllByCollectorEmail(@Param("email") String email);

    Optional<CollectorAssignment> findByRequestIdAndActiveTrue(Long requestId);
}
