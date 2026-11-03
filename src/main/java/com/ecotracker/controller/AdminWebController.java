package com.ecotracker.controller;

import com.ecotracker.dto.AdminSummaryDto;
import com.ecotracker.dto.CompleteRequestDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.Collector;
import com.ecotracker.entity.PickupStatus;
import com.ecotracker.repository.WasteTypeRepository;
import com.ecotracker.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final AdminService adminService;
    private final WasteTypeRepository wasteTypeRepository;

    public AdminWebController(AdminService adminService,
                              WasteTypeRepository wasteTypeRepository) {
        this.adminService = adminService;
        this.wasteTypeRepository = wasteTypeRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) PickupStatus status,
            @RequestParam(required = false) String wasteType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        AdminSummaryDto summary = adminService.getAdminSummary();
        Page<PickupRequestResponseDto> requestsPage = adminService.getFilteredRequests(
                status, wasteType, null, null,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        List<Collector> activeCollectors = adminService.getAllActiveCollectors();

        model.addAttribute("summary", summary);
        model.addAttribute("requestsPage", requestsPage);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentWasteType", wasteType);
        model.addAttribute("wasteTypes", wasteTypeRepository.findByEnabledTrue());
        model.addAttribute("statuses", PickupStatus.values());
        model.addAttribute("collectors", activeCollectors);

        return "admin/dashboard";
    }

    @PostMapping("/requests/{id}/approve")
    public String approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminUser,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.approveRequest(id, adminUser.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Request #" + id + " approved successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails adminUser,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.rejectRequest(id, adminUser.getUsername(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Request #" + id + " has been rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/requests/{id}/assign")
    public String assignCollector(
            @PathVariable Long id,
            @RequestParam Long collectorId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails adminUser,
            RedirectAttributes redirectAttributes) {

        try {
            adminService.assignCollector(id, collectorId, adminUser.getUsername(), notes);
            redirectAttributes.addFlashAttribute("successMessage", "Collector assigned to Request #" + id + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/requests/{id}/complete")
    public String completeRequest(
            @PathVariable Long id,
            @RequestParam String recyclerName,
            @RequestParam BigDecimal materialWeight,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails adminUser,
            RedirectAttributes redirectAttributes) {

        try {
            CompleteRequestDto dto = new CompleteRequestDto(recyclerName, materialWeight, notes);
            adminService.completeRequest(id, adminUser.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Request #" + id + " completed and reward points issued!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
