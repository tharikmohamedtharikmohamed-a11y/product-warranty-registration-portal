# WarrantyHub — Product Warranty Registration Portal

> *"Your Warranties. Organized. Protected. Always Accessible."*

---

## 📌 Project Overview

**WarrantyHub** (Product Warranty Registration Portal) is an enterprise-ready, full-stack web application engineered to centralize post-purchase product protection. It eliminates paper clutter, lost physical receipts, and forgotten warranty expiration dates by providing customers with a secure, digital vault for their products, automated warranty tracking, cloud-backed invoice storage, and streamlined warranty claim workflows.

---

## 🚦 Current Project Status: **PHASE 12 (Completed)**

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

### Phase 9: Invoice Management & Supabase Storage
- **Status:** **COMPLETED**
- **Supabase Storage Integration:** Dedicated backend storage adapter (`SupabaseStorageService`) communicating with private `invoices` bucket over HTTP REST API using native Java 17 `HttpClient`.
- **Zero Frontend Secrets:** Supabase service-role keys and private bucket credentials remain strictly confined to the Spring Boot backend environment.
- **Invoice Upload (`POST /api/invoices/upload`):** Ingests proof-of-purchase documents (`multipart/form-data`) for authenticated user's products.
- **Comprehensive File Validation:** Enforces non-empty payloads, maximum 10 MB file size limit, and strict MIME type / extension checks (`application/pdf`, `image/jpeg`, `image/jpg`, `image/png`).
- **Hierarchical Secure Storage Paths:** Formats objects as `invoices/{userId}/{productId}/{uuid}_{sanitizedFileName}` to prevent path traversal and object collisions.
- **Compensating Rollback:** If PostgreSQL metadata insertion fails after binary upload, an automatic compensating delete is executed against Supabase Storage.
- **Invoice Listing & Detail APIs:** Customer-scoped queries (`GET /api/invoices`, `GET /api/invoices/{id}`, `GET /api/products/{productId}/invoice`) returning clean DTOs without leaking internal storage keys.
- **Secure File Download & Preview:** Streams binary contents directly through Spring Boot with proper `Content-Type` and `Content-Disposition` headers (`attachment` for download, `inline` for browser view).
- **Invoice Deletion (`DELETE /api/invoices/{id}`):** Deletes binary object from Supabase Storage and purges metadata from PostgreSQL table `invoices`.
- **Product Details Integration:** Replaced phase placeholder on `ProductDetailsPage` with an interactive Purchase Invoice panel supporting instant upload, preview, download, and removal.
- **Customer Invoice Vault (`/invoices`):** Dedicated page listing all invoices with format badges, formatted sizes, upload dates, and action buttons.
- **Developer Guides:**
  - [backend/INVOICE_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/INVOICE_MANAGEMENT.md) — Storage architecture, entity mapping, REST APIs, compensating rollbacks, and test coverage.
  - [frontend/INVOICE_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/INVOICE_MANAGEMENT.md) — Frontend routes, upload modal, product invoice panel, and user flows.

### Phase 10: Warranty Claims Engine
- **Status:** **COMPLETED**
- **Claims Entity & Schema Compatibility:** Mapped JPA entity `Claim.java` to PostgreSQL table `claims` (`issue_description` mapped as `claimReason`, `additional_information` as `description` with alias getters/setters) to strictly preserve `spring.jpa.hibernate.ddl-auto=validate`.
- **Warranty Status Eligibility:** Claims can only be filed against active or expiring-soon warranties; expired warranties reject claim filing with HTTP 400 Bad Request.
- **Initial Status Enforcement:** Initial claim status is immutably set to `PENDING` by the backend.
- **Strict Anti-IDOR Ownership:** Product and claim lookups enforce customer ownership; attempts to access or cancel unowned claims return HTTP 404 Not Found.
- **Self-Service Cancellation:** Customers can cancel their own claims if and only if status is `PENDING` (`PATCH /api/claims/{id}/cancel`); non-pending cancellations return HTTP 400 Bad Request.
- **REST Endpoints (`ClaimController`):** `POST /api/claims`, `GET /api/claims`, `GET /api/claims/{id}`, `PATCH /api/claims/{id}/cancel`, `GET /api/products/{productId}/claims`.
- **Dedicated Claims Dashboard (`/claims`):** Filter by status, search by product/reason, and launch "+ File Claim" modal.
- **Claim Details Page (`/claims/:id`):** Full status timeline, product specifications, issue description, and accessible cancel modal.
- **Product Details Integration:** Interactive Warranty Claims card in right column with count badge, filing trigger, and quick links.
- **Automated Test Suite:** 27 automated tests (`ClaimServiceTest` and `ClaimControllerTest`) covering eligibility, ownership, status transitions, and anti-IDOR protections (111 tests passing overall with 0 failures).
- **Developer Guides:**
  - [backend/CLAIMS_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/CLAIMS_MANAGEMENT.md) — Backend entity mapping, claim lifecycle, validation rules, REST APIs, and test coverage.
  - [frontend/CLAIMS_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/CLAIMS_MANAGEMENT.md) — Frontend routes, submit modal, claim details timeline, and user flows.

### Phase 11: Admin Management Module
- **Status:** **COMPLETED**
- **Admin Authentication & Role Authorization:** Enforced strictly via Spring Security (`/api/admin/**` requires `ROLE_ADMIN`). Customers receive HTTP 403 Forbidden with standard JSON message; unauthenticated requests receive HTTP 401 Unauthorized.
- **Global Operations Telemetry (`GET /api/admin/dashboard/stats`):** Authoritative database count queries delivering 16 platform-wide KPIs: Total Users, Customers, Admins, Products, Warranties (Active, Expiring Soon, Expired), Invoices, and Claims (Pending, Approved, In Progress, Completed, Rejected, Cancelled).
- **User Management Directory (`GET /api/admin/users`, `/admin/users`):** Cross-platform user audit view with name and email search filter, system role indicators, and strict exclusion of password hashes or security credentials.
- **Global Product Registry (`GET /api/admin/products`, `/admin/products`):** Administrative read-only directory inspecting customer equipment, serial numbers, retailers, pricing, and coverage status.
- **Global Warranty Portfolio (`GET /api/admin/warranties`, `/admin/warranties`):** Dynamic lifecycle calculations, real-time validity windows, days remaining countdowns, and progress bars across all customer warranties.
- **Global Invoice Documents Vault (`GET /api/admin/invoices`, `/admin/invoices`):** Complete document metadata overview with direct administrative streaming for inline preview and download without exposing Supabase credentials or compromising private storage bucket policies.
- **Warranty Claim Adjudication Queue (`GET /api/admin/claims`, `/admin/claims`):** Central triage queue with lifecycle status filtering and direct navigation to detailed adjudication.
- **Claim Decision Processing & Lifecycle State Machine (`/admin/claims/:id`):** Full claim details inspection with deterministic status transitions:
  - `PENDING` → `APPROVED` (`PATCH /api/admin/claims/{id}/approve`)
  - `PENDING` → `REJECTED` (`PATCH /api/admin/claims/{id}/reject`) — Requires administrative reason
  - `APPROVED` → `IN_PROGRESS` (`PATCH /api/admin/claims/{id}/start`)
  - `IN_PROGRESS` → `COMPLETED` (`PATCH /api/admin/claims/{id}/complete`)
  - Invalid transitions rejected with HTTP 400 Bad Request; terminal statuses (`COMPLETED`, `REJECTED`, `CANCELLED`) remain immutable.
- **Internal Administrative Notes:** Persisted in `claims.additional_information` via internal delimiter; customer-facing responses automatically strip admin notes to ensure customer privacy.
- **Frontend Admin Navigation & Route Guards:** `AdminRoute` guard, role-based post-login redirection (`/admin` vs `/dashboard`), and adaptive navigation header for `ADMIN` vs `CUSTOMER` users.
- **Automated Test Suite:** 37 automated admin unit & integration tests (`AdminServiceTest` and `AdminControllerTest`) validating KPIs, directories, state machine enforcement, customer 403 restrictions, and unauthenticated 401 checks (148 tests passing overall with 0 failures).
- **Developer Guides:**
  - [backend/ADMIN_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/ADMIN_MANAGEMENT.md) — Admin role authorization, REST APIs, KPI queries, state machine validation, and test coverage.
  - [frontend/ADMIN_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/ADMIN_MANAGEMENT.md) — Admin routes, `AdminRoute` guard, operations dashboard, claim adjudication UI, and confirmation modals.

### Phase 12: Dashboard Analytics & Notifications
- **Status:** **COMPLETED**
- **Customer Dashboard Telemetry (`GET /api/dashboard`):** Real database-backed aggregation pipeline delivering equipment summaries, warranty statuses (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`), claim metrics (`pending`, `completed`, etc.), and invoice counts.
- **Expiring Warranties Preview:** Customer dashboard displays up to 5 warranties expiring soonest with exact days remaining countdown badges and quick inspection links.
- **Recent Claims Preview:** Customer dashboard showcases latest submitted claims with real-time status badges and inspection links.
- **Authoritative Recent Activity Timeline:** Derives a unified, timestamped audit trail from real database records (products, invoices, claims) sorted chronologically.
- **Automated Notification Engine (`NotificationService`):**
  - PostgreSQL table `notifications` created in Supabase with user foreign key, type checks (`INFO`, `EXPIRATION`, `CLAIM_UPDATE`, `SYSTEM`), and performance indexes.
  - Triggered during lifecycle events: product registration, invoice upload, claim submission/cancellation, and admin adjudication status changes.
  - User-scoped queries with anti-IDOR protections returning HTTP 404 on unowned notification operations.
  - REST endpoints: `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all`.
- **Global Navbar Notification Bell:**
  - Real-time unread badge counter in `Navbar.jsx`.
  - Dropdown preview displaying recent notifications, relative timestamps, inline mark-as-read, and mark-all-as-read actions.
- **Dedicated Notifications Inbox (`/notifications`):**
  - Full-page user inbox with `All` vs `Unread` filtering tabs, lifecycle type badges, mark read/all read controls, and accessible empty/loading states.
- **Administrative Operational Telemetry (`AdminDashboardPage.jsx`):**
  - Retained all 16 authoritative platform KPIs established in Phase 11.
  - Added 3 live operational previews: Recent Claims Received, Recently Registered Users, and Recently Registered Equipment.
- **Automated Test Suite:** 21 automated unit & integration tests (`NotificationServiceTest`, `NotificationControllerTest`, `DashboardServiceTest`, `DashboardControllerTest`) bringing the backend test suite to **169 tests run with 0 failures and 0 errors**.
- **Frontend Build:** Verified with Vite production build passing with zero errors.
- **Developer Guides:**
  - [backend/NOTIFICATION_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/NOTIFICATION_MANAGEMENT.md)
  - [backend/DASHBOARD_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/DASHBOARD_MANAGEMENT.md)
  - [frontend/NOTIFICATION_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/NOTIFICATION_MANAGEMENT.md)
  - [frontend/DASHBOARD_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/DASHBOARD_MANAGEMENT.md)

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
- **Digital Invoice Vault:** Upload, view, and download purchase receipts (PDF, PNG, JPG up to 10 MB) securely stored in private cloud object storage.
- **Claim Submission & Tracking:** Submit warranty claims for defective products with transparent status tracking (`PENDING`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
- **Personal Dashboard & Alerts:** Comprehensive dashboard displaying active coverage metrics, quick invoice vault access, and 30-day proactive expiration warnings.

### For Administrators
- **Global Overview:** High-level platform KPIs covering active warranties, user accounts, and claim backlogs.
- **Auditing & Verification:** Search and verify registered products, serial numbers, and uploaded purchase documents.
- **Claim Adjudication Workflow:** Review, approve, reject, or update status of submitted customer warranty claims with confirmation dialogs and administrative notes.
- **User Management:** Oversee registered customer and administrator accounts with name and email search filtering.

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
[x] PHASE 9:  Invoice Management & Storage (Completed)
[x] PHASE 10: Warranty Claims Engine (Completed)
[x] PHASE 11: Admin Management Module (Completed)
[x] PHASE 12: Dashboard Analytics & Notifications (Completed)
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
- [backend/INVOICE_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/INVOICE_MANAGEMENT.md) — Backend invoice management entity architecture, Supabase storage integration, REST APIs, and test coverage.
- [backend/CLAIMS_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/CLAIMS_MANAGEMENT.md) — Backend warranty claims entity architecture, validation rules, REST APIs, and test coverage.
- [backend/ADMIN_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/ADMIN_MANAGEMENT.md) — Backend admin management entity architecture, REST APIs, state machine validation, and test coverage.
- [backend/NOTIFICATION_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/NOTIFICATION_MANAGEMENT.md) — Backend notification engine architecture, user-scoping, automated event triggers, and anti-IDOR tests.
- [backend/DASHBOARD_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/backend/DASHBOARD_MANAGEMENT.md) — Backend customer & admin dashboard telemetry aggregation, queries, and test coverage.
- [frontend/FRONTEND_SETUP.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/FRONTEND_SETUP.md) — Frontend developer guide, directory layout, commands, routes, and environment configuration.
- [frontend/AUTHENTICATION.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/AUTHENTICATION.md) — Frontend authentication architecture, JWT lifecycle, route guards, and test guide.
- [frontend/PRODUCT_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/PRODUCT_MANAGEMENT.md) — Frontend product routes, UI components, validation, and user flows.
- [frontend/WARRANTY_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/WARRANTY_MANAGEMENT.md) — Frontend warranty routes, UI components, accessible progress bars, and user flows.
- [frontend/INVOICE_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/INVOICE_MANAGEMENT.md) — Frontend invoice routes, upload modal, product invoice panel, and user flows.
- [frontend/CLAIMS_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/CLAIMS_MANAGEMENT.md) — Frontend warranty claim routes, submit modal, claim details timeline, and user flows.
- [frontend/ADMIN_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/ADMIN_MANAGEMENT.md) — Frontend admin routes, operations dashboard, claim adjudication UI, and confirmation modals.
- [frontend/NOTIFICATION_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/NOTIFICATION_MANAGEMENT.md) — Frontend notification bell dropdown, inbox page, filtering, and unread badges.
- [frontend/DASHBOARD_MANAGEMENT.md](file:///c:/Users/thari/OneDrive/Desktop/Tharik_project/frontend/DASHBOARD_MANAGEMENT.md) — Frontend customer dashboard KPIs, expiring previews, claims previews, activity timeline, and admin operational feeds.




