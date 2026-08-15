package com.warrantyportal.dto;

public class DashboardStatsResponse {

    private long totalUsers;
    private long totalProducts;
    private long totalWarranties;
    private long activeWarranties;
    private long expiredWarranties;
    private long totalInvoices;
    private long totalClaims;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long inProgressClaims;
    private long completedClaims;
    private long cancelledClaims;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalUsers, long totalProducts, long totalWarranties, long activeWarranties, long expiredWarranties, long totalInvoices, long totalClaims, long pendingClaims, long approvedClaims, long rejectedClaims, long inProgressClaims, long completedClaims, long cancelledClaims) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.totalWarranties = totalWarranties;
        this.activeWarranties = activeWarranties;
        this.expiredWarranties = expiredWarranties;
        this.totalInvoices = totalInvoices;
        this.totalClaims = totalClaims;
        this.pendingClaims = pendingClaims;
        this.approvedClaims = approvedClaims;
        this.rejectedClaims = rejectedClaims;
        this.inProgressClaims = inProgressClaims;
        this.completedClaims = completedClaims;
        this.cancelledClaims = cancelledClaims;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalWarranties() {
        return totalWarranties;
    }

    public void setTotalWarranties(long totalWarranties) {
        this.totalWarranties = totalWarranties;
    }

    public long getActiveWarranties() {
        return activeWarranties;
    }

    public void setActiveWarranties(long activeWarranties) {
        this.activeWarranties = activeWarranties;
    }

    public long getExpiredWarranties() {
        return expiredWarranties;
    }

    public void setExpiredWarranties(long expiredWarranties) {
        this.expiredWarranties = expiredWarranties;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public long getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(long totalClaims) {
        this.totalClaims = totalClaims;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public long getApprovedClaims() {
        return approvedClaims;
    }

    public void setApprovedClaims(long approvedClaims) {
        this.approvedClaims = approvedClaims;
    }

    public long getRejectedClaims() {
        return rejectedClaims;
    }

    public void setRejectedClaims(long rejectedClaims) {
        this.rejectedClaims = rejectedClaims;
    }

    public long getInProgressClaims() {
        return inProgressClaims;
    }

    public void setInProgressClaims(long inProgressClaims) {
        this.inProgressClaims = inProgressClaims;
    }

    public long getCompletedClaims() {
        return completedClaims;
    }

    public void setCompletedClaims(long completedClaims) {
        this.completedClaims = completedClaims;
    }

    public long getCancelledClaims() {
        return cancelledClaims;
    }

    public void setCancelledClaims(long cancelledClaims) {
        this.cancelledClaims = cancelledClaims;
    }
}
