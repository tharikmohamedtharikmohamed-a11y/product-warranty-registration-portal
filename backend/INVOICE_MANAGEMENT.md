# WarrantyHub — Backend Invoice Management

## 📌 Module Overview

The **Invoice Management Module** provides secure file ingestion, metadata persistence, customer-isolated storage paths, download streaming, browser previewing, and document deletion for purchase proof invoices in **WarrantyHub**.

Binary files reside exclusively in a private **Supabase Storage** bucket (`invoices`), while document metadata is preserved in PostgreSQL (`invoices` table). Direct frontend access to Supabase credentials or storage buckets is completely eliminated.

---

## 🗄️ Database Mapping & JPA Entity

### 1. Invoices Table Schema (`invoices`)

| Column Name | Type | Modifiers / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY DEFAULT gen_random_uuid()` | Unique invoice record identifier. |
| `user_id` | `UUID` | `NOT NULL, FK -> users(id) ON DELETE RESTRICT` | Customer owner of invoice. |
| `product_id` | `UUID` | `NOT NULL, FK -> products(id) ON DELETE RESTRICT` | Associated registered product. |
| `file_name` | `VARCHAR(255)` | `NOT NULL` | Original sanitized file name. |
| `storage_path` | `VARCHAR(500)` | `NOT NULL UNIQUE` | Private hierarchical object key in Supabase Storage. |
| `file_type` | `VARCHAR(100)` | `NOT NULL, CHECK (file_type IN ('application/pdf', 'image/jpeg', 'image/jpg', 'image/png'))` | MIME content type. |
| `file_size` | `BIGINT` | `NOT NULL, CHECK (file_size > 0 AND file_size <= 10485760)` | File payload size in bytes (max 10 MB). |
| `uploaded_at` | `TIMESTAMPTZ` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Ingestion timestamp. |

### 2. JPA Entity Structure (`Invoice.java`)
```java
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "storage_path", nullable = false, unique = true, length = 500)
    private String storagePath;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;
}
```

---

## ☁️ Storage Architecture & Security

```
Frontend (React Client)
       │ (multipart/form-data with Bearer JWT)
       ▼
InvoiceController (Spring Boot)
       │ (Derives currentUser from @AuthenticationPrincipal)
       ▼
InvoiceService (Business Validation & Ownership Verification)
       │
       ├───► SupabaseStorageService ───► Supabase Storage REST API (private 'invoices' bucket)
       │                                 (Protected via backend SUPABASE_SERVICE_ROLE_KEY)
       │
       └───► InvoiceRepository ────────► PostgreSQL ('invoices' metadata table)
```

### Security & Isolation Guarantees:
1. **Zero Secret Leakage:** Supabase `service_role` key and storage URLs are never exposed to React or the browser.
2. **Customer Scoping:** All operations verify ownership against `currentUser.getId()`. Unowned invoices or products return `404 Not Found` to prevent IDOR scanning.
3. **Unguessable Storage Paths:** Storage paths follow `invoices/{userId}/{productId}/{uuid}_{sanitizedFileName}`.
4. **Path Traversal Protection:** Input file names are sanitized to strip `../`, absolute slashes, and non-whitelisted characters.

---

## 🔄 Flows & Transaction Safety

### 1. Upload Flow (`POST /api/invoices/upload`)
1. Authenticate customer via JWT.
2. Verify target product exists and belongs to `currentUser` (throws `404` otherwise).
3. Validate uploaded file:
   - Check not empty (throws `InvalidFileException: "Invoice file is empty."`).
   - Check size <= 10 MB (throws `InvalidFileException: "Maximum file size is 10 MB."`).
   - Check extension and MIME type against allowed list (`PDF`, `JPG`, `JPEG`, `PNG`).
4. Generate UUID and construct hierarchical storage path.
5. Upload binary payload to private Supabase Storage bucket via `SupabaseStorageService`.
6. Persist invoice metadata in PostgreSQL `invoices` table.
7. **Compensating Rollback:** If PostgreSQL save fails, an automatic compensating delete is executed against Supabase Storage to prevent orphaned objects.

### 2. Download / View Flow
- `GET /api/invoices/{id}/download`: Streams file with `Content-Disposition: attachment; filename="{fileName}"`.
- `GET /api/invoices/{id}/view`: Streams file with `Content-Disposition: inline; filename="{fileName}"` for browser rendering.
- Both endpoints enforce customer ownership.

### 3. Deletion Flow (`DELETE /api/invoices/{id}`)
1. Verify customer ownership of the invoice.
2. Delete binary object from Supabase Storage via `SupabaseStorageService`.
3. Remove invoice metadata from PostgreSQL table `invoices`.
4. Returns `204 No Content`.

---

## 📡 REST API Specification

| Method | Endpoint | Auth | Request Body / Params | Status Codes | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/invoices/upload` | Bearer JWT | `file` (MultipartFile), `productId` (UUID) | 201, 400, 401, 404, 500 | Uploads proof-of-purchase invoice. |
| `GET` | `/api/invoices` | Bearer JWT | *None* | 200, 401 | Retrieves all invoices belonging to current customer. |
| `GET` | `/api/invoices/{id}` | Bearer JWT | *None* | 200, 401, 404 | Retrieves single invoice metadata record. |
| `GET` | `/api/invoices/{id}/download` | Bearer JWT | *None* | 200, 401, 404 | Downloads binary invoice as attachment. |
| `GET` | `/api/invoices/{id}/view` | Bearer JWT | *None* | 200, 401, 404 | Previews invoice inline in browser. |
| `DELETE` | `/api/invoices/{id}` | Bearer JWT | *None* | 204, 401, 404 | Deletes invoice from storage and database. |
| `GET` | `/api/products/{productId}/invoice` | Bearer JWT | *None* | 200, 401, 404 | Retrieves invoice for a specific product. |

---

## 🧪 Automated Testing

Automated test suites verify all validation rules, security isolations, and error conditions without dependency on live Supabase storage:
- `InvoiceServiceTest`: 18 isolated unit tests covering PDF/JPG/JPEG/PNG uploads, file limits, empty files, ownership enforcement, IDOR rejection (404), compensating storage rollbacks, and filename sanitization.
- `InvoiceControllerTest`: 11 MockMvc slice tests covering HTTP endpoints, security authentication entry points (401), content disposition headers, and status codes.
