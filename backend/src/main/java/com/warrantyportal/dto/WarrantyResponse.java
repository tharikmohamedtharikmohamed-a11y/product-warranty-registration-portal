package com.warrantyportal.dto;

import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Safe DTO representation of product warranty status, validity duration, and elapsed progress.
 * Phase 8 — Warranty Management
 */
public class WarrantyResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String brand;
    private String modelNumber;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private Integer warrantyDurationMonths;
    private WarrantyStatus status;
    private long daysRemaining;
    private int progressPercentage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public WarrantyResponse() {
    }

    /**
     * Backward-compatible 8-argument constructor.
     */
    public WarrantyResponse(UUID id, UUID productId, LocalDate startDate, LocalDate expiryDate,
                            WarrantyStatus status, long daysRemaining,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this(id, productId, null, null, null, startDate, expiryDate, null, status, daysRemaining, 0, createdAt, updatedAt);
    }

    /**
     * Full 13-argument constructor for Phase 8.
     */
    public WarrantyResponse(UUID id, UUID productId, String productName, String brand,
                            String modelNumber, LocalDate startDate, LocalDate expiryDate,
                            Integer warrantyDurationMonths, WarrantyStatus status,
                            long daysRemaining, int progressPercentage,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.warrantyDurationMonths = warrantyDurationMonths;
        this.status = status;
        this.daysRemaining = daysRemaining;
        this.progressPercentage = progressPercentage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WarrantyResponse fromWarranty(Warranty warranty) {
        return fromWarranty(warranty, LocalDate.now());
    }

    public static WarrantyResponse fromWarranty(Warranty warranty, LocalDate today) {
        if (warranty == null) {
            return null;
        }

        LocalDate effectiveToday = today != null ? today : LocalDate.now();

        long days = calculateDaysRemaining(warranty.getExpiryDate(), effectiveToday);
        int progress = calculateProgressPercentage(warranty.getStartDate(), warranty.getExpiryDate(), effectiveToday);

        Product product = warranty.getProduct();
        UUID pId = product != null ? product.getId() : null;
        String pName = product != null ? product.getProductName() : null;
        String pBrand = product != null ? product.getBrand() : null;
        String pModel = product != null ? product.getModelNumber() : null;
        Integer duration = product != null ? product.getWarrantyDurationMonths() : null;

        return new WarrantyResponse(
                warranty.getId(),
                pId,
                pName,
                pBrand,
                pModel,
                warranty.getStartDate(),
                warranty.getExpiryDate(),
                duration,
                warranty.getStatus(),
                days,
                progress,
                warranty.getCreatedAt(),
                warranty.getUpdatedAt()
        );
    }

    public static long calculateDaysRemaining(LocalDate expiryDate, LocalDate today) {
        if (expiryDate == null || today == null) {
            return 0;
        }
        if (today.isAfter(expiryDate)) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(today, expiryDate);
        return Math.max(0, days);
    }

    public static int calculateProgressPercentage(LocalDate startDate, LocalDate expiryDate, LocalDate today) {
        if (startDate == null || expiryDate == null || today == null) {
            return 0;
        }
        if (today.isAfter(expiryDate) || today.isEqual(expiryDate)) {
            return 100;
        }
        if (today.isBefore(startDate) || today.isEqual(startDate)) {
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(startDate, expiryDate);
        if (totalDays <= 0) {
            return 100;
        }
        long elapsedDays = ChronoUnit.DAYS.between(startDate, today);
        int percentage = (int) Math.round(((double) elapsedDays / totalDays) * 100);
        return Math.min(100, Math.max(0, percentage));
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
