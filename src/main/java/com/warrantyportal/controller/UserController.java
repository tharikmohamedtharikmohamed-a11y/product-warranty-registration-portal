package com.warrantyportal.controller;

import com.warrantyportal.dto.UserResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(UserResponse.fromEntity(currentUser));
    }
}
