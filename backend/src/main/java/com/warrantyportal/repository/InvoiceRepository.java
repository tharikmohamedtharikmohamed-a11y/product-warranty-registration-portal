package com.warrantyportal.repository;

import com.warrantyportal.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Invoice entity operations.
 * Enforces customer scoping through user ownership.
 * Phase 9 — Invoice Management
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query("SELECT i FROM Invoice i JOIN FETCH i.product p WHERE i.user.id = :userId ORDER BY i.uploadedAt DESC")
    List<Invoice> findByUserIdOrderByUploadedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.product p WHERE i.id = :id AND i.user.id = :userId")
    Optional<Invoice> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.product p WHERE p.id = :productId AND i.user.id = :userId ORDER BY i.uploadedAt DESC")
    List<Invoice> findByProductIdAndUserIdOrderByUploadedAtDesc(@Param("productId") UUID productId, @Param("userId") UUID userId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.product p WHERE p.id = :productId AND i.user.id = :userId ORDER BY i.uploadedAt DESC LIMIT 1")
    Optional<Invoice> findFirstByProductIdAndUserIdOrderByUploadedAtDesc(@Param("productId") UUID productId, @Param("userId") UUID userId);

    boolean existsByStoragePath(String storagePath);

    long countByProductId(UUID productId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.product p JOIN FETCH i.user u ORDER BY i.uploadedAt DESC")
    List<Invoice> findAllWithProductAndUser();
}
