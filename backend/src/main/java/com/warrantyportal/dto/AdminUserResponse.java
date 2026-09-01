package com.warrantyportal.dto;

import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe Administrative Data Transfer Object for User accounts.
 * Strictly omits passwords, hashes, and internal authentication secrets.
 * Phase 11 — Admin Management Module
 */
public class AdminUserResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;
    private OffsetDateTime createdAt;

    public AdminUserResponse() {
    }

    public AdminUserResponse(UUID id, String name, String email, Role role, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static AdminUserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
