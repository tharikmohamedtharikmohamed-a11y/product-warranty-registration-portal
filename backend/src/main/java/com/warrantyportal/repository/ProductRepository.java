package com.warrantyportal.repository;

import com.warrantyportal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Product entity operations.
 * Enforces customer scoping with userId constraints.
 * Phase 7 — Product Management
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.warranty WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Product> findAllByUserIdWithWarrantyOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.warranty WHERE p.id = :id AND p.user.id = :userId")
    Optional<Product> findByIdAndUserIdWithWarranty(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Product> findByIdAndUserId(UUID id, UUID userId);

    boolean existsBySerialNumberIgnoreCaseAndUserId(String serialNumber, UUID userId);

    boolean existsBySerialNumberIgnoreCaseAndUserIdAndIdNot(String serialNumber, UUID userId, UUID id);

    void deleteByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT p FROM Product p JOIN FETCH p.user LEFT JOIN FETCH p.warranty ORDER BY p.createdAt DESC")
    List<Product> findAllWithUserAndWarrantyOrderByCreatedAtDesc();
}
