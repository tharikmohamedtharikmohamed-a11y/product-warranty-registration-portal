package com.warrantyportal.service;

import com.warrantyportal.dto.ProductRequest;
import com.warrantyportal.dto.ProductResponse;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.DuplicateSerialNumberException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.WarrantyRepository;
import com.warrantyportal.entity.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service orchestrating customer product registrations, warranty synchronizations, and lifecycle queries.
 * Enforces strict per-customer data isolation and serial number uniqueness.
 * Phase 7 — Product Management
 * Phase 12 — Dashboard & Notifications
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final WarrantyRepository warrantyRepository;
    private final NotificationService notificationService;

    public ProductService(ProductRepository productRepository, WarrantyRepository warrantyRepository) {
        this(productRepository, warrantyRepository, null);
    }

    @Autowired
    public ProductService(ProductRepository productRepository, WarrantyRepository warrantyRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.warrantyRepository = warrantyRepository;
        this.notificationService = notificationService;
    }

    /**
     * Registers a new product for the authenticated user and automatically provisions a 1-to-1 warranty.
     */
    public ProductResponse createProduct(ProductRequest request, User currentUser) {
        String cleanSerialNumber = request.getSerialNumber().trim();

        if (productRepository.existsBySerialNumberIgnoreCaseAndUserId(cleanSerialNumber, currentUser.getId())) {
            throw new DuplicateSerialNumberException(
                    "A product with serial number '" + cleanSerialNumber + "' is already registered in your account."
            );
        }

        Product product = new Product();
        product.setUser(currentUser);
        product.setProductName(request.getProductName().trim());
        product.setCategory(request.getCategory().trim());
        product.setBrand(request.getBrand().trim());
        product.setModelNumber(request.getModelNumber().trim());
        product.setSerialNumber(cleanSerialNumber);
        product.setPurchaseDate(request.getPurchaseDate());
        product.setSellerName(request.getSellerName().trim());
        product.setPrice(request.getPrice());
        product.setWarrantyDurationMonths(request.getWarrantyDurationMonths());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        LocalDate startDate = request.getPurchaseDate();
        LocalDate expiryDate = startDate.plusMonths(request.getWarrantyDurationMonths());
        WarrantyStatus status = calculateWarrantyStatus(expiryDate);

        Warranty warranty = new Warranty(product, startDate, expiryDate, status);
        product.setWarranty(warranty);

        Product savedProduct = productRepository.save(product);

        // Phase 12: Contextual Notifications
        if (notificationService != null) {
            notificationService.createNotification(
                    currentUser,
                    "Product Registered",
                    "Your product '" + savedProduct.getProductName() + "' (" + savedProduct.getBrand() + ") has been successfully registered.",
                    NotificationType.PRODUCT
            );
            notificationService.notifyAdmins(
                    "New Product Registered",
                    "Customer " + currentUser.getName() + " registered product '" + savedProduct.getProductName() + "' (" + savedProduct.getBrand() + ").",
                    NotificationType.PRODUCT
            );
        }

        return ProductResponse.fromProduct(savedProduct);
    }

    /**
     * Retrieves all registered products belonging strictly to the specified user.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsForUser(UUID userId) {
        return productRepository.findAllByUserIdWithWarrantyOrderByCreatedAtDesc(userId)
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single product by ID scoped strictly to the customer.
     * Returns 404 if the product does not exist or belongs to another customer.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductByIdForUser(UUID productId, UUID userId) {
        Product product = productRepository.findByIdAndUserIdWithWarranty(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return ProductResponse.fromProduct(product);
    }

    /**
     * Updates an existing customer product and recalculates the warranty expiry if dates or duration change.
     */
    public ProductResponse updateProduct(UUID productId, ProductRequest request, UUID userId) {
        Product product = productRepository.findByIdAndUserIdWithWarranty(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String cleanSerialNumber = request.getSerialNumber().trim();
        if (productRepository.existsBySerialNumberIgnoreCaseAndUserIdAndIdNot(cleanSerialNumber, userId, productId)) {
            throw new DuplicateSerialNumberException(
                    "Another product with serial number '" + cleanSerialNumber + "' is already registered in your account."
            );
        }

        product.setProductName(request.getProductName().trim());
        product.setCategory(request.getCategory().trim());
        product.setBrand(request.getBrand().trim());
        product.setModelNumber(request.getModelNumber().trim());
        product.setSerialNumber(cleanSerialNumber);
        product.setSellerName(request.getSellerName().trim());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        boolean datesChanged = !product.getPurchaseDate().isEqual(request.getPurchaseDate())
                || !product.getWarrantyDurationMonths().equals(request.getWarrantyDurationMonths());

        product.setPurchaseDate(request.getPurchaseDate());
        product.setWarrantyDurationMonths(request.getWarrantyDurationMonths());

        if (datesChanged) {
            Warranty warranty = product.getWarranty();
            LocalDate newStartDate = request.getPurchaseDate();
            LocalDate newExpiryDate = newStartDate.plusMonths(request.getWarrantyDurationMonths());
            WarrantyStatus newStatus = calculateWarrantyStatus(newExpiryDate);

            if (warranty == null) {
                warranty = new Warranty(product, newStartDate, newExpiryDate, newStatus);
                product.setWarranty(warranty);
            } else {
                warranty.setStartDate(newStartDate);
                warranty.setExpiryDate(newExpiryDate);
                warranty.setStatus(newStatus);
            }
        }

        Product updatedProduct = productRepository.save(product);
        return ProductResponse.fromProduct(updatedProduct);
    }

    /**
     * Deletes a customer's product and its cascaded warranty.
     */
    public void deleteProduct(UUID productId, UUID userId) {
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        productRepository.delete(product);
    }

    /**
     * Determines the warranty status given its expiration date relative to the current calendar date.
     */
    private WarrantyStatus calculateWarrantyStatus(LocalDate expiryDate) {
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return WarrantyStatus.EXPIRED;
        } else if (expiryDate.isBefore(today.plusDays(30)) || expiryDate.isEqual(today.plusDays(30))) {
            return WarrantyStatus.EXPIRING_SOON;
        } else {
            return WarrantyStatus.ACTIVE;
        }
    }
}
