package com.warrantyportal.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload DTO for creating and updating product registrations.
 * Phase 7 — Product Management
 */
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String productName;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @NotBlank(message = "Model number is required")
    @Size(max = 100, message = "Model number cannot exceed 100 characters")
    private String modelNumber;

    @NotBlank(message = "Serial number is required")
    @Size(max = 150, message = "Serial number cannot exceed 150 characters")
    private String serialNumber;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    @NotBlank(message = "Seller name is required")
    @Size(max = 255, message = "Seller name cannot exceed 255 characters")
    private String sellerName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Warranty duration is required")
    @Min(value = 1, message = "Warranty duration must be at least 1 month")
    private Integer warrantyDurationMonths;

    private String description;

    public ProductRequest() {
    }

    public ProductRequest(String productName, String category, String brand, String modelNumber,
                          String serialNumber, LocalDate purchaseDate, String sellerName,
                          BigDecimal price, Integer warrantyDurationMonths, String description) {
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
}
