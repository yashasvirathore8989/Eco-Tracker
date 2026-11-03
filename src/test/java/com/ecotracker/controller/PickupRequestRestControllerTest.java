package com.ecotracker.controller;

import com.ecotracker.dto.PickupRequestResponseDto;
import com.ecotracker.entity.PickupStatus;
import com.ecotracker.service.AdminService;
import com.ecotracker.service.PickupRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PickupRequestRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PickupRequestService pickupRequestService;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("Section 9.1: Validation failure returns 400 with VALIDATION_ERROR code and fieldErrors")
    @WithMockUser(username = "user@ecotracker.com", roles = {"USER"})
    void testCreateRequestValidationError() throws Exception {
        String invalidJson = """
            {
                "wasteTypeCode": "",
                "description": "Too",
                "quantity": -5.0,
                "unit": "",
                "address": "",
                "preferredAt": null
            }
        """;

        mockMvc.perform(post("/api/v1/pickup-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors.wasteTypeCode").exists())
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    @DisplayName("Section 9: Successful request creation returns 201 with created details")
    @WithMockUser(username = "user@ecotracker.com", roles = {"USER"})
    void testCreateRequestSuccess() throws Exception {
        LocalDateTime future = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
        String validJson = """
            {
                "wasteTypeCode": "E_WASTE",
                "description": "Discarded desktop unit and flat monitor",
                "quantity": 12.5,
                "unit": "kg",
                "address": "123 Elm Street",
                "preferredAt": "%s"
            }
        """.formatted(future);

        PickupRequestResponseDto responseDto = new PickupRequestResponseDto();
        responseDto.setId(104L);
        responseDto.setReferenceNumber("REQ-00104");
        responseDto.setStatus(PickupStatus.PENDING);
        responseDto.setDescription("Discarded desktop unit and flat monitor");
        responseDto.setQuantity(BigDecimal.valueOf(12.5));

        when(pickupRequestService.createRequest(any(), eq("user@ecotracker.com"))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/pickup-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(104))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Pickup request created successfully"));
    }

    @Test
    @DisplayName("Security: Unauthenticated request to /api/v1/pickup-requests is rejected")
    void testUnauthenticatedGetRequests() throws Exception {
        mockMvc.perform(get("/api/v1/pickup-requests"))
                .andExpect(status().is3xxRedirection()); // Redirects to /login
    }
}
