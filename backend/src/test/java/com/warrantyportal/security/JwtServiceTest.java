package com.warrantyportal.security;

import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService token generation, claims extraction, and validation.
 * Phase 4 — Backend Authentication
 */
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set 256-bit test secret and 1-hour expiration
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        testUser = new User("Alice Smith", "alice@example.com", "hashedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);
    }

    @Test
    @DisplayName("Generate JWT token and verify extracted claims")
    void generateTokenAndExtractClaims() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        assertEquals(testUserId, jwtService.extractUserId(token));
        assertEquals("alice@example.com", jwtService.extractEmail(token));
        assertEquals("CUSTOMER", jwtService.extractRole(token));
    }

    @Test
    @DisplayName("Verify token validity for legitimate user")
    void tokenIsValidForCorrectUser() {
        String token = jwtService.generateToken(testUser);

        assertTrue(jwtService.isTokenValid(token, testUser));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Token is invalid for different user ID")
    void tokenIsInvalidForDifferentUser() {
        String token = jwtService.generateToken(testUser);

        User anotherUser = new User("Bob", "bob@example.com", "hashedPassword", Role.CUSTOMER);
        anotherUser.setId(UUID.randomUUID());

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }
}
