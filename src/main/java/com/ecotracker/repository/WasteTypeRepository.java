package com.ecotracker.repository;

import com.ecotracker.entity.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WasteTypeRepository extends JpaRepository<WasteType, Long> {
    Optional<WasteType> findByCodeIgnoreCase(String code);
    List<WasteType> findByEnabledTrue();
}
