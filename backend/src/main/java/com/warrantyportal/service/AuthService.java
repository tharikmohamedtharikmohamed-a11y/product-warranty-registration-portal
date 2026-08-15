package com.warrantyportal.service;

import com.warrantyportal.dto.AuthResponse;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.dto.UserResponse;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.EmailAlreadyExistsException;
import com.warrantyportal.exception.InvalidCredentialsException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service handling customer registration, login verification, and profile retrieval.
 * Phase 4 — Backend Authentication
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // New accounts created through public registration are strictly assigned CUSTOMER role
        User newUser = new User(
                request.getName().trim(),
                normalizedEmail,
                hashedPassword,
                Role.CUSTOMER
        );

        User savedUser = userRepository.save(newUser);

        return new AuthResponse(
                "Registration successful",
                UserResponse.fromUser(savedUser)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(
                "Login successful",
                jwtToken,
                UserResponse.fromUser(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Authenticated user not found"));
        return UserResponse.fromUser(user);
    }
}
