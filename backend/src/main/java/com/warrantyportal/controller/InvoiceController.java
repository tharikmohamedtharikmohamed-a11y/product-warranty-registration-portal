package com.warrantyportal.controller;

import com.warrantyportal.dto.InvoiceDownload;
import com.warrantyportal.dto.InvoiceResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.InvoiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for customer invoice upload, retrieval, download, preview, and removal.
 * All operations are strictly scoped to the authenticated customer.
 * Binary payloads reside in private Supabase Storage; metadata in PostgreSQL.
 * Phase 9 — Invoice Management
 */
@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Uploads a proof-of-purchase invoice document for a customer's product.
     */
    @PostMapping(value = "/api/invoices/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceResponse> uploadInvoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") UUID productId,
            @AuthenticationPrincipal User currentUser) {
        InvoiceResponse response = invoiceService.uploadInvoice(productId, file, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all invoices belonging to the authenticated customer.
     */
    @GetMapping("/api/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @AuthenticationPrincipal User currentUser) {
        List<InvoiceResponse> invoices = invoiceService.getInvoicesForUser(currentUser.getId());
        return ResponseEntity.ok(invoices);
    }

    /**
     * Retrieves a single invoice metadata record by ID scoped to the authenticated customer.
     * Returns 404 if not found or if owned by another customer.
     */
    @GetMapping("/api/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        InvoiceResponse invoice = invoiceService.getInvoiceByIdForUser(id, currentUser.getId());
        return ResponseEntity.ok(invoice);
    }

    /**
     * Downloads an invoice file as an attachment.
     * Returns 404 if not found or if owned by another customer.
     */
    @GetMapping("/api/invoices/{id}/download")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        InvoiceDownload download = invoiceService.downloadInvoice(id, currentUser.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, download.getContentType())
                .body(download.getData());
    }

    /**
     * Streams an invoice file for inline viewing (PDF/images) in the browser.
     * Returns 404 if not found or if owned by another customer.
     */
    @GetMapping("/api/invoices/{id}/view")
    public ResponseEntity<byte[]> viewInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        InvoiceDownload download = invoiceService.downloadInvoice(id, currentUser.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, download.getContentType())
                .body(download.getData());
    }

    /**
     * Deletes an invoice from private Supabase Storage and PostgreSQL metadata.
     * Returns 404 if not found or if owned by another customer.
     */
    @DeleteMapping("/api/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        invoiceService.deleteInvoice(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the latest invoice metadata associated with a specific product ID scoped to the authenticated customer.
     * Returns 404 if product/invoice not found or owned by another customer.
     */
    @GetMapping("/api/products/{productId}/invoice")
    public ResponseEntity<InvoiceResponse> getProductInvoice(
            @PathVariable UUID productId,
            @AuthenticationPrincipal User currentUser) {
        InvoiceResponse invoice = invoiceService.getInvoiceByProductIdForUser(productId, currentUser.getId());
        return ResponseEntity.ok(invoice);
    }
}
