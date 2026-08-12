# WarrantyHub — Product Warranty Registration Portal

> *"Your Warranties. Organized. Protected. Always Accessible."*

---

## 📌 Project Overview

**WarrantyHub** (Product Warranty Registration Portal) is an enterprise-ready, full-stack web application engineered to centralize post-purchase product protection. It eliminates paper clutter, lost physical receipts, and forgotten warranty expiration dates by providing customers with a secure, digital vault for their products, automated warranty tracking, cloud-backed invoice storage, and streamlined warranty claim workflows.

---

## 🚦 Current Project Status: **PHASE 1 (Completed)**

The project is currently at **Phase 1 — Project Planning & Requirements**.

- ✅ **Phase 1 Scope**: Complete architecture, data modeling, API contract design, UI/UX specification, and workflow documentation.
- 🛑 **Code Generation Status**: Neither backend, frontend, nor database schemas have been generated yet, in accordance with the Phase 1 specification.
- 📄 **Full Specification Document**: Please refer to [PROJECT_REQUIREMENTS.md](PROJECT_REQUIREMENTS.md) for the exhaustive 20-section system requirements and architecture blueprint.

---

## 🛠️ Technology Stack

| Layer | Technologies Selected |
| :--- | :--- |
| **Frontend** | React.js (v18), Vite, JavaScript (ESNext), React Router (v6), Axios, Modern CSS System |
| **Backend** | Java 17 (LTS), Spring Boot 3.2.x, Spring Web, Spring Data JPA, Hibernate, Spring Security, JWT (JJWT), Maven |
| **Database** | PostgreSQL (hosted via Supabase) |
| **File Storage** | Supabase Storage (Dedicated invoices object bucket) |
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
[ ] PHASE 2:  Database Design & Supabase Setup
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

Detailed specifications, including database schemas, state machines, route definitions, REST API contracts, and architecture diagrams, are available in [PROJECT_REQUIREMENTS.md](PROJECT_REQUIREMENTS.md).
