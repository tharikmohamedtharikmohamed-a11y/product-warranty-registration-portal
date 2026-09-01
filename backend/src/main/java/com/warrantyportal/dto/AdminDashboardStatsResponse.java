package com.warrantyportal.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing platform-wide administrative KPI metrics
 * and operational telemetry (recent claims, recent users, recent products).
 * Calculated strictly from authoritative database records.
 * Phase 11 & Phase 12 — Admin Management & Operational Telemetry
 */
public class AdminDashboardStatsResponse {

    private long users;
    private long customers;
    private long admins;
    private long products;
    private long warranties;
    private long activeWarranties;
    private long expiringSoonWarranties;
    private long expiredWarranties;
    private long invoices;
    private long claims;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long inProgressClaims;
    private long completedClaims;
    private long cancelledClaims;

    private List<AdminRecentClaimDto> recentClaims = new ArrayList<>();
    private List<AdminRecentUserDto> recentUsers = new ArrayList<>();
    private List<AdminRecentProductDto> recentProducts = new ArrayList<>();

    public AdminDashboardStatsResponse() {
    }

    public AdminDashboardStatsResponse(long users, long customers, long admins, long products,
                                       long warranties, long activeWarranties, long expiringSoonWarranties, long expiredWarranties,
                                       long invoices, long claims, long pendingClaims, long approvedClaims,
                                       long rejectedClaims, long inProgressClaims, long completedClaims, long cancelledClaims) {
        this(users, customers, admins, products, warranties, activeWarranties, expiringSoonWarranties, expiredWarranties,
             invoices, claims, pendingClaims, approvedClaims, rejectedClaims, inProgressClaims, completedClaims, cancelledClaims,
             new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public AdminDashboardStatsResponse(long users, long customers, long admins, long products,
                                       long warranties, long activeWarranties, long expiringSoonWarranties, long expiredWarranties,
                                       long invoices, long claims, long pendingClaims, long approvedClaims,
                                       long rejectedClaims, long inProgressClaims, long completedClaims, long cancelledClaims,
                                       List<AdminRecentClaimDto> recentClaims,
                                       List<AdminRecentUserDto> recentUsers,
                                       List<AdminRecentProductDto> recentProducts) {
        this.users = users;
        this.customers = customers;
        this.admins = admins;
        this.products = products;
        this.warranties = warranties;
        this.activeWarranties = activeWarranties;
        this.expiringSoonWarranties = expiringSoonWarranties;
        this.expiredWarranties = expiredWarranties;
        this.invoices = invoices;
        this.claims = claims;
        this.pendingClaims = pendingClaims;
        this.approvedClaims = approvedClaims;
        this.rejectedClaims = rejectedClaims;
        this.inProgressClaims = inProgressClaims;
        this.completedClaims = completedClaims;
        this.cancelledClaims = cancelledClaims;
        this.recentClaims = recentClaims != null ? recentClaims : new ArrayList<>();
        this.recentUsers = recentUsers != null ? recentUsers : new ArrayList<>();
        this.recentProducts = recentProducts != null ? recentProducts : new ArrayList<>();
    }

    // --- Sub-DTOs for operational previews ---

    public static class AdminRecentClaimDto {
        private UUID id;
        private String customerName;
        private String customerEmail;
        private String productName;
        private String claimReason;
        private String status;
        private OffsetDateTime createdAt;

        public AdminRecentClaimDto() {}

        public AdminRecentClaimDto(UUID id, String customerName, String customerEmail, String productName, String claimReason, String status, OffsetDateTime createdAt) {
            this.id = id;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.productName = productName;
            this.claimReason = claimReason;
            this.status = status;
            this.createdAt = createdAt;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getClaimReason() { return claimReason; }
        public void setClaimReason(String claimReason) { this.claimReason = claimReason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class AdminRecentUserDto {
        private UUID id;
        private String name;
        private String email;
        private String role;
        private OffsetDateTime createdAt;

        public AdminRecentUserDto() {}

        public AdminRecentUserDto(UUID id, String name, String email, String role, OffsetDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.createdAt = createdAt;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class AdminRecentProductDto {
        private UUID id;
        private String productName;
        private String customerName;
        private String brand;
        private LocalDate purchaseDate;
        private String warrantyStatus;

        public AdminRecentProductDto() {}

        public AdminRecentProductDto(UUID id, String productName, String customerName, String brand, LocalDate purchaseDate, String warrantyStatus) {
            this.id = id;
            this.productName = productName;
            this.customerName = customerName;
            this.brand = brand;
            this.purchaseDate = purchaseDate;
            this.warrantyStatus = warrantyStatus;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public LocalDate getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
        public String getWarrantyStatus() { return warrantyStatus; }
        public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
    }

    // --- Getters & Setters ---

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }
    public long getCustomers() { return customers; }
    public void setCustomers(long customers) { this.customers = customers; }
    public long getAdmins() { return admins; }
    public void setAdmins(long admins) { this.admins = admins; }
    public long getProducts() { return products; }
    public void setProducts(long products) { this.products = products; }
    public long getWarranties() { return warranties; }
    public void setWarranties(long warranties) { this.warranties = warranties; }
    public long getActiveWarranties() { return activeWarranties; }
    public void setActiveWarranties(long activeWarranties) { this.activeWarranties = activeWarranties; }
    public long getExpiringSoonWarranties() { return expiringSoonWarranties; }
    public void setExpiringSoonWarranties(long expiringSoonWarranties) { this.expiringSoonWarranties = expiringSoonWarranties; }
    public long getExpiredWarranties() { return expiredWarranties; }
    public void setExpiredWarranties(long expiredWarranties) { this.expiredWarranties = expiredWarranties; }
    public long getInvoices() { return invoices; }
    public void setInvoices(long invoices) { this.invoices = invoices; }
    public long getClaims() { return claims; }
    public void setClaims(long claims) { this.claims = claims; }
    public long getPendingClaims() { return pendingClaims; }
    public void setPendingClaims(long pendingClaims) { this.pendingClaims = pendingClaims; }
    public long getApprovedClaims() { return approvedClaims; }
    public void setApprovedClaims(long approvedClaims) { this.approvedClaims = approvedClaims; }
    public long getRejectedClaims() { return rejectedClaims; }
    public void setRejectedClaims(long rejectedClaims) { this.rejectedClaims = rejectedClaims; }
    public long getInProgressClaims() { return inProgressClaims; }
    public void setInProgressClaims(long inProgressClaims) { this.inProgressClaims = inProgressClaims; }
    public long getCompletedClaims() { return completedClaims; }
    public void setCompletedClaims(long completedClaims) { this.completedClaims = completedClaims; }
    public long getCancelledClaims() { return cancelledClaims; }
    public void setCancelledClaims(long cancelledClaims) { this.cancelledClaims = cancelledClaims; }

    public List<AdminRecentClaimDto> getRecentClaims() { return recentClaims; }
    public void setRecentClaims(List<AdminRecentClaimDto> recentClaims) { this.recentClaims = recentClaims; }
    public List<AdminRecentUserDto> getRecentUsers() { return recentUsers; }
    public void setRecentUsers(List<AdminRecentUserDto> recentUsers) { this.recentUsers = recentUsers; }
    public List<AdminRecentProductDto> getRecentProducts() { return recentProducts; }
    public void setRecentProducts(List<AdminRecentProductDto> recentProducts) { this.recentProducts = recentProducts; }
}
