package com.warrantyportal.dto;

import com.warrantyportal.entity.WarrantyClaim;
import com.warrantyportal.entity.enums.ClaimStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdminClaimResponse {

    private UUID claimId;
    private UUID userId;
    private String userName;
    private UUID productId;
    private String productName;
    private UUID warrantyId;
    private UUID invoiceId;
    private String issueDescription;
    private ClaimStatus claimStatus;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminClaimResponse() {
    }

    public AdminClaimResponse(UUID claimId, UUID userId, String userName, UUID productId, String productName,
                              UUID warrantyId, UUID invoiceId, String issueDescription, ClaimStatus claimStatus,
                              String resolutionNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.claimId = claimId;
        this.userId = userId;
        this.userName = userName;
        this.productId = productId;
        this.productName = productName;
        this.warrantyId = warrantyId;
        this.invoiceId = invoiceId;
        this.issueDescription = issueDescription;
        this.claimStatus = claimStatus;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AdminClaimResponse fromEntity(WarrantyClaim claim) {
        return new AdminClaimResponse(
                claim.getId(),
                claim.getUser() != null ? claim.getUser().getId() : null,
                claim.getUser() != null ? claim.getUser().getName() : null,
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
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
