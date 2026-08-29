# WarrantyHub — Backend Warranty Claims Architecture & Implementation
**Phase 10 — Warranty Claims**

---

## 1. Overview
The Warranty Claims subsystem enables authenticated customers to submit, track, view, and cancel warranty claims against eligible registered products.
Strict enterprise security, ownership validation (anti-IDOR), and warranty eligibility checks ensure that claims can only be filed against active or expiring-soon warranties, while initial status is immutably set to `PENDING`.

> [!NOTE]
> Admin adjudication features (`APPROVED`, `REJECTED`, `IN_PROGRESS`, `COMPLETED`) belong strictly to Phase 11 and are not exposed to customer roles in Phase 10.

---

## 2. Database Mapping & Compatibility
To strictly preserve `spring.jpa.hibernate.ddl-auto=validate` without running destructive DDL or schema alterations against Supabase PostgreSQL:
- **Table Name**: `claims`
- **Columns**:
  - `id` (`UUID`, Primary Key)
  - `user_id` (`UUID NOT NULL`, Foreign Key to `users(id)`)
  - `product_id` (`UUID NOT NULL`, Foreign Key to `products(id)`)
  - `issue_description` (`TEXT NOT NULL`) — mapped in Entity as `claimReason` with dual alias getters/setters (`getClaimReason()` and `getIssueDescription()`)
  - `additional_information` (`TEXT`) — mapped in Entity as `description` with dual alias getters/setters (`getDescription()` and `getAdditionalInformation()`)
  - `claim_date` (`DATE NOT NULL`) — set to current date at creation
  - `status` (`VARCHAR(50) NOT NULL`) — persisted as Enum String (`PENDING`, `APPROVED`, `REJECTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)
  - `created_at` (`TIMESTAMPTZ NOT NULL`, auditing)
  - `updated_at` (`TIMESTAMPTZ NOT NULL`, auditing)

---

## 3. Data Transfer Objects (DTOs)
- **`ClaimRequest`**:
  - `productId` (`UUID`, `@NotNull(message = "Product ID is required")`)
  - `claimReason` (`String`, `@NotBlank(message = "Claim reason is required")`, max 255 chars)
  - `description` (`String`, max 2000 chars)
- **`ClaimResponse`**:
  - `id`, `productId`, `productName`, `brand`, `modelNumber`, `userId`
  - `claimReason`, `description`, `status`, `claimDate`, `createdAt`, `updatedAt`

---

## 4. REST Endpoints Specification

| Method | Endpoint | Description | Auth Required | Status Codes |
|---|---|---|---|---|
| `POST` | `/api/claims` | Submit a new warranty claim | Yes (`CUSTOMER`, `ADMIN`) | `201 Created`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found` |
| `GET` | `/api/claims` | List authenticated user's claims | Yes (`CUSTOMER`, `ADMIN`) | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/claims/{id}` | Get claim details by ID | Yes (`CUSTOMER`, `ADMIN`) | `200 OK`, `401 Unauthorized`, `404 Not Found` |
| `PATCH` | `/api/claims/{id}/cancel` | Cancel an eligible `PENDING` claim | Yes (`CUSTOMER`, `ADMIN`) | `200 OK`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found` |
| `GET` | `/api/products/{productId}/claims` | List claims filed against a specific product | Yes (`CUSTOMER`, `ADMIN`) | `200 OK`, `401 Unauthorized`, `404 Not Found` |

---

## 5. Security & Business Rules Enforcement
1. **Initial Status**: Whenever a claim is created, its status is strictly forced to `PENDING` on the server.
2. **Product Ownership**: Customers cannot file or query claims for products they do not own. A non-existent or unowned `productId` yields `404 Not Found`.
3. **Claim Ownership**: Customers cannot view or cancel claims belonging to other users. Attempting to access an unowned claim returns `404 Not Found` (anti-IDOR).
4. **Warranty Status Eligibility**: Claims can only be filed if the associated product warranty status is `ACTIVE` or `EXPIRING_SOON`. If the warranty is `EXPIRED`, the submission is rejected with `HTTP 400 Bad Request` (`"Warranty has expired. A new claim cannot be submitted."`).
5. **Cancellation Eligibility**: Customers can only cancel claims if the claim status is currently `PENDING`. Attempting to cancel an already processed or cancelled claim yields `HTTP 400 Bad Request` (`"Only claims in PENDING status can be cancelled."`).

---

## 6. Automated Testing Verification
Automated test coverage (`ClaimServiceTest` and `ClaimControllerTest`) verifies:
- Creation of valid claims with `PENDING` status.
- Rejection of claim submissions when warranty is `EXPIRED`.
- Rejection of claim submissions when product is not owned by user (404).
- Retrieval of customer claims and single claim details.
- Anti-IDOR protection when fetching or cancelling claims.
- Successful cancellation of `PENDING` claims to `CANCELLED`.
- Rejection of cancellation for non-pending claims (400).
- Retrieval of claims scoped to a specific product.
