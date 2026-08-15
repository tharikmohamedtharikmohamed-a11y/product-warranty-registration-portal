package com.warrantyportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Authentication response payload for registration and login operations.
 * Phase 4 — Backend Authentication
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String message;
    private String token;
    private UserResponse user;

    public AuthResponse() {
    }

    public AuthResponse(String message, UserResponse user) {
        this.message = message;
        this.user = user;
    }

    public AuthResponse(String message, String token, UserResponse user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
