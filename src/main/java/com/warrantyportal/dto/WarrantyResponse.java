package com.warrantyportal.dto;

import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.enums.WarrantyStatus;

import java.time.LocalDate;
import java.util.UUID;

public class WarrantyResponse {

    private UUID warrantyId;
    private UUID productId;
    private String productName;
    private String brand;
    private LocalDate purchaseDate;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private Integer warrantyDurationMonths;
    private WarrantyStatus status;

    public WarrantyResponse() {
    }

    public WarrantyResponse(UUID warrantyId, UUID productId, String productName, String brand, LocalDate purchaseDate, LocalDate warrantyStartDate, LocalDate warrantyEndDate, Integer warrantyDurationMonths, WarrantyStatus status) {
        this.warrantyId = warrantyId;
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.purchaseDate = purchaseDate;
        this.warrantyStartDate = warrantyStartDate;
        this.warrantyEndDate = warrantyEndDate;
        this.warrantyDurationMonths = warrantyDurationMonths;
        this.status = status;
    }

    public static WarrantyResponse fromEntity(Warranty warranty, WarrantyStatus computedStatus) {
        return new WarrantyResponse(
                warranty.getId(),
                warranty.getProduct() != null ? warranty.getProduct().getId() : null,
                warranty.getProduct() != null ? warranty.getProduct().getProductName() : null,
                warranty.getProduct() != null ? warranty.getProduct().getBrand() : null,
                warranty.getProduct() != null ? warranty.getProduct().getPurchaseDate() : null,
                warranty.getWarrantyStartDate(),
                warranty.getWarrantyEndDate(),
                warranty.getWarrantyPeriodMonths(),
                computedStatus != null ? computedStatus : warranty.getStatus()
        );
    }

    public UUID getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(UUID warrantyId) {
        this.warrantyId = warrantyId;
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

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getWarrantyStartDate() {
        return warrantyStartDate;
    }

    public void setWarrantyStartDate(LocalDate warrantyStartDate) {
        this.warrantyStartDate = warrantyStartDate;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
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
}
