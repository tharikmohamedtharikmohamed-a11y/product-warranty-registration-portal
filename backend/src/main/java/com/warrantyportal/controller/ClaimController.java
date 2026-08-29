package com.warrantyportal.controller;

import com.warrantyportal.dto.ClaimRequest;
import com.warrantyportal.dto.ClaimResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for customer warranty claim submission, queries, and cancellation.
 * All operations are strictly customer-scoped.
 * Phase 10 — Warranty Claims
 */
@RestController
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    /**
     * Submits a new warranty claim against an eligible registered product.
     */
    @PostMapping("/api/claims")
    public ResponseEntity<ClaimResponse> createClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal User currentUser) {
        ClaimResponse response = claimService.createClaim(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all warranty claims submitted by the authenticated customer.
     */
    @GetMapping("/api/claims")
    public ResponseEntity<List<ClaimResponse>> getClaims(
            @AuthenticationPrincipal User currentUser) {
        List<ClaimResponse> claims = claimService.getClaimsForUser(currentUser.getId());
        return ResponseEntity.ok(claims);
    }

    /**
     * Retrieves detailed information for a single claim scoped to the authenticated customer.
     * Returns 404 if not found or owned by another user.
     */
    @GetMapping("/api/claims/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        ClaimResponse claim = claimService.getClaimByIdForUser(id, currentUser.getId());
        return ResponseEntity.ok(claim);
    }

    /**
     * Cancels an eligible pending claim.
     * Only claims with status PENDING can be cancelled.
     */
    @PatchMapping("/api/claims/{id}/cancel")
    public ResponseEntity<ClaimResponse> cancelClaim(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        ClaimResponse response = claimService.cancelClaim(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all claims filed against a specific product owned by the authenticated customer.
     */
    @GetMapping("/api/products/{productId}/claims")
    public ResponseEntity<List<ClaimResponse>> getProductClaims(
            @PathVariable UUID productId,
            @AuthenticationPrincipal User currentUser) {
        List<ClaimResponse> claims = claimService.getClaimsByProductIdForUser(productId, currentUser.getId());
        return ResponseEntity.ok(claims);
    }
}
