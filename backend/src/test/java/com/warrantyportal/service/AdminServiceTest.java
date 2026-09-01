package com.warrantyportal.service;

import com.warrantyportal.dto.*;
import com.warrantyportal.entity.*;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminService verifying platform KPI calculations,
 * entity listing across customers, and strict claim state machine transitions.
 * Phase 11 — Admin Management Module
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarrantyRepository warrantyRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private WarrantyService warrantyService;

    private Clock fixedClock;
    private AdminService adminService;

    private User adminUser;
    private User customerUser;
    private Product product;
    private Warranty warranty;
    private Invoice invoice;
    private Claim claim;
    private UUID claimId;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-20T10:00:00Z"), ZoneOffset.UTC);
        adminService = new AdminService(
                userRepository,
                productRepository,
                warrantyRepository,
                invoiceRepository,
                claimRepository,
                warrantyService,
                fixedClock
        );

        customerUser = new User("Alice", "alice@example.com", "hash", Role.CUSTOMER);
        customerUser.setId(UUID.randomUUID());

        adminUser = new User("Admin", "admin@warrantyhub.com", "hash", Role.ADMIN);
        adminUser.setId(UUID.randomUUID());

        product = new Product(customerUser, "MacBook Pro", "Electronics", "Apple", "MBP-14", "SER-12345",
                LocalDate.of(2026, 1, 15), "Apple Store", new BigDecimal("1999.00"), 24, "Laptop");
        product.setId(UUID.randomUUID());

        warranty = new Warranty(product, LocalDate.of(2026, 1, 15), LocalDate.of(2028, 1, 15), WarrantyStatus.ACTIVE);
        warranty.setId(UUID.randomUUID());
        product.setWarranty(warranty);

        invoice = new Invoice(customerUser, product, "receipt.pdf", "invoices/receipt.pdf", "application/pdf", 1024L);
        invoice.setId(UUID.randomUUID());

        claimId = UUID.randomUUID();
        claim = new Claim(customerUser, product, "Screen flickering", "Flickers when dimmed", ClaimStatus.PENDING);
        claim.setId(claimId);
    }

    @Test
    @DisplayName("getDashboardStats calculates all authoritative counts from database")
    void testGetDashboardStats() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(8L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);

        when(productRepository.count()).thenReturn(15L);

        when(warrantyRepository.count()).thenReturn(15L);
        when(warrantyRepository.countByStatus(WarrantyStatus.ACTIVE)).thenReturn(10L);
        when(warrantyRepository.countByStatus(WarrantyStatus.EXPIRING_SOON)).thenReturn(3L);
        when(warrantyRepository.countByStatus(WarrantyStatus.EXPIRED)).thenReturn(2L);

        when(invoiceRepository.count()).thenReturn(12L);

        when(claimRepository.count()).thenReturn(5L);
        when(claimRepository.countByStatus(ClaimStatus.PENDING)).thenReturn(2L);
        when(claimRepository.countByStatus(ClaimStatus.APPROVED)).thenReturn(1L);
        when(claimRepository.countByStatus(ClaimStatus.REJECTED)).thenReturn(1L);
        when(claimRepository.countByStatus(ClaimStatus.IN_PROGRESS)).thenReturn(0L);
        when(claimRepository.countByStatus(ClaimStatus.COMPLETED)).thenReturn(1L);
        when(claimRepository.countByStatus(ClaimStatus.CANCELLED)).thenReturn(0L);

        AdminDashboardStatsResponse stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.getUsers());
        assertEquals(8L, stats.getCustomers());
        assertEquals(2L, stats.getAdmins());
        assertEquals(15L, stats.getProducts());
        assertEquals(15L, stats.getWarranties());
        assertEquals(10L, stats.getActiveWarranties());
        assertEquals(3L, stats.getExpiringSoonWarranties());
        assertEquals(2L, stats.getExpiredWarranties());
        assertEquals(12L, stats.getInvoices());
        assertEquals(5L, stats.getClaims());
        assertEquals(2L, stats.getPendingClaims());
        assertEquals(1L, stats.getApprovedClaims());
        assertEquals(1L, stats.getRejectedClaims());
        assertEquals(0L, stats.getInProgressClaims());
        assertEquals(1L, stats.getCompletedClaims());
        assertEquals(0L, stats.getCancelledClaims());
    }

    @Test
    @DisplayName("getUsers returns all users mapped to safe AdminUserResponse")
    void testGetUsers() {
        when(userRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(customerUser, adminUser));

        List<AdminUserResponse> users = adminService.getUsers(null);

        assertEquals(2, users.size());
        assertEquals("alice@example.com", users.get(0).getEmail());
        assertEquals(Role.CUSTOMER, users.get(0).getRole());
        assertEquals("admin@warrantyhub.com", users.get(1).getEmail());
        assertEquals(Role.ADMIN, users.get(1).getRole());
    }

    @Test
    @DisplayName("getUsers with search queries name or email filter")
    void testGetUsersWithSearch() {
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc("alice", "alice"))
                .thenReturn(List.of(customerUser));

        List<AdminUserResponse> users = adminService.getUsers("alice");

        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).getName());
    }

    @Test
    @DisplayName("getProducts returns all products across customers")
    void testGetProducts() {
        when(productRepository.findAllWithUserAndWarrantyOrderByCreatedAtDesc()).thenReturn(List.of(product));

        List<AdminProductResponse> products = adminService.getProducts();

        assertEquals(1, products.size());
        assertEquals("MacBook Pro", products.get(0).getProductName());
        assertEquals("Alice", products.get(0).getCustomerName());
        assertEquals("alice@example.com", products.get(0).getCustomerEmail());
        assertEquals(WarrantyStatus.ACTIVE, products.get(0).getWarrantyStatus());
    }

    @Test
    @DisplayName("getWarranties returns all warranties with dynamic calculations")
    void testGetWarranties() {
        when(warrantyRepository.findAllWithProductAndUser()).thenReturn(List.of(warranty));
        when(warrantyService.calculateDaysRemaining(eq(warranty.getExpiryDate()), any(LocalDate.class))).thenReturn(482L);
        when(warrantyService.calculateProgressPercentage(eq(warranty.getStartDate()), eq(warranty.getExpiryDate()), any(LocalDate.class))).thenReturn(34);

        List<AdminWarrantyResponse> warranties = adminService.getWarranties();

        assertEquals(1, warranties.size());
        assertEquals(482L, warranties.get(0).getDaysRemaining());
        assertEquals(34, warranties.get(0).getProgressPercentage());
        assertEquals("Alice", warranties.get(0).getCustomerName());
    }

    @Test
    @DisplayName("getInvoices returns all invoices across customers")
    void testGetInvoices() {
        when(invoiceRepository.findAllWithProductAndUser()).thenReturn(List.of(invoice));

        List<AdminInvoiceResponse> invoices = adminService.getInvoices();

        assertEquals(1, invoices.size());
        assertEquals("receipt.pdf", invoices.get(0).getFileName());
        assertEquals("Alice", invoices.get(0).getCustomerName());
    }

    @Test
    @DisplayName("getClaims returns all claims across customers")
    void testGetClaims() {
        when(claimRepository.findAllWithProductAndUser()).thenReturn(List.of(claim));

        List<AdminClaimResponse> claims = adminService.getClaims();

        assertEquals(1, claims.size());
        assertEquals("Screen flickering", claims.get(0).getClaimReason());
        assertEquals("Alice", claims.get(0).getCustomerName());
        assertEquals(ClaimStatus.PENDING, claims.get(0).getStatus());
    }

    @Test
    @DisplayName("getClaimById returns single claim with details")
    void testGetClaimByIdSuccess() {
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));

        AdminClaimResponse response = adminService.getClaimById(claimId);

        assertNotNull(response);
        assertEquals(claimId, response.getId());
        assertEquals("Screen flickering", response.getClaimReason());
    }

    @Test
    @DisplayName("getClaimById throws 404 when claim is not found")
    void testGetClaimByIdNotFound() {
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getClaimById(claimId));
    }

    @Test
    @DisplayName("approveClaim moves PENDING claim to APPROVED and stores admin note")
    void testApproveClaimSuccess() {
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminClaimDecisionRequest req = new AdminClaimDecisionRequest("Approved for service center repair.");
        AdminClaimResponse response = adminService.approveClaim(claimId, req);

        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        assertEquals("Approved for service center repair.", response.getAdminNotes());
        assertEquals("Flickers when dimmed", response.getDescription());
    }

    @Test
    @DisplayName("rejectClaim moves PENDING claim to REJECTED with rejection reason")
    void testRejectClaimSuccess() {
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminClaimDecisionRequest req = new AdminClaimDecisionRequest("Damage caused by external liquid impact.");
        AdminClaimResponse response = adminService.rejectClaim(claimId, req);

        assertEquals(ClaimStatus.REJECTED, response.getStatus());
        assertEquals("Damage caused by external liquid impact.", response.getAdminNotes());
    }

    @Test
    @DisplayName("startClaim moves APPROVED claim to IN_PROGRESS")
    void testStartClaimSuccess() {
        claim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminClaimDecisionRequest req = new AdminClaimDecisionRequest("Technician dispatched.");
        AdminClaimResponse response = adminService.startClaim(claimId, req);

        assertEquals(ClaimStatus.IN_PROGRESS, response.getStatus());
        assertEquals("Technician dispatched.", response.getAdminNotes());
    }

    @Test
    @DisplayName("completeClaim moves IN_PROGRESS claim to COMPLETED")
    void testCompleteClaimSuccess() {
        claim.setStatus(ClaimStatus.IN_PROGRESS);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminClaimDecisionRequest req = new AdminClaimDecisionRequest("Screen assembly replaced. Tested ok.");
        AdminClaimResponse response = adminService.completeClaim(claimId, req);

        assertEquals(ClaimStatus.COMPLETED, response.getStatus());
        assertEquals("Screen assembly replaced. Tested ok.", response.getAdminNotes());
    }

    @Test
    @DisplayName("approveClaim throws InvalidClaimException when status is not PENDING")
    void testApproveClaimInvalidStatus() {
        claim.setStatus(ClaimStatus.REJECTED);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class,
                () -> adminService.approveClaim(claimId, new AdminClaimDecisionRequest("Approve")));
        assertTrue(ex.getMessage().contains("Only PENDING claims can be approved"));
    }

    @Test
    @DisplayName("rejectClaim throws InvalidClaimException when status is not PENDING")
    void testRejectClaimInvalidStatus() {
        claim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class,
                () -> adminService.rejectClaim(claimId, new AdminClaimDecisionRequest("Reject")));
        assertTrue(ex.getMessage().contains("Only PENDING claims can be rejected"));
    }

    @Test
    @DisplayName("startClaim throws InvalidClaimException when status is not APPROVED")
    void testStartClaimInvalidStatus() {
        claim.setStatus(ClaimStatus.PENDING);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class,
                () -> adminService.startClaim(claimId, new AdminClaimDecisionRequest("Start")));
        assertTrue(ex.getMessage().contains("Only APPROVED claims can be moved to IN_PROGRESS"));
    }

    @Test
    @DisplayName("completeClaim throws InvalidClaimException when status is not IN_PROGRESS")
    void testCompleteClaimInvalidStatus() {
        claim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class,
                () -> adminService.completeClaim(claimId, new AdminClaimDecisionRequest("Complete")));
        assertTrue(ex.getMessage().contains("Only IN_PROGRESS claims can be completed"));
    }

    @Test
    @DisplayName("Terminal statuses (COMPLETED, CANCELLED) cannot be transitioned")
    void testTerminalStatusCannotTransition() {
        claim.setStatus(ClaimStatus.COMPLETED);
        when(claimRepository.findByIdWithProductAndUser(claimId)).thenReturn(Optional.of(claim));

        assertThrows(InvalidClaimException.class, () -> adminService.approveClaim(claimId, null));
        assertThrows(InvalidClaimException.class, () -> adminService.rejectClaim(claimId, null));
        assertThrows(InvalidClaimException.class, () -> adminService.startClaim(claimId, null));
        assertThrows(InvalidClaimException.class, () -> adminService.completeClaim(claimId, null));
    }
}
