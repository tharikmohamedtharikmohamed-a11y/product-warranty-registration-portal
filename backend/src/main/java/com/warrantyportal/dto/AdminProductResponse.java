package com.warrantyportal.dto;

import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.WarrantyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Administrative Data Transfer Object presenting product details
 * alongside owner identification for platform-wide auditing.
 * Phase 11 — Admin Management Module
 */
public class AdminProductResponse {

    private UUID id;
    private String productName;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private LocalDate purchaseDate;
    private BigDecimal price;
    private Integer warrantyDurationMonths;
    private String description;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private WarrantyStatus warrantyStatus;
    private OffsetDateTime createdAt;

    public AdminProductResponse() {
    }

    public AdminProductResponse(UUID id, String productName, String brand, String modelNumber,
                                String serialNumber, LocalDate purchaseDate, BigDecimal price,
                                Integer warrantyDurationMonths, String description, UUID userId,
                                String customerName, String customerEmail, WarrantyStatus warrantyStatus,
                                OffsetDateTime createdAt) {
        this.id = id;
        this.productName = productName;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.warrantyDurationMonths = warrantyDurationMonths;
        this.description = description;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.warrantyStatus = warrantyStatus;
        this.createdAt = createdAt;
    }

    public static AdminProductResponse fromProduct(Product product) {
        if (product == null) {
            return null;
        }

        UUID uId = null;
        String uName = null;
        String uEmail = null;
        if (product.getUser() != null) {
            uId = product.getUser().getId();
            uName = product.getUser().getName();
            uEmail = product.getUser().getEmail();
        }

        WarrantyStatus wStatus = null;
        if (product.getWarranty() != null) {
            wStatus = product.getWarranty().getStatus();
        }

        return new AdminProductResponse(
                product.getId(),
                product.getProductName(),
                product.getBrand(),
                product.getModelNumber(),
                product.getSerialNumber(),
                product.getPurchaseDate(),
                product.getPrice(),
                product.getWarrantyDurationMonths(),
                product.getDescription(),
                uId,
                uName,
                uEmail,
                wStatus,
                product.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getWarrantyDurationMonths() {
        return warrantyDurationMonths;
    }

    public void setWarrantyDurationMonths(Integer warrantyDurationMonths) {
        this.warrantyDurationMonths = warrantyDurationMonths;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public WarrantyStatus getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(WarrantyStatus warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
