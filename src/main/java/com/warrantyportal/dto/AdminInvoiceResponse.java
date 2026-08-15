package com.warrantyportal.dto;

import com.warrantyportal.entity.Invoice;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdminInvoiceResponse {

    private UUID invoiceId;
    private UUID userId;
    private String userName;
    private UUID productId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String storagePath;

    public AdminInvoiceResponse() {
    }

    public AdminInvoiceResponse(UUID invoiceId, UUID userId, String userName, UUID productId,
                                String fileName, String fileType, Long fileSize,
                                LocalDateTime uploadedAt, String storagePath) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.userName = userName;
        this.productId = productId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.storagePath = storagePath;
    }

    public static AdminInvoiceResponse fromEntity(Invoice invoice) {
        return new AdminInvoiceResponse(
                invoice.getId(),
                invoice.getUser() != null ? invoice.getUser().getId() : null,
                invoice.getUser() != null ? invoice.getUser().getName() : null,
                invoice.getProduct() != null ? invoice.getProduct().getId() : null,
                invoice.getFileName(),
                invoice.getFileType(),
                invoice.getFileSize(),
                invoice.getUploadedAt(),
                invoice.getStoragePath()
        );
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
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
}
