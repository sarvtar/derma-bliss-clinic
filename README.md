# Derma Bliss Clinic Management System

Official, secure, responsive clinical management system for **Derma Bliss Clinic**, operated by **Dr. Ankita Santosh Koli**.

---

## Official Clinic Information
- **Clinic Name:** Derma Bliss Clinic
- **Doctor:** Dr. Ankita Santosh Koli
- **Qualifications:** BHMS, PGDMS, CCH, CGO, CSD
- **Specialization:** Cosmetologist & Trichologist
- **Medical Registration Number:** 73892
- **Core Services:** Skin Care, Hair Care, Laser Treatments
- **Official Contact Number:** +91 789 789 2177
- **Address:** DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.
- **Instagram:** `@derma_bliss_clinic`
- **Brand Tagline:** `DERMA BLISS — SKIN | HAIR | LASER`
- **Clinic Timezone:** `Asia/Kolkata` (IST)

---

## Repository Structure

```
derma-bliss-clinic/
├── README.md                      # Project setup & run documentation
├── .gitignore                     # Git ignore rules for Maven, Node, IDEs
├── .env.example                   # Environment configuration template
├── docker-compose.yml             # Local MySQL 8.x container stack
├── docs/                          # Architecture & design specifications
│   ├── architecture.md            # High-level 3-tier system architecture
│   ├── database-schema.md         # Normalized schema & data dictionary
│   ├── security-checklist.md      # Defense-in-depth security checklist
│   └── api-documentation.md       # Complete REST API specification (v1)
├── backend/                       # Spring Boot 3 backend (Java 21)
│   ├── pom.xml                    # Maven dependencies & build configuration
│   ├── mvnw / mvnw.cmd            # Maven wrapper scripts
│   └── src/
│       ├── main/
│       │   ├── java/com/dermabliss/clinic/ # Layered backend source
│       │   └── resources/
│       │       ├── application.yml         # Main Spring Boot config
│       │       ├── application-dev.yml     # Local dev profile
│       │       └── db/migration/           # Flyway database migrations
│       └── test/                  # Automated integration & unit tests
├── admin-website/                 # Admin website (HTML5/CSS3/JavaScript)
│   ├── index.html                 # Navigation entry
│   ├── login.html                 # Doctor authentication
│   ├── dashboard.html             # Operational metrics & today's queue
│   ├── appointments.html          # Appointment booking & scheduling
│   ├── patients.html              # Patient management & medical history
│   ├── medical-records.html       # Clinical notes & consultations
│   ├── prescriptions.html         # Digital prescription generation & PDF
│   ├── billing.html               # Invoicing & payment recording
│   ├── reports.html               # Clinic analytics & financial summaries
│   ├── settings.html              # Clinic timings, fees, slot duration
│   └── assets/                    # Styles (warm blush/gold theme) & JS client
└── mobile-app/                    # Patient mobile application (React Native)
    ├── package.json               # Mobile dependencies
    ├── tsconfig.json              # TypeScript configuration
    ├── App.tsx                    # Root mobile entry
    └── src/                       # Navigation, screens, secure storage
```

---

## Prerequisites (Windows 11)

To run the complete system locally on Windows 11, install:
1. **JDK 21 LTS** (e.g. Eclipse Temurin 21 or Microsoft OpenJDK 21).
   - Via winget: `winget install EclipseAdoptium.Temurin.21.JDK`
2. **Apache Maven 3.9+** (or use the provided Maven wrapper `mvnw.cmd`).
   - Via winget: `winget install Apache.Maven`
3. **MySQL 8.0+** OR **Docker Desktop for Windows**.
   - Via winget: `winget install Oracle.MySQL` or `winget install Docker.DockerDesktop`
4. **Node.js LTS (v20+)** for the mobile app client.
   - Via winget: `winget install OpenJS.NodeJS.LTS`

---

## Quick Start: Starting MySQL Database & Backend

### 1. Start the MySQL Database

#### Option A: Using Docker Compose (Recommended)
From the project root directory, run:
```powershell
docker compose up -d
```
This launches a pre-configured MySQL 8.0 database container named `dermabliss-mysql` listening on port `3306`, with persistent storage in a Docker volume.

#### Option B: Using Local MySQL Server
If MySQL 8.0 is installed locally on Windows:
```powershell
# In MySQL Shell or Command Line Client:
mysql -u root -p
```
Run the following SQL commands:
```sql
CREATE DATABASE IF NOT EXISTS dermabliss_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'dermabliss_user'@'localhost' IDENTIFIED BY 'DermaBliss@2026';
GRANT ALL PRIVILEGES ON dermabliss_db.* TO 'dermabliss_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env` or set environment variables in PowerShell:
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/dermabliss_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME="dermabliss_user"
$env:SPRING_DATASOURCE_PASSWORD="DermaBliss@2026"
$env:DERMABLISS_JWT_SECRET="c2VjdXJlLWRlcm1hLWJsaXNzLXN1cGVyLXNlY3JldC1qd3Qta2V5LTIwMjYtbWluMzJjaGFycw=="
$env:ADMIN_BOOTSTRAP_PASSWORD="ChangeMeInProdDoctor2026!"
```

### 3. Start the Spring Boot Backend
Navigate to the `backend/` directory and execute:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
*(Or `mvn spring-boot:run` if Maven is installed globally).*

Flyway will automatically detect the MySQL database, apply all migration scripts (`V1__init_schema.sql`, `V2__seed_initial_data.sql`), and start the REST API on `http://localhost:8080`.

- **Swagger / OpenAPI Documentation:** `http://localhost:8080/swagger-ui.html`
- **Health Check:** `http://localhost:8080/actuator/health`

### 4. Open the Admin Website
Serve the `admin-website/` directory using any local static web server, or open `admin-website/login.html` in your web browser.

---

## Database Migrations

Database schema and seed data are strictly managed through **Flyway**:
- `backend/src/main/resources/db/migration/V1__init_schema.sql`: Complete normalized schema with 19 tables, foreign key constraints, indexes, and booking protection.
- `backend/src/main/resources/db/migration/V2__seed_initial_data.sql`: Official clinic information, services, and doctor schedule.
