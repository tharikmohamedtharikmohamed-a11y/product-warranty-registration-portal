package com.warrantyportal.controller;

import com.warrantyportal.dto.NotificationResponse;
import com.warrantyportal.dto.UnreadCountResponse;
import com.warrantyportal.entity.User;
import com.warrantyportal.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller exposing user-scoped notification operations.
 * Phase 12 — Dashboard & Notifications
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieve all notifications for the authenticated user, sorted newest first.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal User currentUser) {
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Retrieve unread notification count for the authenticated user.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {
        long count = notificationService.getUnreadCountForUser(currentUser.getId());
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    /**
     * Mark a specific notification as read.
     * Enforces anti-IDOR check: returns 404 if notification not found or owned by another user.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        NotificationResponse response = notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications for the authenticated user as read.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
