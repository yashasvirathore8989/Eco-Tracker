package com.ecotracker.controller;

import com.ecotracker.dto.RewardSummaryDto;
import com.ecotracker.service.RewardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RewardWebController {

    private final RewardService rewardService;

    public RewardWebController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/rewards")
    public String rewards(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        RewardSummaryDto summary = rewardService.getRewardSummary(userDetails.getUsername());
        model.addAttribute("rewards", summary);
        return "user/rewards";
    }
}
