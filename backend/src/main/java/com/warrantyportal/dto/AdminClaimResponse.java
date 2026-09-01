package com.warrantyportal.dto;

import com.warrantyportal.entity.Claim;
import com.warrantyportal.entity.ClaimStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Administrative Data Transfer Object presenting complete warranty claim data,
 * customer identification, product specifications, status, and internal admin notes.
 * Phase 11 — Admin Management Module
 */
public class AdminClaimResponse {

    public static final String ADMIN_NOTE_DELIMITER = "\n---ADMIN_NOTE---\n";

    private UUID id;
    private UUID productId;
    private String productName;
    private String brand;
    private String modelNumber;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private String claimReason;
    private String description;
    private String adminNotes;
    private ClaimStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public AdminClaimResponse() {
    }

    public AdminClaimResponse(UUID id, UUID productId, String productName, String brand,
                              String modelNumber, UUID userId, String customerName, String customerEmail,
                              String claimReason, String description, String adminNotes,
                              ClaimStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.claimReason = claimReason;
        this.description = description;
        this.adminNotes = adminNotes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AdminClaimResponse fromClaim(Claim claim) {
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

        UUID uId = null;
        String uName = null;
        String uEmail = null;
        if (claim.getUser() != null) {
            uId = claim.getUser().getId();
            uName = claim.getUser().getName();
            uEmail = claim.getUser().getEmail();
        }

        String rawDesc = claim.getDescription();
        String customerDesc = rawDesc;
        String adminNotes = null;

        if (rawDesc != null && rawDesc.contains(ADMIN_NOTE_DELIMITER)) {
            int idx = rawDesc.indexOf(ADMIN_NOTE_DELIMITER);
            customerDesc = rawDesc.substring(0, idx);
            adminNotes = rawDesc.substring(idx + ADMIN_NOTE_DELIMITER.length());
        }

        return new AdminClaimResponse(
                claim.getId(),
                pId,
                pName,
                pBrand,
                pModel,
                uId,
                uName,
                uEmail,
                claim.getClaimReason(),
                customerDesc,
                adminNotes,
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
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
