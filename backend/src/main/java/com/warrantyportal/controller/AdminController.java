package com.warrantyportal.controller;

import com.warrantyportal.dto.*;
import com.warrantyportal.service.AdminService;
import com.warrantyportal.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing administrative operations.
 * Strictly protected: requires authenticated ADMIN role.
 * Phase 11 — Admin Management Module
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final InvoiceService invoiceService;

    public AdminController(AdminService adminService, InvoiceService invoiceService) {
        this.adminService = adminService;
        this.invoiceService = invoiceService;
    }

    /**
     * Retrieves global platform-wide KPI statistics from database counts.
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /**
     * Retrieves all user accounts, with optional search by name or email.
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getUsers(search));
    }

    /**
     * Retrieves all registered products across all customers.
     */
    @GetMapping("/products")
    public ResponseEntity<List<AdminProductResponse>> getProducts() {
        return ResponseEntity.ok(adminService.getProducts());
    }

    /**
     * Retrieves all warranties across all customers with live status and countdowns.
     */
    @GetMapping("/warranties")
    public ResponseEntity<List<AdminWarrantyResponse>> getWarranties() {
        return ResponseEntity.ok(adminService.getWarranties());
    }

    /**
     * Retrieves all purchase invoice records across all customers.
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<AdminInvoiceResponse>> getInvoices() {
        return ResponseEntity.ok(adminService.getInvoices());
    }

    /**
     * Downloads an invoice file as an administrator.
     */
    @GetMapping("/invoices/{id}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID id) {
        InvoiceDownload download = invoiceService.downloadInvoiceForAdmin(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, download.getContentType())
                .body(download.getData());
    }

    /**
     * Streams an invoice file for inline viewing as an administrator.
     */
    @GetMapping("/invoices/{id}/view")
    public ResponseEntity<byte[]> viewInvoice(@PathVariable UUID id) {
        InvoiceDownload download = invoiceService.downloadInvoiceForAdmin(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, download.getContentType())
                .body(download.getData());
    }

    /**
     * Retrieves all warranty claims submitted by customers.
     */
    @GetMapping("/claims")
    public ResponseEntity<List<AdminClaimResponse>> getClaims() {
        return ResponseEntity.ok(adminService.getClaims());
    }

    /**
     * Retrieves single warranty claim details by ID.
     */
    @GetMapping("/claims/{id}")
    public ResponseEntity<AdminClaimResponse> getClaimById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getClaimById(id));
    }

    /**
     * Approves a PENDING warranty claim.
     */
    @PatchMapping("/claims/{id}/approve")
    public ResponseEntity<AdminClaimResponse> approveClaim(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid AdminClaimDecisionRequest request) {
        return ResponseEntity.ok(adminService.approveClaim(id, request));
    }

    /**
     * Rejects a PENDING warranty claim.
     */
    @PatchMapping("/claims/{id}/reject")
    public ResponseEntity<AdminClaimResponse> rejectClaim(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid AdminClaimDecisionRequest request) {
        return ResponseEntity.ok(adminService.rejectClaim(id, request));
    }

    /**
     * Moves an APPROVED claim to IN_PROGRESS.
     */
    @PatchMapping("/claims/{id}/start")
    public ResponseEntity<AdminClaimResponse> startClaim(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid AdminClaimDecisionRequest request) {
        return ResponseEntity.ok(adminService.startClaim(id, request));
    }

    /**
     * Marks an IN_PROGRESS claim as COMPLETED.
     */
    @PatchMapping("/claims/{id}/complete")
    public ResponseEntity<AdminClaimResponse> completeClaim(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid AdminClaimDecisionRequest request) {
        return ResponseEntity.ok(adminService.completeClaim(id, request));
    }
}
