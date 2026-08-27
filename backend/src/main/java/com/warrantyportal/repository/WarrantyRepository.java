package com.warrantyportal.repository;

import com.warrantyportal.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Warranty entity operations.
 * Enforces customer scoping through product user ownership.
 * Phase 8 — Warranty Management
 */
@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, UUID> {

    Optional<Warranty> findByProductId(UUID productId);

    void deleteByProductId(UUID productId);

    @Query("SELECT w FROM Warranty w JOIN FETCH w.product p WHERE p.user.id = :userId ORDER BY w.expiryDate ASC")
    List<Warranty> findAllByProductUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Warranty w JOIN FETCH w.product p WHERE w.id = :id AND p.user.id = :userId")
    Optional<Warranty> findByIdAndProductUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT w FROM Warranty w JOIN FETCH w.product p WHERE p.id = :productId AND p.user.id = :userId")
    Optional<Warranty> findByProductIdAndProductUserId(@Param("productId") UUID productId, @Param("userId") UUID userId);
}
