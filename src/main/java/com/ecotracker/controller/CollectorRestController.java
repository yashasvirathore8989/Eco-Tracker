package com.ecotracker.controller;

import com.ecotracker.dto.ApiResponse;
import com.ecotracker.dto.PickupOutcomeDto;
import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.service.CollectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collector")
public class CollectorRestController {

    private final CollectorService collectorService;

    public CollectorRestController(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<PickupRequestResponseDto>>> getAssignments(
            @AuthenticationPrincipal UserDetails collectorUser) {

        List<PickupRequestResponseDto> list = collectorService.getCollectorAssignments(collectorUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Assigned pickups retrieved", list));
    }

    @PostMapping("/requests/{id}/pickup")
    public ResponseEntity<ApiResponse<PickupRequestResponseDto>> recordPickup(
            @PathVariable Long id,
            @RequestBody(required = false) PickupOutcomeDto dto,
            @AuthenticationPrincipal UserDetails collectorUser) {

        if (dto == null) {
            dto = new PickupOutcomeDto();
        }

        PickupRequestResponseDto result = collectorService.recordPickup(id, collectorUser.getUsername(), dto);
        ApiResponse<PickupRequestResponseDto> response = new ApiResponse<>(
                result.getId(),
                result.getStatus().name(),
                "Pickup outcome recorded successfully",
                result
        );
        return ResponseEntity.ok(response);
    }
}
