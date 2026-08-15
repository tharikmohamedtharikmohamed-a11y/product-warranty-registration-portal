# WarrantyHub — Product Warranty Registration Portal

> *"Your Warranties. Organized. Protected. Always Accessible."*

---

## 📌 Project Overview

**WarrantyHub** (Product Warranty Registration Portal) is an enterprise-ready, full-stack web application engineered to centralize post-purchase product protection. It eliminates paper clutter, lost physical receipts, and forgotten warranty expiration dates by providing customers with a secure, digital vault for their products, automated warranty tracking, cloud-backed invoice storage, and streamlined warranty claim workflows.

---

## 🚦 Current Project Status: **PHASE 4 (Completed)**

### Phase 1: Project Planning & Requirements
- **Status:** **COMPLETED**
- **Scope Completed:** Complete system architecture, data modeling, REST API contracts, security blueprint, and Google Stitch UI/UX design specifications.
- **Specification Document:** [PROJECT_REQUIREMENTS.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/PROJECT_REQUIREMENTS.md)

### Phase 2: Database Design & Supabase Setup
- **Status:** **COMPLETED**
- **PostgreSQL Database Designed:** Normalized 3NF schema targeting PostgreSQL 17 on Supabase.
- **Required Tables Defined:** `users`, `products`, `warranties`, `invoices`, `claims`.
- **Relationships Defined:** Strict foreign keys with safe delete rules (`ON DELETE RESTRICT` for users, products, invoices, and claims; `ON DELETE CASCADE` for 1-to-1 product warranties).
- **Constraints & Indexes:** Primary keys (UUID `gen_random_uuid()`), unique constraints, domain check constraints, and 15+ B-Tree indexes.
- **Supabase Storage Configured:** Private `invoices` storage bucket (10 MB limit, PDF/JPEG/PNG).
- **Security & RLS:** Row Level Security (RLS) enabled across all tables to block unauthorized PostgREST client queries while preserving full backend JDBC access for Spring Boot.
- **Database Artifacts:**
  - [database/schema.sql](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/database/schema.sql) — Full PostgreSQL DDL script with triggers and constraints.
  - [database/DATABASE_DESIGN.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/database/DATABASE_DESIGN.md) — Exhaustive database architecture, data dictionary, and storage documentation.

### Phase 3: Backend Initial Setup
- **Status:** **COMPLETED**
- **Spring Boot Backend Created:** Standardized Maven project under `backend/` using Spring Boot 3.2.5.
- **Java 17 Baseline:** Strictly configured for Java 17 LTS (verified with OpenJDK 17.0.20 Temurin).
- **Maven Configured:** Apache Maven 3.9.6 configured with Java 17 toolchain.
- **Database Connectivity:** PostgreSQL JDBC driver and HikariCP connection pooling configured to connect to Supabase PostgreSQL with runtime environment variables.
- **Environment Management:** [backend/.env.example](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/.env.example) template provided; `.env` protected by `.gitignore`.
- **Package Architecture:** Layered packages established under `com.warrantyportal`: `config`, `controller`, `dto`, `entity`, `repository`, `security`, `service`.
- **Health Endpoint:** `GET /api/health` implemented and verified returning `{"status": "UP", "application": "WarrantyHub"}`.
- **CORS Configuration:** Local Vite frontend development origin (`http://localhost:5173`) enabled.
- **Testing & Verification:** Context loading and health tests passing; Tomcat verified on port 8080.
- **Developer Guide:** [backend/BACKEND_SETUP.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/BACKEND_SETUP.md) authored with setup and troubleshooting details.

### Phase 4: Backend Authentication & Security
- **Status:** **COMPLETED**
- **Customer Registration (`POST /api/auth/register`):** Validated account registration with automatic assignment to `CUSTOMER` role (public users cannot register as `ADMIN`).
- **BCrypt Password Hashing:** Passwords encoded with `BCryptPasswordEncoder` before database persistence; plain text passwords never stored, returned, or logged.
- **Customer Login (`POST /api/auth/login`):** Case-insensitive email lookup with BCrypt verification returning signed JWTs and safe user profiles.
- **JWT Engine (`JwtService`):** JJWT 0.12.5 integration with HMAC-SHA256 (`HS256`), user ID subject claims, email/role claims, and configurable 24-hour expiration (`app.jwt.expiration=86400000`).
- **Stateless Filter Chain (`JwtAuthenticationFilter`):** Intercepts requests, parses `Authorization: Bearer <token>`, validates signatures, and populates `SecurityContextHolder`.
- **Protected Profile Endpoint (`GET /api/auth/me`):** Retrieves authenticated user profile from database; rejects unauthenticated requests with HTTP 401 Unauthorized.
- **Clean Exception Handling (`GlobalExceptionHandler`):** Uniform, safe JSON error formats for 400 Bad Request, 401 Unauthorized, and 409 Conflict without database stack trace leaks.
- **Automated Testing Suite:** 18 passing tests covering registration, duplicate email rejection, BCrypt hashing, login success, invalid credentials, JWT generation, and protected `/me` endpoints.
- **Authentication Guide:** [backend/AUTHENTICATION.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/AUTHENTICATION.md) authored with endpoint specifications and cURL examples.

---

## 🛠️ Technology Stack

| Layer | Technologies Selected |
| :--- | :--- |
| **Frontend** | React.js (v18), Vite, JavaScript (ESNext), React Router (v6), Axios, Modern CSS System |
| **Backend** | Java 17 (LTS), Spring Boot 3.2.5, Spring Security 6.x, Spring Data JPA, Hibernate 6.4, JWT (JJWT 0.12.5), Maven 3.9.6 |
| **Database** | PostgreSQL 17 (hosted via Supabase) |
| **File Storage** | Supabase Storage (Dedicated private `invoices` bucket) |
| **Testing** | JUnit 5, Spring Boot Test, Spring Security Test, Mockito, Postman |
| **Version Control** | Git & GitHub |
| **Development Environment** | Antigravity IDE |

---

## 🚀 Key Features

### For Customers
- **Authentication & Security:** Secure JWT-based self-registration and login with BCrypt password encryption.
- **Product Registry:** Register purchased products with details like brand, model number, serial number, purchase date, price, and seller name.
- **Automated Warranty Tracking:** Instant calculation of expiry dates (`purchase date + warranty duration`) with real-time status badges (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`).
- **Digital Invoice Vault:** Upload and view purchase receipts (PDF, PNG, JPG up to 10 MB) securely stored in cloud object storage.
- **Claim Submission & Tracking:** Submit warranty claims for defective products with transparent status tracking (`PENDING`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
- **Personal Dashboard & Alerts:** Comprehensive dashboard displaying active coverage metrics and 30-day proactive expiration warnings.

### For Administrators
- **Global Overview:** High-level platform KPIs covering active warranties, user accounts, and claim backlogs.
- **Auditing & Verification:** Search and verify registered products, serial numbers, and uploaded purchase documents.
- **Claim Adjudication Workflow:** Review, approve, reject, or update status of submitted customer warranty claims.
- **User Management:** Oversee registered users and system integrity.

---

## 🗺️ Roadmap & Development Phases

```
[x] PHASE 1:  Project Planning & Requirements (Completed)
[x] PHASE 2:  Database Design & Supabase Setup (Completed)
[x] PHASE 3:  Backend Initial Setup (Spring Boot 3.2 / Java 17) (Completed)
[x] PHASE 4:  Backend Authentication & Security (Completed)
[ ] PHASE 5:  Frontend Initial Setup (React + Vite)
[ ] PHASE 6:  Frontend Authentication & Route Guards
[ ] PHASE 7:  Product Management Module
[ ] PHASE 8:  Warranty Lifecycle Management
[ ] PHASE 9:  Invoice Management & Storage
[ ] PHASE 10: Warranty Claims Engine
[ ] PHASE 11: Admin Management Module
[ ] PHASE 12: Dashboard Analytics & Notifications
[ ] PHASE 13: Google Stitch UI Implementation
[ ] PHASE 14: Integration & Testing
[ ] PHASE 15: GitHub & Final Documentation
[ ] PHASE 16: Deployment
```

---

## 📖 Documentation Reference

- [PROJECT_REQUIREMENTS.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/PROJECT_REQUIREMENTS.md) — Complete 20-section system requirements and architectural specification.
- [database/DATABASE_DESIGN.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/database/DATABASE_DESIGN.md) — Relational schema design, data dictionary, entity diagrams, and storage architecture.
- [database/schema.sql](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/database/schema.sql) — Executable PostgreSQL DDL schema with triggers and constraints.
- [backend/BACKEND_SETUP.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/BACKEND_SETUP.md) — Developer setup, environment configuration, and execution instructions.
- [backend/AUTHENTICATION.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/AUTHENTICATION.md) — Authentication architecture, JWT token flow, request/response models, and security rules.
