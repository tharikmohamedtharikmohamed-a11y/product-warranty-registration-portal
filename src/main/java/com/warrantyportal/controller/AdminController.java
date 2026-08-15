package com.warrantyportal.controller;

import com.warrantyportal.dto.*;
import com.warrantyportal.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<AdminUserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable("id") UUID id) {
        AdminUserResponse user = adminService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/products")
    public ResponseEntity<List<AdminProductResponse>> getAllProducts() {
        List<AdminProductResponse> products = adminService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/warranties")
    public ResponseEntity<List<AdminWarrantyResponse>> getAllWarranties() {
        List<AdminWarrantyResponse> warranties = adminService.getAllWarranties();
        return ResponseEntity.ok(warranties);
    }

    @GetMapping("/claims")
    public ResponseEntity<List<AdminClaimResponse>> getAllClaims() {
        List<AdminClaimResponse> claims = adminService.getAllClaims();
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/claims/{id}")
    public ResponseEntity<AdminClaimResponse> getClaimById(@PathVariable("id") UUID id) {
        AdminClaimResponse claim = adminService.getClaimById(id);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<AdminInvoiceResponse>> getAllInvoices() {
        List<AdminInvoiceResponse> invoices = adminService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/invoices/{id}/download")
    public ResponseEntity<InvoiceDownloadResponse> getInvoiceDownload(@PathVariable("id") UUID id) {
        InvoiceDownloadResponse response = adminService.getInvoiceDownload(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
