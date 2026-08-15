# WarrantyHub — Backend Setup & Developer Guide
**Document Version:** 1.0.0  
**Phase:** Phase 3 — Backend Initial Setup  
**Application:** Product Warranty Registration Portal  
**Brand:** WarrantyHub  

---

## 1. Environment & Runtime Specifications

| Component | Target Specification | Verified Local Version |
| :--- | :--- | :--- |
| **Java Development Kit** | **Java 17 LTS** | OpenJDK 17.0.20 (Temurin-17.0.20+8) |
| **Build Tool** | **Apache Maven 3.9.x** | Apache Maven 3.9.6 |
| **Framework** | **Spring Boot 3.2.x** | Spring Boot 3.2.5 |
| **Persistence Engine** | **Spring Data JPA / Hibernate 6.x** | Hibernate 6.4.4.Final |
| **Database** | **PostgreSQL 17** | Supabase Project (`warranty-hub` / `bfogwrprtsfvsgmbzvdp`) |
| **Server Port** | **8080** | `http://localhost:8080` |

---

## 2. Package Architecture

The backend codebase adheres strictly to standard layered architecture conventions under `com.warrantyportal`:

```
backend/
├── pom.xml
├── .env.example
├── BACKEND_SETUP.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── warrantyportal/
│   │   │           ├── WarrantyPortalApplication.java   # Main Spring Boot Runner
│   │   │           ├── config/
│   │   │           │   └── WebConfig.java               # CORS (http://localhost:5173)
│   │   │           ├── controller/
│   │   │           │   └── HealthController.java        # GET /api/health
│   │   │           ├── dto/                             # Request/Response payloads (Phase 4+)
│   │   │           ├── entity/                          # JPA Entities (Phase 4+)
│   │   │           ├── repository/                      # Spring Data JPA interfaces (Phase 4+)
│   │   │           ├── security/                        # Spring Security & JWT (Phase 4)
│   │   │           └── service/                         # Business logic services (Phase 4+)
│   │   └── resources/
│   │       └── application.properties                   # Main configuration
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── warrantyportal/
│       │           ├── WarrantyPortalApplicationTests.java # Context loading test
│       │           └── controller/
│       │               └── HealthControllerTest.java    # Health endpoint test
│       └── resources/
│           └── application.properties                   # Test configuration
```

---

## 3. Required Environment Variables

The backend relies on environment variables for externalizing database credentials. Secrets must **never** be committed to Git.

| Variable Name | Description | Example / Default |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC Connection URL to PostgreSQL database | `jdbc:postgresql://db.bfogwrprtsfvsgmbzvdp.supabase.co:5432/postgres?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password set in Supabase Dashboard | *(your_supabase_db_password)* |
| `SERVER_PORT` | HTTP server port | `8080` |

### Setting Environment Variables

#### Windows PowerShell:
```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://db.bfogwrprtsfvsgmbzvdp.supabase.co:5432/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "your_actual_password_here"
```

#### Windows Command Prompt (CMD):
```cmd
set SPRING_DATASOURCE_URL=jdbc:postgresql://db.bfogwrprtsfvsgmbzvdp.supabase.co:5432/postgres?sslmode=require
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=your_actual_password_here
```

#### Linux / macOS:
```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://db.bfogwrprtsfvsgmbzvdp.supabase.co:5432/postgres?sslmode=require"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="your_actual_password_here"
```

---

## 4. Database & Connection Pool Configuration

### 4.1 Supabase PostgreSQL Connection Details
- **Project Ref:** `bfogwrprtsfvsgmbzvdp`
- **Host:** `db.bfogwrprtsfvsgmbzvdp.supabase.co`
- **Port:** `5432` (Direct) or `6543` (Connection Pooler)
- **SSL Mode:** `sslmode=require`

### 4.2 HikariCP Pool Settings
- **Maximum Pool Size:** 10 connections
- **Minimum Idle:** 2 connections
- **Connection Timeout:** 30,000 ms (30 seconds)
- **Idle Timeout:** 300,000 ms (5 minutes)
- **Max Lifetime:** 1,200,000 ms (20 minutes)

### 4.3 Hibernate Validation Mode
Hibernate is configured with `spring.jpa.hibernate.ddl-auto=validate`.  
This strictly validates database schemas against application models without running destructive table drops or recreations. The source of truth for the database schema remains `database/schema.sql`.

---

## 5. Build, Test, & Execution Commands

### 5.1 Run Automated Tests
```powershell
cd backend
mvn clean test
```
*Expected Output:* `BUILD SUCCESS`, `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`

### 5.2 Start Backend Application
```powershell
cd backend
mvn spring-boot:run
```
*Expected Output:* `Tomcat started on port 8080 (http) with context path ''`, `Started WarrantyPortalApplication in ... seconds`

---

## 6. Health & Verification Endpoints

### 6.1 Application Health Endpoint
- **URL:** `http://localhost:8080/api/health`
- **Method:** `GET`
- **Authentication:** Public (No authentication required)
- **Response Format:**
```json
{
  "status": "UP",
  "application": "WarrantyHub"
}
```

### 6.2 Spring Boot Actuator Telemetry
- **URL:** `http://localhost:8080/actuator/health`
- **Method:** `GET`
- **Response:** Detailed health components (Disk space, Ping, and PostgreSQL DataSource connectivity).

---

## 7. Troubleshooting Common Connection Issues

### 1. `The server requested SCRAM-based authentication, but no password was provided`
- **Cause:** `SPRING_DATASOURCE_PASSWORD` has not been set in the active terminal environment.
- **Remedy:** Set the environment variable before launching the application:
  ```powershell
  $env:SPRING_DATASOURCE_PASSWORD = "your_supabase_password"
  mvn spring-boot:run
  ```

### 2. `Connection to db.xxx.supabase.co:5432 refused / timed out`
- **Cause:** Local firewall or ISP blocking outbound port 5432, or network connectivity issue.
- **Remedy:** Use the Supabase session pooler on port 6543:
  ```powershell
  $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require"
  $env:SPRING_DATASOURCE_USERNAME = "postgres.bfogwrprtsfvsgmbzvdp"
  ```

### 3. `Java major version mismatch`
- **Cause:** Multiple JDKs installed; system defaulting to an older or newer Java release.
- **Remedy:** Verify with `java -version` and `mvn -version`. Ensure `JAVA_HOME` points to JDK 17 (e.g., `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot`).
