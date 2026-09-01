package com.warrantyportal.dto;

/**
 * Data Transfer Object representing platform-wide administrative KPI metrics.
 * Calculated strictly from authoritative database records.
 * Phase 11 — Admin Management Module
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

    public AdminDashboardStatsResponse() {
    }

    public AdminDashboardStatsResponse(long users, long customers, long admins, long products,
                                       long warranties, long activeWarranties, long expiringSoonWarranties, long expiredWarranties,
                                       long invoices, long claims, long pendingClaims, long approvedClaims,
                                       long rejectedClaims, long inProgressClaims, long completedClaims, long cancelledClaims) {
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
    }

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
}
