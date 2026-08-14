package com.warrantyportal.dto;

import com.warrantyportal.entity.Invoice;

import java.time.LocalDateTime;
import java.util.UUID;

public class InvoiceResponse {

    private UUID invoiceId;
    private UUID productId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String storagePath;
    private String downloadUrl;

    public InvoiceResponse() {
    }

    public InvoiceResponse(UUID invoiceId, UUID productId, String fileName, String fileType, Long fileSize, LocalDateTime uploadedAt, String storagePath, String downloadUrl) {
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.storagePath = storagePath;
        this.downloadUrl = downloadUrl;
    }

    public static InvoiceResponse fromEntity(Invoice invoice) {
        String downloadUrl = "/api/invoices/" + invoice.getId() + "/download";
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getProduct() != null ? invoice.getProduct().getId() : null,
                invoice.getFileName(),
                invoice.getFileType(),
                invoice.getFileSize(),
                invoice.getUploadedAt(),
                invoice.getStoragePath(),
                downloadUrl
        );
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
