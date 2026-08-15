package com.warrantyportal.repository;

import com.warrantyportal.entity.WarrantyClaim;
import com.warrantyportal.entity.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, UUID> {
    List<WarrantyClaim> findByUserId(UUID userId);
    List<WarrantyClaim> findByProductId(UUID productId);
    List<WarrantyClaim> findByWarrantyId(UUID warrantyId);
    List<WarrantyClaim> findByStatus(ClaimStatus status);
    boolean existsByWarrantyIdAndStatusIn(UUID warrantyId, List<ClaimStatus> statuses);
    long countByStatus(ClaimStatus status);
}
