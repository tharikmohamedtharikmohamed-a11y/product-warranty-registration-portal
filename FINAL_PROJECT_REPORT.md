# Product Warranty Registration Portal — Final Project Report

**Project Title**: Product Warranty Registration Portal  
**Course**: College Capstone Project  
**Author**: Capstone Engineering Team  

---

## 1. Executive Summary & Problem Statement

Modern consumers frequently misplace physical paper receipts and warranty documents, leading to lost coverage, denied service claims, and friction when managing household electronics, appliances, and personal assets. 

The **Product Warranty Registration Portal** is an end-to-end full-stack web application designed to centralize and automate product registration, warranty lifecycle tracking, proof-of-purchase invoice management via cloud storage, and warranty claim processing. The system provides role-based interfaces for **Customers** and **Administrators**.

---

## 2. Technology Stack

### Backend
- **Language & Framework**: Java 21, Spring Boot 3.x
- **Build Tool**: Apache Maven
- **Security & Auth**: Spring Security, JWT (JSON Web Tokens), BCrypt Password Hashing
- **Persistence**: Spring Data JPA, Hibernate ORM
- **Database**: PostgreSQL (Hosted on Supabase)
- **Cloud Object Storage**: Supabase Storage (S3-compatible bucket integration)

### Frontend
- **Framework & Tooling**: React 18.2.0, Vite 5.4.21
- **Routing**: React Router v6
- **HTTP Client**: Axios 1.6.8 (with JWT Request Interceptor & 401 Response Interceptor)
- **Styling**: Vanilla CSS (Custom Design System, modern dark/light card aesthetics)

---

## 3. System Architecture & Workflows

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           REACT FRONTEND (Vite)                         │
│  Customer Portal (Dashboard, Products, Warranties, Invoices, Claims)    │
│  Admin Portal (Dashboard Stats, Claims Processing, Users, Products)     │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ HTTP / REST APIs (Axios + JWT)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT BACKEND                             │
│  Security Layer (JwtAuthenticationFilter, SecurityConfig)              │
│  Controllers (Auth, Product, Warranty, Invoice, Claim, Admin)           │
│  Services & Repositories (Data validation, Business Logic, JPA)        │
└──────────────────┬──────────────────────────────────────┬───────────────┘
                   │ JDBC / JPA                           │ HTTP API
                   ▼                                      ▼
┌─────────────────────────────────────┐ ┌─────────────────────────────────┐
│     Supabase PostgreSQL Database    │ │    Supabase Cloud Object Storage│
│  (Users, Products, Warranties,      │ │   (Invoice Files / Presigned    │
│   Invoices, Warranty Claims)        │ │    Download Signed URLs)         │
└─────────────────────────────────────┘ └─────────────────────────────────┘
```

---

## 4. Key Modules & Features

### A. Authentication & Role-Based Authorization
- **Registration**: Allows new customers to register securely.
- **Login**: Validates credentials via BCrypt and issues a signed JWT token.
- **Role System**: Supports `CUSTOMER` and `ADMIN` roles.
- **Frontend Route Protection**: `<ProtectedRoute />` handles customer access; `<AdminRoute />` blocks non-admin customers from admin routes.

### B. Customer Product & Warranty Management
- **Product Registration**: Customers register products with brand, serial number, purchase date, and price.
- **Automatic Warranty Calculation**: The backend automatically generates a linked warranty record (e.g. 12 months default) with active/expiring/expired status.
- **Product Actions**: Customers can view, edit, or delete their owned products.

### C. Invoice Upload & Cloud Storage
- **Invoice Upload**: Supports PDF, JPG, and PNG proof-of-purchase documents up to 10MB.
- **Supabase Storage Integration**: Invoice files are securely streamed to cloud storage; metadata is saved in PostgreSQL.
- **Secure Download**: Download/viewing uses presigned signed URLs generated on-demand.

### D. Customer Warranty Claims
- **Claim Submission**: Customers can file claims for eligible active products/warranties.
- **Duplicate Claim Prevention**: Backend enforces that no multiple active claims (`PENDING` or `IN_PROGRESS`) exist for the same warranty.
- **Claim Timeline & Cancellation**: Customers track claim status and can cancel pending claims.

### E. Admin Dashboard & Claim Processing
- **System Statistics**: Displays real system metrics (Total Users, Products, Warranties, Active Warranties, Invoices, Claims Breakdown).
- **Claim Processing**: Admin reviews submitted claims, attached invoices, and updates status (`APPROVED`, `REJECTED`, `IN_PROGRESS`, `COMPLETED`) with resolution notes.
- **System Management Views**: Admin tables for all Users, Products, Warranties, and Invoices.

---

## 5. Security & Safety Controls

1. **Credential Safety**: No Supabase service keys, JWT secrets, or DB passwords are present in frontend source code. Frontend `.env` only contains `VITE_API_BASE_URL`.
2. **Backend Enforcement**: Product, warranty, invoice, and claim ownership is strictly verified by Spring Boot before returning data or modifying state.
3. **Password Security**: Passwords are saved as BCrypt hashes in PostgreSQL and stripped from JWT tokens/API responses.

---

## 6. How to Run the Application

### Prerequisites
- JDK 21
- Node.js v18+ & npm
- Maven 3.x

### Running the Backend (Spring Boot)
```bash
# Navigate to project root
cd d:/tharik_project

# Run unit tests
mvn clean test

# Run application
mvn spring-boot:run
```
Backend will start on `http://localhost:8080`.

### Running the Frontend (React + Vite)
```bash
# Navigate to frontend folder
cd d:/tharik_project/frontend

# Install dependencies
npm install

# Run development server
npm run dev
```
Frontend will start on `http://localhost:5173`.

---

## 7. Final Verification Results

| Test Suite / Objective | Status | Result |
|-----------------------|--------|--------|
| **Backend Unit Tests (`mvn test`)** | **PASS** | **55 / 55 Passed (0 Failures, 0 Errors, BUILD SUCCESS)** |
| **Frontend Build (`npm run build`)** | **PASS** | **0 Errors (`✓ built in 2.01s`)** |
| **Authentication & JWT** | **PASS** | Tokens issued, validated, and removed on logout |
| **Customer Route Protection** | **PASS** | Unauthenticated requests redirected to `/login` |
| **Admin Route Protection** | **PASS** | Non-admin customers blocked and redirected to `/unauthorized` |
| **Product & Warranty Engine** | **PASS** | Products created with automatic warranty calculation |
| **Invoice Cloud Storage** | **PASS** | File uploads to Supabase & signed download URL generation |
| **Warranty Claims Engine** | **PASS** | Real claim creation, duplicate protection, & claim lifecycle |
| **Admin Processing Flow** | **PASS** | Status updates & resolution notes saved and reflected on customer side |
