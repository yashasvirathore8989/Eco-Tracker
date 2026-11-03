package com.ecotracker.dto;

import java.time.LocalDate;
import java.util.List;

public class RewardSummaryDto {

    private Long totalPoints;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastQualifyingDate;
    private List<RewardTransactionDto> transactions;

    public RewardSummaryDto() {
    }

    public RewardSummaryDto(Long totalPoints, int currentStreak, int longestStreak, LocalDate lastQualifyingDate, List<RewardTransactionDto> transactions) {
        this.totalPoints = totalPoints;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastQualifyingDate = lastQualifyingDate;
        this.transactions = transactions;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastQualifyingDate() {
        return lastQualifyingDate;
    }

    public void setLastQualifyingDate(LocalDate lastQualifyingDate) {
        this.lastQualifyingDate = lastQualifyingDate;
    }

    public List<RewardTransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<RewardTransactionDto> transactions) {
        this.transactions = transactions;
    }
}
