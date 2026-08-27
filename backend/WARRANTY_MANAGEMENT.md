# WarrantyHub — Backend Warranty Management

## 📌 Module Overview

The **Warranty Management Module** provides comprehensive tracking, automated lifecycle state evaluation, remaining protection countdowns, elapsed duration progress calculations, and automatic database status synchronization for all product warranties in **WarrantyHub**.

Each warranty is bound 1-to-1 to a customer's product (`warranties.product_id UNIQUE`) and belongs to the customer through that product (`Product -> User`). Customer isolation is strictly enforced across all repository queries and REST controllers.

---

## 🗄️ Database Mapping & JPA Entity

### 1. Warranties Table Schema (`warranties`)
The `Warranty` entity maps directly to the authoritative Phase 2 relational schema:

| Column Name | Type | Modifiers / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY DEFAULT gen_random_uuid()` | Unique warranty record identifier. |
| `product_id` | `UUID` | `NOT NULL UNIQUE, FK -> products(id) ON DELETE CASCADE` | 1-to-1 foreign key reference to registered product. |
| `start_date` | `DATE` | `NOT NULL` | Warranty commencement date (matches purchase date). |
| `expiry_date` | `DATE` | `NOT NULL, CHECK (expiry_date >= start_date)` | Calculated expiration date (`start_date + warranty_duration_months`). |
| `status` | `VARCHAR(50)` | `NOT NULL, CHECK (status IN ('ACTIVE', 'EXPIRING_SOON', 'EXPIRED'))` | Lifecycle status indicator. |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Initial provisioning audit timestamp. |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Last updated timestamp (via DB trigger). |

### 2. JPA Entity Structure (`Warranty.java`)
```java
@Entity
@Table(name = "warranties")
public class Warranty {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WarrantyStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
```

---

## ⏱️ Warranty Status, Days Remaining & Progress Logic

### 1. Authoritative Status Rules
The backend calculates the warranty status dynamically from `expiryDate` and the current date (`today`):

| Status | Condition | Description |
| :--- | :--- | :--- |
| `ACTIVE` | `!today.isAfter(expiryDate) && daysRemaining > 30` | Normal active coverage with more than 30 days remaining. |
| `EXPIRING_SOON` | `!today.isAfter(expiryDate) && daysRemaining <= 30` | Imminent expiration; proactive alert threshold. |
| `EXPIRED` | `today.isAfter(expiryDate)` | Warranty coverage has concluded. |

### 2. Days Remaining Calculation
```java
public static long calculateDaysRemaining(LocalDate expiryDate, LocalDate today) {
    if (expiryDate == null || today == null || today.isAfter(expiryDate)) {
        return 0;
    }
    return Math.max(0, ChronoUnit.DAYS.between(today, expiryDate));
}
```
- Active warranty: returns positive remaining days.
- Expired warranty: returns `0` (never negative).

### 3. Elapsed Progress Percentage
Calculates the exact percentage of total warranty duration that has elapsed, bounded between `0%` and `100%`:
```java
public static int calculateProgressPercentage(LocalDate startDate, LocalDate expiryDate, LocalDate today) {
    if (startDate == null || expiryDate == null || today == null) return 0;
    if (today.isAfter(expiryDate) || today.isEqual(expiryDate)) return 100;
    if (today.isBefore(startDate) || today.isEqual(startDate)) return 0;
    long totalDays = ChronoUnit.DAYS.between(startDate, expiryDate);
    if (totalDays <= 0) return 100;
    long elapsedDays = ChronoUnit.DAYS.between(startDate, today);
    return Math.min(100, Math.max(0, (int) Math.round(((double) elapsedDays / totalDays) * 100)));
}
```

### 4. Automatic Database Synchronization on Read
When any warranty query is executed (`getWarrantiesForUser`, `getWarrantyByIdForUser`, `getWarrantyByProductIdForUser`):
1. The service calculates the current status against `LocalDate.now()`.
2. If `warranty.getStatus() != calculatedStatus`, the entity's status is updated in the database:
   ```java
   warranty.setStatus(calculatedStatus);
   warranty = warrantyRepository.save(warranty);
   ```
3. Returned DTO contains synchronized status and current metrics. No background job or cron required.

---

## 🔒 Customer Ownership & Security Enforcement

Ownership is verified through the object graph:
$$\text{User} \longrightarrow \text{Product} \longrightarrow \text{Warranty}$$

1. **No Trusted Client User IDs:** Endpoints extract user ID strictly from `@AuthenticationPrincipal User currentUser`.
2. **Customer-Scoped Queries:** All queries join with `product` and filter by `product.user.id = :userId`:
   - `findAllByProductUserId(UUID userId)`
   - `findByIdAndProductUserId(UUID id, UUID userId)`
   - `findByProductIdAndProductUserId(UUID productId, UUID userId)`
3. **Data Isolation (HTTP 404):** If Customer A requests Customer B's warranty or product ID, the backend returns **HTTP 404 Not Found**, preventing cross-customer enumeration and information leaks.

---

## 📡 REST API Specifications

### 1. Get Customer's Warranties
- **Endpoint:** `GET /api/warranties`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Response (`200 OK`):**
  ```json
  [
    {
      "id": "w1w2w3w4-e5f6-7890-abcd-ef1234567890",
      "productId": "p1p2p3p4-e5f6-7890-abcd-ef1234567890",
      "productName": "Sony Bravia 65 4K OLED",
      "brand": "Sony",
      "modelNumber": "XR-65A80L",
      "startDate": "2026-08-01",
      "expiryDate": "2028-08-01",
      "warrantyDurationMonths": 24,
      "status": "ACTIVE",
      "daysRemaining": 680,
      "progressPercentage": 7,
      "createdAt": "2026-09-20T21:55:00Z",
      "updatedAt": "2026-09-20T21:55:00Z"
    }
  ]
  ```

### 2. Get Warranty by ID
- **Endpoint:** `GET /api/warranties/{id}`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Response (`200 OK`):** Single `WarrantyResponse` object. Returns `404 Not Found` if unowned.

### 3. Get Warranty by Product ID
- **Endpoint:** `GET /api/products/{productId}/warranty`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Response (`200 OK`):** Single `WarrantyResponse` object. Returns `404 Not Found` if product unowned or missing.

---

## 🧪 Testing Verification

All warranty calculations and endpoints are verified via JUnit 5 and Spring Security slice tests:
- **`WarrantyServiceTest` (11 unit tests):**
  1. Customer warranty list retrieval and DTO mapping
  2. Warranty lookup by ID
  3. 404 rejection on unowned warranty ID
  4. Warranty lookup by product ID
  5. 404 rejection on unowned product ID
  6. Status calculation: `ACTIVE` (> 30 days remaining)
  7. Status calculation: `EXPIRING_SOON` (<= 30 days remaining)
  8. Status calculation: `EXPIRED` (past expiration date)
  9. Days remaining: bounded `>= 0`
  10. Progress percentage: scaled `0%` to `100%`
  11. Automatic status persistence sync on read
- **`WarrantyControllerTest` (6 MockMvc slice tests):**
  1. `GET /api/warranties` 401 unauthenticated
  2. `GET /api/warranties` 200 OK list
  3. `GET /api/warranties/{id}` 200 OK
  4. `GET /api/warranties/{id}` 404 Not Found
  5. `GET /api/products/{productId}/warranty` 200 OK
  6. `GET /api/products/{productId}/warranty` 404 Not Found
- **Total Backend Test Suite:** 53/53 passing tests.
