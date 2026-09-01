package com.warrantyportal.controller;

import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.DashboardResponse;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice tests for DashboardController endpoints and security scoping.
 * Phase 12 — Dashboard & Notifications
 */
@WebMvcTest(controllers = DashboardController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

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

    @Test
    @DisplayName("GET /api/dashboard should return 200 with structured dashboard JSON for authenticated user")
    void shouldReturnDashboardDataForAuthenticatedUser() throws Exception {
        DashboardResponse response = new DashboardResponse(
                new DashboardResponse.ProductsSummary(3),
                new DashboardResponse.WarrantiesSummary(3, 2, 1, 0),
                new DashboardResponse.ClaimsSummary(2, 1, 0, 0, 1, 0, 0),
                new DashboardResponse.InvoicesSummary(2),
                List.of(new DashboardResponse.ExpiringWarrantyItem(UUID.randomUUID(), UUID.randomUUID(), "Monitor", "LG", LocalDate.of(2026, 10, 1), 10, "EXPIRING_SOON")),
                List.of(new DashboardResponse.RecentClaimItem(UUID.randomUUID(), UUID.randomUUID(), "Monitor", "Dead pixels", "PENDING", OffsetDateTime.now())),
                List.of(new DashboardResponse.RecentActivityItem("PRODUCT", "Product Registered", "Registered Monitor", OffsetDateTime.now(), UUID.randomUUID()))
        );

        when(dashboardService.getCustomerDashboard(testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + validToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.total").value(3))
                .andExpect(jsonPath("$.warranties.active").value(2))
                .andExpect(jsonPath("$.warranties.expiringSoon").value(1))
                .andExpect(jsonPath("$.claims.pending").value(1))
                .andExpect(jsonPath("$.claims.completed").value(1))
                .andExpect(jsonPath("$.invoices.total").value(2))
                .andExpect(jsonPath("$.expiringWarranties[0].productName").value("Monitor"))
                .andExpect(jsonPath("$.recentClaims[0].claimReason").value("Dead pixels"))
                .andExpect(jsonPath("$.recentActivity[0].activityType").value("PRODUCT"));

        verify(dashboardService).getCustomerDashboard(testUserId);
    }

    @Test
    @DisplayName("GET /api/dashboard should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
