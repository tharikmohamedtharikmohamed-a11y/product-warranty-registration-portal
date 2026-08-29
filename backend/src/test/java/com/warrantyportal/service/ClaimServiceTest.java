package com.warrantyportal.service;

import com.warrantyportal.dto.ClaimRequest;
import com.warrantyportal.dto.ClaimResponse;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.*;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.ClaimRepository;
import com.warrantyportal.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClaimService verifying claim creation, lifecycle initial status,
 * warranty eligibility rules, customer ownership isolation, and cancellation transitions.
 * Phase 10 — Warranty Claims
 */
@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarrantyService warrantyService;

    private ClaimService claimService;

    private User userA;
    private User userB;
    private Product productA;
    private UUID userAId;
    private UUID userBId;
    private UUID productAId;
    private UUID claimId;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, productRepository, warrantyService);

        userAId = UUID.randomUUID();
        userA = new User("Alice Developer", "alice@example.com", "hashedPass", Role.CUSTOMER);
        userA.setId(userAId);

        userBId = UUID.randomUUID();
        userB = new User("Bob Smith", "bob@example.com", "hashedPass", Role.CUSTOMER);
        userB.setId(userBId);

        productAId = UUID.randomUUID();
        productA = new Product(userA, "Sony Bravia 65 4K OLED", "Electronics", "Sony", "XR-65A80L",
                "SONY-2026-XYZ", LocalDate.of(2026, 1, 15), "Best Buy",
                BigDecimal.valueOf(1999.99), 24, "Main TV");
        productA.setId(productAId);

        claimId = UUID.randomUUID();
    }

    private WarrantyResponse createSampleWarranty(WarrantyStatus status) {
        return new WarrantyResponse(
                UUID.randomUUID(),
                productAId,
                productA.getProductName(),
                productA.getBrand(),
                productA.getModelNumber(),
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2028, 1, 15),
                24,
                status,
                status == WarrantyStatus.EXPIRED ? 0 : 500,
                status == WarrantyStatus.EXPIRED ? 100 : 30,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("1. Create claim persists claim and returns response")
    void testCreateClaim() {
        ClaimRequest request = new ClaimRequest(productAId, "Display flickering", "Screen turns off intermittently");
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(warrantyService.getWarrantyByProductIdForUser(productAId, userAId))
                .thenReturn(createSampleWarranty(WarrantyStatus.ACTIVE));

        Claim savedClaim = new Claim(userA, productA, "Display flickering", "Screen turns off intermittently", ClaimStatus.PENDING);
        savedClaim.setId(claimId);
        savedClaim.setCreatedAt(OffsetDateTime.now());
        savedClaim.setUpdatedAt(OffsetDateTime.now());

        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponse response = claimService.createClaim(request, userA);

        assertNotNull(response);
        assertEquals(claimId, response.getId());
        assertEquals(productAId, response.getProductId());
        assertEquals("Sony Bravia 65 4K OLED", response.getProductName());
        assertEquals("Display flickering", response.getClaimReason());
        assertEquals(ClaimStatus.PENDING, response.getStatus());
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    @DisplayName("2. New claim always starts in PENDING status")
    void testNewClaimStartsPending() {
        ClaimRequest request = new ClaimRequest(productAId, "Speaker distortion", "Left speaker buzzing");
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(warrantyService.getWarrantyByProductIdForUser(productAId, userAId))
                .thenReturn(createSampleWarranty(WarrantyStatus.ACTIVE));

        Claim savedClaim = new Claim(userA, productA, "Speaker distortion", "Left speaker buzzing", ClaimStatus.PENDING);
        savedClaim.setId(claimId);
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponse response = claimService.createClaim(request, userA);
        assertEquals(ClaimStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("3. Customer ownership: Unauthenticated user is rejected")
    void testUnauthenticatedUserRejected() {
        ClaimRequest request = new ClaimRequest(productAId, "Reason", "Desc");
        assertThrows(ResourceNotFoundException.class, () -> claimService.createClaim(request, null));
    }

    @Test
    @DisplayName("4. Product ownership enforcement: Claim against another user's product returns 404")
    void testProductOwnershipEnforcement() {
        ClaimRequest request = new ClaimRequest(productAId, "Audio fault", "Audio cuts out");
        when(productRepository.findByIdAndUserId(productAId, userBId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                claimService.createClaim(request, userB)
        );
        assertTrue(ex.getMessage().contains("Product not found with id: " + productAId));
        verifyNoInteractions(warrantyService);
        verifyNoInteractions(claimRepository);
    }

    @Test
    @DisplayName("5. Warranty eligibility: ACTIVE warranty allows claim")
    void testActiveWarrantyAllowsClaim() {
        ClaimRequest request = new ClaimRequest(productAId, "Broken port", "HDMI 1 not working");
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(warrantyService.getWarrantyByProductIdForUser(productAId, userAId))
                .thenReturn(createSampleWarranty(WarrantyStatus.ACTIVE));

        Claim savedClaim = new Claim(userA, productA, "Broken port", "HDMI 1 not working", ClaimStatus.PENDING);
        savedClaim.setId(claimId);
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponse response = claimService.createClaim(request, userA);
        assertNotNull(response);
        assertEquals(ClaimStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("6. Warranty eligibility: EXPIRING_SOON warranty allows claim")
    void testExpiringSoonWarrantyAllowsClaim() {
        ClaimRequest request = new ClaimRequest(productAId, "Power failure", "TV won't turn on");
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(warrantyService.getWarrantyByProductIdForUser(productAId, userAId))
                .thenReturn(createSampleWarranty(WarrantyStatus.EXPIRING_SOON));

        Claim savedClaim = new Claim(userA, productA, "Power failure", "TV won't turn on", ClaimStatus.PENDING);
        savedClaim.setId(claimId);
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);

        ClaimResponse response = claimService.createClaim(request, userA);
        assertNotNull(response);
        assertEquals(ClaimStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("7. Warranty eligibility: EXPIRED warranty rejects claim with InvalidClaimException")
    void testExpiredWarrantyRejectsClaim() {
        ClaimRequest request = new ClaimRequest(productAId, "Old defect", "Screen defect");
        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(warrantyService.getWarrantyByProductIdForUser(productAId, userAId))
                .thenReturn(createSampleWarranty(WarrantyStatus.EXPIRED));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class, () ->
                claimService.createClaim(request, userA)
        );
        assertEquals("Warranty has expired. A new claim cannot be submitted.", ex.getMessage());
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("8. List customer claims returns claims for authenticated user")
    void testListCustomerClaims() {
        Claim c1 = new Claim(userA, productA, "Issue 1", "Desc 1", ClaimStatus.PENDING);
        c1.setId(UUID.randomUUID());
        Claim c2 = new Claim(userA, productA, "Issue 2", "Desc 2", ClaimStatus.APPROVED);
        c2.setId(UUID.randomUUID());

        when(claimRepository.findByUserIdOrderByCreatedAtDesc(userAId)).thenReturn(List.of(c1, c2));

        List<ClaimResponse> list = claimService.getClaimsForUser(userAId);
        assertEquals(2, list.size());
        assertEquals("Issue 1", list.get(0).getClaimReason());
        assertEquals("Issue 2", list.get(1).getClaimReason());
    }

    @Test
    @DisplayName("9. Get customer claim returns single claim for owner")
    void testGetCustomerClaim() {
        Claim c = new Claim(userA, productA, "Main issue", "Main desc", ClaimStatus.PENDING);
        c.setId(claimId);
        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(c));

        ClaimResponse response = claimService.getClaimByIdForUser(claimId, userAId);
        assertNotNull(response);
        assertEquals(claimId, response.getId());
        assertEquals("Main issue", response.getClaimReason());
    }

    @Test
    @DisplayName("10. Reject unowned claim lookup with 404")
    void testRejectUnownedClaim() {
        when(claimRepository.findByIdAndUserId(claimId, userBId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                claimService.getClaimByIdForUser(claimId, userBId)
        );
    }

    @Test
    @DisplayName("11. Cancel pending claim succeeds and transitions status to CANCELLED")
    void testCancelPendingClaim() {
        Claim claim = new Claim(userA, productA, "Issue", "Desc", ClaimStatus.PENDING);
        claim.setId(claimId);

        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaimResponse response = claimService.cancelClaim(claimId, userAId);

        assertNotNull(response);
        assertEquals(ClaimStatus.CANCELLED, response.getStatus());
        assertEquals(ClaimStatus.CANCELLED, claim.getStatus());
        verify(claimRepository, times(1)).save(claim);
    }

    @Test
    @DisplayName("12. Reject cancellation of APPROVED claim")
    void testRejectCancellationOfApprovedClaim() {
        Claim claim = new Claim(userA, productA, "Issue", "Desc", ClaimStatus.APPROVED);
        claim.setId(claimId);

        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class, () ->
                claimService.cancelClaim(claimId, userAId)
        );
        assertEquals("Only pending claims can be cancelled.", ex.getMessage());
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("13. Reject cancellation of REJECTED claim")
    void testRejectCancellationOfRejectedClaim() {
        Claim claim = new Claim(userA, productA, "Issue", "Desc", ClaimStatus.REJECTED);
        claim.setId(claimId);

        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class, () ->
                claimService.cancelClaim(claimId, userAId)
        );
        assertEquals("Only pending claims can be cancelled.", ex.getMessage());
    }

    @Test
    @DisplayName("14. Reject cancellation of IN_PROGRESS claim")
    void testRejectCancellationOfInProgressClaim() {
        Claim claim = new Claim(userA, productA, "Issue", "Desc", ClaimStatus.IN_PROGRESS);
        claim.setId(claimId);

        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class, () ->
                claimService.cancelClaim(claimId, userAId)
        );
        assertEquals("Only pending claims can be cancelled.", ex.getMessage());
    }

    @Test
    @DisplayName("15. Reject cancellation of COMPLETED claim")
    void testRejectCancellationOfCompletedClaim() {
        Claim claim = new Claim(userA, productA, "Issue", "Desc", ClaimStatus.COMPLETED);
        claim.setId(claimId);

        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class, () ->
                claimService.cancelClaim(claimId, userAId)
        );
        assertEquals("Only pending claims can be cancelled.", ex.getMessage());
    }

    @Test
    @DisplayName("16. Reject cancellation of already CANCELLED claim")
    void testRejectCancellationOfCancelledClaim() {
        Claim claim = new Claim(userA, productA, "Issue", "Desc", ClaimStatus.CANCELLED);
        claim.setId(claimId);

        when(claimRepository.findByIdAndUserId(claimId, userAId)).thenReturn(Optional.of(claim));

        InvalidClaimException ex = assertThrows(InvalidClaimException.class, () ->
                claimService.cancelClaim(claimId, userAId)
        );
        assertEquals("Only pending claims can be cancelled.", ex.getMessage());
    }

    @Test
    @DisplayName("17. Reject cancellation of unowned claim (returns 404)")
    void testRejectCancellationOfUnownedClaim() {
        when(claimRepository.findByIdAndUserId(claimId, userBId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                claimService.cancelClaim(claimId, userBId)
        );
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("18. Get claims by product returns product claims")
    void testGetClaimsByProductIdForUser() {
        Claim c = new Claim(userA, productA, "Power cord issue", "Frayed cable", ClaimStatus.PENDING);
        c.setId(claimId);

        when(productRepository.findByIdAndUserId(productAId, userAId)).thenReturn(Optional.of(productA));
        when(claimRepository.findByProductIdAndUserIdOrderByCreatedAtDesc(productAId, userAId))
                .thenReturn(List.of(c));

        List<ClaimResponse> list = claimService.getClaimsByProductIdForUser(productAId, userAId);
        assertEquals(1, list.size());
        assertEquals("Power cord issue", list.get(0).getClaimReason());
    }
}
