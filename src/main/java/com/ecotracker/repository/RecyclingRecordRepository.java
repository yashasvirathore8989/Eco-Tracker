package com.ecotracker.repository;

import com.ecotracker.entity.RecyclingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecyclingRecordRepository extends JpaRepository<RecyclingRecord, Long> {
    Optional<RecyclingRecord> findByRequestId(Long requestId);
}
