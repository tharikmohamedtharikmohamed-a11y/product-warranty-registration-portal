# Backend Architecture — Phase 11: Admin Management Module

## 1. Overview
The Admin Management Module establishes centralized, platform-wide administrative controls for WarrantyHub. It delivers operational oversight across all registered users, equipment registries, warranties, uploaded invoice documents, and customer warranty claim adjudication.

Strict access control is enforced at both the HTTP filter chain and controller method levels, preventing privilege escalation and data leakage.

---

## 2. Administrator Role & Authorization Architecture

### 2.1 Role Model
System authorization relies strictly on the existing `Role` enumeration:
- `Role.CUSTOMER` — Standard platform consumer. Scoped strictly to customer-owned resources.
- `Role.ADMIN` — Privileged operator with access to platform-wide telemetry, cross-customer entity directories, and claim adjudication endpoints.

No additional role entities, auxiliary tables, or external authorization services are introduced.

### 2.2 Security Enforcement
Security is enforced using a defense-in-depth model across the Spring Security 6.x pipeline:
1. **URL Authorization in `SecurityConfig`:**
   ```java
   .requestMatchers("/api/admin/**").hasRole("ADMIN")
   ```
2. **Method-Level Pre-Authorization in `AdminController`:**
   ```java
   @RestController
   @RequestMapping("/api/admin")
   @PreAuthorize("hasRole('ADMIN')")
   public class AdminController { ... }
   ```
3. **Authentication Failure Handler (401 Unauthorized):**
   Unauthenticated requests (missing or invalid JWT) receive HTTP 401 with standard JSON error:
   ```json
   {
     "message": "Unauthorized access. Valid authentication token required."
   }
   ```
4. **Access Denied Handler (403 Forbidden):**
   Authenticated `CUSTOMER` accounts attempting to invoke `/api/admin/**` endpoints receive HTTP 403 Forbidden:
   ```json
   {
     "message": "Access denied. Admin privileges required."
   }
   ```

---

## 3. Administrative REST Endpoints

All endpoints require `Authorization: Bearer <ADMIN_JWT_TOKEN>`.

| HTTP Method | Endpoint | Description | Success Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard/stats` | Platform-wide operational KPI counts | `200 OK` |
| `GET` | `/api/admin/users` | Retrieve all registered users with optional search | `200 OK` |
| `GET` | `/api/admin/products` | Retrieve all registered equipment across all customers | `200 OK` |
| `GET` | `/api/admin/warranties` | Retrieve all warranty coverage records with live calculations | `200 OK` |
| `GET` | `/api/admin/invoices` | Retrieve all purchase invoice metadata across all customers | `200 OK` |
| `GET` | `/api/admin/invoices/{id}/download` | Stream binary invoice asset for administrative download | `200 OK` |
| `GET` | `/api/admin/invoices/{id}/view` | Stream binary invoice asset for administrative inline preview | `200 OK` |
| `GET` | `/api/admin/claims` | Retrieve all customer warranty claims | `200 OK` |
| `GET` | `/api/admin/claims/{id}` | Retrieve single warranty claim details by ID | `200 OK` |
| `PATCH` | `/api/admin/claims/{id}/approve` | Approve a PENDING claim | `200 OK` |
| `PATCH` | `/api/admin/claims/{id}/reject` | Reject a PENDING claim | `200 OK` |
| `PATCH` | `/api/admin/claims/{id}/start` | Transition an APPROVED claim to IN_PROGRESS | `200 OK` |
| `PATCH` | `/api/admin/claims/{id}/complete` | Transition an IN_PROGRESS claim to COMPLETED | `200 OK` |

---

## 4. Platform KPI Calculation & Authoritative Counts

Operational telemetry is calculated using database count queries rather than loading large entity sets into JVM memory:

```java
public AdminDashboardStatsResponse getDashboardStats() {
    long users = userRepository.count();
    long customers = userRepository.countByRole(Role.CUSTOMER);
    long admins = userRepository.countByRole(Role.ADMIN);

    long products = productRepository.count();

    long warranties = warrantyRepository.count();
    long activeWarranties = warrantyRepository.countByStatus(WarrantyStatus.ACTIVE);
    long expiringSoonWarranties = warrantyRepository.countByStatus(WarrantyStatus.EXPIRING_SOON);
    long expiredWarranties = warrantyRepository.countByStatus(WarrantyStatus.EXPIRED);

    long invoices = invoiceRepository.count();

    long claims = claimRepository.count();
    long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
    long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
    long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
    long inProgressClaims = claimRepository.countByStatus(ClaimStatus.IN_PROGRESS);
    long completedClaims = claimRepository.countByStatus(ClaimStatus.COMPLETED);
    long cancelledClaims = claimRepository.countByStatus(ClaimStatus.CANCELLED);

    return new AdminDashboardStatsResponse(...);
}
```

Response format (`AdminDashboardStatsResponse`):
```json
{
  "users": 10,
  "customers": 8,
  "admins": 2,
  "products": 15,
  "warranties": 15,
  "activeWarranties": 10,
  "expiringSoonWarranties": 3,
  "expiredWarranties": 2,
  "invoices": 12,
  "claims": 5,
  "pendingClaims": 2,
  "approvedClaims": 1,
  "rejectedClaims": 1,
  "inProgressClaims": 0,
  "completedClaims": 1,
  "cancelledClaims": 0
}
```

---

## 5. Cross-Customer Entity Directories

### 5.1 User Directory (`GET /api/admin/users`)
- Optional query parameter `?search=` filters by name or email (case-insensitive) using `findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc`.
- Mapped through `AdminUserResponse`:
  - `id`, `name`, `email`, `role`, `createdAt`
  - Sensitive authentication fields (`password`, hash, salts) are never mapped or exposed.

### 5.2 Product Registry (`GET /api/admin/products`)
- Executes fetch join `findAllWithUserAndWarrantyOrderByCreatedAtDesc` to eliminate N+1 queries.
- Presents customer identity (`customerName`, `customerEmail`), product specs, serial number, purchase date, price, and associated warranty coverage status.

### 5.3 Warranty Portfolio (`GET /api/admin/warranties`)
- Executes fetch join `findAllWithProductAndUser` ordered by expiry date.
- Employs authoritative `WarrantyService` calculation methods:
  - `calculateDaysRemaining(expiryDate, today)`
  - `calculateProgressPercentage(startDate, expiryDate, today)`
- Maintains standard statuses: `ACTIVE`, `EXPIRING_SOON`, `EXPIRED`.

### 5.4 Invoice Vault (`GET /api/admin/invoices`)
- Lists document metadata across all customer accounts.
- Administrative downloads (`GET /api/admin/invoices/{id}/download`) and inline preview (`GET /api/admin/invoices/{id}/view`) bypass customer isolation while keeping Supabase Storage private bucket credentials strictly confined to backend execution.

---

## 6. Claim Adjudication Workflow & State Machine

### 6.1 State Machine Transitions
Warranty claim statuses follow a deterministic lifecycle:

```
                  ┌───────────────┐
                  │    PENDING    │
                  └───────┬───────┘
                          │
            ┌─────────────┴─────────────┐
            ▼                           ▼
    ┌───────────────┐           ┌───────────────┐
    │   APPROVED    │           │   REJECTED    │ (Terminal)
    └───────┬───────┘           └───────────────┘
            │
            ▼
    ┌───────────────┐
    │  IN_PROGRESS  │
    └───────┬───────┘
            │
            ▼
    ┌───────────────┐
    │   COMPLETED   │ (Terminal)
    └───────────────┘
```

Allowed transitions:
1. `PENDING` → `APPROVED` via `PATCH /api/admin/claims/{id}/approve`
2. `PENDING` → `REJECTED` via `PATCH /api/admin/claims/{id}/reject`
3. `APPROVED` → `IN_PROGRESS` via `PATCH /api/admin/claims/{id}/start`
4. `IN_PROGRESS` → `COMPLETED` via `PATCH /api/admin/claims/{id}/complete`

### 6.2 Transition Validation
Any invalid status change (e.g. `REJECTED` → `APPROVED`, `PENDING` → `COMPLETED`, `COMPLETED` → anything) throws an `InvalidClaimException` translated by `GlobalExceptionHandler` to HTTP 400 Bad Request with a clear explanation:
- *"Only PENDING claims can be approved. Current status: REJECTED"*
- *"Only APPROVED claims can be moved to IN_PROGRESS. Current status: PENDING"*
- *"Only IN_PROGRESS claims can be completed. Current status: APPROVED"*

### 6.3 Admin Notes Storage & Customer Privacy Protection
To preserve database validation mode (`spring.jpa.hibernate.ddl-auto=validate`) without altering the PostgreSQL `claims` schema:
- Internal admin notes are persisted in the existing column `additional_information` (entity field `description`) separated by an internal delimiter:
  ```java
  public static final String ADMIN_NOTE_DELIMITER = "\n---ADMIN_NOTE---\n";
  ```
- **Customer Isolation:** When `ClaimResponse.fromClaim(claim)` produces customer-facing JSON, any text following `ADMIN_NOTE_DELIMITER` is stripped.
- **Admin Visibility:** `AdminClaimResponse.fromClaim(claim)` parses both `description` (customer statement) and `adminNotes` (internal rationale).

---

## 7. Data Transfer Objects (DTOs)

| DTO | Direction | Purpose | Sensitive Fields Excluded |
| :--- | :--- | :--- | :--- |
| `AdminDashboardStatsResponse` | Out | Platform KPI numbers | None |
| `AdminUserResponse` | Out | User directory details | Password hash, security tokens |
| `AdminProductResponse` | Out | Equipment specs + customer info | Customer passwords/secrets |
| `AdminWarrantyResponse` | Out | Coverage + progress + customer | Storage paths/credentials |
| `AdminInvoiceResponse` | Out | Invoice metadata + customer info | Storage keys, presigned URLs |
| `AdminClaimResponse` | Out | Claim specs + admin notes + customer | Password hashes |
| `AdminClaimDecisionRequest` | In | Admin note payload for status updates | None |

---

## 8. Automated Test Coverage

The backend test suite verifies:
- `AdminControllerTest`:
  - `GET /api/admin/dashboard/stats`: 200 OK for ADMIN, 403 for CUSTOMER, 401 unauthenticated
  - `GET /api/admin/users`: 200 OK for ADMIN, 403 for CUSTOMER
  - `GET /api/admin/products`: 200 OK for ADMIN, 403 for CUSTOMER
  - `GET /api/admin/warranties`: 200 OK for ADMIN, 403 for CUSTOMER
  - `GET /api/admin/invoices`: 200 OK for ADMIN, 403 for CUSTOMER
  - `GET /api/admin/claims`: 200 OK for ADMIN, 403 for CUSTOMER
  - `PATCH /api/admin/claims/{id}/approve`: 200 OK for ADMIN, 403 for CUSTOMER
  - `PATCH /api/admin/claims/{id}/reject`: 200 OK for ADMIN
  - `PATCH /api/admin/claims/{id}/start`: 200 OK for ADMIN
  - `PATCH /api/admin/claims/{id}/complete`: 200 OK for ADMIN
  - Status transition validation: 400 Bad Request on invalid transitions
- `AdminServiceTest`:
  - Authoritative dashboard KPI count calculations
  - User search filtering and safe DTO mapping
  - Product, warranty, and invoice listings across all accounts
  - PENDING → APPROVED transition with note persistence
  - PENDING → REJECTED transition with rejection reason
  - APPROVED → IN_PROGRESS transition
  - IN_PROGRESS → COMPLETED transition
  - Illegal transition rejection for all non-matching states
  - Terminal state immutability (`COMPLETED`, `CANCELLED`, `REJECTED`)
