package com.warrantyportal.service;

import com.warrantyportal.dto.NotificationResponse;
import com.warrantyportal.entity.Notification;
import com.warrantyportal.entity.NotificationType;
import com.warrantyportal.entity.Role;
import com.warrantyportal.entity.User;
import com.warrantyportal.exception.ResourceNotFoundException;
import com.warrantyportal.repository.NotificationRepository;
import com.warrantyportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService verifying notification creation,
 * user-scoped retrieval, unread counter, read status updates, anti-IDOR protection, and admin broadcast.
 * Phase 12 — Dashboard & Notifications
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    private User userA;
    private User userB;
    private User adminUser;
    private UUID userAId;
    private UUID userBId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);

        userAId = UUID.randomUUID();
        userBId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        userA = new User("Alice Smith", "alice@example.com", "hashA", Role.CUSTOMER);
        userA.setId(userAId);

        userB = new User("Bob Jones", "bob@example.com", "hashB", Role.CUSTOMER);
        userB.setId(userBId);

        adminUser = new User("Admin Root", "admin@example.com", "hashAdmin", Role.ADMIN);
        adminUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should successfully create a notification for a user")
    void shouldCreateNotification() {
        Notification saved = new Notification(userA, "Product Registered", "Your product has been registered", NotificationType.PRODUCT);
        saved.setId(notificationId);
        saved.setCreatedAt(OffsetDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(
                userA, "Product Registered", "Your product has been registered", NotificationType.PRODUCT
        );

        assertNotNull(response);
        assertEquals(notificationId, response.getId());
        assertEquals("Product Registered", response.getTitle());
        assertEquals("Your product has been registered", response.getMessage());
        assertEquals(NotificationType.PRODUCT, response.getType());
        assertFalse(response.isRead());
    }

    @Test
    @DisplayName("Should safely handle null user on notification creation")
    void shouldReturnNullForNullUser() {
        NotificationResponse response = notificationService.createNotification(
                null, "Title", "Message", NotificationType.PRODUCT
        );
        assertNull(response);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should catch repository exception without throwing to protect caller")
    void shouldHandleRepositoryExceptionGracefully() {
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB error"));

        NotificationResponse response = notificationService.createNotification(
                userA, "Title", "Message", NotificationType.PRODUCT
        );

        assertNull(response);
    }

    @Test
    @DisplayName("Should broadcast notifications to all administrator accounts")
    void shouldNotifyAllAdmins() {
        User admin2 = new User("Admin Second", "admin2@example.com", "hashAdmin2", Role.ADMIN);
        admin2.setId(UUID.randomUUID());

        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(adminUser, admin2));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        notificationService.notifyAdmins("New Claim", "Customer filed claim", NotificationType.CLAIM);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> captured = captor.getAllValues();
        assertEquals(2, captured.size());
        assertEquals("New Claim", captured.get(0).getTitle());
        assertEquals("New Claim", captured.get(1).getTitle());
    }

    @Test
    @DisplayName("Should retrieve notifications scoped strictly to the calling user")
    void shouldGetNotificationsForUser() {
        Notification n1 = new Notification(userA, "Title 1", "Message 1", NotificationType.PRODUCT);
        n1.setId(UUID.randomUUID());
        Notification n2 = new Notification(userA, "Title 2", "Message 2", NotificationType.WARRANTY);
        n2.setId(UUID.randomUUID());

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userAId)).thenReturn(List.of(n1, n2));

        List<NotificationResponse> result = notificationService.getNotificationsForUser(userAId);

        assertEquals(2, result.size());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userAId);
    }

    @Test
    @DisplayName("Should return correct unread count for user")
    void shouldGetUnreadCountForUser() {
        when(notificationRepository.countByUserIdAndReadFalse(userAId)).thenReturn(3L);

        long count = notificationService.getUnreadCountForUser(userAId);

        assertEquals(3L, count);
        verify(notificationRepository).countByUserIdAndReadFalse(userAId);
    }

    @Test
    @DisplayName("Should mark a notification as read for its owner")
    void shouldMarkAsReadForOwner() {
        Notification notification = new Notification(userA, "Claim Approved", "Claim approved", NotificationType.CLAIM);
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findByIdAndUserId(notificationId, userAId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(notificationId, userAId);

        assertTrue(response.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException (404) when marking notification owned by another user (anti-IDOR)")
    void shouldRejectMarkAsReadForOtherUser() {
        // User B attempts to mark User A's notification as read
        when(notificationRepository.findByIdAndUserId(notificationId, userBId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.markAsRead(notificationId, userBId);
        });

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark all notifications as read for user")
    void shouldMarkAllAsRead() {
        when(notificationRepository.markAllAsReadByUserId(userAId)).thenReturn(5);

        notificationService.markAllAsRead(userAId);

        verify(notificationRepository).markAllAsReadByUserId(userAId);
    }
}
