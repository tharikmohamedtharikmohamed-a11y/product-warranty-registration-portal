package com.warrantyportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Data transfer object for customer warranty claim submission.
 * Excludes user, status, and administrative adjudication fields.
 * Phase 10 — Warranty Claims
 */
public class ClaimRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "Claim reason is required")
    @Size(max = 2000, message = "Claim reason must not exceed 2000 characters")
    private String claimReason;

    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    public ClaimRequest() {
    }

    public ClaimRequest(UUID productId, String claimReason, String description) {
        this.productId = productId;
        this.claimReason = claimReason;
        this.description = description;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getClaimReason() {
        return claimReason;
    }

    public void setClaimReason(String claimReason) {
        this.claimReason = claimReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
