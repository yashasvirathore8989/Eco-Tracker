package com.ecotracker.controller;

import com.ecotracker.dto.PickupOutcomeDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.Collector;
import com.ecotracker.service.CollectorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/collector")
public class CollectorWebController {

    private final CollectorService collectorService;

    public CollectorWebController(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails collectorUser, Model model) {
        String email = collectorUser.getUsername();
        Collector profile = collectorService.getCollectorProfile(email);
        List<PickupRequestResponseDto> activeAssignments = collectorService.getCollectorAssignments(email);
        List<PickupRequestResponseDto> allHistory = collectorService.getAllCollectorHistory(email);

        model.addAttribute("collector", profile);
        model.addAttribute("assignments", activeAssignments);
        model.addAttribute("history", allHistory);
        return "collector/dashboard";
    }

    @PostMapping("/requests/{id}/pickup")
    public String recordPickup(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) BigDecimal verifiedQuantity,
            @AuthenticationPrincipal UserDetails collectorUser,
            RedirectAttributes redirectAttributes) {

        try {
            PickupOutcomeDto dto = new PickupOutcomeDto(notes, verifiedQuantity);
            collectorService.recordPickup(id, collectorUser.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Pickup recorded successfully for Request #" + id + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/collector/dashboard";
    }
}
