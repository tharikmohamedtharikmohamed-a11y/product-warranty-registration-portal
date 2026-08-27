package com.warrantyportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.WarrantyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for WarrantyController endpoints, security, and customer scoping.
 * Phase 8 — Warranty Management
 */
@WebMvcTest(controllers = WarrantyController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class WarrantyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WarrantyService warrantyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String validToken = "valid.test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = new User("Alice Developer", "alice@example.com", "hashedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);

        when(jwtService.extractUserId(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);
    }

    private WarrantyResponse createSampleResponse(UUID id, UUID productId) {
        return new WarrantyResponse(
                id,
                productId,
                "Sony Bravia 65 OLED",
                "Sony",
                "XR-65A80L",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2028, 8, 1),
                24,
                WarrantyStatus.ACTIVE,
                700,
                5,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("1. GET /api/warranties returns 401 Unauthorized without JWT")
    void getWarranties_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/warranties"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. GET /api/warranties returns 200 OK with customer warranties")
    void getWarranties_Authenticated_Returns200() throws Exception {
        UUID warrantyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        WarrantyResponse response = createSampleResponse(warrantyId, productId);

        when(warrantyService.getWarrantiesForUser(testUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/warranties")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(warrantyId.toString()))
                .andExpect(jsonPath("$[0].productName").value("Sony Bravia 65 OLED"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].daysRemaining").value(700))
                .andExpect(jsonPath("$[0].progressPercentage").value(5));
    }

    @Test
    @DisplayName("3. GET /api/warranties/{id} returns 200 OK when found")
    void getWarrantyById_Found_Returns200() throws Exception {
        UUID warrantyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        WarrantyResponse response = createSampleResponse(warrantyId, productId);

        when(warrantyService.getWarrantyByIdForUser(warrantyId, testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/warranties/" + warrantyId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(warrantyId.toString()))
                .andExpect(jsonPath("$.brand").value("Sony"))
                .andExpect(jsonPath("$.warrantyDurationMonths").value(24));
    }

    @Test
    @DisplayName("4. GET /api/warranties/{id} returns 404 Not Found when unowned or non-existent")
    void getWarrantyById_NotFound_Returns404() throws Exception {
        UUID randomId = UUID.randomUUID();

        when(warrantyService.getWarrantyByIdForUser(randomId, testUserId))
                .thenThrow(new ResourceNotFoundException("Warranty not found with id: " + randomId));

        mockMvc.perform(get("/api/warranties/" + randomId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Warranty not found with id: " + randomId));
    }

    @Test
    @DisplayName("5. GET /api/products/{productId}/warranty returns 200 OK when product and warranty exist")
    void getProductWarranty_Found_Returns200() throws Exception {
        UUID warrantyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        WarrantyResponse response = createSampleResponse(warrantyId, productId);

        when(warrantyService.getWarrantyByProductIdForUser(productId, testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/products/" + productId + "/warranty")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(warrantyId.toString()))
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("6. GET /api/products/{productId}/warranty returns 404 Not Found when unowned")
    void getProductWarranty_NotFound_Returns404() throws Exception {
        UUID productId = UUID.randomUUID();

        when(warrantyService.getWarrantyByProductIdForUser(productId, testUserId))
                .thenThrow(new ResourceNotFoundException("Warranty not found for product id: " + productId));

        mockMvc.perform(get("/api/products/" + productId + "/warranty")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Warranty not found for product id: " + productId));
    }
}
