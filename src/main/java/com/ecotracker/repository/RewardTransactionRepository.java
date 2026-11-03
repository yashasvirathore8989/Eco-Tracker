package com.ecotracker.repository;

import com.ecotracker.entity.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

    List<RewardTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RewardTransaction> findByRequestId(Long requestId);

    boolean existsByRequestId(Long requestId);

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardTransaction r WHERE r.user.id = :userId")
    Long getTotalPointsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardTransaction r")
    Long getTotalSystemPoints();
}
