# WarrantyHub — Frontend Notifications Integration & UI Components
**Phase 12 — Dashboard & Notifications**

---

## 1. Overview
The Frontend Notification subsystem provides seamless real-time notifications awareness and dedicated inbox management for WarrantyHub users.

Key highlights:
- Persistent navbar notification bell icon with real-time unread badge counter.
- Interactive dropdown preview displaying the top 5 recent notifications with instant "mark as read" capability.
- Dedicated full-page Notifications Inbox (`/notifications`) with tab filtering (`All` vs `Unread`), bulk actions, and rich lifecycle badges.

---

## 2. Component Architecture

### 1. `NotificationBell.jsx` (`frontend/src/components/NotificationBell.jsx`)
- Positioned globally in `Navbar.jsx` alongside the user profile and logout action.
- Fetches unread count on mount and re-checks every 60 seconds.
- Displays an unread badge counter (e.g. `1`, `5`, `99+`) styled in high-visibility danger red.
- Clicking toggles an accessible dropdown panel with:
  - Header: Unread badge count and "Mark all as read" button.
  - Body: Scrollable list of recent notifications with relative time ago formatting (e.g. "5m ago", "2h ago").
  - Actions: Mark individual notification as read without closing dropdown.
  - Navigation: Clicking any notification marks it read and routes to `/notifications`.
  - Footer: Direct link to full inbox (`View all notifications →`).
  - Accessibility: Full keyboard support (Escape key close, ARIA expanded state) and click-outside dismissal.

### 2. `NotificationsPage.jsx` (`frontend/src/pages/NotificationsPage.jsx`)
- Dedicated user inbox located at route `/notifications`.
- Filter tabs: `All ({count})` and `Unread ({count})`.
- Type-specific styling badges:
  - `EXPIRATION`: Amber warning badge for expiring warranties.
  - `CLAIM_UPDATE`: Sky blue informational badge for claim review and status changes.
  - `SYSTEM`: Primary blue badge for system notices.
  - `INFO`: Slate neutral badge for product/invoice additions.
- Interactive controls:
  - Individual "Mark as read" button on each unread notification.
  - Global "Mark all as read" button in the header.
- States:
  - Clean loading spinner during asynchronous data fetching.
  - Empty state with custom icon and reassuring copy when all caught up.
  - Error state with integrated retry action.

---

## 3. Services Integration (`notificationService.js`)

Provides unified Promise-based communication with backend REST endpoints:
- `getNotifications()`: Calls `GET /api/notifications`.
- `getUnreadCount()`: Calls `GET /api/notifications/unread-count`.
- `markAsRead(id)`: Calls `PATCH /api/notifications/${id}/read`.
- `markAllAsRead()`: Calls `PATCH /api/notifications/read-all`.

---

## 4. Route Registration (`AppRoutes.jsx`)
Protected behind `ProtectedRoute`:
```jsx
<Route element={<ProtectedRoute />}>
  <Route path="/dashboard" element={<DashboardPage />} />
  <Route path="/notifications" element={<NotificationsPage />} />
  {/* Other protected routes */}
</Route>
```
Unauthenticated attempts automatically redirect to `/login`.
