package com.warrantyportal.service;

import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyStatus;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.WarrantyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing warranty tracking, lifecycle status evaluations, days remaining,
 * elapsed progress calculations, and automatic status persistence synchronization.
 * Phase 8 — Warranty Management
 */
@Service
@Transactional
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public WarrantyService(WarrantyRepository warrantyRepository) {
        this(warrantyRepository, Clock.systemDefaultZone());
    }

    public WarrantyService(WarrantyRepository warrantyRepository, Clock clock) {
        this.warrantyRepository = warrantyRepository;
        this.clock = clock;
    }

    /**
     * Retrieves all warranties belonging to the authenticated customer, updating persisted status if needed.
     */
    public List<WarrantyResponse> getWarrantiesForUser(UUID userId) {
        LocalDate today = LocalDate.now(clock);
        List<Warranty> warranties = warrantyRepository.findAllByProductUserId(userId);

        return warranties.stream()
                .map(warranty -> syncAndMap(warranty, today))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single warranty by ID scoped strictly to the customer.
     */
    public WarrantyResponse getWarrantyByIdForUser(UUID warrantyId, UUID userId) {
        LocalDate today = LocalDate.now(clock);
        Warranty warranty = warrantyRepository.findByIdAndProductUserId(warrantyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty not found with id: " + warrantyId));

        return syncAndMap(warranty, today);
    }

    /**
     * Retrieves a product's warranty scoped strictly to the customer.
     */
    public WarrantyResponse getWarrantyByProductIdForUser(UUID productId, UUID userId) {
        LocalDate today = LocalDate.now(clock);
        Warranty warranty = warrantyRepository.findByProductIdAndProductUserId(productId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty not found for product id: " + productId));

        return syncAndMap(warranty, today);
    }

    /**
     * Synchronizes persisted status if expired/expiring date thresholds have shifted over time,
     * and constructs the enriched response DTO.
     */
    private WarrantyResponse syncAndMap(Warranty warranty, LocalDate today) {
        WarrantyStatus calculatedStatus = calculateStatus(warranty.getExpiryDate(), today);
        if (warranty.getStatus() != calculatedStatus) {
            warranty.setStatus(calculatedStatus);
            warranty = warrantyRepository.save(warranty);
        }
        return WarrantyResponse.fromWarranty(warranty, today);
    }

    /**
     * Calculates warranty status based on expiry date and evaluation date.
     * ACTIVE: > 30 days remaining
     * EXPIRING_SOON: <= 30 days remaining and not expired
     * EXPIRED: today is after expiry date
     */
    public WarrantyStatus calculateStatus(LocalDate expiryDate, LocalDate today) {
        if (expiryDate == null) {
            return WarrantyStatus.EXPIRED;
        }
        if (today.isAfter(expiryDate)) {
            return WarrantyStatus.EXPIRED;
        }
        long days = WarrantyResponse.calculateDaysRemaining(expiryDate, today);
        if (days <= 30) {
            return WarrantyStatus.EXPIRING_SOON;
        }
        return WarrantyStatus.ACTIVE;
    }

    /**
     * Calculates remaining days (never negative, 0 on expired).
     */
    public long calculateDaysRemaining(LocalDate expiryDate, LocalDate today) {
        return WarrantyResponse.calculateDaysRemaining(expiryDate, today);
    }

    /**
     * Calculates elapsed duration percentage (0 to 100).
     */
    public int calculateProgressPercentage(LocalDate startDate, LocalDate expiryDate, LocalDate today) {
        return WarrantyResponse.calculateProgressPercentage(startDate, expiryDate, today);
    }
}
