package com.warrantyportal.dto;

import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;

import java.util.UUID;

/**
 * Safe user response representation omitting security credentials.
 * Phase 4 — Backend Authentication
 */
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(UUID id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
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
}
