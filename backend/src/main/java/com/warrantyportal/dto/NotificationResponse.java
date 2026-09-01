package com.warrantyportal.dto;

import com.warrantyportal.entity.Notification;
import com.warrantyportal.entity.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Safe Data Transfer Object for Notification payloads.
 * Excludes sensitive user details and internal JPA fields.
 * Phase 12 — Dashboard & Notifications
 */
public class NotificationResponse {

    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean read;
    private OffsetDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, String title, String message, NotificationType type, boolean read, OffsetDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static NotificationResponse fromNotification(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
