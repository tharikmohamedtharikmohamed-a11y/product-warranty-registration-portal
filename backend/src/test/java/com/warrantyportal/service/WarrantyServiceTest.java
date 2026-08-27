package com.warrantyportal.service;

import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.WarrantyRepository;
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
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WarrantyService verifying lifecycle status calculations,
 * days remaining, progress percentage, user scoping, and auto status synchronization.
 * Phase 8 — Warranty Management
 */
@ExtendWith(MockitoExtension.class)
class WarrantyServiceTest {

    @Mock
    private WarrantyRepository warrantyRepository;

    private WarrantyService warrantyService;

    private User testUser;
    private UUID userId;
    private final LocalDate fixedToday = LocalDate.of(2026, 9, 20);

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-09-20T10:00:00Z"),
                ZoneId.of("UTC")
        );
        warrantyService = new WarrantyService(warrantyRepository, fixedClock);

        userId = UUID.randomUUID();
        testUser = new User("Alice Developer", "alice@example.com", "hashPass", Role.CUSTOMER);
        testUser.setId(userId);
    }

    private Product createSampleProduct(UUID productId, LocalDate purchaseDate, int durationMonths) {
        Product product = new Product(
                testUser,
                "Sony Bravia 65 4K OLED",
                "Television",
                "Sony",
                "XR-65A80L",
                "SN-SONY-BRAVIA-1",
                purchaseDate,
                "Best Buy",
                new BigDecimal("1999.99"),
                durationMonths,
                "Living room TV"
        );
        product.setId(productId);
        return product;
    }

    @Test
    @DisplayName("1. Get customer's warranties returns enriched list sorted and mapped")
    void getWarrantiesForUser_Success() {
        UUID productId = UUID.randomUUID();
        Product product = createSampleProduct(productId, LocalDate.of(2026, 8, 1), 24);
        Warranty warranty = new Warranty(product, LocalDate.of(2026, 8, 1), LocalDate.of(2028, 8, 1), WarrantyStatus.ACTIVE);
        warranty.setId(UUID.randomUUID());
        product.setWarranty(warranty);

        when(warrantyRepository.findAllByProductUserId(userId)).thenReturn(List.of(warranty));

        List<WarrantyResponse> responses = warrantyService.getWarrantiesForUser(userId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        WarrantyResponse resp = responses.get(0);
        assertEquals(warranty.getId(), resp.getId());
        assertEquals(productId, resp.getProductId());
        assertEquals("Sony Bravia 65 4K OLED", resp.getProductName());
        assertEquals("Sony", resp.getBrand());
        assertEquals("XR-65A80L", resp.getModelNumber());
        assertEquals(24, resp.getWarrantyDurationMonths());
        assertEquals(WarrantyStatus.ACTIVE, resp.getStatus());
        assertTrue(resp.getDaysRemaining() > 30);
    }

    @Test
    @DisplayName("2. Get warranty by ID returns mapped response when owned by customer")
    void getWarrantyByIdForUser_Found() {
        UUID warrantyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = createSampleProduct(productId, LocalDate.of(2026, 8, 1), 24);
        Warranty warranty = new Warranty(product, LocalDate.of(2026, 8, 1), LocalDate.of(2028, 8, 1), WarrantyStatus.ACTIVE);
        warranty.setId(warrantyId);
        product.setWarranty(warranty);

        when(warrantyRepository.findByIdAndProductUserId(warrantyId, userId)).thenReturn(Optional.of(warranty));

        WarrantyResponse resp = warrantyService.getWarrantyByIdForUser(warrantyId, userId);

        assertNotNull(resp);
        assertEquals(warrantyId, resp.getId());
        assertEquals(productId, resp.getProductId());
        assertEquals("Sony", resp.getBrand());
    }

    @Test
    @DisplayName("3. Get warranty by ID throws ResourceNotFoundException when not found or unowned")
    void getWarrantyByIdForUser_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(warrantyRepository.findByIdAndProductUserId(randomId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warrantyService.getWarrantyByIdForUser(randomId, userId));
    }

    @Test
    @DisplayName("4. Get warranty by Product ID returns warranty when owned")
    void getWarrantyByProductIdForUser_Found() {
        UUID productId = UUID.randomUUID();
        Product product = createSampleProduct(productId, LocalDate.of(2026, 8, 1), 24);
        Warranty warranty = new Warranty(product, LocalDate.of(2026, 8, 1), LocalDate.of(2028, 8, 1), WarrantyStatus.ACTIVE);
        warranty.setId(UUID.randomUUID());
        product.setWarranty(warranty);

        when(warrantyRepository.findByProductIdAndProductUserId(productId, userId)).thenReturn(Optional.of(warranty));

        WarrantyResponse resp = warrantyService.getWarrantyByProductIdForUser(productId, userId);

        assertNotNull(resp);
        assertEquals(productId, resp.getProductId());
        assertEquals("Sony Bravia 65 4K OLED", resp.getProductName());
    }

    @Test
    @DisplayName("5. Get warranty by Product ID throws ResourceNotFoundException when unowned")
    void getWarrantyByProductIdForUser_NotFound() {
        UUID productId = UUID.randomUUID();
        when(warrantyRepository.findByProductIdAndProductUserId(productId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warrantyService.getWarrantyByProductIdForUser(productId, userId));
    }

    @Test
    @DisplayName("6. Status calculation: ACTIVE when more than 30 days remaining")
    void calculateStatus_Active() {
        LocalDate expiryDate = fixedToday.plusDays(31);
        WarrantyStatus status = warrantyService.calculateStatus(expiryDate, fixedToday);
        assertEquals(WarrantyStatus.ACTIVE, status);
    }

    @Test
    @DisplayName("7. Status calculation: EXPIRING_SOON when exactly 30 days or fewer remaining")
    void calculateStatus_ExpiringSoon() {
        LocalDate expiry30 = fixedToday.plusDays(30);
        assertEquals(WarrantyStatus.EXPIRING_SOON, warrantyService.calculateStatus(expiry30, fixedToday));

        LocalDate expiry1 = fixedToday.plusDays(1);
        assertEquals(WarrantyStatus.EXPIRING_SOON, warrantyService.calculateStatus(expiry1, fixedToday));

        LocalDate expiryToday = fixedToday;
        assertEquals(WarrantyStatus.EXPIRING_SOON, warrantyService.calculateStatus(expiryToday, fixedToday));
    }

    @Test
    @DisplayName("8. Status calculation: EXPIRED when today is after expiry date")
    void calculateStatus_Expired() {
        LocalDate pastExpiry = fixedToday.minusDays(1);
        WarrantyStatus status = warrantyService.calculateStatus(pastExpiry, fixedToday);
        assertEquals(WarrantyStatus.EXPIRED, status);
    }

    @Test
    @DisplayName("9. Days remaining: never negative and returns 0 when expired")
    void calculateDaysRemaining_NeverNegative() {
        LocalDate future = fixedToday.plusDays(45);
        assertEquals(45, warrantyService.calculateDaysRemaining(future, fixedToday));

        LocalDate past = fixedToday.minusDays(10);
        assertEquals(0, warrantyService.calculateDaysRemaining(past, fixedToday));

        LocalDate today = fixedToday;
        assertEquals(0, warrantyService.calculateDaysRemaining(today, fixedToday));
    }

    @Test
    @DisplayName("10. Progress percentage: correctly scales between 0 and 100")
    void calculateProgressPercentage_ScalesCorrectly() {
        LocalDate start = fixedToday.minusDays(50);
        LocalDate expiry = fixedToday.plusDays(50);
        // Total = 100 days, Elapsed = 50 days -> 50%
        int progressHalfway = warrantyService.calculateProgressPercentage(start, expiry, fixedToday);
        assertEquals(50, progressHalfway);

        // Before start date -> 0%
        int progressBefore = warrantyService.calculateProgressPercentage(fixedToday.plusDays(10), fixedToday.plusDays(100), fixedToday);
        assertEquals(0, progressBefore);

        // Past expiry -> 100%
        int progressPast = warrantyService.calculateProgressPercentage(fixedToday.minusDays(100), fixedToday.minusDays(10), fixedToday);
        assertEquals(100, progressPast);
    }

    @Test
    @DisplayName("11. Automatic status synchronization updates persisted status in DB when read")
    void autoSyncStatus_UpdatesDatabase() {
        UUID warrantyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = createSampleProduct(productId, fixedToday.minusMonths(12), 12);
        // Expiry was yesterday, but status in DB was still ACTIVE
        Warranty warranty = new Warranty(product, fixedToday.minusMonths(12), fixedToday.minusDays(1), WarrantyStatus.ACTIVE);
        warranty.setId(warrantyId);
        product.setWarranty(warranty);

        when(warrantyRepository.findByIdAndProductUserId(warrantyId, userId)).thenReturn(Optional.of(warranty));
        when(warrantyRepository.save(any(Warranty.class))).thenAnswer(i -> i.getArgument(0));

        WarrantyResponse resp = warrantyService.getWarrantyByIdForUser(warrantyId, userId);

        assertEquals(WarrantyStatus.EXPIRED, resp.getStatus());
        assertEquals(0, resp.getDaysRemaining());
        assertEquals(100, resp.getProgressPercentage());
        verify(warrantyRepository, times(1)).save(warranty);
    }
}
