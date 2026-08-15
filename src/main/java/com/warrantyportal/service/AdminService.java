package com.warrantyportal.service;

import com.warrantyportal.dto.*;
import com.warrantyportal.entity.Invoice;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyClaim;
import com.warrantyportal.entity.enums.ClaimStatus;
import com.warrantyportal.entity.enums.WarrantyStatus;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarrantyRepository warrantyRepository;
    private final InvoiceRepository invoiceRepository;
    private final WarrantyClaimRepository claimRepository;
    private final SupabaseStorageService storageService;
    private final WarrantyService warrantyService;

    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        WarrantyRepository warrantyRepository,
                        InvoiceRepository invoiceRepository,
                        WarrantyClaimRepository claimRepository,
                        SupabaseStorageService storageService,
                        WarrantyService warrantyService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warrantyRepository = warrantyRepository;
        this.invoiceRepository = invoiceRepository;
        this.claimRepository = claimRepository;
        this.storageService = storageService;
        this.warrantyService = warrantyService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return AdminUserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(AdminProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminWarrantyResponse> getAllWarranties() {
        return warrantyRepository.findAll().stream()
                .map(warranty -> {
                    WarrantyStatus computedStatus = warrantyService.calculateWarrantyStatus(warranty.getWarrantyEndDate());
                    return AdminWarrantyResponse.fromEntity(warranty, computedStatus);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminClaimResponse> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(AdminClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminClaimResponse getClaimById(UUID claimId) {
        WarrantyClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found with ID: " + claimId));
        return AdminClaimResponse.fromEntity(claim);
    }

    @Transactional(readOnly = true)
    public List<AdminInvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(AdminInvoiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceDownloadResponse getInvoiceDownload(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));

        int expiresInSeconds = 3600;
        String signedUrl = storageService.createSignedUrl(invoice.getStoragePath(), expiresInSeconds);

        return new InvoiceDownloadResponse(
                invoice.getId(),
                invoice.getFileName(),
                invoice.getFileType(),
                signedUrl,
                expiresInSeconds
        );
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalWarranties = warrantyRepository.count();

        LocalDate today = LocalDate.now();
        long expiredWarranties = warrantyRepository.countByWarrantyEndDateBefore(today);
        long activeWarranties = totalWarranties - expiredWarranties;

        long totalInvoices = invoiceRepository.count();
        long totalClaims = claimRepository.count();

        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long inProgressClaims = claimRepository.countByStatus(ClaimStatus.IN_PROGRESS);
        long completedClaims = claimRepository.countByStatus(ClaimStatus.COMPLETED);
        long cancelledClaims = claimRepository.countByStatus(ClaimStatus.CANCELLED);

        return new DashboardStatsResponse(
                totalUsers,
                totalProducts,
                totalWarranties,
                activeWarranties,
                expiredWarranties,
                totalInvoices,
                totalClaims,
                pendingClaims,
                approvedClaims,
                rejectedClaims,
                inProgressClaims,
                completedClaims,
                cancelledClaims
        );
    }
}
