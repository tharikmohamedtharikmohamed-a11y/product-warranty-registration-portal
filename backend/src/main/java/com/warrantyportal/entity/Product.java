package com.warrantyportal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA Entity mapping to the 'products' table.
 * Represents customer-registered products with purchase and warranty duration metadata.
 * Phase 7 — Product Management
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model_number", nullable = false, length = 100)
    private String modelNumber;

    @Column(name = "serial_number", nullable = false, length = 150)
    private String serialNumber;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "warranty_duration_months", nullable = false)
    private Integer warrantyDurationMonths;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Warranty warranty;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Product() {
    }

    public Product(User user, String productName, String category, String brand,
                   String modelNumber, String serialNumber, LocalDate purchaseDate,
                   String sellerName, BigDecimal price, Integer warrantyDurationMonths,
                   String description) {
        this.user = user;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Warranty getWarranty() {
        return warranty;
    }

    public void setWarranty(Warranty warranty) {
        this.warranty = warranty;
        if (warranty != null) {
            warranty.setProduct(this);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
