package com.warrantyportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateClaimRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Warranty ID is required")
    private UUID warrantyId;

    private UUID invoiceId;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    public CreateClaimRequest() {
    }

    public CreateClaimRequest(UUID productId, UUID warrantyId, UUID invoiceId, String issueDescription) {
        this.productId = productId;
        this.warrantyId = warrantyId;
        this.invoiceId = invoiceId;
        this.issueDescription = issueDescription;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
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
}
