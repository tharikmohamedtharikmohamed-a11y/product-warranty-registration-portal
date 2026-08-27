# WarrantyHub — Product Warranty Registration Portal

> *"Your Warranties. Organized. Protected. Always Accessible."*

---

## 📌 Project Overview

**WarrantyHub** (Product Warranty Registration Portal) is an enterprise-ready, full-stack web application engineered to centralize post-purchase product protection. It eliminates paper clutter, lost physical receipts, and forgotten warranty expiration dates by providing customers with a secure, digital vault for their products, automated warranty tracking, cloud-backed invoice storage, and streamlined warranty claim workflows.

---

## 🚦 Current Project Status: **PHASE 7 (Completed)**

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

### Phase 5: Frontend Initial Setup
- **Status:** **COMPLETED**
- **React & Vite Baseline:** Pinned strictly to React 18.x (`^18.3.1`) and Vite 5.x (`^5.4.14`) in `frontend/`.
- **Routing Infrastructure:** React Router DOM 6.x configured with `AppRoutes`, `PublicLayout`, and `ProtectedRoute` prepared for Phase 6.
- **HTTP Client:** Reusable Axios instance in `src/services/api.js` configured with `VITE_API_BASE_URL=http://localhost:8080`.
- **Responsive Design System:** Pure Vanilla CSS in `src/index.css` with custom properties, typography, buttons, accessible focus states, and responsive breakpoints (desktop, tablet, mobile).
- **WarrantyHub Landing Page:** Professional hero section, core feature breakdown (Product Registration, Automated Tracking, Invoice Vault, Claims), and live backend health telemetry status badge.
- **Placeholder Views & 404 Routing:** Clean placeholder pages for `/login`, `/register`, `/forgot-password`, `/reset-password`, and custom 404 `/not-found`.
- **Developer Guide:** [frontend/FRONTEND_SETUP.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/FRONTEND_SETUP.md) authored detailing directory layout, commands, routes, and environment configuration.

### Phase 6: Frontend Authentication & Route Guards
- **Status:** **COMPLETED**
- **Global Auth State (`AuthContext` & `useAuth`):** Centralized state management tracking `user`, `loading`, and `isAuthenticated` across the application.
- **Local Storage JWT Token (`warrantyhub_token`):** Persisted locally and automatically rehydrated via `GET /api/auth/me` on application boot/refresh.
- **Axios Interceptors:** Automatic Bearer token attachment on outgoing requests and centralized 401 handling with automatic session purge.
- **Interactive Registration (`/register`):** Full client-side validation, password confirmation, duplicate email detection (409 Conflict), and automatic navigation to login.
- **Interactive Login (`/login`):** Validated sign-in form, show/hide password toggle, generic error alerts (401 Unauthorized), and immediate redirect to `/dashboard`.
- **Route Guards:**
  - `ProtectedRoute`: Guards `/dashboard`, rendering an accessible spinner during session verification and redirecting unauthenticated visitors to `/login`.
  - `PublicRoute`: Guards `/login` and `/register`, redirecting logged-in customers straight to `/dashboard`.
- **Authentication-Aware Navigation:** Navbar adapts dynamically, displaying Dashboard navigation, active user greeting, and Logout action button when authenticated.
- **Authentication Guide:** [frontend/AUTHENTICATION.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/AUTHENTICATION.md) authored with architecture diagrams, workflows, and test steps.

### Phase 7: Product Management Module
- **Status:** **COMPLETED**
- **Product Entity & Schema Mapping:** JPA entity `Product.java` mapped to `products` table with UUID primary keys and foreign key binding to `users.id`.
- **Automated Warranty Provisioning:** Registering a product transactionally provisions a 1-to-1 active warranty record in `warranties` with calculated `expiryDate = purchaseDate + warrantyDurationMonths`.
- **Warranty Recalibration on Edit:** Editing product purchase date or duration automatically recalculates existing warranty start and expiry dates.
- **Strict Customer Isolation:** Spring Security `@AuthenticationPrincipal` enforces per-customer query filtering; cross-customer access returns HTTP 404 Not Found to prevent data discovery.
- **Duplicate Serial Detection:** Rejects duplicate serial numbers for the same customer with HTTP 409 Conflict.
- **Cascaded Deletion:** Deleting a product automatically cascades deletion to its associated warranty record.
- **Frontend Product Portfolio (`/products`):** Interactive card grid with search, category/status filters, real-time portfolio metrics strip, empty states, and delete confirmation modal.
- **Validated Registration Form (`/products/register`):** Comprehensive input validation, dynamic live warranty calculation preview card, and field-level error mapping.
- **Product Details & Warranty Timeline (`/products/:id`):** Full specification inspection, 1-click serial clipboard copy, and visual warranty elapsed progress bar.
- **Product Edit Interface (`/products/:id/edit`):** Pre-populated edit form with live recalculated warranty preview and instant synchronization.
- **Developer Guides:**
  - [backend/PRODUCT_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/PRODUCT_MANAGEMENT.md) — Backend entity architecture, REST APIs, ownership rules, and test coverage.
  - [frontend/PRODUCT_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/PRODUCT_MANAGEMENT.md) — Frontend routes, UI components, validation, and user flows.

### Phase 8: Warranty Lifecycle Management
- **Status:** **COMPLETED**
- **Dynamic Status Evaluation:** Authoritative backend calculation of warranty states (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`) based on evaluation date and expiry date thresholds.
- **Days Remaining Calculation:** Safe countdown calculation ensuring non-negative values and returning 0 for expired warranties.
- **Elapsed Duration Progress:** Normalized percentage tracking `progressPercentage` (0% to 100%) reflecting elapsed warranty lifespan.
- **Automatic Status Synchronization:** Queries trigger automatic on-read status checks that persist state changes to the database without requiring cron/batch overhead.
- **Customer Scoping & 404 Protection:** Warranty lookups enforce `Warranty -> Product -> User` isolation via `findAllByProductUserId`, `findByIdAndProductUserId`, and `findByProductIdAndProductUserId`.
- **Warranty Portfolio (`/warranties`):** Comprehensive customer warranty tracking dashboard featuring metrics counters, search, status filtering, accessible progress bars, and direct product links.
- **Warranty Details Page (`/warranties/:id`):** Granular coverage review, official expiry dates, status advisories, and accessible progress indicators (`role="progressbar"`).
- **Product Details Integration:** Updated `ProductDetailsPage` to query `/api/products/{productId}/warranty` and showcase a dedicated Warranty Protection panel.
- **Developer Guides:**
  - [backend/WARRANTY_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/WARRANTY_MANAGEMENT.md) — Warranty entity, status calculation rules, progress formulas, REST APIs, and test coverage.
  - [frontend/WARRANTY_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/WARRANTY_MANAGEMENT.md) — Frontend routes, UI components, accessible progress bars, and user flows.

---

## 🛠️ Technology Stack

| Layer | Technologies Selected |
| :--- | :--- |
| **Frontend** | React.js (v18), Vite (v5), JavaScript (ESNext), React Router (v6), Axios, Modern CSS System |
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
[x] PHASE 5:  Frontend Initial Setup (React + Vite) (Completed)
[x] PHASE 6:  Frontend Authentication & Route Guards (Completed)
[x] PHASE 7:  Product Management Module (Completed)
[x] PHASE 8:  Warranty Lifecycle Management (Completed)
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
- [backend/PRODUCT_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/PRODUCT_MANAGEMENT.md) — Backend product management entity architecture, REST APIs, ownership rules, and test coverage.
- [backend/WARRANTY_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/WARRANTY_MANAGEMENT.md) — Backend warranty management entity architecture, lifecycle calculations, REST APIs, and test coverage.
- [frontend/FRONTEND_SETUP.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/FRONTEND_SETUP.md) — Frontend developer guide, directory layout, commands, routes, and environment configuration.
- [frontend/AUTHENTICATION.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/AUTHENTICATION.md) — Frontend authentication architecture, JWT lifecycle, route guards, and test guide.
- [frontend/PRODUCT_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/PRODUCT_MANAGEMENT.md) — Frontend product routes, UI components, validation, and user flows.
- [frontend/WARRANTY_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/WARRANTY_MANAGEMENT.md) — Frontend warranty routes, UI components, accessible progress bars, and user flows.


