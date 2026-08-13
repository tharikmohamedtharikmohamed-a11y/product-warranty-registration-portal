package com.warrantyportal.controller;

import com.warrantyportal.dto.WarrantyResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.AuthService;
import com.warrantyportal.service.WarrantyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/warranties")
public class WarrantyController {

    private final WarrantyService warrantyService;
    private final AuthService authService;

    public WarrantyController(WarrantyService warrantyService, AuthService authService) {
        this.warrantyService = warrantyService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<WarrantyResponse>> getUserWarranties() {
        User currentUser = authService.getCurrentUser();
        List<WarrantyResponse> warranties = warrantyService.getUserWarranties(currentUser);
        return ResponseEntity.ok(warranties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyResponse> getWarrantyById(@PathVariable("id") UUID id) {
        User currentUser = authService.getCurrentUser();
        WarrantyResponse response = warrantyService.getWarrantyById(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
