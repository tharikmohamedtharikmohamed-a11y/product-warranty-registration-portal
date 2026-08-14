package com.warrantyportal.service;

import com.warrantyportal.dto.ClaimResponse;
import com.warrantyportal.dto.CreateClaimRequest;
import com.warrantyportal.dto.UpdateClaimStatusRequest;
import com.warrantyportal.entity.Invoice;
import com.warrantyportal.entity.Product;
import com.warrantyportal.entity.User;
import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.WarrantyClaim;
import com.warrantyportal.entity.enums.ClaimStatus;
import com.warrantyportal.exception.DuplicateClaimException;
import com.warrantyportal.exception.InvalidClaimException;
import com.warrantyportal.exception.ResourceForbiddenException;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.InvoiceRepository;
import com.warrantyportal.repository.ProductRepository;
import com.warrantyportal.repository.WarrantyClaimRepository;
import com.warrantyportal.repository.WarrantyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WarrantyClaimService {

    private final WarrantyClaimRepository claimRepository;
    private final ProductRepository productRepository;
    private final WarrantyRepository warrantyRepository;
    private final InvoiceRepository invoiceRepository;

    public WarrantyClaimService(WarrantyClaimRepository claimRepository,
                                ProductRepository productRepository,
                                WarrantyRepository warrantyRepository,
                                InvoiceRepository invoiceRepository) {
        this.claimRepository = claimRepository;
        this.productRepository = productRepository;
        this.warrantyRepository = warrantyRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest request, User currentUser) {
        // 1. Verify Product Existence and Ownership
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this product");
        }

        // 2. Verify Warranty Existence and Ownership
        Warranty warranty = warrantyRepository.findById(request.getWarrantyId())
                .orElseThrow(() -> new ResourceNotFoundException("Warranty not found with ID: " + request.getWarrantyId()));

        if (!warranty.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this warranty");
        }

        if (!warranty.getProduct().getId().equals(product.getId())) {
            throw new InvalidClaimException("Warranty does not belong to the specified product");
        }

        // 3. Verify Warranty Validity (Expiration Check)
        if (LocalDate.now().isAfter(warranty.getWarrantyEndDate())) {
            throw new InvalidClaimException("Warranty has expired");
        }

        // 4. Verify Optional Invoice Existence and Ownership
        Invoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + request.getInvoiceId()));

            if (!invoice.getUser().getId().equals(currentUser.getId())) {
                throw new ResourceForbiddenException("Access denied: You do not own this invoice");
            }
        }

        // 5. Check Duplicate Active Claims (PENDING or IN_PROGRESS)
        boolean hasActiveClaim = claimRepository.existsByWarrantyIdAndStatusIn(
                warranty.getId(),
                Arrays.asList(ClaimStatus.PENDING, ClaimStatus.IN_PROGRESS)
        );

        if (hasActiveClaim) {
            throw new DuplicateClaimException("An active warranty claim already exists for this warranty");
        }

        // 6. Create and Save Claim
        WarrantyClaim claim = new WarrantyClaim();
        claim.setProduct(product);
        claim.setWarranty(warranty);
        claim.setUser(currentUser);
        claim.setInvoice(invoice);
        claim.setIssueDescription(request.getIssueDescription());
        claim.setStatus(ClaimStatus.PENDING);

        WarrantyClaim savedClaim = claimRepository.save(claim);
        return ClaimResponse.fromEntity(savedClaim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getUserClaims(User currentUser) {
        List<WarrantyClaim> claims = claimRepository.findByUserId(currentUser.getId());
        return claims.stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(UUID claimId, User currentUser) {
        WarrantyClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found with ID: " + claimId));

        if (!claim.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this warranty claim");
        }

        return ClaimResponse.fromEntity(claim);
    }

    @Transactional
    public ClaimResponse cancelClaim(UUID claimId, User currentUser) {
        WarrantyClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found with ID: " + claimId));

        if (!claim.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceForbiddenException("Access denied: You do not own this warranty claim");
        }

        if (claim.getStatus() == ClaimStatus.COMPLETED || claim.getStatus() == ClaimStatus.CANCELLED) {
            throw new InvalidClaimException("Cannot cancel claim in status: " + claim.getStatus());
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        WarrantyClaim updatedClaim = claimRepository.save(claim);
        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Transactional
    public ClaimResponse updateClaimStatusByAdmin(UUID claimId, UpdateClaimStatusRequest request) {
        WarrantyClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty claim not found with ID: " + claimId));

        claim.setStatus(request.getStatus());
        if (request.getResolutionNotes() != null && !request.getResolutionNotes().isBlank()) {
            claim.setResolution(request.getResolutionNotes());
        }

        WarrantyClaim updatedClaim = claimRepository.save(claim);
        return ClaimResponse.fromEntity(updatedClaim);
    }
}
