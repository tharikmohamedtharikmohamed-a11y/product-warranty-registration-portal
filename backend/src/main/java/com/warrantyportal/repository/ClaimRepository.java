package com.warrantyportal.repository;

import com.warrantyportal.entity.Claim;
import com.warrantyportal.entity.ClaimStatus;
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
 * Phase 11 — Admin Management Module
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

    long countByStatus(ClaimStatus status);

    @Query("SELECT c FROM Claim c JOIN FETCH c.product p JOIN FETCH c.user u ORDER BY c.createdAt DESC")
    List<Claim> findAllWithProductAndUser();

    @Query("SELECT c FROM Claim c JOIN FETCH c.product p JOIN FETCH c.user u WHERE c.id = :id")
    Optional<Claim> findByIdWithProductAndUser(@Param("id") UUID id);
}
