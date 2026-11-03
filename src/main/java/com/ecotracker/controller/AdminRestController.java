package com.ecotracker.controller;

import com.ecotracker.dto.*;
import com.ecotracker.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminRestController {

    private final AdminService adminService;

    public AdminRestController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminSummaryDto>> getSummary() {
        AdminSummaryDto summary = adminService.getAdminSummary();
        return ResponseEntity.ok(ApiResponse.success("Admin summary counts", summary));
    }

    @PatchMapping("/requests/{id}/approve")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminUser) {

        PickupRequestResponseDto result = adminService.approveRequest(id, adminUser.getUsername());
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                result.getId(),
                result.getStatus().name(),
                "Pickup request approved successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/requests/{id}/reject")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequestDto dto,
            @AuthenticationPrincipal UserDetails adminUser) {

        PickupRequestResponseDto result = adminService.rejectRequest(id, adminUser.getUsername(), dto.getReason());
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                result.getId(),
                result.getStatus().name(),
                "Pickup request rejected",
                result
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{id}/assign")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> assignCollector(
            @PathVariable Long id,
            @Valid @RequestBody AssignCollectorDto dto,
            @AuthenticationPrincipal UserDetails adminUser) {

        PickupRequestResponseDto result = adminService.assignCollector(id, dto.getCollectorId(), adminUser.getUsername(), dto.getNotes());
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                result.getId(),
                result.getStatus().name(),
                "Collector assigned successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/{id}/complete")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> completeRequest(
            @PathVariable Long id,
            @Valid @RequestBody CompleteRequestDto dto,
            @AuthenticationPrincipal UserDetails adminUser) {

        PickupRequestResponseDto result = adminService.completeRequest(id, adminUser.getUsername(), dto);
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                result.getId(),
                result.getStatus().name(),
                "Request completed successfully and rewards issued",
                result
        );
        return ResponseEntity.ok(response);
    }
}
