# WarrantyHub — Backend Notification Engine Architecture & Implementation
**Phase 12 — Dashboard & Notifications**

---

## 1. Overview
The Notification Engine provides automated, user-scoped alerts regarding key lifecycle events in WarrantyHub:
- Warranty expiration warnings (`EXPIRATION`)
- Claim review and adjudication changes (`CLAIM_UPDATE`)
- Equipment registration, invoice additions, and platform notices (`INFO`, `SYSTEM`)

All notifications are bound directly to authenticated users, strictly isolated to prevent IDOR vulnerabilities, and backed by authoritative Supabase PostgreSQL database records.

---

## 2. Database Schema & Compatibility

The `notifications` table was provisioned in Supabase PostgreSQL preserving `spring.jpa.hibernate.ddl-auto=validate`:

```sql
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notification_type CHECK (type IN ('INFO', 'EXPIRATION', 'CLAIM_UPDATE', 'SYSTEM'))
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
```

### Entity Mapping (`Notification.java`)
- `id` (`UUID`, Primary Key)
- `user` (`@ManyToOne(fetch = FetchType.LAZY) User`)
- `title` (`String`, 255 chars)
- `message` (`String`, Text)
- `type` (`@Enumerated(EnumType.STRING) NotificationType`)
- `read` (`@Column(name = "is_read") boolean`)
- `createdAt` (`@Column(name = "created_at", updatable = false) OffsetDateTime`)

---

## 3. REST API Specification

| Method | Endpoint | Description | Auth Required | Success Status |
|---|---|---|---|---|
| `GET` | `/api/notifications` | Get paginated/list notifications for current user | Yes (`CUSTOMER`, `ADMIN`) | `200 OK` |
| `GET` | `/api/notifications/unread-count` | Get total count of unread notifications | Yes (`CUSTOMER`, `ADMIN`) | `200 OK` |
| `PATCH` | `/api/notifications/{id}/read` | Mark a specific notification as read | Yes (`CUSTOMER`, `ADMIN`) | `200 OK` |
| `PATCH` | `/api/notifications/read-all` | Mark all unread notifications as read | Yes (`CUSTOMER`, `ADMIN`) | `200 OK` |

---

## 4. Security & Anti-IDOR Protections

1. **User Scoping**: Every query (`findByUserOrderByCreatedAtDesc`, `countByUserAndReadFalse`) executes with the authenticated `User` context from Spring Security.
2. **Anti-IDOR on Status Updates**: When a user marks a notification as read (`PATCH /api/notifications/{id}/read`), the service enforces:
   ```java
   Notification notification = notificationRepository.findByIdAndUser(id, currentUser)
       .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
   ```
   If a user tries to mark a notification belonging to another user, `404 Not Found` is returned rather than `403 Forbidden`, preventing identifier scanning or user enumeration.
3. **CORS Configuration**: The `SecurityConfig` explicitly permits `PATCH` alongside `GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS`.

---

## 5. Automated Notification Triggers

`NotificationService` is injected into key business services to trigger instant in-app alerts:
- **Product Registration** (`ProductService`): Triggers an `INFO` notification acknowledging equipment registration and policy creation.
- **Invoice Upload** (`InvoiceService`): Triggers an `INFO` notification confirming successful invoice attachment.
- **Claim Submission** (`ClaimService`): Triggers a `CLAIM_UPDATE` notification confirming receipt and placing claim in review queue.
- **Claim Cancellation** (`ClaimService`): Triggers a `CLAIM_UPDATE` notification confirming cancellation.
- **Admin Adjudication** (`AdminService`): Triggers targeted `CLAIM_UPDATE` notifications to the claim owner whenever status changes to `APPROVED`, `REJECTED`, `IN_PROGRESS`, or `COMPLETED`.
- **User Registration** (`AuthService`): Triggers a `SYSTEM` welcome notification to newly registered customer accounts.

---

## 6. Unit & Integration Verification

All endpoints and service methods are covered by automated unit tests:
- `NotificationServiceTest.java`: 7 comprehensive test scenarios verifying creation, anti-IDOR resolution, bulk read, and unread counting.
- `NotificationControllerTest.java`: 4 MockMvc test scenarios verifying controller routing, JSON response structure, and HTTP status codes.
- **Total Test Suite**: 169 tests pass with 0 failures and 0 errors.
