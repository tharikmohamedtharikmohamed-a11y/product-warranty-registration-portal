package com.warrantyportal.service;

import com.warrantyportal.dto.DashboardResponse;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.*;
import com.warrantyportal.repository.ClaimRepository;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardService verifying customer-scoped metric calculations,
 * expiring warranty filtering, recent claims, and real recent activity derivation.
 * Phase 12 — Dashboard & Notifications
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarrantyService warrantyService;

    private DashboardService dashboardService;

    private User customer;
    private UUID customerId;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-21T10:00:00Z"), ZoneId.of("UTC"));
        dashboardService = new DashboardService(productRepository, claimRepository, invoiceRepository, warrantyService, fixedClock);

        customerId = UUID.randomUUID();
        customer = new User("Customer Alice", "alice@example.com", "hash", Role.CUSTOMER);
        customer.setId(customerId);
    }

    @Test
    @DisplayName("Should return customer dashboard metrics strictly scoped to authenticated user")
    void shouldReturnCustomerDashboardMetrics() {
        // Mock products
        Product p1 = new Product(customer, "MacBook Pro", "Electronics", "Apple", "MBP16", "SN100",
                LocalDate.of(2026, 1, 1), "Apple Store", new BigDecimal("2499.00"), 12, null);
        p1.setId(UUID.randomUUID());
        p1.setCreatedAt(OffsetDateTime.now());

        Product p2 = new Product(customer, "Smart TV", "Appliances", "Samsung", "TV65", "SN200",
                LocalDate.of(2026, 2, 1), "BestBuy", new BigDecimal("1200.00"), 24, null);
        p2.setId(UUID.randomUUID());
        p2.setCreatedAt(OffsetDateTime.now());

        when(productRepository.findAllByUserIdWithWarrantyOrderByCreatedAtDesc(customerId))
                .thenReturn(List.of(p1, p2));

        // Mock warranties
        WarrantyResponse w1 = new WarrantyResponse(UUID.randomUUID(), p1.getId(), p1.getProductName(), p1.getBrand(), p1.getModelNumber(),
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), 12, WarrantyStatus.ACTIVE, 102, 50, OffsetDateTime.now(), OffsetDateTime.now());
        WarrantyResponse w2 = new WarrantyResponse(UUID.randomUUID(), p2.getId(), p2.getProductName(), p2.getBrand(), p2.getModelNumber(),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 10, 5), 24, WarrantyStatus.EXPIRING_SOON, 14, 90, OffsetDateTime.now(), OffsetDateTime.now());

        when(warrantyService.getWarrantiesForUser(customerId)).thenReturn(List.of(w1, w2));

        // Mock claims
        Claim c1 = new Claim(customer, p1, "Display flickering", "Details", ClaimStatus.PENDING);
        c1.setId(UUID.randomUUID());
        c1.setCreatedAt(OffsetDateTime.now());

        when(claimRepository.findByUserIdOrderByCreatedAtDesc(customerId)).thenReturn(List.of(c1));

        // Mock invoices
        Invoice inv1 = new Invoice(customer, p1, "receipt.pdf", "invoices/a/b/receipt.pdf", "application/pdf", 1024L);
        inv1.setId(UUID.randomUUID());
        inv1.setUploadedAt(OffsetDateTime.now());

        when(invoiceRepository.findByUserIdOrderByUploadedAtDesc(customerId)).thenReturn(List.of(inv1));

        DashboardResponse response = dashboardService.getCustomerDashboard(customerId);

        assertNotNull(response);

        // Product KPIs
        assertEquals(2, response.getProducts().getTotal());

        // Warranty KPIs
        assertEquals(2, response.getWarranties().getTotal());
        assertEquals(1, response.getWarranties().getActive());
        assertEquals(1, response.getWarranties().getExpiringSoon());
        assertEquals(0, response.getWarranties().getExpired());

        // Claim KPIs
        assertEquals(1, response.getClaims().getTotal());
        assertEquals(1, response.getClaims().getPending());
        assertEquals(0, response.getClaims().getApproved());
        assertEquals(0, response.getClaims().getCompleted());

        // Invoice KPIs
        assertEquals(1, response.getInvoices().getTotal());

        // Expiring warranties preview
        assertEquals(1, response.getExpiringWarranties().size());
        assertEquals("Smart TV", response.getExpiringWarranties().get(0).getProductName());
        assertEquals("EXPIRING_SOON", response.getExpiringWarranties().get(0).getStatus());

        // Recent claims preview
        assertEquals(1, response.getRecentClaims().size());
        assertEquals("Display flickering", response.getRecentClaims().get(0).getClaimReason());

        // Recent activity
        assertFalse(response.getRecentActivity().isEmpty());
    }

    @Test
    @DisplayName("Should handle empty portfolio cleanly without null pointers")
    void shouldHandleEmptyCustomerPortfolio() {
        when(productRepository.findAllByUserIdWithWarrantyOrderByCreatedAtDesc(customerId)).thenReturn(List.of());
        when(warrantyService.getWarrantiesForUser(customerId)).thenReturn(List.of());
        when(claimRepository.findByUserIdOrderByCreatedAtDesc(customerId)).thenReturn(List.of());
        when(invoiceRepository.findByUserIdOrderByUploadedAtDesc(customerId)).thenReturn(List.of());

        DashboardResponse response = dashboardService.getCustomerDashboard(customerId);

        assertNotNull(response);
        assertEquals(0, response.getProducts().getTotal());
        assertEquals(0, response.getWarranties().getTotal());
        assertEquals(0, response.getClaims().getTotal());
        assertEquals(0, response.getInvoices().getTotal());
        assertTrue(response.getExpiringWarranties().isEmpty());
        assertTrue(response.getRecentClaims().isEmpty());
        assertTrue(response.getRecentActivity().isEmpty());
    }
}
