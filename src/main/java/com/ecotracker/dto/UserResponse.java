package com.ecotracker.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private Long totalPoints;
    private int currentStreak;
    private int longestStreak;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, String phone, Set<String> roles, LocalDateTime createdAt, Long totalPoints, int currentStreak, int longestStreak) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roles = roles;
        this.createdAt = createdAt;
        this.totalPoints = totalPoints;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
}
