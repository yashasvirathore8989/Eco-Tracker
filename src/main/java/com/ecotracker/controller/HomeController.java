package com.ecotracker.controller;

import com.ecotracker.repository.PickupRequestRepository;
import com.ecotracker.repository.UserRepository;
import com.ecotracker.repository.WasteTypeRepository;
import com.ecotracker.service.RewardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final WasteTypeRepository wasteTypeRepository;
    private final PickupRequestRepository pickupRequestRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;

    public HomeController(WasteTypeRepository wasteTypeRepository,
                          PickupRequestRepository pickupRequestRepository,
                          UserRepository userRepository,
                          RewardService rewardService) {
        this.wasteTypeRepository = wasteTypeRepository;
        this.pickupRequestRepository = pickupRequestRepository;
        this.userRepository = userRepository;
        this.rewardService = rewardService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("wasteTypes", wasteTypeRepository.findByEnabledTrue());
        model.addAttribute("totalPickups", pickupRequestRepository.count());
        model.addAttribute("totalCompleted", pickupRequestRepository.countByStatus(com.ecotracker.entity.PickupStatus.COMPLETED));
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalPoints", rewardService.getTotalSystemPoints());
        return "home";
    }
}
