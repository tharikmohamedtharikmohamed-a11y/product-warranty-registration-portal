package com.warrantyportal.service;

import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.enums.WarrantyStatus;
import com.warrantyportal.exception.ResourceForbiddenException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.WarrantyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final ProductRepository productRepository;

    public WarrantyService(WarrantyRepository warrantyRepository, ProductRepository productRepository) {
        this.warrantyRepository = warrantyRepository;
        this.productRepository = productRepository;
    }

    /**
     * Dynamically calculates current warranty status based on the current date:
     * - EXPIRED: Current date is after warranty end date.
     * - EXPIRING_SOON: 30 days or less remaining before end date.
     * - ACTIVE: More than 30 days remaining.
     */
    public WarrantyStatus calculateWarrantyStatus(LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (endDate == null || today.isAfter(endDate)) {
            return WarrantyStatus.EXPIRED;
        }
        long daysRemaining = ChronoUnit.DAYS.between(today, endDate);
        if (daysRemaining <= 30) {
            return WarrantyStatus.EXPIRING_SOON;
        }
        return WarrantyStatus.ACTIVE;
    }

    @Transactional
    public Warranty createWarrantyForProduct(Product product, User user, Integer warrantyDurationMonths) {
        LocalDate startDate = product.getPurchaseDate();
        LocalDate endDate = startDate.plusMonths(warrantyDurationMonths);
        WarrantyStatus computedStatus = calculateWarrantyStatus(endDate);

        Warranty warranty = new Warranty();
        warranty.setProduct(product);
        warranty.setUser(user);
        warranty.setWarrantyStartDate(startDate);
        warranty.setWarrantyEndDate(endDate);
        warranty.setWarrantyPeriodMonths(warrantyDurationMonths);
        warranty.setStatus(computedStatus);

        return warrantyRepository.save(warranty);
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> getUserWarranties(User currentUser) {
        List<Warranty> warranties = warrantyRepository.findByUserId(currentUser.getId());
        return warranties.stream()
                .map(w -> WarrantyResponse.fromEntity(w, calculateWarrantyStatus(w.getWarrantyEndDate())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WarrantyResponse getWarrantyById(UUID warrantyId, User currentUser) {
        Warranty warranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty not found with ID: " + warrantyId));

        if (!warranty.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this warranty");
        }

        return WarrantyResponse.fromEntity(warranty, calculateWarrantyStatus(warranty.getWarrantyEndDate()));
    }

    @Transactional(readOnly = true)
    public WarrantyResponse getWarrantyByProductId(UUID productId, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this product");
        }

        Warranty warranty = warrantyRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty not found for product ID: " + productId));

        return WarrantyResponse.fromEntity(warranty, calculateWarrantyStatus(warranty.getWarrantyEndDate()));
    }
}
