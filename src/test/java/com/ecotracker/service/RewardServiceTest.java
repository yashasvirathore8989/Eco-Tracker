package com.ecotracker.service;

import com.ecotracker.entity.*;
import com.ecotracker.repository.RewardTransactionRepository;
import com.ecotracker.repository.UserRepository;
import com.ecotracker.repository.UserStreakRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardTransactionRepository rewardTransactionRepository;

    @Mock
    private UserStreakRepository userStreakRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RewardService rewardService;

    private User user;
    private WasteType eWaste;
    private WasteType dryWaste;
    private PickupRequest request;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john@example.com", "hash", "+15550123");
        user.setId(10L);

        eWaste = new WasteType("E_WASTE", "Electronic Waste", true);
        dryWaste = new WasteType("DRY_WASTE", "Dry Waste", true);

        request = new PickupRequest();
        request.setId(100L);
        request.setUser(user);
        request.setWasteType(eWaste);
        request.setQuantity(BigDecimal.valueOf(10.0));
        request.setStatus(PickupStatus.COMPLETED);
    }

    @Test
    @DisplayName("FR-14: Award points calculation for E-Waste (50 base + 10/kg)")
    void testAwardPointsEWaste() {
        when(rewardTransactionRepository.findByRequestId(100L)).thenReturn(Optional.empty());
        when(userStreakRepository.findByUserId(10L)).thenReturn(Optional.of(new UserStreak(user)));
        when(rewardTransactionRepository.save(any(RewardTransaction.class))).thenAnswer(i -> i.getArgument(0));

        RewardTransaction tx = rewardService.awardPointsForCompletedRequest(request);

        assertNotNull(tx);
        // 50 base + 10 * 10 = 150 points
        assertEquals(150, tx.getPoints());
        verify(rewardTransactionRepository, times(1)).save(any(RewardTransaction.class));
    }

    @Test
    @DisplayName("FR-14: Award points calculation for Dry Waste (20 base + 5/kg)")
    void testAwardPointsDryWaste() {
        request.setWasteType(dryWaste);
        request.setQuantity(BigDecimal.valueOf(10.0));

        when(rewardTransactionRepository.findByRequestId(100L)).thenReturn(Optional.empty());
        when(userStreakRepository.findByUserId(10L)).thenReturn(Optional.of(new UserStreak(user)));
        when(rewardTransactionRepository.save(any(RewardTransaction.class))).thenAnswer(i -> i.getArgument(0));

        RewardTransaction tx = rewardService.awardPointsForCompletedRequest(request);

        assertNotNull(tx);
        // 20 base + 5 * 10 = 70 points
        assertEquals(70, tx.getPoints());
    }

    @Test
    @DisplayName("FR-14 Idempotency: Repeated call for same request returns existing transaction without duplicate save")
    void testAwardPointsIdempotent() {
        RewardTransaction existingTx = new RewardTransaction(user, request, 150, "Completed E WASTE pickup");
        when(rewardTransactionRepository.findByRequestId(100L)).thenReturn(Optional.of(existingTx));

        RewardTransaction result = rewardService.awardPointsForCompletedRequest(request);

        assertSame(existingTx, result);
        verify(rewardTransactionRepository, never()).save(any(RewardTransaction.class));
    }

    @Test
    @DisplayName("Streak Calculation: Consecutive days increments streak and awards bonus points")
    void testStreakIncrementAndBonus() {
        UserStreak existingStreak = new UserStreak(user);
        existingStreak.setCurrentStreak(2);
        existingStreak.setLongestStreak(2);
        existingStreak.setLastQualifyingDate(LocalDate.now().minusDays(1)); // yesterday

        when(rewardTransactionRepository.findByRequestId(100L)).thenReturn(Optional.empty());
        when(userStreakRepository.findByUserId(10L)).thenReturn(Optional.of(existingStreak));
        when(rewardTransactionRepository.save(any(RewardTransaction.class))).thenAnswer(i -> i.getArgument(0));

        RewardTransaction tx = rewardService.awardPointsForCompletedRequest(request);

        // Streak incremented to 3, plus 15 bonus streak points: 150 + 15 = 165 points
        assertEquals(165, tx.getPoints());
        assertEquals(3, existingStreak.getCurrentStreak());
        assertEquals(3, existingStreak.getLongestStreak());
        assertEquals(LocalDate.now(), existingStreak.getLastQualifyingDate());
    }
}
