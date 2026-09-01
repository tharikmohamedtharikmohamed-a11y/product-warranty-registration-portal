package com.warrantyportal.dto;

import com.warrantyportal.entity.Invoice;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Administrative Data Transfer Object presenting purchase invoice metadata
 * alongside the owning customer and product information.
 * Phase 11 — Admin Management Module
 */
public class AdminInvoiceResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private String fileName;
    private String fileType;
    private long fileSize;
    private OffsetDateTime uploadedAt;

    public AdminInvoiceResponse() {
    }

    public AdminInvoiceResponse(UUID id, UUID productId, String productName, UUID userId,
                                String customerName, String customerEmail, String fileName,
                                String fileType, long fileSize, OffsetDateTime uploadedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public static AdminInvoiceResponse fromInvoice(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        UUID pId = null;
        String pName = null;
        if (invoice.getProduct() != null) {
            pId = invoice.getProduct().getId();
            pName = invoice.getProduct().getProductName();
        }

        UUID uId = null;
        String uName = null;
        String uEmail = null;
        if (invoice.getUser() != null) {
            uId = invoice.getUser().getId();
            uName = invoice.getUser().getName();
            uEmail = invoice.getUser().getEmail();
        }

        return new AdminInvoiceResponse(
                invoice.getId(),
                pId,
                pName,
                uId,
                uName,
                uEmail,
                invoice.getFileName(),
                invoice.getFileType(),
                invoice.getFileSize(),
                invoice.getUploadedAt()
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
