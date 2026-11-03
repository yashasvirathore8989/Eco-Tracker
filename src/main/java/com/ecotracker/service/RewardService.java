package com.ecotracker.service;

import com.ecotracker.dto.RewardSummaryDto;
import com.ecotracker.dto.RewardTransactionDto;
import com.ecotracker.entity.*;
import com.ecotracker.exception.ResourceNotFoundException;
import com.ecotracker.repository.RewardTransactionRepository;
import com.ecotracker.repository.UserRepository;
import com.ecotracker.repository.UserStreakRepository;
import com.ecotracker.util.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

    private final RewardTransactionRepository rewardTransactionRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserRepository userRepository;

    public RewardService(RewardTransactionRepository rewardTransactionRepository,
                         UserStreakRepository userStreakRepository,
                         UserRepository userRepository) {
        this.rewardTransactionRepository = rewardTransactionRepository;
        this.userStreakRepository = userStreakRepository;
        this.userRepository = userRepository;
    }

    /**
     * Calculates and issues reward points idempotently for a completed pickup request.
     * Guaranteed to award points at most once per request.
     */
    @Transactional
    public RewardTransaction awardPointsForCompletedRequest(PickupRequest request) {
        if (request == null || request.getUser() == null) {
            throw new IllegalArgumentException("Valid pickup request and user are required to award points");
        }

        // Idempotency check: if reward already exists for this request, return it immediately without duplicate points
        Optional<RewardTransaction> existing = rewardTransactionRepository.findByRequestId(request.getId());
        if (existing.isPresent()) {
            log.info("Reward already awarded for request id: {}", request.getId());
            return existing.get();
        }

        User user = request.getUser();
        UserStreak streak = userStreakRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserStreak newStreak = new UserStreak(user);
                    return userStreakRepository.save(newStreak);
                });

        // 1. Calculate points based on waste type and quantity
        int basePoints = 20;
        int pointsPerUnit = 5;
        String wasteTypeCode = request.getWasteType() != null ? request.getWasteType().getCode() : "DRY_WASTE";

        if ("E_WASTE".equalsIgnoreCase(wasteTypeCode)) {
            basePoints = 50;
            pointsPerUnit = 10;
        }

        BigDecimal qty = request.getQuantity() != null ? request.getQuantity() : BigDecimal.ONE;
        int quantityPoints = qty.multiply(BigDecimal.valueOf(pointsPerUnit)).intValue();
        int calculatedPoints = basePoints + Math.max(0, quantityPoints);

        // 2. Update Streak logic
        LocalDate today = LocalDate.now();
        LocalDate lastDate = streak.getLastQualifyingDate();
        boolean streakMaintained = false;

        if (lastDate == null) {
            streak.setCurrentStreak(1);
        } else if (lastDate.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streakMaintained = true;
        } else if (lastDate.equals(today)) {
            // Already recycled today; keep current streak
        } else {
            // Streak broken; reset to 1
            streak.setCurrentStreak(1);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streak.setLastQualifyingDate(today);
        userStreakRepository.save(streak);

        // Add bonus points if maintaining streak
        int streakBonus = streakMaintained && streak.getCurrentStreak() >= 2 ? 15 : 0;
        int totalPoints = calculatedPoints + streakBonus;

        String reason = String.format("Completed %s pickup (#%s)%s",
                wasteTypeCode.replace("_", " "),
                request.getReferenceNumber(),
                streakBonus > 0 ? " + " + streakBonus + " streak bonus" : "");

        RewardTransaction transaction = new RewardTransaction(user, request, totalPoints, reason);
        RewardTransaction saved = rewardTransactionRepository.save(transaction);
        log.info("Awarded {} points to user {} for request {}", totalPoints, user.getEmail(), request.getReferenceNumber());
        return saved;
    }

    @Transactional(readOnly = true)
    public RewardSummaryDto getRewardSummary(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Long totalPoints = rewardTransactionRepository.getTotalPointsByUserId(user.getId());
        UserStreak streak = userStreakRepository.findByUserId(user.getId())
                .orElse(new UserStreak(user));

        List<RewardTransactionDto> transactions = rewardTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(DtoMapper::toRewardTransactionDto)
                .collect(Collectors.toList());

        return new RewardSummaryDto(
                totalPoints,
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastQualifyingDate(),
                transactions
        );
    }

    @Transactional(readOnly = true)
    public Long getTotalSystemPoints() {
        return rewardTransactionRepository.getTotalSystemPoints();
    }
}
