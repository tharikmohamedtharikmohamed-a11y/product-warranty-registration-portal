package com.warrantyportal.service;

import com.warrantyportal.dto.NotificationResponse;
import com.warrantyportal.entity.Notification;
import com.warrantyportal.entity.NotificationType;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.NotificationRepository;
import com.warrantyportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing user-scoped notifications, unread tracking,
 * and contextual alert dispatching for business lifecycle events.
 * Phase 12 — Dashboard & Notifications
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates and persists a notification for a target user.
     * Failsafe: exception caught and logged to prevent non-critical notification errors from failing core operations.
     */
    public NotificationResponse createNotification(User user, String title, String message, NotificationType type) {
        if (user == null || user.getId() == null) {
            logger.warn("Cannot create notification for null or unpersisted user.");
            return null;
        }

        try {
            Notification notification = new Notification(user, title, message, type);
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification created [id={}, user={}, type={}, title={}]",
                    saved.getId(), user.getId(), type, title);
            return NotificationResponse.fromNotification(saved);
        } catch (Exception e) {
            logger.error("Failed to create notification for user {}: {}", user.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Dispatches an administrative notification to all active system administrators.
     */
    public void notifyAdmins(String title, String message, NotificationType type) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            for (User admin : admins) {
                createNotification(admin, title, message, type);
            }
        } catch (Exception e) {
            logger.error("Failed to broadcast admin notification [title={}]: {}", title, e.getMessage());
        }
    }

    /**
     * Retrieves all notifications belonging to the authenticated user, sorted newest first.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the top 5 most recent notifications for dashboard or dropdown preview.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotificationsForUser(UUID userId) {
        return notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the count of unread notifications for the authenticated user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCountForUser(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Marks a specific notification as read.
     * Enforces anti-IDOR ownership check: throws 404 if notification not found or belongs to another user.
     */
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        logger.info("Notification {} marked as read by user {}", notificationId, userId);
        return NotificationResponse.fromNotification(updated);
    }

    /**
     * Marks all unread notifications belonging to the authenticated user as read.
     */
    public void markAllAsRead(UUID userId) {
        int updatedCount = notificationRepository.markAllAsReadByUserId(userId);
        logger.info("Marked {} notifications as read for user {}", updatedCount, userId);
    }
}
