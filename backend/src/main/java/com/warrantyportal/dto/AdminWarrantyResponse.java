package com.warrantyportal.dto;

import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Administrative Data Transfer Object presenting warranty specifications,
 * calculated countdowns, progress metrics, and associated customer identity.
 * Phase 11 — Admin Management Module
 */
public class AdminWarrantyResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String brand;
    private String modelNumber;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private Integer warrantyDurationMonths;
    private WarrantyStatus status;
    private long daysRemaining;
    private int progressPercentage;
    private OffsetDateTime createdAt;

    public AdminWarrantyResponse() {
    }

    public AdminWarrantyResponse(UUID id, UUID productId, String productName, String brand,
                                 String modelNumber, UUID userId, String customerName, String customerEmail,
                                 LocalDate startDate, LocalDate expiryDate, Integer warrantyDurationMonths,
                                 WarrantyStatus status, long daysRemaining, int progressPercentage,
                                 OffsetDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.warrantyDurationMonths = warrantyDurationMonths;
        this.status = status;
        this.daysRemaining = daysRemaining;
        this.progressPercentage = progressPercentage;
        this.createdAt = createdAt;
    }

    public static AdminWarrantyResponse fromWarranty(Warranty warranty, long daysRemaining, int progressPercentage) {
        if (warranty == null) {
            return null;
        }

        UUID pId = null;
        String pName = null;
        String pBrand = null;
        String pModel = null;
        Integer duration = null;
        UUID uId = null;
        String uName = null;
        String uEmail = null;

        if (warranty.getProduct() != null) {
            pId = warranty.getProduct().getId();
            pName = warranty.getProduct().getProductName();
            pBrand = warranty.getProduct().getBrand();
            pModel = warranty.getProduct().getModelNumber();
            duration = warranty.getProduct().getWarrantyDurationMonths();

            if (warranty.getProduct().getUser() != null) {
                uId = warranty.getProduct().getUser().getId();
                uName = warranty.getProduct().getUser().getName();
                uEmail = warranty.getProduct().getUser().getEmail();
            }
        }

        return new AdminWarrantyResponse(
                warranty.getId(),
                pId,
                pName,
                pBrand,
                pModel,
                uId,
                uName,
                uEmail,
                warranty.getStartDate(),
                warranty.getExpiryDate(),
                duration,
                warranty.getStatus(),
                daysRemaining,
                progressPercentage,
                warranty.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getWarrantyDurationMonths() {
        return warrantyDurationMonths;
    }

    public void setWarrantyDurationMonths(Integer warrantyDurationMonths) {
        this.warrantyDurationMonths = warrantyDurationMonths;
    }

    public WarrantyStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyStatus status) {
        this.status = status;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
