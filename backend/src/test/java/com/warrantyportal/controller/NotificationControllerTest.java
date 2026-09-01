package com.warrantyportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrantyportal.config.SecurityConfig;
import com.warrantyportal.dto.NotificationResponse;
import com.warrantyportal.entity.NotificationType;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.GlobalExceptionHandler;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.UserRepository;
import com.warrantyportal.security.JwtAuthenticationFilter;
import com.warrantyportal.security.JwtService;
import com.warrantyportal.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for NotificationController endpoints and security scoping.
 * Phase 12 — Dashboard & Notifications
 */
@WebMvcTest(controllers = NotificationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private final UUID testUserId = UUID.randomUUID();
    private final String validToken = "valid.test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = new User("Alice Developer", "alice@example.com", "hashedPassword", Role.CUSTOMER);
        testUser.setId(testUserId);

        when(jwtService.extractUserId(validToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/notifications should return 200 with notification list for authenticated user")
    void shouldReturnNotificationsForAuthenticatedUser() throws Exception {
        NotificationResponse n1 = new NotificationResponse(
                UUID.randomUUID(), "Product Registered", "Product registered successfully",
                NotificationType.PRODUCT, false, OffsetDateTime.now()
        );

        when(notificationService.getNotificationsForUser(testUserId)).thenReturn(List.of(n1));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + validToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Product Registered"))
                .andExpect(jsonPath("$[0].type").value("PRODUCT"))
                .andExpect(jsonPath("$[0].read").value(false));

        verify(notificationService).getNotificationsForUser(testUserId);
    }

    @Test
    @DisplayName("GET /api/notifications should return 401 when unauthenticated")
    void shouldReturn401WhenGetNotificationsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count should return 200 with unread count")
    void shouldReturnUnreadCount() throws Exception {
        when(notificationService.getUnreadCountForUser(testUserId)).thenReturn(4L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + validToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4));

        verify(notificationService).getUnreadCountForUser(testUserId);
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count should return 401 when unauthenticated")
    void shouldReturn401WhenGetUnreadCountUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read should return 200 when owned by user")
    void shouldMarkNotificationAsRead() throws Exception {
        UUID notifId = UUID.randomUUID();
        NotificationResponse updated = new NotificationResponse(
                notifId, "Claim Approved", "Claim has been approved",
                NotificationType.CLAIM, true, OffsetDateTime.now()
        );

        when(notificationService.markAsRead(notifId, testUserId)).thenReturn(updated);

        mockMvc.perform(patch("/api/notifications/{id}/read", notifId)
                        .header("Authorization", "Bearer " + validToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notifId.toString()))
                .andExpect(jsonPath("$.read").value(true));

        verify(notificationService).markAsRead(notifId, testUserId);
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read should return 404 when notification not found or unowned (anti-IDOR)")
    void shouldReturn404WhenMarkAsReadUnowned() throws Exception {
        UUID unownedId = UUID.randomUUID();
        when(notificationService.markAsRead(unownedId, testUserId))
                .thenThrow(new ResourceNotFoundException("Notification not found with id: " + unownedId));

        mockMvc.perform(patch("/api/notifications/{id}/read", unownedId)
                        .header("Authorization", "Bearer " + validToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/notifications/read-all should mark all as read and return 200")
    void shouldMarkAllNotificationsAsRead() throws Exception {
        doNothing().when(notificationService).markAllAsRead(testUserId);

        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + validToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));

        verify(notificationService).markAllAsRead(testUserId);
    }

    @Test
    @DisplayName("PATCH /api/notifications/read-all should return 401 when unauthenticated")
    void shouldReturn401WhenMarkAllReadUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
