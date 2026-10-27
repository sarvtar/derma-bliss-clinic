# Derma Bliss Clinic Management System — Architecture & System Design

## 1. Executive Summary & Clinic Profile

Derma Bliss Clinic is a specialized single-doctor dermatology, cosmetology, and trichology clinic operating in Thane, Maharashtra, India. This software system provides a unified clinical and administrative management platform.

### Official Clinic Configuration
- **Clinic Name:** Derma Bliss Clinic
- **Doctor:** Dr. Ankita Santosh Koli
- **Qualifications:** BHMS, PGDMS, CCH, CGO, CSD
- **Specialization:** Cosmetologist & Trichologist
- **Medical Registration Number:** 73892
- **Core Clinical Services:** Skin Care, Hair Care, Laser Treatments
- **Official Contact Number:** +91 789 789 2177
- **Clinic Address:** DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.
- **Instagram Handle:** `@derma_bliss_clinic`
- **Official Tagline:** `DERMA BLISS — SKIN | HAIR | LASER`
- **Default Timezone:** `Asia/Kolkata` (IST, UTC+05:30)

---

## 2. High-Level Architecture Overview

The system strictly adheres to a centralized 3-tier architecture with two independent client surfaces communicating with a unified Spring Boot 3 backend over HTTPS REST APIs:

```
+------------------------------------+        +------------------------------------+
|            Admin Website           |        |         Patient Mobile App         |
|      (HTML5 / CSS3 / JS ES6+)      |        |       (React Native / TS)          |
|    Used by Dr. Ankita S. Koli     |        |      Used by Clinic Patients       |
+-----------------+------------------+        +-----------------+------------------+
                  |                                             |
                  | HTTPS (REST API + JSON)                     | HTTPS (REST API + JSON)
                  | Authorization: Bearer <JWT>                 | Authorization: Bearer <JWT>
                  +----------------------+----------------------+
                                         |
                                         v
                         +-------------------------------+
                         |      Spring Boot 3 Backend    |
                         |           (Java 21)           |
                         |  - Spring Web (REST API)      |
                         |  - Spring Security (JWT, RBAC)|
                         |  - Spring Data JPA (Hibernate)|
                         |  - Flyway Database Migration  |
                         +---------------+---------------+
                                         |
                                         | JDBC / TLS (Connection Pool: HikariCP)
                                         v
                         +-------------------------------+
                         |        MySQL 8 Database       |
                         |  - Normalized InnoDB Engine   |
                         |  - Strict Foreign Keys        |
                         |  - Concurrency Integrity Locks|
                         |  - UTC Event Timestamps       |
                         +-------------------------------+
```

### Architectural Tenets
1. **Zero Direct Database Connections:** Clients (admin web and mobile app) NEVER access MySQL directly; every operation passes through the authenticated and authorized Spring Boot REST API.
2. **Single Doctor Domain Model:** No multi-doctor search, doctor comparison, multi-branch routing, or pharmacy inventory. The clinic schedule, records, and prescriptions revolve around Dr. Ankita Santosh Koli.
3. **Decoupled Client Frontends:** The Admin website is lightweight, modern, responsive vanilla HTML/CSS/JS without heavyweight frontend framework overhead, prioritizing administrative velocity, readability, and security. The patient mobile app is a single cross-platform React Native codebase.
4. **Defense-in-Depth:** Authorization is enforced at the backend service layer via Spring Security method security and explicit patient ownership validation.

---

## 3. Backend Layered Architecture

The Spring Boot backend adopts a clean hexagonal/layered design with strict boundary separation:

```
[ HTTP Requests ]
       │
       ▼
┌────────────────────────────────────────────────────────┐
│  Controller Layer (REST Endpoints)                    │
│  - DTO validation via Jakarta Bean Validation (@Valid) │
│  - HTTP status codes & standard API response envelope  │
│  - No business logic or SQL queries                   │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│  Security Layer (Filters & Interceptors)               │
│  - JwtAuthenticationFilter (Extract & Validate Bearer) │
│  - RateLimitingFilter (Bucket4j / In-memory Token)     │
│  - SecurityContextHolder population                    │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│  Service Layer (Business Logic & Transactions)         │
│  - @Transactional transaction demarcation              │
│  - Concurrency slot reservation & locking              │
│  - IDOR ownership validation                           │
│  - Audit log event emission                            │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│  Repository Layer (Spring Data JPA)                    │
│  - Spring Data JPA Repositories                        │
│  - Explicit JPQL / Native queries with prepared params │
│  - Pessimistic write locks (LockModeType.PESSIMISTIC_WRITE)
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│  Database Layer (MySQL 8)                              │
│  - Flyway managed DDL/DML schema                       │
│  - InnoDB ACID transactions                            │
└────────────────────────────────────────────────────────┘
```

---

## 4. Security & Authentication Architecture

### Identity & Role-Based Access Control (RBAC)
The system defines two mutually exclusive roles:
1. `ROLE_ADMIN`:
   - Granted exclusively to Dr. Ankita Santosh Koli.
   - Access to clinic-wide patient records, schedule configuration, digital prescriptions, consultation records, queue management, billing reports, and audit logs.
   - Initial bootstrap through secure environment-variable injection (`ADMIN_BOOTSTRAP_PASSWORD`), never committed to source code.
2. `ROLE_PATIENT`:
   - Granted to self-registered patients.
   - Strictly restricted to own patient records, appointments, prescriptions, invoices, and queue tokens.

### Token Lifecycle & Session Management
- **Access Tokens:** Short-lived JSON Web Tokens (15-minute lifespan) signed using HMAC-SHA256 (or RSA-256) containing `sub` (user_id), `role`, and `iss`.
- **Refresh Tokens:** Long-lived tokens (7-day lifespan) stored as cryptographic SHA-256 hashes in MySQL (`refresh_tokens` table) with device metadata, expiry timestamp, and revocation flags.
- **Client Storage:**
  - *Admin Website:* Managed via secure, SameSite=Strict, HttpOnly cookies for session tokens or memory-held Bearer tokens refreshed via rotating refresh credentials.
  - *Mobile App:* Stored in OS-native secure hardware storage (`Expo SecureStore` / `react-native-keychain` / Android Keystore / iOS Keychain Services). Never in plain `AsyncStorage`.
- **Password Security:** Salted BCrypt hashing with work factor 12.

---

## 5. Appointment Booking & Concurrency Design

### Double-Booking Prevention Strategy
To ensure that concurrent booking requests can never reserve the same doctor appointment slot:
1. **Slot Resolution:** Available slots are dynamically generated from `doctor_availability` minus `blocked_slots` minus active `appointments`.
2. **Atomic Reservation Transaction:** When a patient submits a booking:
   - Begin `@Transactional(isolation = Isolation.READ_COMMITTED)`.
   - Acquire a pessimistic write lock or check slot availability using:
     ```sql
     SELECT COUNT(*) FROM appointments 
     WHERE appointment_date = :date 
       AND start_time = :startTime 
       AND status IN ('CONFIRMED', 'RESCHEDULED') 
     FOR UPDATE;
     ```
   - If count > 0, throw a `SlotConflictException` (HTTP 409 Conflict).
   - Insert new appointment with status `CONFIRMED`.
   - The database also enforces slot uniqueness through a composite unique constraint or filtered active index.
   - Commit transaction.
3. **Automatic Confirmation:** Successful reservations immediately transition to `CONFIRMED` status without manual doctor intervention.

---

## 6. Timezone Handling Policy

- **Storage Standard:** All timestamps (`created_at`, `updated_at`, `login_time`, `audit_time`) are persisted in UTC (`TIMESTAMP` or `DATETIME` UTC).
- **Appointment Dates & Times:** Stored as `DATE` (YYYY-MM-DD) and `TIME` (HH:MM:SS) representing clinic wall-clock time in the configured clinic timezone (`Asia/Kolkata`).
- **Client Presentation:** The backend provides ISO-8601 strings and configured timezone metadata, ensuring accurate rendering across all devices.

---

## 7. Medical Document Storage & Privacy

1. **Storage Isolation:** Uploaded reports (PDFs, JPGs, PNGs) are saved to an isolated private local directory or private object store (`storage/medical-documents/`).
2. **Filename Sanitization:** Stored files are assigned random UUID names (e.g. `e4d909c2-9b2f-48d6-931b-789a5e8f4f31.pdf`) to eliminate directory traversal and enumeration attacks.
3. **No Direct Public URLs:** Static file serving is disabled for document paths. Every document download is routed through `GET /api/v1/medical-documents/{id}/download` requiring authentication and authorization (patient ownership or doctor role).
