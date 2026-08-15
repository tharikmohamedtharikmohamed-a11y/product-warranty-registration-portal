package com.warrantyportal.service;

import com.warrantyportal.dto.AuthResponse;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.EmailAlreadyExistsException;
import com.warrantyportal.exception.InvalidCredentialsException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService covering registration, BCrypt hashing, and login verification.
 * Phase 4 — Backend Authentication
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("1. Registration success with CUSTOMER role and hashed password")
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "Password123");

        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$12$hashedPasswordExample");

        User savedUser = new User("Jane Doe", "jane@example.com", "$2a$12$hashedPasswordExample", Role.CUSTOMER);
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());
        assertEquals("jane@example.com", response.getUser().getEmail());
        assertEquals("Jane Doe", response.getUser().getName());
        assertEquals(Role.CUSTOMER, response.getUser().getRole());

        // Verify password hashing was invoked
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("$2a$12$hashedPasswordExample", capturedUser.getPassword());
        assertNotEquals("Password123", capturedUser.getPassword());
    }

    @Test
    @DisplayName("2. Registration fails when email already exists")
    void registerDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "existing@example.com", "Password123");

        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("3. Email is normalized to lowercase and trimmed during registration")
    void registerNormalizesEmail() {
        RegisterRequest request = new RegisterRequest("  John Doe  ", "  JOHN@Example.COM  ", "Password123");

        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User savedUser = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("john@example.com", userCaptor.getValue().getEmail());
        assertEquals("John Doe", userCaptor.getValue().getName());
    }

    @Test
    @DisplayName("4. Login success returns valid JWT token and safe user profile")
    void loginSuccess() {
        LoginRequest request = new LoginRequest("john@example.com", "Password123");

        User user = new User("John Doe", "john@example.com", "$2a$12$hashed", Role.CUSTOMER);
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "$2a$12$hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("5. Login with invalid password throws InvalidCredentialsException")
    void loginInvalidPasswordThrowsException() {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");

        User user = new User("John Doe", "john@example.com", "$2a$12$hashed", Role.CUSTOMER);
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$12$hashed")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("6. Login with nonexistent email throws InvalidCredentialsException")
    void loginNonexistentEmailThrowsException() {
        LoginRequest request = new LoginRequest("unknown@example.com", "Password123");

        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }
}
