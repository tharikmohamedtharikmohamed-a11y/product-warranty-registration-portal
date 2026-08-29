package com.warrantyportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.ClaimRequest;
import com.warrantyportal.dto.ClaimResponse;
import com.warrantyportal.entity.ClaimStatus;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.ClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for ClaimController endpoints, security, and customer scoping.
 * Phase 10 — Warranty Claims
 */
@WebMvcTest(controllers = ClaimController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String validToken = "valid.test.jwt.token";
    private final UUID claimId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = new User("Alice Developer", "alice@example.com", "hashedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);

        when(jwtService.extractUserId(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);
    }

    private ClaimResponse createSampleClaimResponse(ClaimStatus status) {
        return new ClaimResponse(
                claimId,
                productId,
                "Sony Bravia 65 4K OLED",
                "Sony",
                "XR-65A80L",
                "Display flicker",
                "Screen flashes periodically",
                status,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("1. POST /api/claims returns 401 Unauthorized without JWT")
    void testCreateClaimUnauthorizedWithoutToken() throws Exception {
        ClaimRequest request = new ClaimRequest(productId, "Display issue", "Details");

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. POST /api/claims returns 201 Created with valid token")
    void testCreateClaimSuccess() throws Exception {
        ClaimRequest request = new ClaimRequest(productId, "Display flicker", "Screen flashes periodically");
        ClaimResponse response = createSampleClaimResponse(ClaimStatus.PENDING);

        when(claimService.createClaim(any(ClaimRequest.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(claimId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.claimReason").value("Display flicker"));
    }

    @Test
    @DisplayName("3. POST /api/claims returns 400 Bad Request for validation error")
    void testCreateClaimValidationError() throws Exception {
        ClaimRequest invalidRequest = new ClaimRequest(null, "", "");

        mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("4. GET /api/claims returns 200 OK with list of customer claims")
    void testGetClaims() throws Exception {
        ClaimResponse response = createSampleClaimResponse(ClaimStatus.PENDING);
        when(claimService.getClaimsForUser(testUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/claims")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(claimId.toString()));
    }

    @Test
    @DisplayName("5. GET /api/claims/{id} returns 200 OK for owned claim")
    void testGetClaimById() throws Exception {
        ClaimResponse response = createSampleClaimResponse(ClaimStatus.PENDING);
        when(claimService.getClaimByIdForUser(claimId, testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/claims/" + claimId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(claimId.toString()))
                .andExpect(jsonPath("$.claimReason").value("Display flicker"));
    }

    @Test
    @DisplayName("6. GET /api/claims/{id} returns 404 Not Found for unowned claim")
    void testGetClaimByIdNotFound() throws Exception {
        when(claimService.getClaimByIdForUser(claimId, testUserId))
                .thenThrow(new ResourceNotFoundException("Claim not found with id: " + claimId));

        mockMvc.perform(get("/api/claims/" + claimId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Claim not found with id: " + claimId));
    }

    @Test
    @DisplayName("7. PATCH /api/claims/{id}/cancel returns 200 OK for pending claim")
    void testCancelClaimSuccess() throws Exception {
        ClaimResponse response = createSampleClaimResponse(ClaimStatus.CANCELLED);
        when(claimService.cancelClaim(claimId, testUserId)).thenReturn(response);

        mockMvc.perform(patch("/api/claims/" + claimId + "/cancel")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("8. PATCH /api/claims/{id}/cancel returns 400 Bad Request if claim not pending")
    void testCancelClaimNotPending() throws Exception {
        when(claimService.cancelClaim(claimId, testUserId))
                .thenThrow(new InvalidClaimException("Only pending claims can be cancelled."));

        mockMvc.perform(patch("/api/claims/" + claimId + "/cancel")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only pending claims can be cancelled."));
    }

    @Test
    @DisplayName("9. GET /api/products/{productId}/claims returns 200 OK with product claims")
    void testGetProductClaims() throws Exception {
        ClaimResponse response = createSampleClaimResponse(ClaimStatus.PENDING);
        when(claimService.getClaimsByProductIdForUser(productId, testUserId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/" + productId + "/claims")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(productId.toString()));
    }
}
