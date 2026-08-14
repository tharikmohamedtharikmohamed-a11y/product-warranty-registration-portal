package com.warrantyportal.controller;

import com.warrantyportal.dto.ClaimResponse;
import com.warrantyportal.dto.CreateClaimRequest;
import com.warrantyportal.dto.UpdateClaimStatusRequest;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.AuthService;
import com.warrantyportal.service.WarrantyClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class WarrantyClaimController {

    private final WarrantyClaimService claimService;
    private final AuthService authService;

    public WarrantyClaimController(WarrantyClaimService claimService, AuthService authService) {
        this.claimService = claimService;
        this.authService = authService;
    }

    @PostMapping("/api/claims")
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateClaimRequest request) {
        User currentUser = authService.getCurrentUser();
        ClaimResponse response = claimService.createClaim(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/claims")
    public ResponseEntity<List<ClaimResponse>> getUserClaims() {
        User currentUser = authService.getCurrentUser();
        List<ClaimResponse> claims = claimService.getUserClaims(currentUser);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/api/claims/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        ClaimResponse response = claimService.getClaimById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/claims/{id}/cancel")
    public ResponseEntity<ClaimResponse> cancelClaim(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        ClaimResponse response = claimService.cancelClaim(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/admin/claims/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> updateClaimStatusByAdmin(@PathVariable("id") UUID id,
                                                                   @Valid @RequestBody UpdateClaimStatusRequest request) {
        ClaimResponse response = claimService.updateClaimStatusByAdmin(id, request);
        return ResponseEntity.ok(response);
    }
}
