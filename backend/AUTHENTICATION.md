# WarrantyHub — Backend Authentication & Security Guide
**Document Version:** 1.0.0  
**Phase:** Phase 4 — Backend Authentication  
**Application:** Product Warranty Registration Portal  
**Brand:** WarrantyHub  

---

## 1. Overview

WarrantyHub implements a stateless, token-based authentication and authorization system using:
- **Spring Security 6.x**
- **JSON Web Tokens (JJWT 0.12.5)** with HMAC-SHA256 (`HS256`)
- **BCrypt Password Hashing** (Strength factor 10+)
- **PostgreSQL / Supabase Persistence** for the `users` entity
- **Role-Based Access Control (RBAC)** supporting `CUSTOMER` and `ADMIN` roles

All publicly registered accounts are strictly assigned the `CUSTOMER` role. The database schema enforces unique email constraints (treated case-insensitively).

---

## 2. Environment Variables

The backend requires the following environment variables to be set in the active execution environment:

| Variable Name | Description | Example / Recommended Format |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC Connection URL to PostgreSQL/Supabase | `jdbc:postgresql://db.bfogwrprtsfvsgmbzvdp.supabase.co:5432/postgres?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password set in Supabase Dashboard | *(your_supabase_db_password)* |
| `JWT_SECRET` | 256-bit cryptographic secret for signing JWTs | *(your_secure_256_bit_random_secret)* |

> [!WARNING]
> Never commit actual passwords or JWT secret keys to version control. Keep `.env` ignored by Git.

---

## 3. Authentication REST API Endpoints

### 3.1 Customer Registration
- **Endpoint:** `POST /api/auth/register`
- **Access:** Public (No authentication required)
- **Headers:** `Content-Type: application/json`

#### Request Payload:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123"
}
```

#### Validation Rules:
- `name`: Required, non-blank, max 255 characters.
- `email`: Required, valid email format, normalized to lowercase and trimmed.
- `password`: Required, minimum 8 characters.

#### Success Response (`201 Created`):
```json
{
  "message": "Registration successful",
  "user": {
    "id": "a4f3c7e0-1234-4b5a-9876-000000000001",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

#### Error Responses:
- **`400 Bad Request`** (Validation Failure):
```json
{
  "message": "Validation failed",
  "errors": {
    "password": "Password must be at least 8 characters long"
  }
}
```
- **`409 Conflict`** (Duplicate Email):
```json
{
  "message": "Email is already registered"
}
```

---

### 3.2 Customer Login
- **Endpoint:** `POST /api/auth/login`
- **Access:** Public (No authentication required)
- **Headers:** `Content-Type: application/json`

#### Request Payload:
```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

#### Success Response (`200 OK`):
```json
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhNGYzYzdlMC0xMjM0LTRiNWEtOTg3Ni0wMDAwMDAwMDAwMDEiLCJlbWFpbCI6ImpvaG5AZXhhbXBsZS5jb20iLCJyb2xlIjoiQ1VTVE9NRVIiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE3MjM0NjAwMDAsImV4cCI6MTcyMzU0NjQwMH0.placeholderSignature",
  "user": {
    "id": "a4f3c7e0-1234-4b5a-9876-000000000001",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

#### Error Response (`401 Unauthorized`):
```json
{
  "message": "Invalid email or password"
}
```
*(Generic error returned regardless of whether the email was not found or the password was incorrect, preventing user enumeration).*

---

### 3.3 Get Current Authenticated User Profile
- **Endpoint:** `GET /api/auth/me`
- **Access:** Protected (Requires valid JWT Bearer token)
- **Headers:** `Authorization: Bearer <JWT_TOKEN>`

#### Success Response (`200 OK`):
```json
{
  "id": "a4f3c7e0-1234-4b5a-9876-000000000001",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

#### Error Response (`401 Unauthorized`):
```json
{
  "message": "Unauthorized access. Valid authentication token required."
}
```

---

## 4. JWT Authentication Flow Architecture

```
[ Client ]                            [ Spring Boot Backend ]                         [ Database ]
    │                                            │                                          │
    │  1. POST /api/auth/register                │                                          │
    ├───────────────────────────────────────────►│  BCrypt.hash(password)                   │
    │                                            │  Insert User (role = CUSTOMER)           │
    │                                            ├─────────────────────────────────────────►│
    │  2. 201 Created (Safe User Profile)        │◄─────────────────────────────────────────┤
    │◄───────────────────────────────────────────┤                                          │
    │                                            │                                          │
    │  3. POST /api/auth/login                   │                                          │
    ├───────────────────────────────────────────►│  FindByEmail(normalizedEmail)           │
    │                                            ├─────────────────────────────────────────►│
    │                                            │◄─────────────────────────────────────────┤
    │                                            │  BCrypt.matches(password, user.hash)     │
    │                                            │  JwtService.generateToken(user)          │
    │  4. 200 OK { token, user }                 │                                          │
    │◄───────────────────────────────────────────┤                                          │
    │                                            │                                          │
    │  5. GET /api/auth/me (Bearer <token>)      │                                          │
    ├───────────────────────────────────────────►│  JwtAuthenticationFilter:                │
    │                                            │    - Parse & verify HMAC-SHA256 signature│
    │                                            │    - Check expiration (24h)              │
    │                                            │    - Extract userId claim                │
    │                                            │  Load fresh user profile                 │
    │                                            ├─────────────────────────────────────────►│
    │                                            │◄─────────────────────────────────────────┤
    │  6. 200 OK (Current User Details)          │                                          │
    │◄───────────────────────────────────────────┤                                          │
```

---

## 5. Testing with Postman / cURL

### Step 1: Check Application Health
```bash
curl -X GET http://localhost:8080/api/health
```

### Step 2: Register New Customer
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Consumer",
    "email": "jane@example.com",
    "password": "SecurePassword123!"
  }'
```

### Step 3: Login to Retrieve JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "SecurePassword123!"
  }'
```

### Step 4: Access Protected Profile Endpoint
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <TOKEN_RETURNED_FROM_STEP_3>"
```

---

## 6. Security Principles Implemented
1. **No Plaintext Passwords:** Passwords hashed with BCrypt prior to persistence.
2. **Stateless Sessions:** No server-side HTTP session storage; authentication state is validated per-request via JWT.
3. **No Over-Posting Privilege Escalation:** Registration endpoints strictly force `role = Role.CUSTOMER`.
4. **Information Disclosure Prevention:** Generic 401 error message avoids revealing if an email exists.
5. **Sanitization:** Emails are trimmed and converted to lowercase during registration and authentication.
6. **No Secret Leakage:** Passwords, password hashes, and database secrets are excluded from JSON payloads.
