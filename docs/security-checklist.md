# Derma Bliss Clinic — Security Design & Compliance Checklist

## 1. Security Architecture & Threat Model

The Derma Bliss Clinic platform processes sensitive personal and clinical health information. The system implements a defense-in-depth model across every tier.

---

## 2. Security Controls Matrix

### 2.1 Transport Layer Security (TLS/HTTPS)
- [x] **Production TLS Enforcement:** All external traffic must terminate on TLS 1.2+ with modern cipher suites. Insecure HTTP requests are automatically redirected to HTTPS (301 Permanent).
- [x] **HSTS:** `Strict-Transport-Security: max-age=31536000; includeSubDomains` header enabled in production.
- [x] **Secure Cookie Flags:** `HttpOnly`, `Secure`, and `SameSite=Strict` configured for all authentication-related cookies.

### 2.2 Authentication & Credential Security
- [x] **Password Hashing:** Salted BCrypt hashing with work factor 12 (`BCryptPasswordEncoder(12)`). Passwords are never stored in plain text or reversible formats.
- [x] **Anti-Enumeration Protection:** Authentication failure messages return generic feedback: `"Invalid phone number or password"`. API responses never differentiate between an unknown user and an incorrect password.
- [x] **Brute-Force & Rate Limiting:**
  - Login endpoints limited to 5 failed attempts per IP/account window (15-minute lock).
  - Account `lockout_until` timestamp populated in `users` table upon 5 consecutive failed attempts.
- [x] **Token Lifecycles:**
  - JWT Access Token: 15-minute validity, digitally signed with HMAC-SHA256 (minimum 256-bit secret key).
  - Refresh Token: 7-day validity, persisted in MySQL `refresh_tokens` as a SHA-256 hash. One-time rotation on refresh with immediate revocation on logout.
- [x] **Zero Hardcoded Secrets:** Administrator credentials, database passwords, and JWT secret keys are injected exclusively via environment variables (`DERMABLISS_JWT_SECRET`, `SPRING_DATASOURCE_PASSWORD`, `ADMIN_BOOTSTRAP_PASSWORD`).

### 2.3 Authorization & Access Control (Anti-IDOR)
- [x] **Deny-by-Default Policy:** All API routes require explicit authentication unless explicitly whitelisted in `SecurityFilterChain` (e.g. `/api/v1/auth/**`, `/api/v1/clinic/**`).
- [x] **Role-Based Access Control:** Strict separation between `ROLE_ADMIN` (Dr. Ankita Santosh Koli) and `ROLE_PATIENT`.
- [x] **Insecure Direct Object Reference (IDOR) Mitigation:**
  - On every patient-specific resource request (appointments, prescriptions, medical records, invoices), the service layer validates:
    ```java
    if (!resource.getPatient().getUser().getId().equals(authenticatedUser.getId()) 
        && !authenticatedUser.hasRole("ROLE_ADMIN")) {
        throw new AccessDeniedException("Access denied to requested medical resource");
    }
    ```
  - Patients can never enumerate or access other patients' records by guessing integer IDs.

### 2.4 Injection & Input Validation
- [x] **SQL Injection Defense:** All queries use Spring Data JPA parameterized JPQL or prepared statements. Raw dynamic string concatenation in SQL queries is prohibited.
- [x] **Request Validation:** Every inbound DTO is annotated with `@Valid` and Jakarta Bean Validation constraints (`@NotBlank`, `@Size`, `@Pattern`, `@NotNull`).
- [x] **Cross-Site Scripting (XSS):** Web client renders dynamic content using text nodes (`textContent`) and safe templating; unsafe `innerHTML` injection is avoided.

### 2.5 API Security & CORS
- [x] **Restricted CORS Policy:** CORS is configured with an explicit origin allowlist. Wildcard `*` origins are rejected when credentials (`allowCredentials=true`) are supported.
- [x] **Response Sanitization:** Stack traces and internal database exception details are intercepted by `GlobalExceptionHandler` and replaced with structured error DTOs (`timestamp`, `status`, `error`, `message`, `path`).

### 2.6 Medical Document Privacy & Storage Security
- [x] **Isolated Private Storage:** Uploaded patient files are stored outside the web root (`storage/medical-documents/`). No direct URL path is exposed to the web server.
- [x] **File Validation:**
  - Allowed extensions: `.pdf`, `.jpg`, `.jpeg`, `.png`.
  - Content-Type / MIME verification matching file magic bytes.
  - Maximum upload size: 10 MB per file.
- [x] **Randomized UUID Storage Names:** Physical files are named with cryptographically random UUIDs (`<uuid>.<ext>`) to prevent path traversal (`../`) and predictable filename attacks.
- [x] **Download Auditing:** Every download of a medical report or prescription generates an audit log entry (`event_type: RECORD_ACCESS`).

### 2.7 Concurrency & Double-Booking Protection
- [x] **Transactional Integrity:** Booking operations are executed inside `@Transactional(isolation = Isolation.READ_COMMITTED)`.
- [x] **Pessimistic Locking:** Conflicting slot queries acquire row-level locks to prevent race conditions during simultaneous booking attempts.

### 2.8 Push Notification & Privacy Hygiene
- [x] **Zero Clinical Data on Lock Screens:** Push notifications never include diagnoses, prescribed medication names, or sensitive symptoms.
- [x] **Standard Safe Messaging:** `"Your Derma Bliss Clinic appointment details have been updated. Please open the app to view them."`

### 2.9 Compliance & Audit Trail (DPDP Act India Alignment)
- [x] **Immutable Audit Logging:** System records login events, administrative patient accesses, prescription amendments, and billing changes into `audit_logs`.
- [x] **Data Minimization:** Only operational clinical and demographic data required for dermatology practice is collected.
- [x] **No Medical Data in Debug Logs:** Passwords, tokens, payment references, and clinical symptoms are strictly masked from application logs.
