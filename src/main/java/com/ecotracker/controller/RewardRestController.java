package com.ecotracker.controller;

import com.ecotracker.dto.ApiResponse;
import com.ecotracker.dto.RewardSummaryDto;
import com.ecotracker.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardRestController {

    private final RewardService rewardService;

    public RewardRestController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<RewardSummaryDto>> getMyRewards(
            @AuthenticationPrincipal UserDetails userDetails) {

        RewardSummaryDto summary = rewardService.getRewardSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User rewards and ledger retrieved", summary));
    }
}
