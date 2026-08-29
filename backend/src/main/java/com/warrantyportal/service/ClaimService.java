package com.warrantyportal.service;

import com.warrantyportal.dto.ClaimRequest;
import com.warrantyportal.dto.ClaimResponse;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.Claim;
import com.warrantyportal.entity.ClaimStatus;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.ClaimRepository;
import com.warrantyportal.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating customer warranty claims, warranty eligibility validation,
 * customer ownership isolation, and cancellation transitions.
 * Phase 10 — Warranty Claims
 */
@Service
@Transactional
public class ClaimService {

    private static final Logger logger = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final ProductRepository productRepository;
    private final WarrantyService warrantyService;

    public ClaimService(ClaimRepository claimRepository,
                        ProductRepository productRepository,
                        WarrantyService warrantyService) {
        this.claimRepository = claimRepository;
        this.productRepository = productRepository;
        this.warrantyService = warrantyService;
    }

    /**
     * Submits a new warranty claim for an authenticated customer's product.
     * Enforces that the product exists, belongs to the customer, and has an active warranty.
     */
    public ClaimResponse createClaim(ClaimRequest request, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new ResourceNotFoundException("User authentication required");
        }

        UUID productId = request.getProductId();

        // 1. Verify product ownership (returns 404 if product does not exist or belongs to another user)
        Product product = productRepository.findByIdAndUserId(productId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // 2. Verify warranty eligibility via authoritative WarrantyService
        WarrantyResponse warranty = warrantyService.getWarrantyByProductIdForUser(productId, currentUser.getId());
        if (warranty == null || warranty.getStatus() == WarrantyStatus.EXPIRED) {
            logger.warn("Claim rejected: Product {} warranty is expired or missing", productId);
            throw new InvalidClaimException("Warranty has expired. A new claim cannot be submitted.");
        }

        // 3. Construct new claim with forced initial status PENDING
        String claimReason = request.getClaimReason() != null ? request.getClaimReason().trim() : "";
        String description = request.getDescription() != null ? request.getDescription().trim() : null;

        Claim claim = new Claim(currentUser, product, claimReason, description, ClaimStatus.PENDING);

        Claim savedClaim = claimRepository.save(claim);
        logger.info("Warranty claim created successfully: claimId={}, productId={}, status={}",
                savedClaim.getId(), productId, savedClaim.getStatus());

        return ClaimResponse.fromClaim(savedClaim);
    }

    /**
     * Retrieves all warranty claims submitted by the authenticated customer.
     */
    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsForUser(UUID userId) {
        return claimRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ClaimResponse::fromClaim)
                .toList();
    }

    /**
     * Retrieves a single claim by ID, verifying customer ownership.
     * Returns 404 if not found or owned by another user to prevent IDOR scanning.
     */
    @Transactional(readOnly = true)
    public ClaimResponse getClaimByIdForUser(UUID claimId, UUID userId) {
        Claim claim = claimRepository.findByIdAndUserId(claimId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
        return ClaimResponse.fromClaim(claim);
    }

    /**
     * Retrieves all claims filed against a specific product owned by the authenticated customer.
     */
    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByProductIdForUser(UUID productId, UUID userId) {
        // Verify product ownership first
        productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return claimRepository.findByProductIdAndUserIdOrderByCreatedAtDesc(productId, userId)
                .stream()
                .map(ClaimResponse::fromClaim)
                .toList();
    }

    /**
     * Cancels an existing claim. Cancellation is permitted ONLY if the claim is currently in PENDING status.
     */
    public ClaimResponse cancelClaim(UUID claimId, UUID userId) {
        Claim claim = claimRepository.findByIdAndUserId(claimId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            logger.warn("Invalid cancellation attempt on claim {}: status is {}", claimId, claim.getStatus());
            throw new InvalidClaimException("Only pending claims can be cancelled.");
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        Claim updatedClaim = claimRepository.save(claim);
        logger.info("Claim {} successfully cancelled by user {}", claimId, userId);

        return ClaimResponse.fromClaim(updatedClaim);
    }
}
