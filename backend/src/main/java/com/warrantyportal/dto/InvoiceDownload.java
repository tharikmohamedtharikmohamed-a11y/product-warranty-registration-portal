package com.warrantyportal.dto;

/**
 * Data transfer object encapsulating binary file contents, original file name, and MIME content type
 * for streaming downloads and previews.
 * Phase 9 — Invoice Management
 */
public class InvoiceDownload {

    private final byte[] data;
    private final String fileName;
    private final String contentType;

    public InvoiceDownload(byte[] data, String fileName, String contentType) {
        this.data = data;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }
}
