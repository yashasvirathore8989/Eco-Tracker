package com.ecotracker.repository;

import com.ecotracker.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {
    Optional<Collector> findByUserId(Long userId);
    Optional<Collector> findByUserEmailIgnoreCase(String email);
    List<Collector> findByActiveTrue();
}
