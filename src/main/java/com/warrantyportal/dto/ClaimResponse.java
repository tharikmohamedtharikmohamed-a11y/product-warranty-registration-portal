package com.warrantyportal.dto;

import com.warrantyportal.entity.WarrantyClaim;
import com.warrantyportal.entity.enums.ClaimStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClaimResponse {

    private UUID claimId;
    private UUID productId;
    private String productName;
    private UUID warrantyId;
    private UUID invoiceId;
    private String issueDescription;
    private ClaimStatus status;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClaimResponse() {
    }

    public ClaimResponse(UUID claimId, UUID productId, String productName, UUID warrantyId, UUID invoiceId,
                         String issueDescription, ClaimStatus status, String resolutionNotes,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.claimId = claimId;
        this.productId = productId;
        this.productName = productName;
        this.warrantyId = warrantyId;
        this.invoiceId = invoiceId;
        this.issueDescription = issueDescription;
        this.status = status;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ClaimResponse fromEntity(WarrantyClaim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getProduct() != null ? claim.getProduct().getId() : null,
                claim.getProduct() != null ? claim.getProduct().getProductName() : null,
                claim.getWarranty() != null ? claim.getWarranty().getId() : null,
                claim.getInvoice() != null ? claim.getInvoice().getId() : null,
                claim.getIssueDescription(),
                claim.getStatus(),
                claim.getResolution(),
                claim.getClaimDate(),
                claim.getUpdatedAt()
        );
    }

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
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

    public UUID getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(UUID warrantyId) {
        this.warrantyId = warrantyId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
