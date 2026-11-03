package com.ecotracker.repository;

import com.ecotracker.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    @Query("SELECT s FROM UserStreak s WHERE s.user.id = :userId")
    Optional<UserStreak> findByUserId(@Param("userId") Long userId);
}
