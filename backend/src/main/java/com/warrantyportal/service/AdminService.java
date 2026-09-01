package com.warrantyportal.service;

import com.warrantyportal.dto.*;
import com.warrantyportal.entity.*;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementing administrative business logic.
 * Aggregates platform-wide KPIs, lists global entities, and manages warranty claim adjudication.
 * Phase 11 — Admin Management Module
 */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarrantyRepository warrantyRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClaimRepository claimRepository;
    private final WarrantyService warrantyService;
    private final Clock clock;

    @Autowired
    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        WarrantyRepository warrantyRepository,
                        InvoiceRepository invoiceRepository,
                        ClaimRepository claimRepository,
                        WarrantyService warrantyService) {
        this(userRepository, productRepository, warrantyRepository, invoiceRepository, claimRepository, warrantyService, Clock.systemDefaultZone());
    }

    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        WarrantyRepository warrantyRepository,
                        InvoiceRepository invoiceRepository,
                        ClaimRepository claimRepository,
                        WarrantyService warrantyService,
                        Clock clock) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warrantyRepository = warrantyRepository;
        this.invoiceRepository = invoiceRepository;
        this.claimRepository = claimRepository;
        this.warrantyService = warrantyService;
        this.clock = clock;
    }

    /**
     * Compute authoritative platform-wide KPI statistics strictly from database counts.
     */
    public AdminDashboardStatsResponse getDashboardStats() {
        long users = userRepository.count();
        long customers = userRepository.countByRole(Role.CUSTOMER);
        long admins = userRepository.countByRole(Role.ADMIN);

        long products = productRepository.count();

        long warranties = warrantyRepository.count();
        long activeWarranties = warrantyRepository.countByStatus(WarrantyStatus.ACTIVE);
        long expiringSoonWarranties = warrantyRepository.countByStatus(WarrantyStatus.EXPIRING_SOON);
        long expiredWarranties = warrantyRepository.countByStatus(WarrantyStatus.EXPIRED);

        long invoices = invoiceRepository.count();

        long claims = claimRepository.count();
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long inProgressClaims = claimRepository.countByStatus(ClaimStatus.IN_PROGRESS);
        long completedClaims = claimRepository.countByStatus(ClaimStatus.COMPLETED);
        long cancelledClaims = claimRepository.countByStatus(ClaimStatus.CANCELLED);

        return new AdminDashboardStatsResponse(
                users, customers, admins, products,
                warranties, activeWarranties, expiringSoonWarranties, expiredWarranties,
                invoices, claims, pendingClaims, approvedClaims, rejectedClaims, inProgressClaims, completedClaims, cancelledClaims
        );
    }

    /**
     * Retrieve all users or search by name or email.
     */
    public List<AdminUserResponse> getUsers(String search) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim();
            users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(query, query);
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc();
        }
        return users.stream()
                .map(AdminUserResponse::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all registered products across all customers.
     */
    public List<AdminProductResponse> getProducts() {
        return productRepository.findAllWithUserAndWarrantyOrderByCreatedAtDesc().stream()
                .map(AdminProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all warranties across all customers with real-time lifecycle calculations.
     */
    public List<AdminWarrantyResponse> getWarranties() {
        LocalDate today = LocalDate.now(clock);
        return warrantyRepository.findAllWithProductAndUser().stream()
                .map(w -> {
                    long days = warrantyService.calculateDaysRemaining(w.getExpiryDate(), today);
                    int progress = warrantyService.calculateProgressPercentage(w.getStartDate(), w.getExpiryDate(), today);
                    return AdminWarrantyResponse.fromWarranty(w, days, progress);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all purchase invoice records across all customers.
     */
    public List<AdminInvoiceResponse> getInvoices() {
        return invoiceRepository.findAllWithProductAndUser().stream()
                .map(AdminInvoiceResponse::fromInvoice)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all warranty claims across all customers.
     */
    public List<AdminClaimResponse> getClaims() {
        return claimRepository.findAllWithProductAndUser().stream()
                .map(AdminClaimResponse::fromClaim)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve single claim details by ID with complete customer and product metadata.
     */
    public AdminClaimResponse getClaimById(UUID id) {
        Claim claim = claimRepository.findByIdWithProductAndUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found with ID: " + id));
        return AdminClaimResponse.fromClaim(claim);
    }

    /**
     * Approve a PENDING claim.
     */
    @Transactional
    public AdminClaimResponse approveClaim(UUID id, AdminClaimDecisionRequest request) {
        Claim claim = findClaimOrThrow(id);
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new InvalidClaimException("Only PENDING claims can be approved. Current status: " + claim.getStatus());
        }
        claim.setStatus(ClaimStatus.APPROVED);
        updateClaimAdminNotes(claim, request != null ? request.getAdminNotes() : null);
        Claim saved = claimRepository.save(claim);
        return AdminClaimResponse.fromClaim(saved);
    }

    /**
     * Reject a PENDING claim. Reason is required.
     */
    @Transactional
    public AdminClaimResponse rejectClaim(UUID id, AdminClaimDecisionRequest request) {
        if (request == null || request.getAdminNotes() == null || request.getAdminNotes().trim().isEmpty()) {
            throw new InvalidClaimException("Rejection reason is required in administrative notes.");
        }
        Claim claim = findClaimOrThrow(id);
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new InvalidClaimException("Only PENDING claims can be rejected. Current status: " + claim.getStatus());
        }
        claim.setStatus(ClaimStatus.REJECTED);
        updateClaimAdminNotes(claim, request.getAdminNotes());
        Claim saved = claimRepository.save(claim);
        return AdminClaimResponse.fromClaim(saved);
    }

    /**
     * Move an APPROVED claim to IN_PROGRESS (commence defect triage / repair dispatch).
     */
    @Transactional
    public AdminClaimResponse startClaim(UUID id, AdminClaimDecisionRequest request) {
        Claim claim = findClaimOrThrow(id);
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new InvalidClaimException("Only APPROVED claims can be moved to IN_PROGRESS. Current status: " + claim.getStatus());
        }
        claim.setStatus(ClaimStatus.IN_PROGRESS);
        updateClaimAdminNotes(claim, request != null ? request.getAdminNotes() : null);
        Claim saved = claimRepository.save(claim);
        return AdminClaimResponse.fromClaim(saved);
    }

    /**
     * Mark an IN_PROGRESS claim as COMPLETED (resolution finalized).
     */
    @Transactional
    public AdminClaimResponse completeClaim(UUID id, AdminClaimDecisionRequest request) {
        Claim claim = findClaimOrThrow(id);
        if (claim.getStatus() != ClaimStatus.IN_PROGRESS) {
            throw new InvalidClaimException("Only IN_PROGRESS claims can be completed. Current status: " + claim.getStatus());
        }
        claim.setStatus(ClaimStatus.COMPLETED);
        updateClaimAdminNotes(claim, request != null ? request.getAdminNotes() : null);
        Claim saved = claimRepository.save(claim);
        return AdminClaimResponse.fromClaim(saved);
    }

    private Claim findClaimOrThrow(UUID id) {
        return claimRepository.findByIdWithProductAndUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found with ID: " + id));
    }

    private void updateClaimAdminNotes(Claim claim, String newAdminNote) {
        if (newAdminNote == null || newAdminNote.trim().isEmpty()) {
            return;
        }
        String rawDesc = claim.getDescription();
        String customerDesc = rawDesc;
        if (rawDesc != null && rawDesc.contains(AdminClaimResponse.ADMIN_NOTE_DELIMITER)) {
            customerDesc = rawDesc.substring(0, rawDesc.indexOf(AdminClaimResponse.ADMIN_NOTE_DELIMITER));
        }
        String updated = (customerDesc != null ? customerDesc : "") + AdminClaimResponse.ADMIN_NOTE_DELIMITER + newAdminNote.trim();
        claim.setDescription(updated);
    }
}
