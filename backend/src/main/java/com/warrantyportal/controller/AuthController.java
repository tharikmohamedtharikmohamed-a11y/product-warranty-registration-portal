package com.warrantyportal.controller;

import com.warrantyportal.dto.AuthResponse;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.dto.UserResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing authentication endpoints: registration, login, and current user profile.
 * Phase 4 — Backend Authentication
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserResponse response = authService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
