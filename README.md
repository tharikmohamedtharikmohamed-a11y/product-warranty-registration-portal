# Product Warranty Registration Portal

A robust, enterprise-ready Java Spring Boot REST API backend designed for digital product warranty registration, purchase invoice management using Supabase Storage, warranty claims tracking, and administrator management.

---

## Project Description

Customers frequently misplace physical paper receipts, lose warranty cards, or forget exact warranty expiration dates for high-value electronics and household appliances. When products break down, filing warranty claims becomes stressful and error-prone due to missing documentation.

The **Product Warranty Registration Portal** solves this problem by providing a centralized digital portal where customers can:
- Digitally register their purchased products (brand, model, serial number, price, purchase date).
- Track automated warranty start dates, end dates, and real-time status (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`).
- Upload and securely store digital purchase invoices (PDF, JPG, PNG up to 10MB) via Supabase Storage.
- Submit, track, and manage warranty claims with automated status progression.

Administrators have access to management tools, user audits, warranty inspection, status transition rules, and dashboard analytics.

---

## Technology Stack

- **Core Backend Framework**: Java 17, Spring Boot 3.2.3
- **Build System**: Apache Maven
- **Database & Persistence**: Supabase PostgreSQL, Spring Data JPA, Hibernate ORM, H2 Database (for automated test suite)
- **Cloud Storage**: Supabase Storage REST API (`product-invoices` bucket)
- **Security & Authorization**: Spring Security, JWT (JSON Web Tokens), BCrypt Password Hashing
- **REST & Serialization**: Jackson JSON, Spring WebMVC, Jakarta Validation (`@Valid`, `@NotBlank`, `@Email`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test (`MockMvc`), H2 In-Memory DB

---

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│              Frontend Web / Mobile Application              │
│                 (React / Angular / Vue / iOS)               │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTPS / REST API / JWT
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend Service                 │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Security Filter Chain (JWT Auth, BCrypt, CORS, RBAC)    │ │
│ └────────────────────────────┬────────────────────────────┘ │
│ ┌────────────────────────────▼────────────────────────────┐ │
│ │ Controller Layer (/api/auth, /products, /claims, etc.)  │ │
│ └────────────────────────────┬────────────────────────────┘ │
│ ┌────────────────────────────▼────────────────────────────┐ │
│ │ Service Layer (Business Logic, Ownership Validation)    │ │
│ └───────────────────────────┬─┴───────────────────────────┘ │
└─────────────────────────────┼───────────────────────────────┘
                              │
               ┌──────────────┴──────────────┐
               │                             │
               ▼                             ▼
┌──────────────────────────────┐ ┌──────────────────────────────┐
│     Supabase PostgreSQL      │ │       Supabase Storage       │
│  (Users, Products, Claims)   │ │     (`product-invoices`)     │
└──────────────────────────────┘ └──────────────────────────────┘
```

---

## Features

### Customer Features
- **Account Registration & Login**: Secure registration (`CUSTOMER` role default) and BCrypt password hashing.
- **Product Registration**: Register purchased products with unique serial numbers and automatic warranty computation.
- **Warranty Management**: Real-time status calculation (`ACTIVE`, `EXPIRING_SOON` <= 30 days, `EXPIRED`).
- **Invoice Upload & Download**: Upload PDF/JPG/PNG invoices (max 10MB) to Supabase Storage with signed download links.
- **Warranty Claims**: Submit warranty claims for valid warranties, view claim history, and cancel eligible `PENDING` claims.

### Administrator Features (`ROLE_ADMIN`)
- **User Audit**: View complete user directory and detailed profile attributes.
- **Product & Warranty Inspection**: View all registered products and warranties system-wide.
- **Claim Management**: Process warranty claims and update statuses (`PENDING` -> `APPROVED` / `REJECTED` / `IN_PROGRESS` -> `COMPLETED`) with state transition validation.
- **Invoice Metadata & Access**: View invoice upload records and generate admin signed download URLs.
- **Dashboard Statistics**: Real-time summary metrics (`totalUsers`, `totalProducts`, `activeWarranties`, `pendingClaims`, etc.) via efficient database count queries.

---

## API Endpoint Summary Table

| Category | Method | Endpoint | Authorization | Description |
|---|---|---|---|---|
| **Health** | `GET` | `/api/health` | Public | System status and database connectivity health check |
| **Auth** | `POST` | `/api/auth/register` | Public | Register new customer account |
| **Auth** | `POST` | `/api/auth/login` | Public | Login and receive JWT access token |
| **Products** | `POST` | `/api/products` | Customer | Register product & auto-create warranty |
| **Products** | `GET` | `/api/products` | Customer | List authenticated customer's products |
| **Products** | `GET` | `/api/products/{id}` | Customer | Get customer product by ID (IDOR protected) |
| **Products** | `PUT` | `/api/products/{id}` | Customer | Update customer product details |
| **Products** | `DELETE` | `/api/products/{id}` | Customer | Delete product & associated warranty/invoices |
| **Warranties**| `GET` | `/api/warranties` | Customer | List authenticated customer's warranties |
| **Warranties**| `GET` | `/api/warranties/{id}` | Customer | Get warranty by ID |
| **Warranties**| `GET` | `/api/products/{productId}/warranty` | Customer | Get warranty for specific product |
| **Invoices** | `POST` | `/api/invoices/upload` | Customer | Upload purchase invoice file to Supabase Storage |
| **Invoices** | `GET` | `/api/invoices` | Customer | List customer's invoice metadata |
| **Invoices** | `GET` | `/api/invoices/{id}` | Customer | Get invoice metadata by ID |
| **Invoices** | `GET` | `/api/invoices/{id}/download` | Customer | Generate signed download link for invoice |
| **Invoices** | `DELETE` | `/api/invoices/{id}` | Customer | Delete invoice metadata & Supabase storage object |
| **Claims** | `POST` | `/api/claims` | Customer | Submit warranty claim for valid active warranty |
| **Claims** | `GET` | `/api/claims` | Customer | List customer's warranty claims |
| **Claims** | `GET` | `/api/claims/{id}` | Customer | Get warranty claim details by ID |
| **Claims** | `PUT` | `/api/claims/{id}/cancel` | Customer | Cancel eligible `PENDING` warranty claim |
| **Admin** | `GET` | `/api/admin/users` | Admin (`ROLE_ADMIN`) | List all portal users |
| **Admin** | `GET` | `/api/admin/users/{id}` | Admin (`ROLE_ADMIN`) | Get user details by ID |
| **Admin** | `GET` | `/api/admin/products` | Admin (`ROLE_ADMIN`) | List all registered products across portal |
| **Admin** | `GET` | `/api/admin/warranties` | Admin (`ROLE_ADMIN`) | List all warranties across portal |
| **Admin** | `GET` | `/api/admin/claims` | Admin (`ROLE_ADMIN`) | List all warranty claims across portal |
| **Admin** | `GET` | `/api/admin/claims/{id}` | Admin (`ROLE_ADMIN`) | Get warranty claim details by ID |
| **Admin** | `PUT` | `/api/admin/claims/{id}/status` | Admin (`ROLE_ADMIN`) | Update claim status & resolution notes |
| **Admin** | `GET` | `/api/admin/invoices` | Admin (`ROLE_ADMIN`) | List all invoice metadata across portal |
| **Admin** | `GET` | `/api/admin/invoices/{id}/download` | Admin (`ROLE_ADMIN`) | Generate admin signed download link |
| **Admin** | `GET` | `/api/admin/dashboard/stats` | Admin (`ROLE_ADMIN`) | Get dashboard summary metrics |

---

## Environment Variables

Configure the following environment variables prior to running the application. Placeholder defaults are defined in `src/main/resources/application.properties`.

```env
# Database Credentials
SUPABASE_DB_URL=jdbc:postgresql://<YOUR_SUPABASE_HOST>:5432/postgres
SUPABASE_DB_USERNAME=postgres
SUPABASE_DB_PASSWORD=<YOUR_DATABASE_PASSWORD>

# Supabase Storage & API Credentials
SUPABASE_URL=https://<YOUR_PROJECT_REF>.supabase.co
SUPABASE_SERVICE_KEY=<YOUR_SUPABASE_SERVICE_KEY>
SUPABASE_STORAGE_BUCKET=product-invoices

# JWT Security
JWT_SECRET=<YOUR_HIGH_ENTROPY_BASE64_JWT_SECRET_KEY>
JWT_EXPIRATION=86400000

# CORS Allowed Origins
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:4200
```

---

## Running the Project

### Prerequisites
- **Java**: OpenJDK 17 or Java 21 installed (`java -version`).
- **Maven**: Maven 3.8+ installed (or use included Maven Wrapper `mvnw` / `mvnw.cmd`).

### 1. Build and Run Automated Tests
```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### 2. Run the Application Locally
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The application will start on port `8080` (accessible at `http://localhost:8080`).

---

## Testing with Postman

A complete, ready-to-import Postman collection is included in the project root:
- File: [`warranty_portal_postman_collection.json`](file:///d:/tharik_project/warranty_portal_postman_collection.json)

### Instructions:
1. Import `warranty_portal_postman_collection.json` into Postman.
2. Set environment variables `baseUrl` (`http://localhost:8080`), `customerToken`, and `adminToken`.
3. Execute authentication requests (`Register Customer`, `Login Customer`, `Login Admin`) to auto-populate token variables.
