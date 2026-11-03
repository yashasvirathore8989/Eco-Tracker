package com.ecotracker.controller;

import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.dto.RewardSummaryDto;
import com.ecotracker.dto.UserResponse;
import com.ecotracker.service.AuthService;
import com.ecotracker.service.PickupRequestService;
import com.ecotracker.service.RewardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserDashboardController {

    private final PickupRequestService pickupRequestService;
    private final RewardService rewardService;
    private final AuthService authService;

    public UserDashboardController(PickupRequestService pickupRequestService,
                                   RewardService rewardService,
                                   AuthService authService) {
        this.pickupRequestService = pickupRequestService;
        this.rewardService = rewardService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        UserResponse userProfile = authService.getUserProfile(email);
        List<PickupRequestResponseDto> requests = pickupRequestService.getUserRequests(email);
        RewardSummaryDto rewards = rewardService.getRewardSummary(email);
        long activeCount = pickupRequestService.getActiveRequestCount(email);

        model.addAttribute("user", userProfile);
        model.addAttribute("requests", requests);
        model.addAttribute("rewards", rewards);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("totalCount", requests.size());

        return "user/dashboard";
    }
}
