package com.warrantyportal.dto;

import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.enums.WarrantyStatus;

import java.time.LocalDate;
import java.util.UUID;

public class AdminWarrantyResponse {

    private UUID warrantyId;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String userName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationMonths;
    private WarrantyStatus warrantyStatus;

    public AdminWarrantyResponse() {
    }

    public AdminWarrantyResponse(UUID warrantyId, UUID productId, String productName, UUID userId,
                                 String userName, LocalDate startDate, LocalDate endDate,
                                 Integer durationMonths, WarrantyStatus warrantyStatus) {
        this.warrantyId = warrantyId;
        this.productId = productId;
        this.productName = productName;
        this.userId = userId;
        this.userName = userName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMonths = durationMonths;
        this.warrantyStatus = warrantyStatus;
    }

    public static AdminWarrantyResponse fromEntity(Warranty warranty, WarrantyStatus computedStatus) {
        return new AdminWarrantyResponse(
                warranty.getId(),
                warranty.getProduct() != null ? warranty.getProduct().getId() : null,
                warranty.getProduct() != null ? warranty.getProduct().getProductName() : null,
                warranty.getUser() != null ? warranty.getUser().getId() : null,
                warranty.getUser() != null ? warranty.getUser().getName() : null,
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public WarrantyStatus getWarrantyStatus() {
        return warrantyStatus;
    }

    public void setWarrantyStatus(WarrantyStatus warrantyStatus) {
        this.warrantyStatus = warrantyStatus;
    }
}
