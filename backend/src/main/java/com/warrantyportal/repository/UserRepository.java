package com.warrantyportal.repository;

import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity operations.
 * Phase 4 — Backend Authentication
 * Phase 11 — Admin Management Module
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(String name, String email);
}
