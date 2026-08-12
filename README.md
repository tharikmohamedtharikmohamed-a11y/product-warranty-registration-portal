# WarrantyHub — Product Warranty Registration Portal

> *"Your Warranties. Organized. Protected. Always Accessible."*

---

## 📌 Project Overview

**WarrantyHub** (Product Warranty Registration Portal) is an enterprise-ready, full-stack web application engineered to centralize post-purchase product protection. It eliminates paper clutter, lost physical receipts, and forgotten warranty expiration dates by providing customers with a secure, digital vault for their products, automated warranty tracking, cloud-backed invoice storage, and streamlined warranty claim workflows.

---

## 🚦 Current Project Status: **PHASE 2 (Completed)**

### Phase 1: Project Planning & Requirements
- **Status:** **COMPLETED**
- **Scope Completed:** Complete system architecture, data modeling, REST API contracts, security blueprint, and Google Stitch UI/UX design specifications.
- **Specification Document:** [PROJECT_REQUIREMENTS.md](PROJECT_REQUIREMENTS.md)

### Phase 2: Database Design & Supabase Setup
- **Status:** **COMPLETED**
- **PostgreSQL Database Designed:** Normalized 3NF schema targeting PostgreSQL 17 on Supabase.
- **Required Tables Defined:**
  - \users\: Customer & Admin authentication credentials, roles (\CUSTOMER\, \ADMIN\), and timestamps.
  - \products\: Product metadata, purchase dates, serial numbers, and warranty durations.
  - \warranties\: 1-to-1 derived warranty tracking records with status (\ACTIVE\, \EXPIRING_SOON\, \EXPIRED\).
  - \invoices\: Metadata referencing stored purchase receipts in cloud object storage.
  - \claims\: Customer-filed warranty claim records with state machine status tracking.
- **Relationships Defined:** Strict foreign keys with safe deletion rules (\ON DELETE RESTRICT\ for users, products, invoices, and claims; \ON DELETE CASCADE\ for 1-to-1 product warranties).
- **Constraints Defined:** Primary keys (UUID \gen_random_uuid()\), unique constraints (email, serial lookups, storage path), and domain check constraints (prices, durations, file sizes <= 10MB, status enums).
- **Indexes Defined:** B-Tree indexing on foreign keys, email lookups, serial numbers, warranty expiry ranges, and claim statuses.
- **Supabase Storage Configured:** Private \invoices\ storage bucket created with a 10 MB file size limit and MIME-type restrictions (\pplication/pdf\, \image/jpeg\, \image/png\).
- **Security & RLS:** Row Level Security (RLS) enabled across all tables to block unauthorized PostgREST client queries while preserving full backend JDBC access for Spring Boot.
- **Database Artifacts:**
  - [database/schema.sql](database/schema.sql) — Full PostgreSQL DDL script with triggers and constraints.
  - [database/DATABASE_DESIGN.md](database/DATABASE_DESIGN.md) — Exhaustive database architecture, data dictionary, and storage documentation.

---

## 🛠️ Technology Stack

| Layer | Technologies Selected |
| :--- | :--- |
| **Frontend** | React.js (v18), Vite, JavaScript (ESNext), React Router (v6), Axios, Modern CSS System |
| **Backend** | Java 17 (LTS), Spring Boot 3.2.x, Spring Web, Spring Data JPA, Hibernate, Spring Security, JWT (JJWT), Maven |
| **Database** | PostgreSQL 17 (hosted via Supabase) |
| **File Storage** | Supabase Storage (Dedicated private invoices bucket) |
| **Testing** | JUnit 5, Spring Boot Test, Mockito, Postman, Browser Testing |
| **Version Control** | Git & GitHub |
| **Development Environment** | Antigravity IDE |

---

## 🚀 Key Features

### For Customers
- **Authentication & Security:** Secure JWT-based self-registration and login with BCrypt password encryption.
- **Product Registry:** Register purchased products with details like brand, model number, serial number, purchase date, price, and seller name.
- **Automated Warranty Tracking:** Instant calculation of expiry dates (purchase date + warranty duration) with real-time status badges (ACTIVE, EXPIRING_SOON, EXPIRED).
- **Digital Invoice Vault:** Upload and view purchase receipts (PDF, PNG, JPG up to 10 MB) securely stored in cloud object storage.
- **Claim Submission & Tracking:** Submit warranty claims for defective products with transparent status tracking (PENDING, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED).
- **Personal Dashboard & Alerts:** Comprehensive dashboard displaying active coverage metrics and 30-day proactive expiration warnings.

### For Administrators
- **Global Overview:** High-level platform KPIs covering active warranties, user accounts, and claim backlogs.
- **Auditing & Verification:** Search and verify registered products, serial numbers, and uploaded purchase documents.
- **Claim Adjudication Workflow:** Review, approve, reject, or update status of submitted customer warranty claims.
- **User Management:** Oversee registered users and system integrity.

---

## 🗺️ Roadmap & Development Phases

\\\
[x] PHASE 1:  Project Planning & Requirements (Completed)
[x] PHASE 2:  Database Design & Supabase Setup (Completed)
[ ] PHASE 3:  Backend Initial Setup (Spring Boot 3.2 / Java 17)
[ ] PHASE 4:  Backend Authentication & Security
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
\\\

---

## 📖 Documentation Reference

- [PROJECT_REQUIREMENTS.md](PROJECT_REQUIREMENTS.md) — Complete 20-section system requirements and architectural specification.
- [database/DATABASE_DESIGN.md](database/DATABASE_DESIGN.md) — Relational schema design, data dictionary, entity diagrams, and storage architecture.
- [database/schema.sql](database/schema.sql) — Executable PostgreSQL DDL schema with triggers and constraints.
