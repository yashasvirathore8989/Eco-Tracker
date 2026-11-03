package com.ecotracker.controller;

import com.ecotracker.dto.ApiResponse;
import com.ecotracker.dto.PickupRequestCreateDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.PickupStatus;
import com.ecotracker.service.AdminService;
import com.ecotracker.service.PickupRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pickup-requests")
public class PickupRequestRestController {

    private final PickupRequestService pickupRequestService;
    private final AdminService adminService;

    public PickupRequestRestController(PickupRequestService pickupRequestService,
                                       AdminService adminService) {
        this.pickupRequestService = pickupRequestService;
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPickupRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication,
            @RequestParam(required = false) PickupStatus status,
            @RequestParam(required = false) String wasteType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            Page<PickupRequestResponseDto> adminList = adminService.getFilteredRequests(
                    status, wasteType, null, null, PageRequest.of(page, size));
            return ResponseEntity.ok(ApiResponse.success("Requests fetched successfully", adminList));
        } else {
            List<PickupRequestResponseDto> userRequests = pickupRequestService.getUserRequests(userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success("User requests fetched successfully", userRequests));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> createRequest(
            @Valid @RequestBody PickupRequestCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        PickupRequestResponseDto created = pickupRequestService.createRequest(dto, userDetails.getUsername());
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                created.getId(),
                created.getStatus().name(),
                "Pickup request created successfully",
                created
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> getRequestDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCollector = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COLLECTOR"));

        PickupRequestResponseDto dto = pickupRequestService.getAuthorizedRequestDetail(id, userDetails.getUsername(), isAdmin, isCollector);
        return ResponseEntity.ok(ApiResponse.success("Request details retrieved", dto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> cancelRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String reason = body != null ? body.get("reason") : null;

        PickupRequestResponseDto updated = pickupRequestService.cancelRequest(id, userDetails.getUsername(), reason, isAdmin);
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                updated.getId(),
                updated.getStatus().name(),
                "Pickup request cancelled successfully",
                updated
        );
        return ResponseEntity.ok(response);
    }
}
