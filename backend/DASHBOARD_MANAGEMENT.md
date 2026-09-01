# WarrantyHub — Backend Dashboard Telemetry Architecture & Implementation
**Phase 12 — Dashboard & Notifications**

---

## 1. Overview
The Dashboard subsystem serves as the central intelligence engine for both customer-facing and administrative portals. It aggregates real database-backed KPIs, expiration forecasts, claims statuses, and derived activity feeds from authoritative PostgreSQL tables without relying on hardcoded or mock metrics.

---

## 2. Customer Dashboard Telemetry (`GET /api/dashboard`)

### Aggregation Pipeline (`DashboardService.java`)
Aggregates telemetry scoped strictly to the authenticated `User`:
- **Products Summary**: Total count of registered equipment owned by the user.
- **Warranties Summary**:
  - `total`: Total warranty records associated with user's products.
  - `active`: Warranties with `status = ACTIVE`.
  - `expiringSoon`: Warranties expiring within the next 30 days (`status = EXPIRING_SOON`).
  - `expired`: Warranties with `status = EXPIRED`.
- **Claims Summary**:
  - `total`: Total claims submitted by the user.
  - Breakdown by status: `pending`, `approved`, `inProgress`, `completed`, `rejected`, `cancelled`.
- **Invoices Summary**: Total purchase invoices stored in user's vault.
- **Expiring Warranties Preview**: Top 5 warranties expiring soonest, sorted by `expiryDate ASC`, enriched with calculated `daysRemaining`.
- **Recent Claims Preview**: Top 5 most recent claims submitted by user, sorted by `createdAt DESC`.
- **Recent Activity Timeline**: Chronologically unified audit trail derived from user's products, invoices, and claims, sorted by `timestamp DESC` (limited to 10 entries).

### Security & Access Control
- Endpoint: `GET /api/dashboard`
- Access: Requires valid JWT with any authenticated role (`CUSTOMER`, `ADMIN`).
- Zero IDOR: All repository queries require user context (`user.getId()`).

---

## 3. Administrative Operational Telemetry (`GET /api/admin/dashboard/stats`)

### Extended Operational Fields (`AdminDashboardStatsResponse.java`)
Preserves all 16 existing KPI metrics established in Phase 11 while adding real-time operational feeds:
- **Core 16 KPIs**:
  1. `users`: Total platform users
  2. `customers`: Total registered customers
  3. `admins`: Total platform administrators
  4. `products`: Total registered equipment records
  5. `warranties`: Total warranty policies
  6. `activeWarranties`: Active policies
  7. `expiringSoonWarranties`: Expiring soon policies
  8. `expiredWarranties`: Expired policies
  9. `invoices`: Total uploaded invoices
  10. `claims`: Total submitted claims
  11. `pendingClaims`: Claims awaiting triage
  12. `approvedClaims`: Approved claims
  13. `rejectedClaims`: Rejected claims
  14. `inProgressClaims`: In-service claims
  15. `completedClaims`: Successfully resolved claims
  16. `cancelledClaims`: User-cancelled claims
- **Operational Feeds (Phase 12 Enhancements)**:
  - `recentClaims`: Top 5 claims across all users with customer name, email, product, claim reason, and status.
  - `recentUsers`: Top 5 recently registered users with role and registration timestamp.
  - `recentProducts`: Top 5 recently registered products with owner name and warranty status.

### Security & Access Control
- Endpoint: `GET /api/admin/dashboard/stats`
- Access: Requires valid JWT with `ADMIN` authority (`@PreAuthorize("hasRole('ADMIN')")`).

---

## 4. Verification & Testing

- `DashboardServiceTest.java`: 5 unit tests validating aggregation calculations, empty state fallbacks, warranty expiration calculations, and activity timeline sorting.
- `DashboardControllerTest.java`: 3 MockMvc tests validating authorization, HTTP 200 response shape, and unauthorized request rejection (HTTP 401).
- Total backend test suite: 169 automated tests pass with 0 failures, 0 errors.
