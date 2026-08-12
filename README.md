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
- **Phase 1 (Current)**: Project Foundation, Supabase PostgreSQL configuration, REST Health Check endpoint, Exception Handling framework.
- **Phase 2**: User Authentication & JWT Security (Customer & Admin roles).
- **Phase 3**: Product Registration & Warranty Management (CRUD APIs).
- **Phase 4**: Invoice Upload (Cloud Storage / Supabase Storage) & Expiry Notifications.
- **Phase 5**: Admin Dashboard & Warranty Claim Tracking.

---

## Project Architecture

```
warranty-portal/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/warrantyportal/
│   │   │       ├── config/               # Database and Application Configurations
│   │   │       ├── controller/           # REST Controllers
│   │   │       ├── dto/                  # Data Transfer Objects
│   │   │       ├── entity/               # JPA Entities (Future Phase)
│   │   │       ├── exception/            # Global Exception Handling
│   │   │       ├── repository/           # Spring Data JPA Repositories (Future Phase)
│   │   │       ├── service/              # Business Logic Layer (Future Phase)
│   │   │       └── WarrantyPortalApplication.java
│   │   └── resources/
│   │       └── application.properties    # Configuration Properties
├── .env.example                          # Environment Variables Example Template
├── .gitignore                            # Git Ignore Configuration
├── pom.xml                               # Maven Build Dependencies
└── README.md                             # Project Documentation
```

---

## Database Design

### Overview of Database Entities & Tables
The database schema consists of 5 core entities designed with UUID primary keys and relational integrity tailored for Supabase PostgreSQL:

1. **`users` (`User.java`)**:
   - Stores user accounts (Customers and Admins).
   - Primary key: `id` (UUID).
   - Unique constraints: `email`.
   - Enum field: `role` (`CUSTOMER`, `ADMIN`).

2. **`products` (`Product.java`)**:
   - Represents physical items purchased by customers.
   - Primary key: `id` (UUID).
   - Foreign key: `user_id` -> `users(id)`.
   - Unique constraint: `serial_number`.

3. **`warranties` (`Warranty.java`)**:
   - Represents the digital warranty contract bound to a registered product.
   - Primary key: `id` (UUID).
   - Foreign keys: `product_id` -> `products(id)` (1:1), `user_id` -> `users(id)`.
   - Enum field: `status` (`ACTIVE`, `EXPIRING_SOON`, `EXPIRED`).

4. **`invoices` (`Invoice.java`)**:
   - Stores proof of purchase metadata (file name & Supabase Storage path).
   - Primary key: `id` (UUID).
   - Foreign keys: `product_id` -> `products(id)`, `user_id` -> `users(id)`.

5. **`warranty_claims` (`WarrantyClaim.java`)**:
   - Tracks service & repair requests filed by users for covered products.
   - Primary key: `id` (UUID).
   - Foreign keys: `product_id` -> `products(id)`, `warranty_id` -> `warranties(id)`, `user_id` -> `users(id)`.
   - Enum field: `status` (`PENDING`, `APPROVED`, `REJECTED`, `COMPLETED`).

---

### Entity Relationship Diagram

```text
       +------------------+
       |      USER        |
       +------------------+
       | id (UUID)        |
       | name             |
       | email (UNIQUE)   |
       | role (ENUM)      |
       +--------+---------+
                |
                | 1:N
   +------------+------------+--------------------+
   |                         |                    |
   v 1:N                     v 1:N                v 1:N
+--+---------------+   +-----+-------+   +--------+---------+
|    PRODUCT       |   |   INVOICE   |   | WARRANTY CLAIM   |
+------------------+   +-------------+   +------------------+
| id (UUID)        |   | id (UUID)   |   | id (UUID)        |
| productName      |   | fileName    |   | issueDescription |
| serialNo(UNIQUE) |   | storagePath |   | status (ENUM)    |
+--------+---------+   +-------------+   +------------------+
         |
         | 1:1
         v
+--------+---------+
|    WARRANTY      |
+------------------+
| id (UUID)        |
| status (ENUM)    |
| start/end dates  |
+------------------+
```

---

### Simple Student Explanation
Think of this database like a digital locker for your tech gear:
1. **User**: You (the customer) create an account.
2. **Product**: You register a gadget you bought (like a laptop or phone) under your account.
3. **Warranty**: Every product gets a digital warranty card attached to it showing when your warranty starts and expires.
4. **Invoice**: You upload a picture of your receipt. The app saves the image securely in Supabase Cloud Storage and keeps a reference in the `invoices` table.
5. **Warranty Claim**: If your gadget breaks down while the warranty is `ACTIVE`, you file a claim ticket (`warranty_claims`). The admin updates the claim status from `PENDING` to `APPROVED` or `COMPLETED`.

---

## Supabase Setup Instructions

Follow these step-by-step instructions to connect this application to Supabase PostgreSQL:

1. **Sign Up / Log In to Supabase**:
   - Navigate to [https://supabase.com/](https://supabase.com/) and log in or create a free account.

2. **Create a New Project**:
   - Click on **New Project**.
   - Choose your organization, set a **Project Name** (e.g., `warranty-portal`), set a secure **Database Password**, and select a region closest to you.
   - Click **Create new project** and wait for deployment to complete (~2 minutes).

3. **Obtain PostgreSQL Connection Info**:
   - Go to **Project Settings** (gear icon in the left sidebar) -> **Database**.
   - Scroll down to the **Connection Info** section.
   - Locate the **Host**, **Database name** (`postgres`), **Port** (`5432`), and **User** (`postgres`).
   - Copy the Connection String (JDBC URL):
     `jdbc:postgresql://db.<YOUR-PROJECT-REF>.supabase.co:5432/postgres`

---

## Environment Variables

Do NOT commit your real database credentials to Git. Set the following environment variables on your system or run environment:

| Variable Name | Description | Example / Value |
|---|---|---|
| `SUPABASE_DB_URL` | Supabase JDBC URL | `jdbc:postgresql://db.xxxxxx.supabase.co:5432/postgres` |
| `SUPABASE_DB_USERNAME` | Supabase DB User | `postgres` |
| `SUPABASE_DB_PASSWORD` | Supabase DB Password | `YourSecurePassword123` |

### Setting Environment Variables locally:

**PowerShell (Windows)**:
```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://db.xxxxxx.supabase.co:5432/postgres"
$env:SUPABASE_DB_USERNAME="postgres"
$env:SUPABASE_DB_PASSWORD="your_password"
```

**Bash / Linux / macOS**:
```bash
export SUPABASE_DB_URL="jdbc:postgresql://db.xxxxxx.supabase.co:5432/postgres"
export SUPABASE_DB_USERNAME="postgres"
export SUPABASE_DB_PASSWORD="your_password"
```

---

## How to Run

### Prerequisites
- JDK 17 or higher installed.
- Apache Maven installed.

### Build Project
```bash
mvn clean package
```

### Run Application
```bash
mvn spring-boot:run
```

---

## API Health Check

Once the application is running on port `8080`, test the health check endpoint:

**Request**:
```http
GET http://localhost:8080/api/health
```

**Response (JSON)**:
```json
{
  "status": "UP",
  "message": "Product Warranty Registration Portal is running",
  "timestamp": "2026-08-13T19:48:00"
}
```
