# Product Warranty Registration Portal

## Project Overview
The **Product Warranty Registration Portal** is a centralized digital platform designed to help consumers securely register their purchased products, keep track of warranty periods, upload purchase invoices, and request warranty claims seamlessly.

---

## Problem Statement
Many customers lose physical warranty cards or forget the expiration date of their product warranties. As a result, they encounter frustration and financial loss when attempting to claim warranty services. This project eliminates paper clutter by storing digital proof of purchase and warranty terms in a secure, accessible web portal.

---

## Objectives
- Digital central repository for product warranty management.
- Real-time tracking of warranty expiration dates.
- Secure invoice image and PDF file uploads (Future Phase).
- Simple RESTful API backend built using Java & Spring Boot.
- Cloud database backend using Supabase PostgreSQL.

---

## Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.3 (REST Web API, Data JPA)
- **Build Tool**: Apache Maven
- **ORM Provider**: Hibernate
- **Database**: Supabase PostgreSQL
- **Security & Auth**: Spring Security + JWT *(Phase 2)*
- **Frontend**: Single Page Application / React / Vue *(Phase 3)*

---

## Planned Features
- **Phase 1 (Completed)**: Project Foundation, Supabase PostgreSQL configuration, REST Health Check endpoint, Exception Handling framework.
- **Phase 2 (Completed)**: Database Entities (`User`, `Product`, `Warranty`, `Invoice`, `WarrantyClaim`) & Repositories.
- **Phase 3 (Completed)**: User Authentication & Security (BCrypt, JWT, Spring Security, CUSTOMER & ADMIN roles).
- **Phase 4 (Completed)**: Product Registration & Warranty Management (CRUD APIs).
- **Phase 5 (Completed)**: Purchase Invoice Upload & Supabase Storage Integration (`product-invoices`).
- **Phase 6 (Completed)**: Warranty Claim Management & Status Tracking.

---

## Warranty Claim Management (Phase 6)

Authenticated customers can submit and track warranty claims for their registered products. The system validates product/warranty ownership, ensures the warranty has not expired (`400 Bad Request` if expired), and prevents duplicate active claims (`409 Conflict` if a `PENDING` or `IN_PROGRESS` claim already exists for the warranty).

### Claim Status Lifecycle
1. **`PENDING`**: Initial status when a customer submits a claim.
2. **`IN_PROGRESS`**: Claim is being evaluated or serviced.
3. **`APPROVED`**: Warranty claim has been approved by Admin/Staff.
4. **`REJECTED`**: Warranty claim has been rejected.
5. **`COMPLETED`**: Service or replacement has been fulfilled.
6. **`CANCELLED`**: Customer cancelled a `PENDING` claim.

---

## Warranty Claim API Endpoints

### 1. Submit Warranty Claim
**`POST /api/claims`**  
*Header required*: `Authorization: Bearer <token>`

**Request Payload**:
```json
{
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "warrantyId": "f50e8400-e29b-41d4-a716-446655440000",
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "issueDescription": "Screen flickering and touch unresponsive"
}
```

**Success Response (`201 Created`)**:
```json
{
  "claimId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "productName": "iPhone 15",
  "warrantyId": "f50e8400-e29b-41d4-a716-446655440000",
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "issueDescription": "Screen flickering and touch unresponsive",
  "status": "PENDING",
  "resolutionNotes": null,
  "createdAt": "2026-08-13T22:45:00",
  "updatedAt": "2026-08-13T22:45:00"
}
```

---

### 2. Get User Claims
**`GET /api/claims`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
[
  {
    "claimId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
    "productName": "iPhone 15",
    "warrantyId": "f50e8400-e29b-41d4-a716-446655440000",
    "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
    "issueDescription": "Screen flickering and touch unresponsive",
    "status": "PENDING",
    "resolutionNotes": null,
    "createdAt": "2026-08-13T22:45:00",
    "updatedAt": "2026-08-13T22:45:00"
  }
]
```

---

### 3. Get Claim By ID
**`GET /api/claims/{id}`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
{
  "claimId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "productName": "iPhone 15",
  "warrantyId": "f50e8400-e29b-41d4-a716-446655440000",
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "issueDescription": "Screen flickering and touch unresponsive",
  "status": "PENDING",
  "resolutionNotes": null,
  "createdAt": "2026-08-13T22:45:00",
  "updatedAt": "2026-08-13T22:45:00"
}
```

---

### 4. Cancel Claim (Customer)
**`PUT /api/claims/{id}/cancel`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
{
  "claimId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "productName": "iPhone 15",
  "warrantyId": "f50e8400-e29b-41d4-a716-446655440000",
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "issueDescription": "Screen flickering and touch unresponsive",
  "status": "CANCELLED",
  "resolutionNotes": null,
  "createdAt": "2026-08-13T22:45:00",
  "updatedAt": "2026-08-13T22:46:00"
}
```

---

### 5. Update Claim Status (Admin Only)
**`PUT /api/admin/claims/{id}/status`**  
*Header required*: `Authorization: Bearer <admin-token>`  
*Role required*: `ADMIN`

**Request Payload**:
```json
{
  "status": "APPROVED",
  "resolutionNotes": "Device approved for free display replacement service"
}
```

**Success Response (`200 OK`)**:
```json
{
  "claimId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "productName": "iPhone 15",
  "warrantyId": "f50e8400-e29b-41d4-a716-446655440000",
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "issueDescription": "Screen flickering and touch unresponsive",
  "status": "APPROVED",
  "resolutionNotes": "Device approved for free display replacement service",
  "createdAt": "2026-08-13T22:45:00",
  "updatedAt": "2026-08-13T22:50:00"
}
```

---


---

## Purchase Invoice Upload & Supabase Storage (Phase 5)

Authenticated customers can upload proof-of-purchase invoices for registered products. Invoices are stored in Supabase Storage under structured path `invoices/{userId}/{productId}/{uniqueFileName}`, with metadata tracked in PostgreSQL.

### Key File Validation Rules
1. **Allowed Formats**: `PDF` (`application/pdf`), `JPG` (`image/jpeg`), `JPEG` (`image/jpeg`), `PNG` (`image/png`).
2. **Maximum File Size**: **10 MB** (enforced by Spring Boot multipart configuration and service-level validation).
3. **Owner Isolation**: Customers can only upload, view, download, or delete invoices for products they own (`403 Forbidden` returned otherwise).
4. **Secure Signed Downloads**: `GET /api/invoices/{id}/download` generates a short-lived (1 hour) signed URL. Secret keys are never exposed.

---

## Invoice API Endpoints

### 1. Upload Purchase Invoice
**`POST /api/invoices/upload`**  
*Content-Type*: `multipart/form-data`  
*Header required*: `Authorization: Bearer <token>`  
*Form Parameters*: `productId` (UUID), `file` (MultipartFile)

**Success Response (`201 Created`)**:
```json
{
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "fileName": "purchase_receipt.pdf",
  "fileType": "application/pdf",
  "fileSize": 245678,
  "uploadedAt": "2026-08-13T22:40:00",
  "storagePath": "invoices/user-uuid/product-uuid/uuid_purchase_receipt.pdf",
  "downloadUrl": "/api/invoices/d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c/download"
}
```

---

### 2. Get User Invoices
**`GET /api/invoices`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
[
  {
    "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
    "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
    "fileName": "purchase_receipt.pdf",
    "fileType": "application/pdf",
    "fileSize": 245678,
    "uploadedAt": "2026-08-13T22:40:00",
    "storagePath": "invoices/user-uuid/product-uuid/uuid_purchase_receipt.pdf",
    "downloadUrl": "/api/invoices/d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c/download"
  }
]
```

---

### 3. Get Invoice By ID
**`GET /api/invoices/{id}`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
{
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "productId": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "fileName": "purchase_receipt.pdf",
  "fileType": "application/pdf",
  "fileSize": 245678,
  "uploadedAt": "2026-08-13T22:40:00",
  "storagePath": "invoices/user-uuid/product-uuid/uuid_purchase_receipt.pdf",
  "downloadUrl": "/api/invoices/d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c/download"
}
```

---

### 4. Download / View Invoice Signed Link
**`GET /api/invoices/{id}/download`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
{
  "invoiceId": "d8a1b2c3-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "fileName": "purchase_receipt.pdf",
  "fileType": "application/pdf",
  "downloadUrl": "https://cyzgjkjjhwqssobvovfj.supabase.co/storage/v1/object/sign/product-invoices/invoices/...?token=...",
  "expiresInSeconds": 3600
}
```

---

### 5. Delete Invoice
**`DELETE /api/invoices/{id}`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`204 No Content`)**

---

## User Authentication & Security (Phase 3)

The application enforces stateless, token-based authentication using **Spring Security** and **JSON Web Tokens (JWT)**. Passwords stored in Supabase PostgreSQL are strictly hashed using **BCrypt**.

### Key Security Design Principles
1. **BCrypt Password Hashing**: Plaintext passwords are never stored in the database or logged. All passwords are hashed using `BCryptPasswordEncoder` before persistence.
2. **Strict Role Assignment**: Public registration (`POST /api/auth/register`) always assigns `UserRole.CUSTOMER`. Any `role` field in public client payloads (such as `"role": "ADMIN"`) is explicitly ignored to prevent privilege escalation.
3. **Safe API Responses**: Passwords and sensitive fields are excluded from all DTO responses (`UserResponse`, `AuthResponse`).
4. **Controlled Admin Seeding**: No public endpoint exists for admin creation. An `AdminInitializer` bean seeds the default administrator on startup if no admin user is present in the database.
5. **Stateless Security**: Spring Security is configured with stateless sessions (`SessionCreationPolicy.STATELESS`). Unauthenticated requests to protected endpoints return a structured `401 Unauthorized` JSON response.

---

## Environment Variables Configuration

Configure the following environment variables on your server or environment:

| Variable Name | Description | Default / Example Value |
|---|---|---|
| `SUPABASE_DB_URL` | Supabase JDBC Database URL | `jdbc:postgresql://db.xxxxxx.supabase.co:5432/postgres` |
| `SUPABASE_DB_USERNAME` | Supabase Database Username | `postgres` |
| `SUPABASE_DB_PASSWORD` | Supabase Database Password | `YourSecurePassword123` |
| `JWT_SECRET` | Base64/Hex secret key (min 256 bits / 32 bytes) | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION` | Token expiration in milliseconds | `86400000` (24 Hours) |
| `ADMIN_SEED_ENABLED` | Enable local default admin seeding | `true` |
| `ADMIN_SEED_EMAIL` | Controlled default admin email | `admin@warrantyportal.com` |
| `ADMIN_SEED_PASSWORD` | Controlled default admin password | `AdminPassword123` |

### Setting Environment Variables locally:

**PowerShell (Windows)**:
```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://db.xxxxxx.supabase.co:5432/postgres"
$env:SUPABASE_DB_USERNAME="postgres"
$env:SUPABASE_DB_PASSWORD="your_password"
$env:JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
$env:JWT_EXPIRATION="86400000"
```

---

## Authentication API Endpoints

### 1. Public Customer Registration
**`POST /api/auth/register`**

**Request Payload**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123",
  "phone": "9876543210"
}
```

**Success Response (`201 Created`)**:
```json
{
  "id": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "role": "CUSTOMER"
}
```

---

### 2. User Login
**`POST /api/auth/login`**

**Request Payload**:
```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

**Success Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZSI6IkNVU1RPTUVSIiwiZXhwIjoxNzcxMjM0NTY3fQ.signature",
  "tokenType": "Bearer",
  "user": {
    "id": "c7a84e31-8f5b-4c22-921a-123456789abc",
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "CUSTOMER"
  }
}
```

---

### 3. Get Authenticated User Profile (Protected)
**`GET /api/users/me`**  
*Header required*: `Authorization: Bearer <token>`

**Success Response (`200 OK`)**:
```json
{
  "id": "c7a84e31-8f5b-4c22-921a-123456789abc",
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "role": "CUSTOMER"
}
```

---

### 4. Health Check (Public)
**`GET /api/health`**

**Success Response (`200 OK`)**:
```json
{
  "status": "UP",
  "message": "Product Warranty Registration Portal is running",
  "timestamp": "2026-08-13T21:55:00"
}
```

---

## Controlled First Admin Creation for Testing

To create the initial administrator for testing:
1. Set the following environment variables (or rely on default properties in `application.properties`):
   ```bash
   ADMIN_SEED_ENABLED=true
   ADMIN_SEED_EMAIL=admin@warrantyportal.com
   ADMIN_SEED_PASSWORD=AdminPassword123
   ```
2. Start the Spring Boot application. The `AdminInitializer` checks if an `ADMIN` user exists. If not, it creates the admin account automatically.
3. Authenticate as Admin:
   ```http
   POST /api/auth/login
   {
     "email": "admin@warrantyportal.com",
     "password": "AdminPassword123"
   }
   ```

