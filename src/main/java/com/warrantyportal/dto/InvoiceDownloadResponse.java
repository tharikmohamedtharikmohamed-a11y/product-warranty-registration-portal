package com.warrantyportal.dto;

import java.util.UUID;

public class InvoiceDownloadResponse {

    private UUID invoiceId;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private int expiresInSeconds;

    public InvoiceDownloadResponse() {
    }

    public InvoiceDownloadResponse(UUID invoiceId, String fileName, String fileType, String downloadUrl, int expiresInSeconds) {
        this.invoiceId = invoiceId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
        this.expiresInSeconds = expiresInSeconds;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
