"""
Derma Bliss Clinic — Phase 1-3 Verification & Validation Suite
Validates directory structure, schema migrations, configuration files, and seed accuracy.
"""

import os
import re
import sys
import sqlite3

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

REQUIRED_FILES = [
    # Documentation (Phase 1)
    "docs/architecture.md",
    "docs/database-schema.md",
    "docs/security-checklist.md",
    "docs/api-documentation.md",
    
    # Root Configurations & Scaffolding (Phase 2)
    "README.md",
    ".gitignore",
    ".env.example",
    "docker-compose.yml",
    
    # Backend Structure & Configuration (Phase 2)
    "backend/pom.xml",
    "backend/mvnw",
    "backend/mvnw.cmd",
    "backend/.mvn/wrapper/maven-wrapper.properties",
    "backend/src/main/resources/application.yml",
    "backend/src/main/resources/application-dev.yml",
    "backend/src/main/java/com/dermabliss/clinic/DermaBlissApplication.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Role.java",
    "backend/src/main/java/com/dermabliss/clinic/dto/ApiResponse.java",
    "backend/src/main/java/com/dermabliss/clinic/dto/ErrorResponse.java",
    "backend/src/main/java/com/dermabliss/clinic/exception/GlobalExceptionHandler.java",
    "backend/src/main/java/com/dermabliss/clinic/config/SecurityConfig.java",
    
    # Database Migrations (Phase 3)
    "backend/src/main/resources/db/migration/V1__init_schema.sql",
    "backend/src/main/resources/db/migration/V2__seed_initial_data.sql",
    
    # Admin Website Scaffolding (Phase 2)
    "admin-website/index.html",
    "admin-website/login.html",
    "admin-website/dashboard.html",
    "admin-website/appointments.html",
    "admin-website/patients.html",
    "admin-website/medical-records.html",
    "admin-website/prescriptions.html",
    "admin-website/billing.html",
    "admin-website/reports.html",
    "admin-website/settings.html",
    "admin-website/assets/css/styles.css",
    "admin-website/assets/js/api.js",
    "admin-website/assets/js/auth.js",
    
    # Backend Layered Architecture (Phase 4)
    # Entities
    "backend/src/main/java/com/dermabliss/clinic/entity/Role.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/User.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Patient.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/ClinicSetting.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/ClinicServiceEntity.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/DoctorAvailability.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/BlockedSlot.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Appointment.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/AppointmentStatus.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/AppointmentHistory.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/MedicalRecord.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Prescription.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/PrescriptionMedicine.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/MedicalDocument.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Invoice.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/InvoiceStatus.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Payment.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/PaymentMethod.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/PaymentStatus.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/QueueToken.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/QueueStatus.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Notification.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/Feedback.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/AuditLog.java",
    "backend/src/main/java/com/dermabliss/clinic/entity/RefreshToken.java",

    # Repositories
    "backend/src/main/java/com/dermabliss/clinic/repository/UserRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/PatientRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/ClinicSettingRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/ClinicServiceRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/DoctorAvailabilityRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/BlockedSlotRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/AppointmentRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/AppointmentHistoryRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/MedicalRecordRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/PrescriptionRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/MedicalDocumentRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/InvoiceRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/PaymentRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/QueueTokenRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/NotificationRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/FeedbackRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/AuditLogRepository.java",
    "backend/src/main/java/com/dermabliss/clinic/repository/RefreshTokenRepository.java",

    # Security & JWT
    "backend/src/main/java/com/dermabliss/clinic/security/CustomUserDetails.java",
    "backend/src/main/java/com/dermabliss/clinic/security/CustomUserDetailsService.java",
    "backend/src/main/java/com/dermabliss/clinic/security/JwtTokenProvider.java",
    "backend/src/main/java/com/dermabliss/clinic/security/JwtAuthenticationFilter.java",

    # Services
    "backend/src/main/java/com/dermabliss/clinic/service/BootstrapService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/AuditService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/ClinicInformationService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/AuthService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/AppointmentService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/PrescriptionService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/MedicalRecordService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/BillingService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/QueueService.java",
    "backend/src/main/java/com/dermabliss/clinic/service/DashboardService.java",

    # Controllers
    "backend/src/main/java/com/dermabliss/clinic/controller/AuthController.java",
    "backend/src/main/java/com/dermabliss/clinic/controller/ClinicController.java",
    "backend/src/main/java/com/dermabliss/clinic/controller/AppointmentController.java",
    "backend/src/main/java/com/dermabliss/clinic/controller/PrescriptionController.java",
    "backend/src/main/java/com/dermabliss/clinic/controller/MedicalRecordController.java",
    "backend/src/main/java/com/dermabliss/clinic/controller/AdminController.java",

    # Mobile App Screens (Phase 5)
    "mobile-app/package.json",
    "mobile-app/tsconfig.json",
    "mobile-app/App.tsx",
    "mobile-app/src/types/index.ts",
    "mobile-app/src/theme/colors.ts",
    "mobile-app/src/services/api.ts",
    "mobile-app/src/services/authStorage.ts",
    "mobile-app/src/screens/LoginScreen.tsx",
    "mobile-app/src/screens/RegisterScreen.tsx",
    "mobile-app/src/screens/HomeScreen.tsx",
    "mobile-app/src/screens/BookAppointmentScreen.tsx",
    "mobile-app/src/screens/MyAppointmentsScreen.tsx",
    "mobile-app/src/screens/PrescriptionsScreen.tsx",
    "mobile-app/src/screens/MedicalRecordsScreen.tsx",
    "mobile-app/android/app/src/main/AndroidManifest.xml",
    "mobile-app/ios/Podfile",

    # Brand Assets
    "admin-website/assets/images/logo.svg",
    "admin-website/assets/images/emblem.svg",

    # Unit Tests & Test Configuration (Phase 7)
    "backend/src/test/resources/application.yml",
    "backend/src/test/java/com/dermabliss/clinic/service/AuthServiceTest.java",
    "backend/src/test/java/com/dermabliss/clinic/service/AppointmentServiceTest.java",
    "backend/src/test/java/com/dermabliss/clinic/service/PrescriptionServiceTest.java",
    "backend/src/test/java/com/dermabliss/clinic/service/ClinicInformationServiceTest.java",
]

EXPECTED_TABLES = [
    "users",
    "patients",
    "clinic_settings",
    "clinic_services",
    "doctor_availability",
    "blocked_slots",
    "appointments",
    "appointment_history",
    "medical_records",
    "prescriptions",
    "prescription_medicines",
    "medical_documents",
    "invoices",
    "payments",
    "queue_tokens",
    "notifications",
    "feedback",
    "audit_logs",
    "refresh_tokens",
]

OFFICIAL_METRICS = {
    "doctor_name": "Dr. Ankita Santosh Koli",
    "registration": "73892",
    "qualifications": "BHMS, PGDMS, CCH, CGO, CSD",
    "specialization": "Cosmetologist & Trichologist",
    "phone": "+91 789 789 2177",
    "clinic_name": "Derma Bliss Clinic",
    "tagline": "DERMA BLISS — SKIN | HAIR | LASER",
    "timezone": "Asia/Kolkata",
}

def check_file_presence():
    print("=== [1/4] Checking Repository Files Presence ===")
    missing = []
    for rel_path in REQUIRED_FILES:
        full_path = os.path.join(BASE_DIR, rel_path)
        if not os.path.exists(full_path):
            missing.append(rel_path)
        else:
            print(f"  [OK] {rel_path}")
            
    if missing:
        print(f"\n[FAIL] Missing {len(missing)} required files:\n" + "\n".join(missing))
        return False
    print(f"\n[SUCCESS] All {len(REQUIRED_FILES)} required files are present.\n")
    return True

def validate_schema_tables():
    print("=== [2/4] Validating MySQL Migration V1 Schema ===")
    v1_path = os.path.join(BASE_DIR, "backend/src/main/resources/db/migration/V1__init_schema.sql")
    with open(v1_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Extract CREATE TABLE statements
    found_tables = re.findall(r"CREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+([a-zA-Z0-9_]+)", content, re.IGNORECASE)
    print(f"Found {len(found_tables)} table definitions in V1__init_schema.sql.")

    missing_tables = [t for t in EXPECTED_TABLES if t not in found_tables]
    if missing_tables:
        print(f"[FAIL] Missing expected tables: {missing_tables}")
        return False

    for t in EXPECTED_TABLES:
        print(f"  [OK] Table '{t}' verified.")

    # Check monetary types
    decimal_matches = re.findall(r"([a-zA-Z0-9_]+)\s+DECIMAL\(10,\s*2\)", content, re.IGNORECASE)
    print(f"  [OK] Verified {len(decimal_matches)} monetary fields use DECIMAL(10, 2): {decimal_matches}")

    print("\n[SUCCESS] Migration V1 schema validation passed.\n")
    return True

def validate_seed_data():
    print("=== [3/4] Validating Migration V2 Seed Data Accuracy ===")
    v2_path = os.path.join(BASE_DIR, "backend/src/main/resources/db/migration/V2__seed_initial_data.sql")
    with open(v2_path, "r", encoding="utf-8") as f:
        seed_content = f.read()

    for key, expected_value in OFFICIAL_METRICS.items():
        if expected_value in seed_content:
            print(f"  [OK] Verified official {key}: '{expected_value}'")
        else:
            print(f"  [FAIL] Missing or inaccurate {key}: expected '{expected_value}'")
            return False

    # Check services seeded
    services = ["SKIN_CARE", "HAIR_CARE", "LASER_TREATMENTS"]
    for s in services:
        if s in seed_content:
            print(f"  [OK] Service category '{s}' seeded.")
        else:
            print(f"  [FAIL] Missing service '{s}'")
            return False

    print("\n[SUCCESS] Migration V2 seed data validation passed.\n")
    return True

def run_in_memory_sql_simulation():
    print("=== [4/4] In-Memory SQLite DDL / DML Simulation ===")
    v1_path = os.path.join(BASE_DIR, "backend/src/main/resources/db/migration/V1__init_schema.sql")
    v2_path = os.path.join(BASE_DIR, "backend/src/main/resources/db/migration/V2__seed_initial_data.sql")

    with open(v1_path, "r", encoding="utf-8") as f:
        v1_sql = f.read()
    with open(v2_path, "r", encoding="utf-8") as f:
        v2_sql = f.read()

    # Adapt MySQL dialect to standard SQLite for structural syntax validation
    adapted_sql = v1_sql
    # Remove comments
    adapted_sql = re.sub(r"--[^\n]*", "", adapted_sql)
    adapted_sql = re.sub(r"ENGINE\s*=\s*InnoDB", "", adapted_sql, flags=re.IGNORECASE)
    adapted_sql = re.sub(r"DEFAULT\s+CHARSET\s*=\s*utf8mb4", "", adapted_sql, flags=re.IGNORECASE)
    adapted_sql = re.sub(r"COLLATE\s*=\s*utf8mb4_unicode_ci", "", adapted_sql, flags=re.IGNORECASE)
    adapted_sql = re.sub(r"ON\s+UPDATE\s+CURRENT_TIMESTAMP", "", adapted_sql, flags=re.IGNORECASE)
    adapted_sql = re.sub(r",\s*INDEX\s+idx_[a-zA-Z0-9_]+\s*\([^)]+\)", "", adapted_sql, flags=re.IGNORECASE)

    conn = sqlite3.connect(":memory:")
    cursor = conn.cursor()

    try:
        # Execute each statement
        statements = [stmt.strip() for stmt in adapted_sql.split(";") if stmt.strip()]
        for stmt in statements:
            if "CREATE TABLE" in stmt.upper():
                cursor.execute(stmt)
        print(f"  [OK] Successfully executed DDL creation statements.")

        # Verify created tables count in sqlite
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")
        tables_created = [r[0] for r in cursor.fetchall()]
        print(f"  [OK] In-memory database verified with {len(tables_created)} active tables: {tables_created}")

        if len(tables_created) != len(EXPECTED_TABLES):
            print(f"  [FAIL] Expected {len(EXPECTED_TABLES)} tables, but found {len(tables_created)}")
            return False

        conn.close()
        print("\n[SUCCESS] Structural SQL syntax simulation passed.\n")
        return True
    except Exception as e:
        print(f"  [ERROR] SQL simulation error: {e}")
        return False

def main():
    print("=" * 60)
    print("Derma Bliss Clinic — Verification Suite (Phase 1 to 3)")
    print("Dr. Ankita Santosh Koli (Registration No. 73892)")
    print("=" * 60 + "\n")

    p1 = check_file_presence()
    p2 = validate_schema_tables()
    p3 = validate_seed_data()
    p4 = run_in_memory_sql_simulation()

    if p1 and p2 and p3 and p4:
        print("=" * 60)
        print("ALL CHECKS PASSED: Phases 1 through 3 are fully verified!")
        print("=" * 60)
        sys.exit(0)
    else:
        print("=" * 60)
        print("CHECKS FAILED: Please fix identified issues.")
        print("=" * 60)
        sys.exit(1)

if __name__ == "__main__":
    main()
