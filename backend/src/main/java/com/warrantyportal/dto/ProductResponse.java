package com.warrantyportal.dto;

import com.warrantyportal.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe DTO response payload representing a registered product and its associated warranty.
 * Phase 7 — Product Management
 */
public class ProductResponse {

    private UUID id;
    private UUID userId;
    private String productName;
    private String category;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private LocalDate purchaseDate;
    private String sellerName;
    private BigDecimal price;
    private Integer warrantyDurationMonths;
    private String description;
    private WarrantyResponse warranty;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ProductResponse() {
    }

    public ProductResponse(UUID id, UUID userId, String productName, String category,
                           String brand, String modelNumber, String serialNumber,
                           LocalDate purchaseDate, String sellerName, BigDecimal price,
                           Integer warrantyDurationMonths, String description,
                           WarrantyResponse warranty, OffsetDateTime createdAt,
                           OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.productName = productName;
        this.category = category;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate;
        this.sellerName = sellerName;
        this.price = price;
        this.warrantyDurationMonths = warrantyDurationMonths;
        this.description = description;
        this.warranty = warranty;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductResponse fromProduct(Product product) {
        if (product == null) {
            return null;
        }

        WarrantyResponse warrantyResponse = null;
        if (product.getWarranty() != null) {
            warrantyResponse = WarrantyResponse.fromWarranty(product.getWarranty());
        }

        UUID uId = product.getUser() != null ? product.getUser().getId() : null;

        return new ProductResponse(
                product.getId(),
                uId,
                product.getProductName(),
                product.getCategory(),
                product.getBrand(),
                product.getModelNumber(),
                product.getSerialNumber(),
                product.getPurchaseDate(),
                product.getSellerName(),
                product.getPrice(),
                product.getWarrantyDurationMonths(),
                product.getDescription(),
                warrantyResponse,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
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

    public WarrantyResponse getWarranty() {
        return warranty;
    }

    public void setWarranty(WarrantyResponse warranty) {
        this.warranty = warranty;
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
