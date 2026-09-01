package com.warrantyportal.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Customer Dashboard Aggregation Response DTO.
 * Exposes real database-backed metrics, expiring warranties, recent claims, and real recent activity.
 * Phase 12 — Dashboard & Notifications
 */
public class DashboardResponse {

    private ProductsSummary products;
    private WarrantiesSummary warranties;
    private ClaimsSummary claims;
    private InvoicesSummary invoices;
    private List<ExpiringWarrantyItem> expiringWarranties = new ArrayList<>();
    private List<RecentClaimItem> recentClaims = new ArrayList<>();
    private List<RecentActivityItem> recentActivity = new ArrayList<>();

    public DashboardResponse() {
        this.products = new ProductsSummary();
        this.warranties = new WarrantiesSummary();
        this.claims = new ClaimsSummary();
        this.invoices = new InvoicesSummary();
    }

    public DashboardResponse(ProductsSummary products,
                             WarrantiesSummary warranties,
                             ClaimsSummary claims,
                             InvoicesSummary invoices,
                             List<ExpiringWarrantyItem> expiringWarranties,
                             List<RecentClaimItem> recentClaims,
                             List<RecentActivityItem> recentActivity) {
        this.products = products != null ? products : new ProductsSummary();
        this.warranties = warranties != null ? warranties : new WarrantiesSummary();
        this.claims = claims != null ? claims : new ClaimsSummary();
        this.invoices = invoices != null ? invoices : new InvoicesSummary();
        this.expiringWarranties = expiringWarranties != null ? expiringWarranties : new ArrayList<>();
        this.recentClaims = recentClaims != null ? recentClaims : new ArrayList<>();
        this.recentActivity = recentActivity != null ? recentActivity : new ArrayList<>();
    }

    // --- Sub-DTOs ---

    public static class ProductsSummary {
        private long total;

        public ProductsSummary() {}
        public ProductsSummary(long total) { this.total = total; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }

    public static class WarrantiesSummary {
        private long total;
        private long active;
        private long expiringSoon;
        private long expired;

        public WarrantiesSummary() {}
        public WarrantiesSummary(long total, long active, long expiringSoon, long expired) {
            this.total = total;
            this.active = active;
            this.expiringSoon = expiringSoon;
            this.expired = expired;
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        public long getExpiringSoon() { return expiringSoon; }
        public void setExpiringSoon(long expiringSoon) { this.expiringSoon = expiringSoon; }
        public long getExpired() { return expired; }
        public void setExpired(long expired) { this.expired = expired; }
    }

    public static class ClaimsSummary {
        private long total;
        private long pending;
        private long approved;
        private long inProgress;
        private long completed;
        private long rejected;
        private long cancelled;

        public ClaimsSummary() {}
        public ClaimsSummary(long total, long pending, long approved, long inProgress, long completed, long rejected, long cancelled) {
            this.total = total;
            this.pending = pending;
            this.approved = approved;
            this.inProgress = inProgress;
            this.completed = completed;
            this.rejected = rejected;
            this.cancelled = cancelled;
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }
        public long getApproved() { return approved; }
        public void setApproved(long approved) { this.approved = approved; }
        public long getInProgress() { return inProgress; }
        public void setInProgress(long inProgress) { this.inProgress = inProgress; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public long getRejected() { return rejected; }
        public void setRejected(long rejected) { this.rejected = rejected; }
        public long getCancelled() { return cancelled; }
        public void setCancelled(long cancelled) { this.cancelled = cancelled; }
    }

    public static class InvoicesSummary {
        private long total;

        public InvoicesSummary() {}
        public InvoicesSummary(long total) { this.total = total; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }

    public static class ExpiringWarrantyItem {
        private UUID warrantyId;
        private UUID productId;
        private String productName;
        private String brand;
        private LocalDate expiryDate;
        private long daysRemaining;
        private String status;

        public ExpiringWarrantyItem() {}

        public ExpiringWarrantyItem(UUID warrantyId, UUID productId, String productName, String brand, LocalDate expiryDate, long daysRemaining, String status) {
            this.warrantyId = warrantyId;
            this.productId = productId;
            this.productName = productName;
            this.brand = brand;
            this.expiryDate = expiryDate;
            this.daysRemaining = daysRemaining;
            this.status = status;
        }

        public UUID getWarrantyId() { return warrantyId; }
        public void setWarrantyId(UUID warrantyId) { this.warrantyId = warrantyId; }
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public long getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class RecentClaimItem {
        private UUID claimId;
        private UUID productId;
        private String productName;
        private String claimReason;
        private String status;
        private OffsetDateTime createdAt;

        public RecentClaimItem() {}

        public RecentClaimItem(UUID claimId, UUID productId, String productName, String claimReason, String status, OffsetDateTime createdAt) {
            this.claimId = claimId;
            this.productId = productId;
            this.productName = productName;
            this.claimReason = claimReason;
            this.status = status;
            this.createdAt = createdAt;
        }

        public UUID getClaimId() { return claimId; }
        public void setClaimId(UUID claimId) { this.claimId = claimId; }
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getClaimReason() { return claimReason; }
        public void setClaimReason(String claimReason) { this.claimReason = claimReason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class RecentActivityItem {
        private String activityType; // PRODUCT, INVOICE, CLAIM
        private String title;
        private String description;
        private OffsetDateTime timestamp;
        private UUID referenceId;

        public RecentActivityItem() {}

        public RecentActivityItem(String activityType, String title, String description, OffsetDateTime timestamp, UUID referenceId) {
            this.activityType = activityType;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.referenceId = referenceId;
        }

        public String getActivityType() { return activityType; }
        public void setActivityType(String activityType) { this.activityType = activityType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
        public UUID getReferenceId() { return referenceId; }
        public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
    }

    // --- Main Getters & Setters ---

    public ProductsSummary getProducts() { return products; }
    public void setProducts(ProductsSummary products) { this.products = products; }
    public WarrantiesSummary getWarranties() { return warranties; }
    public void setWarranties(WarrantiesSummary warranties) { this.warranties = warranties; }
    public ClaimsSummary getClaims() { return claims; }
    public void setClaims(ClaimsSummary claims) { this.claims = claims; }
    public InvoicesSummary getInvoices() { return invoices; }
    public void setInvoices(InvoicesSummary invoices) { this.invoices = invoices; }
    public List<ExpiringWarrantyItem> getExpiringWarranties() { return expiringWarranties; }
    public void setExpiringWarranties(List<ExpiringWarrantyItem> expiringWarranties) { this.expiringWarranties = expiringWarranties; }
    public List<RecentClaimItem> getRecentClaims() { return recentClaims; }
    public void setRecentClaims(List<RecentClaimItem> recentClaims) { this.recentClaims = recentClaims; }
    public List<RecentActivityItem> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivityItem> recentActivity) { this.recentActivity = recentActivity; }
}
