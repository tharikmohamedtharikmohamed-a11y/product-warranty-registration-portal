package com.warrantyportal.dto;

import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.enums.WarrantyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private String productName;
    private String category;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private LocalDate purchaseDate;
    private String sellerName;
    private Double price;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private WarrantyResponse warranty;

    public ProductResponse() {
    }

    public ProductResponse(UUID id, String productName, String category, String brand, String modelNumber, String serialNumber, LocalDate purchaseDate, String sellerName, Double price, String description, LocalDateTime createdAt, LocalDateTime updatedAt, WarrantyResponse warranty) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate;
        this.sellerName = sellerName;
        this.price = price;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.warranty = warranty;
    }

    public static ProductResponse fromEntity(Product product, WarrantyStatus computedWarrantyStatus) {
        WarrantyResponse warrantyResp = null;
        if (product.getWarranty() != null) {
            warrantyResp = WarrantyResponse.fromEntity(product.getWarranty(), computedWarrantyStatus);
        }
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCategory(),
                product.getBrand(),
                product.getModelNumber(),
                product.getSerialNumber(),
                product.getPurchaseDate(),
                product.getSellerName(),
                product.getPrice(),
                product.getDescription(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                warrantyResp
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public WarrantyResponse getWarranty() {
        return warranty;
    }

    public void setWarranty(WarrantyResponse warranty) {
        this.warranty = warranty;
    }
}
