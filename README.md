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
- **Phase 4**: Product Registration & Warranty Management (CRUD APIs).
- **Phase 5**: Invoice Upload (Cloud Storage / Supabase Storage) & Expiry Notifications.
- **Phase 6**: Admin Dashboard & Warranty Claim Tracking.

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

