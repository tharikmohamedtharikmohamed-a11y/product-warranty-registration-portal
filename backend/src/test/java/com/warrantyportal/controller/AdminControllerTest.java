package com.warrantyportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.*;
import com.warrantyportal.entity.ClaimStatus;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.AdminService;
import com.warrantyportal.service.InvoiceService;
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
 * Controller tests for AdminController validating admin endpoints,
 * role authorization (403 Forbidden for CUSTOMER), and 401 for unauthenticated requests.
 * Phase 11 — Admin Management Module
 */
@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User adminUser;
    private User customerUser;
    private UUID adminId;
    private UUID customerId;
    private String adminToken;
    private String customerToken;
    private UUID claimId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        claimId = UUID.randomUUID();

        adminUser = new User("Admin", "admin@warrantyhub.com", "hash", Role.ADMIN);
        adminUser.setId(adminId);

        customerUser = new User("Customer", "customer@example.com", "hash", Role.CUSTOMER);
        customerUser.setId(customerId);

        adminToken = "valid-admin-jwt-token";
        customerToken = "valid-customer-jwt-token";

        // Mock JWT service for admin token
        when(jwtService.extractUserId(adminToken)).thenReturn(adminId);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(jwtService.isTokenValid(adminToken, adminUser)).thenReturn(true);

        // Mock JWT service for customer token
        when(jwtService.extractUserId(customerToken)).thenReturn(customerId);
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customerUser));
        when(jwtService.isTokenValid(customerToken, customerUser)).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/admin/dashboard/stats returns 200 OK for ADMIN")
    void testGetDashboardStatsAsAdmin() throws Exception {
        AdminDashboardStatsResponse stats = new AdminDashboardStatsResponse(
                10, 8, 2, 15, 15, 10, 3, 2, 12, 5, 2, 1, 1, 0, 1, 0
        );
        when(adminService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").value(10))
                .andExpect(jsonPath("$.customers").value(8))
                .andExpect(jsonPath("$.admins").value(2));
    }

    @Test
    @DisplayName("GET /api/admin/dashboard/stats returns 403 Forbidden for CUSTOMER")
    void testGetDashboardStatsAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied. Admin privileges required."));
    }

    @Test
    @DisplayName("GET /api/admin/dashboard/stats returns 401 Unauthorized without token")
    void testGetDashboardStatsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/admin/users returns 200 OK for ADMIN")
    void testGetUsersAsAdmin() throws Exception {
        when(adminService.getUsers(any())).thenReturn(List.of(
                new AdminUserResponse(customerId, "Customer", "customer@example.com", Role.CUSTOMER, OffsetDateTime.now())
        ));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("customer@example.com"));
    }

    @Test
    @DisplayName("GET /api/admin/users returns 403 Forbidden for CUSTOMER")
    void testGetUsersAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/products returns 200 OK for ADMIN")
    void testGetProductsAsAdmin() throws Exception {
        when(adminService.getProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/admin/products returns 403 Forbidden for CUSTOMER")
    void testGetProductsAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/warranties returns 200 OK for ADMIN")
    void testGetWarrantiesAsAdmin() throws Exception {
        when(adminService.getWarranties()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/warranties")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/admin/warranties returns 403 Forbidden for CUSTOMER")
    void testGetWarrantiesAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/warranties")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/invoices returns 200 OK for ADMIN")
    void testGetInvoicesAsAdmin() throws Exception {
        when(adminService.getInvoices()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/invoices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/admin/invoices returns 403 Forbidden for CUSTOMER")
    void testGetInvoicesAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/invoices")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/claims returns 200 OK for ADMIN")
    void testGetClaimsAsAdmin() throws Exception {
        when(adminService.getClaims()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/claims")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/admin/claims returns 403 Forbidden for CUSTOMER")
    void testGetClaimsAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/claims")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/admin/claims/{id}/approve returns 200 OK for ADMIN")
    void testApproveClaimAsAdmin() throws Exception {
        AdminClaimResponse resp = new AdminClaimResponse(
                claimId, UUID.randomUUID(), "Product", "Brand", "Model",
                customerId, "Customer", "customer@example.com",
                "Reason", "Description", "Approved by admin",
                ClaimStatus.APPROVED, OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(adminService.approveClaim(eq(claimId), any())).thenReturn(resp);

        AdminClaimDecisionRequest req = new AdminClaimDecisionRequest("Approved by admin");

        mockMvc.perform(patch("/api/admin/claims/" + claimId + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.adminNotes").value("Approved by admin"));
    }

    @Test
    @DisplayName("PATCH /api/admin/claims/{id}/approve returns 403 Forbidden for CUSTOMER")
    void testApproveClaimAsCustomerForbidden() throws Exception {
        mockMvc.perform(patch("/api/admin/claims/" + claimId + "/approve")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/admin/claims/{id}/reject returns 200 OK for ADMIN")
    void testRejectClaimAsAdmin() throws Exception {
        AdminClaimResponse resp = new AdminClaimResponse(
                claimId, UUID.randomUUID(), "Product", "Brand", "Model",
                customerId, "Customer", "customer@example.com",
                "Reason", "Description", "Rejection reason",
                ClaimStatus.REJECTED, OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(adminService.rejectClaim(eq(claimId), any())).thenReturn(resp);

        AdminClaimDecisionRequest req = new AdminClaimDecisionRequest("Rejection reason");

        mockMvc.perform(patch("/api/admin/claims/" + claimId + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("PATCH /api/admin/claims/{id}/start returns 200 OK for ADMIN")
    void testStartClaimAsAdmin() throws Exception {
        AdminClaimResponse resp = new AdminClaimResponse(
                claimId, UUID.randomUUID(), "Product", "Brand", "Model",
                customerId, "Customer", "customer@example.com",
                "Reason", "Description", "In progress",
                ClaimStatus.IN_PROGRESS, OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(adminService.startClaim(eq(claimId), any())).thenReturn(resp);

        mockMvc.perform(patch("/api/admin/claims/" + claimId + "/start")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/admin/claims/{id}/complete returns 200 OK for ADMIN")
    void testCompleteClaimAsAdmin() throws Exception {
        AdminClaimResponse resp = new AdminClaimResponse(
                claimId, UUID.randomUUID(), "Product", "Brand", "Model",
                customerId, "Customer", "customer@example.com",
                "Reason", "Description", "Repair completed",
                ClaimStatus.COMPLETED, OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(adminService.completeClaim(eq(claimId), any())).thenReturn(resp);

        mockMvc.perform(patch("/api/admin/claims/" + claimId + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PATCH /api/admin/claims/{id}/approve returns 400 Bad Request on invalid transition")
    void testApproveClaimInvalidTransition() throws Exception {
        when(adminService.approveClaim(eq(claimId), any()))
                .thenThrow(new InvalidClaimException("Only PENDING claims can be approved. Current status: REJECTED"));

        mockMvc.perform(patch("/api/admin/claims/" + claimId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only PENDING claims can be approved. Current status: REJECTED"));
    }
}
