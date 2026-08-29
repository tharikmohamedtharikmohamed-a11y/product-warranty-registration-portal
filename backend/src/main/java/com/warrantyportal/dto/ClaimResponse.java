package com.warrantyportal.dto;

import com.warrantyportal.entity.Claim;
import com.warrantyportal.entity.ClaimStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data transfer object returning customer-facing claim details,
 * associated product specifications, and current lifecycle status.
 * Phase 10 — Warranty Claims
 */
public class ClaimResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String brand;
    private String modelNumber;
    private String claimReason;
    private String description;
    private ClaimStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ClaimResponse() {
    }

    public ClaimResponse(UUID id, UUID productId, String productName, String brand, String modelNumber,
                         String claimReason, String description, ClaimStatus status,
                         OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.claimReason = claimReason;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ClaimResponse fromClaim(Claim claim) {
        if (claim == null) {
            return null;
        }

        UUID pId = null;
        String pName = null;
        String pBrand = null;
        String pModel = null;

        if (claim.getProduct() != null) {
            pId = claim.getProduct().getId();
            pName = claim.getProduct().getProductName();
            pBrand = claim.getProduct().getBrand();
            pModel = claim.getProduct().getModelNumber();
        }

        return new ClaimResponse(
                claim.getId(),
                pId,
                pName,
                pBrand,
                pModel,
                claim.getClaimReason(),
                claim.getDescription(),
                claim.getStatus(),
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
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

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
