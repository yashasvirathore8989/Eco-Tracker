package com.ecotracker.controller;

import com.ecotracker.dto.PickupRequestCreateDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.exception.ValidationException;
import com.ecotracker.repository.WasteTypeRepository;
import com.ecotracker.service.PickupRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
public class PickupRequestWebController {

    private final PickupRequestService pickupRequestService;
    private final WasteTypeRepository wasteTypeRepository;

    public PickupRequestWebController(PickupRequestService pickupRequestService,
                                      WasteTypeRepository wasteTypeRepository) {
        this.pickupRequestService = pickupRequestService;
        this.wasteTypeRepository = wasteTypeRepository;
    }

    @GetMapping("/new")
    public String showNewRequestForm(Model model) {
        if (!model.containsAttribute("requestDto")) {
            model.addAttribute("requestDto", new PickupRequestCreateDto());
        }
        model.addAttribute("wasteTypes", wasteTypeRepository.findByEnabledTrue());
        return "user/new-request";
    }

    @PostMapping("/new")
    public String submitNewRequest(
            @Valid @ModelAttribute("requestDto") PickupRequestCreateDto requestDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("wasteTypes", wasteTypeRepository.findByEnabledTrue());
            return "user/new-request";
        }

        try {
            PickupRequestResponseDto created = pickupRequestService.createRequest(requestDto, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Pickup request #" + created.getReferenceNumber() + " submitted successfully!");
            return "redirect:/requests/" + created.getId();
        } catch (ValidationException ex) {
            model.addAttribute("wasteTypes", wasteTypeRepository.findByEnabledTrue());
            if (ex.getFieldErrors() != null) {
                ex.getFieldErrors().forEach((field, msg) -> bindingResult.rejectValue(field, "error." + field, msg));
            } else {
                model.addAttribute("errorMessage", ex.getMessage());
            }
            return "user/new-request";
        }
    }

    @GetMapping("/{id}")
    public String viewRequestDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication,
            Model model) {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCollector = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COLLECTOR"));

        PickupRequestResponseDto request = pickupRequestService.getAuthorizedRequestDetail(
                id, userDetails.getUsername(), isAdmin, isCollector);

        model.addAttribute("request", request);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isCollector", isCollector);
        return "user/request-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            pickupRequestService.cancelRequest(id, userDetails.getUsername(), reason, isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Request cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/requests/" + id;
    }
}
