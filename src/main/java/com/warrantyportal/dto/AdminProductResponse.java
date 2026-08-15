package com.warrantyportal.dto;

import com.warrantyportal.entity.Product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdminProductResponse {

    private UUID productId;
    private String productName;
    private String category;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private String sellerName;
    private Double price;
    private UUID userId;
    private String userName;
    private LocalDate purchaseDate;
    private LocalDateTime createdAt;

    public AdminProductResponse() {
    }

    public AdminProductResponse(UUID productId, String productName, String category, String brand,
                                String modelNumber, String serialNumber, String sellerName, Double price,
                                UUID userId, String userName, LocalDate purchaseDate, LocalDateTime createdAt) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.brand = brand;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.sellerName = sellerName;
        this.price = price;
        this.userId = userId;
        this.userName = userName;
        this.purchaseDate = purchaseDate;
        this.createdAt = createdAt;
    }

    public static AdminProductResponse fromEntity(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCategory(),
                product.getBrand(),
                product.getModelNumber(),
                product.getSerialNumber(),
                product.getSellerName(),
                product.getPrice(),
                product.getUser() != null ? product.getUser().getId() : null,
                product.getUser() != null ? product.getUser().getName() : null,
                product.getPurchaseDate(),
                product.getCreatedAt()
        );
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
