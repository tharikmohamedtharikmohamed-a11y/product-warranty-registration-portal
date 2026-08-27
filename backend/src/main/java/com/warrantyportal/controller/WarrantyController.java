package com.warrantyportal.controller;

import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.WarrantyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for customer warranty queries, status monitoring, and coverage details.
 * All operations are strictly scoped to the authenticated customer.
 * Phase 8 — Warranty Management
 */
@RestController
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    /**
     * Retrieves all warranties belonging to the authenticated customer.
     */
    @GetMapping("/api/warranties")
    public ResponseEntity<List<WarrantyResponse>> getWarranties(
            @AuthenticationPrincipal User currentUser) {
        List<WarrantyResponse> warranties = warrantyService.getWarrantiesForUser(currentUser.getId());
        return ResponseEntity.ok(warranties);
    }

    /**
     * Retrieves a single warranty by ID scoped to the authenticated customer.
     * Returns 404 if not found or if owned by another customer.
     */
    @GetMapping("/api/warranties/{id}")
    public ResponseEntity<WarrantyResponse> getWarrantyById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        WarrantyResponse warranty = warrantyService.getWarrantyByIdForUser(id, currentUser.getId());
        return ResponseEntity.ok(warranty);
    }

    /**
     * Retrieves the warranty associated with a specific product ID scoped to the authenticated customer.
     * Returns 404 if product/warranty not found or owned by another customer.
     */
    @GetMapping("/api/products/{productId}/warranty")
    public ResponseEntity<WarrantyResponse> getProductWarranty(
            @PathVariable UUID productId,
            @AuthenticationPrincipal User currentUser) {
        WarrantyResponse warranty = warrantyService.getWarrantyByProductIdForUser(productId, currentUser.getId());
        return ResponseEntity.ok(warranty);
    }
}
