# WarrantyHub — Database Design & Storage Specification
**Document Version:** 1.0.0  
**Phase:** Phase 2 — Database Design & Supabase Setup  
**Application:** Product Warranty Registration Portal  
**Target Engine:** PostgreSQL 15+ / Supabase  
**Backend Framework:** Java 17 / Spring Boot 3.2.x (Spring Data JPA)  

---

## 1. Database Overview

The WarrantyHub relational schema provides an ACID-compliant, highly indexed, normalized persistence layer designed to support consumer product warranty tracking, invoice archiving, and warranty claim management.

The database is deployed on **PostgreSQL 17** via **Supabase**, with the application architecture adhering to a strict three-tier model:
```
React Frontend  ──(REST/JSON)──►  Spring Boot 3.2 Backend  ──(JDBC/PostgreSQL)──►  Supabase Database
                                          │
                                          └──(Supabase Storage API)────────────►  Supabase 'invoices' Bucket
```

Direct client-side database connections from the browser are disallowed. Spring Boot mediates all data access and authorization rules.

---

## 2. Relational Architecture & Entity Diagrams

### 2.1 Text / ASCII Entity Relationship Diagram

```
users
  |
  +---- products (user_id -> users.id [RESTRICT])
  |        |
  |        +---- warranties (product_id -> products.id [CASCADE])
  |
  +---- invoices (user_id -> users.id [RESTRICT])
  |        |
  |        +---- products (product_id -> products.id [RESTRICT])
  |
  +---- claims (user_id -> users.id [RESTRICT])
           |
           +---- products (product_id -> products.id [RESTRICT])
```

### 2.2 Relational Model & Cardinality Details

```
+-------------------------------------------------------------------------+
|                                  USERS                                  |
+-------------------------------------------------------------------------+
| PK  id          UUID (gen_random_uuid())                                |
|     email       VARCHAR(255) UNIQUE                                     |
|     password    VARCHAR(255) [BCrypt Hash]                              |
|     name        VARCHAR(255)                                            |
|     role        VARCHAR(50) ['CUSTOMER', 'ADMIN']                       |
|     created_at  TIMESTAMPTZ                                             |
|     updated_at  TIMESTAMPTZ                                             |
+-------------------+-----------------+-----------------+-----------------+
                    | 1               | 1               | 1
                    |                 |                 |
                    | 0..*            | 0..*            | 0..*
                    v                 v                 v
+-----------------------+     +-----------------+     +-------------------+
|       PRODUCTS        |     |    INVOICES     |     |      CLAIMS       |
+-----------------------+     +-----------------+     +-------------------+
| PK  id          UUID  |     | PK  id    UUID  |     | PK  id      UUID  |
| FK  user_id     UUID  |     | FK  user  UUID  |     | FK  user    UUID  |
|     product_nam ...   |     | FK  prod  UUID  |     | FK  prod    UUID  |
|     category    ...   |     |     file_name   |     |     issue_desc  |
|     brand       ...   |     |     storage_p UK|     |     additional  |
|     model_num   ...   |     |     file_type   |     |     status      |
|     serial_num  ...   |     |     file_size   |     |     created_at  |
|     purchase_dt ...   |     |     uploaded_at |     |     updated_at  |
|     seller_name ...   |     +--------+--------+     +---------+---------+
|     price       ...   |              |                        |
|     warranty_mo ...   |              |                        |
|     description ...   |              |                        |
|     created_at  ...   |              |                        |
|     updated_at  ...   |              |                        |
+-----------+-----------+              |                        |
            | 1                        |                        |
            |                          | 0..*                   | 0..*
            v 1                        v                        v
+-----------------------+              +------------------------+
|      WARRANTIES       |              | References products.id |
+-----------------------+              | via RESTRICT           |
| PK  id          UUID  |              +------------------------+
| FK  product_id  UUID  |
|     start_date  DATE  |
|     expiry_date DATE  |
|     status      ...   |
|     created_at  ...   |
|     updated_at  ...   |
+-----------------------+
```

---

## 3. Database Tables & Column Specifications

### 3.1 `users` Table
Stores user credentials, profile information, and authorization roles.

| Column Name | Data Type | Nullable | Default Value | Constraints & Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | **NO** | `gen_random_uuid()` | Primary Key |
| `name` | `VARCHAR(255)` | **NO** | *None* | Full legal display name |
| `email` | `VARCHAR(255)` | **NO** | *None* | Unique email address (login credential) |
| `password` | `VARCHAR(255)` | **NO** | *None* | BCrypt password hash (never plaintext) |
| `role` | `VARCHAR(50)` | **NO** | `'CUSTOMER'` | `CHECK (role IN ('CUSTOMER', 'ADMIN'))` |
| `created_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Account creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Last profile update timestamp |

### 3.2 `products` Table
Stores purchased products registered by customers.

| Column Name | Data Type | Nullable | Default Value | Constraints & Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | **NO** | `gen_random_uuid()` | Primary Key |
| `user_id` | `UUID` | **NO** | *None* | FK $\rightarrow$ `users(id)` ON DELETE RESTRICT |
| `product_name` | `VARCHAR(255)` | **NO** | *None* | User-assigned or official product title |
| `category` | `VARCHAR(100)` | **NO** | *None* | Category classification (Electronics, Appliances, etc.) |
| `brand` | `VARCHAR(100)` | **NO** | *None* | Manufacturer brand name |
| `model_number` | `VARCHAR(100)` | **NO** | *None* | Manufacturer model code |
| `serial_number` | `VARCHAR(150)` | **NO** | *None* | Physical device serial number |
| `purchase_date` | `DATE` | **NO** | *None* | Invoice purchase date |
| `seller_name` | `VARCHAR(255)` | **NO** | *None* | Retail vendor or online marketplace name |
| `price` | `NUMERIC(12, 2)` | **NO** | *None* | `CHECK (price >= 0)` |
| `warranty_duration_months` | `INTEGER` | **NO** | *None* | `CHECK (warranty_duration_months > 0)` |
| `description` | `TEXT` | YES | `NULL` | Optional item notes or specifications |
| `created_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Registration timestamp |
| `updated_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Last modification timestamp |

### 3.3 `warranties` Table
Stores 1-to-1 calculated warranty tracking records for registered products.

| Column Name | Data Type | Nullable | Default Value | Constraints & Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | **NO** | `gen_random_uuid()` | Primary Key |
| `product_id` | `UUID` | **NO** | *None* | UNIQUE, FK $\rightarrow$ `products(id)` ON DELETE CASCADE |
| `start_date` | `DATE` | **NO** | *None* | Coverage effective date (`purchase_date`) |
| `expiry_date` | `DATE` | **NO** | *None* | `CHECK (expiry_date >= start_date)` |
| `status` | `VARCHAR(50)` | **NO** | *None* | `CHECK (status IN ('ACTIVE', 'EXPIRING_SOON', 'EXPIRED'))` |
| `created_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Last status update timestamp |

### 3.4 `invoices` Table
Stores metadata for uploaded purchase invoices. Binary objects reside in Supabase Storage.

| Column Name | Data Type | Nullable | Default Value | Constraints & Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | **NO** | `gen_random_uuid()` | Primary Key |
| `user_id` | `UUID` | **NO** | *None* | FK $\rightarrow$ `users(id)` ON DELETE RESTRICT |
| `product_id` | `UUID` | **NO** | *None* | FK $\rightarrow$ `products(id)` ON DELETE RESTRICT |
| `file_name` | `VARCHAR(255)` | **NO** | *None* | Original sanitized filename |
| `storage_path` | `VARCHAR(500)` | **NO** | *None* | UNIQUE cloud path in Supabase Storage |
| `file_type` | `VARCHAR(100)` | **NO** | *None* | `CHECK (file_type IN ('application/pdf', 'image/jpeg', 'image/jpg', 'image/png'))` |
| `file_size` | `BIGINT` | **NO** | *None* | `CHECK (file_size > 0 AND file_size <= 10485760)` (Max 10 MB) |
| `uploaded_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Upload timestamp |

### 3.5 `claims` Table
Stores warranty claim requests initiated by customers.

| Column Name | Data Type | Nullable | Default Value | Constraints & Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | **NO** | `gen_random_uuid()` | Primary Key |
| `user_id` | `UUID` | **NO** | *None* | FK $\rightarrow$ `users(id)` ON DELETE RESTRICT |
| `product_id` | `UUID` | **NO** | *None* | FK $\rightarrow$ `products(id)` ON DELETE RESTRICT |
| `issue_description` | `TEXT` | **NO** | *None* | Detailed description of device malfunction |
| `additional_information` | `TEXT` | YES | `NULL` | Supplemental troubleshooting notes or seller notes |
| `status` | `VARCHAR(50)` | **NO** | `'PENDING'` | `CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))` |
| `created_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Claim submission timestamp |
| `updated_at` | `TIMESTAMPTZ` | **NO** | `CURRENT_TIMESTAMP` | Status transition timestamp |

---

## 4. Foreign Key Constraints & Safe Deletion Rules

| Constraint Name | Source Column | Target Column | On Delete Action | Design Rationale |
| :--- | :--- | :--- | :--- | :--- |
| `fk_products_user` | `products.user_id` | `users.id` | **RESTRICT** | Prevents accidental deletion of customer accounts while registered products exist. Account deletion requires deliberate data cleanup. |
| `fk_warranties_product` | `warranties.product_id` | `products.id` | **CASCADE** | Warranty is a 1-to-1 derived attribute of a product. If a product is legitimately removed (and has no dependent invoices/claims), its warranty record is automatically purged. |
| `fk_invoices_user` | `invoices.user_id` | `users.id` | **RESTRICT** | Protects audit records and uploaded invoice assets from accidental loss. |
| `fk_invoices_product` | `invoices.product_id` | `products.id` | **RESTRICT** | Prevents deleting a product while physical proof-of-purchase files exist in Supabase Storage. Storage files must be deleted first. |
| `fk_claims_user` | `claims.user_id` | `users.id` | **RESTRICT** | Prevents deletion of users with claim history. |
| `fk_claims_product` | `claims.product_id` | `products.id` | **RESTRICT** | Strict audit preservation: a product that has been subject to formal warranty claims cannot be deleted, preserving resolution history. |

---

## 5. Indexing Architecture & Query Optimization

| Table | Index Name | Columns Indexed | Type | Purpose / Query Optimized |
| :--- | :--- | :--- | :--- | :--- |
| `users` | `users_pkey` | `(id)` | BTREE (UNIQUE) | Primary key lookup |
| `users` | `users_email_key` | `(email)` | BTREE (UNIQUE) | Login credential lookup (`findByEmail`) |
| `users` | `idx_users_email` | `(email)` | BTREE | Explicit query index on email |
| `products` | `products_pkey` | `(id)` | BTREE (UNIQUE) | Primary key lookup |
| `products` | `idx_products_user_id` | `(user_id)` | BTREE | Fast retrieval of user's registered products |
| `products` | `idx_products_serial_number` | `(serial_number)` | BTREE | Audit/search by device serial number |
| `products` | `idx_products_category` | `(category)` | BTREE | Product category filtering |
| `products` | `idx_products_brand` | `(brand)` | BTREE | Brand-based queries and analytics |
| `warranties` | `warranties_pkey` | `(id)` | BTREE (UNIQUE) | Primary key lookup |
| `warranties` | `warranties_product_id_key` | `(product_id)` | BTREE (UNIQUE) | Enforces 1-to-1 relationship with product |
| `warranties` | `idx_warranties_product_id` | `(product_id)` | BTREE | Direct lookup by product ID |
| `warranties` | `idx_warranties_status` | `(status)` | BTREE | Dashboard metrics filtering (`ACTIVE`, `EXPIRING_SOON`) |
| `warranties` | `idx_warranties_expiry_date` | `(expiry_date)` | BTREE | Daily cron job and expiry range queries |
| `invoices` | `invoices_pkey` | `(id)` | BTREE (UNIQUE) | Primary key lookup |
| `invoices` | `invoices_storage_path_key` | `(storage_path)` | BTREE (UNIQUE) | Guarantees uniqueness of cloud storage object paths |
| `invoices` | `idx_invoices_user_id` | `(user_id)` | BTREE | Fetch all invoices owned by customer |
| `invoices` | `idx_invoices_product_id` | `(product_id)` | BTREE | Fetch all invoices attached to a product |
| `invoices` | `idx_invoices_storage_path` | `(storage_path)` | BTREE | Rapid lookup by storage reference |
| `claims` | `claims_pkey` | `(id)` | BTREE (UNIQUE) | Primary key lookup |
| `claims` | `idx_claims_user_id` | `(user_id)` | BTREE | Fetch claims submitted by a specific user |
| `claims` | `idx_claims_product_id` | `(product_id)` | BTREE | Fetch claim history for a product |
| `claims` | `idx_claims_status` | `(status)` | BTREE | Admin adjudication queue filtering (`PENDING`) |

---

## 6. Business Logic: Warranty Status Model

### 6.1 Status Values
- **`ACTIVE`**: Product is within the warranty coverage window and more than 30 days remain before expiry.
- **`EXPIRING_SOON`**: Product coverage expires within 30 days ($0 \le \text{days\_remaining} \le 30$).
- **`EXPIRED`**: The current date has exceeded the calculated `expiry_date`.

### 6.2 Date Computation
Warranty calculation is executed deterministically by Spring Boot upon product creation:
$$\text{start\_date} = \text{purchase\_date}$$
$$\text{expiry\_date} = \text{purchase\_date} + \text{warranty\_duration\_months}$$

A scheduled background process synchronizes database status flags daily at midnight (`0 0 0 * * *`), with real-time dynamic calculation also evaluated during API queries.

---

## 7. Business Logic: Warranty Claim State Machine

Claims follow a controlled lifecycle governed by backend business rules:

```
[Customer Submits] ──► PENDING ──────┬──(Customer Cancels)──► CANCELLED
                         │           │
                         │           └──(Admin Rejects)─────► REJECTED
                         │
                   (Admin Approves)
                         │
                         ▼
                      APPROVED
                         │
                    (Dispatched)
                         │
                         ▼
                    IN_PROGRESS
                         │
                    (Resolved)
                         │
                         ▼
                     COMPLETED
```

### 7.1 Status Definitions
| Status | Allowed Transitions | Transition Initiator | Business Rules |
| :--- | :--- | :--- | :--- |
| `PENDING` | `APPROVED`, `REJECTED`, `CANCELLED` | Admin (Approve/Reject), Customer (Cancel) | Initial default state upon submission. |
| `CANCELLED` | *None* (Terminal) | Customer | Only allowed while claim is in `PENDING` status. |
| `REJECTED` | *None* (Terminal) | Admin | Mandatory reason must be provided in administrative notes. |
| `APPROVED` | `IN_PROGRESS` | Admin | Validates active warranty coverage and invoice proof. |
| `IN_PROGRESS` | `COMPLETED` | Admin | Repair or replacement fulfillment is underway. |
| `COMPLETED` | *None* (Terminal) | Admin | Claim successfully resolved and closed. |

---

## 8. Invoice Storage Design & Supabase Storage Integration

### 8.1 Supabase Storage Bucket Configuration
- **Bucket ID:** `invoices`
- **Bucket Name:** `invoices`
- **Visibility:** **Private** (`public = false`)
- **File Size Limit:** $10\text{ MB} = 10,485,760\text{ bytes}$
- **Permitted MIME Types:**
  - `application/pdf`
  - `image/jpeg`
  - `image/png`

### 8.2 Object Key / Path Convention
Files are organized hierarchically to enforce user isolation and facilitate bulk cleanup:
```
invoices/{userId}/{productId}/{uuid}_{sanitizedFileName}
```

**Concrete Example:**
```
invoices/a4f3c7e0-1234-4b5a-9876-000000000001/f9b8c7d6-5432-4a1b-8765-000000000002/9e2d3c4b-a108-41f2-8921-123456789abc_BestBuy_Receipt.pdf
```

### 8.3 Storage Ingestion & Retrieval Flow
1. **Upload:**
   - Frontend sends `multipart/form-data` to Spring Boot endpoint `POST /api/invoices/upload`.
   - Spring Boot validates file type, scans for security threats, generates a random UUID prefix, and uploads binary to Supabase Storage via authenticated backend client.
   - Spring Boot persists the metadata in PostgreSQL `invoices` table.
2. **Download / Preview:**
   - Customer requests `GET /api/invoices/{id}/download`.
   - Spring Boot verifies resource ownership (`invoice.user_id == current_user.id`).
   - Backend streams file or produces a short-lived (e.g., 60-second) signed URL generated with the backend's Supabase service credentials.

---

## 9. Security Architecture & Threat Defense

### 9.1 Row Level Security (RLS)
Row Level Security is enabled on all 5 tables (`users`, `products`, `warranties`, `invoices`, `claims`).
- By default, PostgREST requests using the public `anon` or standard `authenticated` Supabase roles cannot read, insert, update, or delete any records.
- The Spring Boot application connects directly over standard PostgreSQL JDBC using the `postgres` administrative role, which possesses `BYPASSRLS` privileges.
- This creates an effective defense-in-depth: even if the public anon key is exposed in client-side code, direct database tampering via PostgREST is completely prevented.

### 9.2 IDOR (Insecure Direct Object Reference) Prevention
All Spring Boot service implementations must enforce ownership verification at the application layer:
```java
// Spring Boot Service Layer Pattern:
public ProductResponse getProductById(UUID productId, UUID currentUserId, boolean isAdmin) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    if (!product.getUserId().equals(currentUserId) && !isAdmin) {
        throw new AccessDeniedException("Unauthorized access to product record");
    }
    return mapToResponse(product);
}
```

### 9.3 Credentials Isolation
- Supabase database connection strings, passwords, and service role keys are managed exclusively via environment variables (`.env`).
- No secrets or credentials are hardcoded in schema files, properties templates, or Git tracking.
