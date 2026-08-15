package com.warrantyportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.AuthResponse;
import com.warrantyportal.dto.LoginRequest;
import com.warrantyportal.dto.RegisterRequest;
import com.warrantyportal.dto.UserResponse;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.EmailAlreadyExistsException;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.InvalidCredentialsException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice tests for AuthController endpoints, error handling, and security guards.
 * Phase 4 — Backend Authentication
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "encodedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);
    }

    @Test
    @DisplayName("POST /api/auth/register returns 201 Created on valid request")
    void registerReturns201Created() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Password123");
        UserResponse userResponse = new UserResponse(testUserId, "John Doe", "john@example.com", Role.CUSTOMER);
        AuthResponse authResponse = new AuthResponse("Registration successful", userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 409 Conflict on duplicate email")
    void registerReturns409OnDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "existing@example.com", "Password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 Bad Request when password is under 8 characters")
    void registerReturns400OnWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 OK with token on valid credentials")
    void loginReturns200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "Password123");
        UserResponse userResponse = new UserResponse(testUserId, "John Doe", "john@example.com", Role.CUSTOMER);
        AuthResponse authResponse = new AuthResponse("Login successful", "valid.jwt.token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("valid.jwt.token"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 Unauthorized on invalid credentials")
    void loginReturns401OnInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("GET /api/auth/me returns 401 Unauthorized without JWT token")
    void getMeReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access. Valid authentication token required."));
    }

    @Test
    @DisplayName("GET /api/auth/me returns 200 OK with user profile when valid Bearer token provided")
    void getMeReturns200WithValidToken() throws Exception {
        String token = "valid.jwt.token";
        UserResponse userResponse = new UserResponse(testUserId, "John Doe", "john@example.com", Role.CUSTOMER);

        when(jwtService.extractUserId(token)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(token, testUser)).thenReturn(true);
        when(authService.getCurrentUser(testUserId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}
