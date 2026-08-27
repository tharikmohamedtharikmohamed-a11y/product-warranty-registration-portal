# WarrantyHub — Backend Product Management

## 📌 Module Overview

The **Product Management Module** enables authenticated customers to register, inspect, update, and remove their purchased products within **WarrantyHub**. Each registered product is strictly bound to the authenticated customer's account (`user_id`), and product registration automatically generates a corresponding 1-to-1 active warranty record within the PostgreSQL database.

---

## 🗄️ Database Mapping & JPA Entity

### 1. Products Table Schema (`products`)
The `Product` JPA entity strictly maps to the authoritative Phase 2 relational schema:

| Column Name | Type | Modifiers / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY DEFAULT gen_random_uuid()` | Unique product identifier. |
| `user_id` | `UUID` | `NOT NULL, FK -> users(id) ON DELETE RESTRICT` | Foreign key referencing the product owner. |
| `product_name` | `VARCHAR(255)` | `NOT NULL, CHECK (LENGTH(TRIM(product_name)) > 0)` | Name/model title of the product. |
| `category` | `VARCHAR(100)` | `NOT NULL` | Product category (e.g., Electronics, Audio). |
| `brand` | `VARCHAR(100)` | `NOT NULL` | Brand / manufacturer name. |
| `model_number` | `VARCHAR(100)` | `NOT NULL` | Manufacturer model code/identifier. |
| `serial_number` | `VARCHAR(150)` | `NOT NULL, CHECK (LENGTH(TRIM(serial_number)) > 0)` | Unique hardware serial number. |
| `purchase_date` | `DATE` | `NOT NULL` | Official date of product purchase. |
| `seller_name` | `VARCHAR(255)` | `NOT NULL` | Retailer or merchant name. |
| `price` | `NUMERIC(12, 2)` | `NOT NULL, CHECK (price >= 0)` | Purchase price in USD. |
| `warranty_duration_months` | `INTEGER` | `NOT NULL, CHECK (warranty_duration_months > 0)` | Total warranty coverage duration in months. |
| `description` | `TEXT` | `NULLABLE` | Optional customer notes or accessory list. |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp of initial registration. |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp of last update (via DB trigger). |

### 2. JPA Entity Structure (`Product.java`)
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model_number", nullable = false, length = 100)
    private String modelNumber;

    @Column(name = "serial_number", nullable = false, length = 150)
    private String serialNumber;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "warranty_duration_months", nullable = false)
    private Integer warrantyDurationMonths;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Warranty warranty;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
```

---

## 🔒 Customer Ownership & Security Enforcement

1. **Security Context Resolution:** The backend never accepts `userId` from incoming request bodies. The owner identity is obtained directly from Spring Security's authenticated `SecurityContext` via `@AuthenticationPrincipal User currentUser`.
2. **Repository-Level Filtering:** All queries restrict results by the customer's UUID:
   - `findAllByUserIdWithWarrantyOrderByCreatedAtDesc(UUID userId)`
   - `findByIdAndUserIdWithWarranty(UUID id, UUID userId)`
   - `findByIdAndUserId(UUID id, UUID userId)`
3. **Cross-Customer Leak Prevention:** If Customer A attempts to access or modify a product belonging to Customer B (e.g. `GET /api/products/{otherUserId}`), the backend immediately throws a `ResourceNotFoundException` returning **HTTP 404 Not Found**. This prevents attackers from enumerating or discovering product IDs registered by other customers.
4. **Duplicate Serial Number Prevention:** The repository checks for duplicate serial numbers scoped per customer:
   - On Create: `existsBySerialNumberIgnoreCaseAndUserId(serialNumber, userId)`
   - On Update: `existsBySerialNumberIgnoreCaseAndUserIdAndIdNot(serialNumber, userId, id)`
   - Throws `DuplicateSerialNumberException` returning **HTTP 409 Conflict** if violated.

---

## ⚙️ Automatic Warranty Provisioning & Synchronization

### 1. Creation Workflow (`POST /api/products`)
When a customer registers a product:
1. `startDate` is initialized to the `purchaseDate`.
2. `expiryDate` is calculated as `purchaseDate.plusMonths(warrantyDurationMonths)`.
3. Initial `status` is set to `ACTIVE` (or `EXPIRING_SOON`/`EXPIRED` based on calendar comparison).
4. Both records are persisted transactionally (`@Transactional`) ensuring atomic creation.

### 2. Update Workflow (`PUT /api/products/{id}`)
If the customer modifies `purchaseDate` or `warrantyDurationMonths`:
1. The service detects date changes.
2. The associated `Warranty` record is retrieved.
3. `startDate` and `expiryDate` are recalculated:
   ```java
   warranty.setStartDate(request.getPurchaseDate());
   warranty.setExpiryDate(request.getPurchaseDate().plusMonths(request.getWarrantyDurationMonths()));
   ```
4. No secondary warranty is created; the 1-to-1 database constraint remains intact.

### 3. Deletion Workflow (`DELETE /api/products/{id}`)
When a product is deleted:
1. Ownership is verified.
2. The product is deleted via `ProductRepository`.
3. Database `ON DELETE CASCADE` on `warranties.product_id` automatically removes the linked warranty record.

---

## 📡 REST API Specifications

### 1. Register Product
- **Endpoint:** `POST /api/products`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Request Body:**
  ```json
  {
    "productName": "Galaxy S23 Ultra",
    "category": "Mobile Phone",
    "brand": "Samsung",
    "modelNumber": "SM-S918B",
    "serialNumber": "SN123456789",
    "purchaseDate": "2026-09-01",
    "sellerName": "Samsung Store",
    "price": 1199.99,
    "warrantyDurationMonths": 24,
    "description": "Primary smartphone"
  }
  ```
- **Response (`201 Created`):**
  ```json
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "userId": "98765432-abcd-ef01-2345-6789abcdef01",
    "productName": "Galaxy S23 Ultra",
    "category": "Mobile Phone",
    "brand": "Samsung",
    "modelNumber": "SM-S918B",
    "serialNumber": "SN123456789",
    "purchaseDate": "2026-09-01",
    "sellerName": "Samsung Store",
    "price": 1199.99,
    "warrantyDurationMonths": 24,
    "description": "Primary smartphone",
    "warranty": {
      "id": "w1w2w3w4-e5f6-7890-abcd-ef1234567890",
      "productId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "startDate": "2026-09-01",
      "expiryDate": "2028-09-01",
      "status": "ACTIVE",
      "daysRemaining": 711,
      "createdAt": "2026-09-20T21:55:00Z",
      "updatedAt": "2026-09-20T21:55:00Z"
    },
    "createdAt": "2026-09-20T21:55:00Z",
    "updatedAt": "2026-09-20T21:55:00Z"
  }
  ```

### 2. Get All User Products
- **Endpoint:** `GET /api/products`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Response (`200 OK`):** List of `ProductResponse` objects sorted by `createdAt DESC`. Returns `[]` if no products registered.

### 3. Get Single Product by ID
- **Endpoint:** `GET /api/products/{id}`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Response (`200 OK`):** Single `ProductResponse` object. Returns `404 Not Found` if missing or owned by another user.

### 4. Update Product
- **Endpoint:** `PUT /api/products/{id}`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Request Body:** Complete `ProductRequest` payload with updated attributes.
- **Response (`200 OK`):** Updated `ProductResponse` with recalibrated warranty.

### 5. Delete Product
- **Endpoint:** `DELETE /api/products/{id}`
- **Security:** Authenticated (`Bearer <JWT>`)
- **Response (`204 No Content`):** Empty body upon successful deletion.

---

## 🛡️ Validation & Error Handling

| Scenario | HTTP Status | Response Schema |
| :--- | :--- | :--- |
| Missing/invalid fields | `400 Bad Request` | `{"message": "Validation failed", "errors": {"field": "error message"}}` |
| Unauthenticated request | `401 Unauthorized` | `{"message": "Full authentication is required to access this resource"}` |
| Non-existent or unowned product | `404 Not Found` | `{"message": "Product not found with id: <id>"}` |
| Duplicate serial number for customer | `409 Conflict` | `{"message": "A product with serial number '<sn>' is already registered in your account."}` |
| Server-side exception | `500 Internal Server Error` | `{"message": "An unexpected internal error occurred"}` |

---

## 🧪 Testing Verification

All product management endpoints, customer isolation logic, and warranty calculations are verified via comprehensive JUnit 5 and Spring Security slice tests:
- **`ProductServiceTest`**: 9 unit tests verifying warranty creation, duplicate serial detection, user isolation, update recalculations, and deletion.
- **`ProductControllerTest`**: 9 MockMvc tests verifying 401 unauthenticated access, 201 creation, 400 validation errors, 409 conflict, 200 retrieval, 404 security denial, and 204 deletion.
- **Overall Suite**: 36/36 tests passing cleanly.
