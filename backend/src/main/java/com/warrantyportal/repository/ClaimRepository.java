package com.warrantyportal.repository;

import com.warrantyportal.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Claim entity operations.
 * Enforces customer scoping through user ownership.
 * Phase 10 — Warranty Claims
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    @Query("SELECT c FROM Claim c JOIN FETCH c.product p WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Claim> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT c FROM Claim c JOIN FETCH c.product p WHERE c.id = :id AND c.user.id = :userId")
    Optional<Claim> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT c FROM Claim c JOIN FETCH c.product p WHERE p.id = :productId AND c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Claim> findByProductIdAndUserIdOrderByCreatedAtDesc(@Param("productId") UUID productId, @Param("userId") UUID userId);

    long countByProductId(UUID productId);
}
