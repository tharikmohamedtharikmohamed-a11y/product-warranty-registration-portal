package com.warrantyportal.dto;

import com.warrantyportal.entity.Invoice;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Client-facing DTO for Invoice metadata.
 * Does NOT expose internal storage paths, service keys, or backend credentials.
 * Phase 9 — Invoice Management
 */
public class InvoiceResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private OffsetDateTime uploadedAt;

    public InvoiceResponse() {
    }

    public InvoiceResponse(UUID id, UUID productId, String productName, String fileName,
                           String fileType, Long fileSize, OffsetDateTime uploadedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public static InvoiceResponse fromInvoice(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getProduct() != null ? invoice.getProduct().getId() : null,
                invoice.getProduct() != null ? invoice.getProduct().getProductName() : null,
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
