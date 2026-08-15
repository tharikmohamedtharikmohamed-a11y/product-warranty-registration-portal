package com.warrantyportal.repository;

import com.warrantyportal.entity.Warranty;
import com.warrantyportal.entity.enums.WarrantyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, UUID> {
    Optional<Warranty> findByProductId(UUID productId);
    List<Warranty> findByUserId(UUID userId);
    List<Warranty> findByStatus(WarrantyStatus status);
    long countByStatus(WarrantyStatus status);
    long countByWarrantyEndDateBefore(LocalDate date);
}
