package com.warrantyportal.service;

import com.warrantyportal.dto.DashboardResponse;
import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.*;
import com.warrantyportal.repository.ClaimRepository;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service aggregating customer dashboard metrics, expiring warranty alerts,
 * recent claims, and real database-derived recent activity.
 * Phase 12 — Dashboard & Notifications
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

        private final ProductRepository productRepository;
        private final ClaimRepository claimRepository;
        private final InvoiceRepository invoiceRepository;
        private final WarrantyService warrantyService;
        private final Clock clock;

        @org.springframework.beans.factory.annotation.Autowired
        public DashboardService(ProductRepository productRepository,
                        ClaimRepository claimRepository,
                        InvoiceRepository invoiceRepository,
                        WarrantyService warrantyService) {
                this(productRepository, claimRepository, invoiceRepository, warrantyService, Clock.systemDefaultZone());
        }

        public DashboardService(ProductRepository productRepository,
                        ClaimRepository claimRepository,
                        InvoiceRepository invoiceRepository,
                        WarrantyService warrantyService,
                        Clock clock) {
                this.productRepository = productRepository;
                this.claimRepository = claimRepository;
                this.invoiceRepository = invoiceRepository;
                this.warrantyService = warrantyService;
                this.clock = clock;
        }

        /**
         * Aggregates customer-scoped dashboard telemetry.
         */
        public DashboardResponse getCustomerDashboard(UUID userId) {
                LocalDate today = LocalDate.now(clock);

                // 1. Fetch user's registered products
                List<Product> products = productRepository.findAllByUserIdWithWarrantyOrderByCreatedAtDesc(userId);
                DashboardResponse.ProductsSummary productsSummary = new DashboardResponse.ProductsSummary(
                                products.size());

                // 2. Fetch user's warranties and evaluate real-time lifecycle status
                List<WarrantyResponse> warranties = warrantyService.getWarrantiesForUser(userId);
                long activeWarranties = warranties.stream().filter(w -> w.getStatus() == WarrantyStatus.ACTIVE).count();
                long expiringSoonWarranties = warranties.stream()
                                .filter(w -> w.getStatus() == WarrantyStatus.EXPIRING_SOON).count();
                long expiredWarranties = warranties.stream().filter(w -> w.getStatus() == WarrantyStatus.EXPIRED)
                                .count();

                DashboardResponse.WarrantiesSummary warrantiesSummary = new DashboardResponse.WarrantiesSummary(
                                warranties.size(), activeWarranties, expiringSoonWarranties, expiredWarranties);

                // 3. Fetch user's claims
                List<Claim> claims = claimRepository.findByUserIdOrderByCreatedAtDesc(userId);
                long pendingClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.PENDING).count();
                long approvedClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED).count();
                long inProgressClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.IN_PROGRESS).count();
                long completedClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.COMPLETED).count();
                long rejectedClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.REJECTED).count();
                long cancelledClaims = claims.stream().filter(c -> c.getStatus() == ClaimStatus.CANCELLED).count();

                DashboardResponse.ClaimsSummary claimsSummary = new DashboardResponse.ClaimsSummary(
                                claims.size(), pendingClaims, approvedClaims, inProgressClaims, completedClaims,
                                rejectedClaims, cancelledClaims);

                // 4. Fetch user's invoices
                List<Invoice> invoices = invoiceRepository.findByUserIdOrderByUploadedAtDesc(userId);
                DashboardResponse.InvoicesSummary invoicesSummary = new DashboardResponse.InvoicesSummary(
                                invoices.size());

                // 5. Expiring Warranties Preview (Top 5 EXPIRING_SOON)
                List<DashboardResponse.ExpiringWarrantyItem> expiringList = warranties.stream()
                                .filter(w -> w.getStatus() == WarrantyStatus.EXPIRING_SOON)
                                .sorted(Comparator.comparing(WarrantyResponse::getDaysRemaining))
                                .limit(5)
                                .map(w -> new DashboardResponse.ExpiringWarrantyItem(
                                                w.getId(),
                                                w.getProductId(),
                                                w.getProductName(),
                                                w.getBrand(),
                                                w.getExpiryDate(),
                                                w.getDaysRemaining(),
                                                w.getStatus().name()))
                                .collect(Collectors.toList());

                // 6. Recent Claims Preview (Top 5)
                List<DashboardResponse.RecentClaimItem> recentClaimsList = claims.stream()
                                .limit(5)
                                .map(c -> new DashboardResponse.RecentClaimItem(
                                                c.getId(),
                                                c.getProduct() != null ? c.getProduct().getId() : null,
                                                c.getProduct() != null ? c.getProduct().getProductName()
                                                                : "Unknown Product",
                                                c.getClaimReason(),
                                                c.getStatus().name(),
                                                c.getCreatedAt()))
                                .collect(Collectors.toList());

                // 7. Recent Activity (Derived from real database records, sorted newest first)
                List<DashboardResponse.RecentActivityItem> activityItems = new ArrayList<>();

                for (Product p : products) {
                        if (p.getCreatedAt() != null) {
                                activityItems.add(new DashboardResponse.RecentActivityItem(
                                                "PRODUCT",
                                                "Product Registered",
                                                p.getProductName() + " (" + p.getBrand() + ") registered",
                                                p.getCreatedAt(),
                                                p.getId()));
                        }
                }

                for (Invoice inv : invoices) {
                        if (inv.getUploadedAt() != null) {
                                String prodName = inv.getProduct() != null ? inv.getProduct().getProductName()
                                                : "Product";
                                activityItems.add(new DashboardResponse.RecentActivityItem(
                                                "INVOICE",
                                                "Invoice Uploaded",
                                                "Invoice uploaded for " + prodName,
                                                inv.getUploadedAt(),
                                                inv.getId()));
                        }
                }

                for (Claim c : claims) {
                        if (c.getCreatedAt() != null) {
                                String prodName = c.getProduct() != null ? c.getProduct().getProductName() : "Product";
                                activityItems.add(new DashboardResponse.RecentActivityItem(
                                                "CLAIM",
                                                "Claim " + c.getStatus().name(),
                                                "Claim status: " + c.getStatus().name() + " for " + prodName,
                                                c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt(),
                                                c.getId()));
                        }
                }

                // Sort descending by timestamp and limit to 5
                List<DashboardResponse.RecentActivityItem> recentActivity = activityItems.stream()
                                .sorted(Comparator.comparing(DashboardResponse.RecentActivityItem::getTimestamp,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .limit(5)
                                .collect(Collectors.toList());

                return new DashboardResponse(
                                productsSummary,
                                warrantiesSummary,
                                claimsSummary,
                                invoicesSummary,
                                expiringList,
                                recentClaimsList,
                                recentActivity);
        }
}
