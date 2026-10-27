# Derma Bliss Clinic — Database Schema & Data Dictionary

## 1. Schema Design Principles

1. **Storage Engine:** MySQL 8.x InnoDB for ACID compliance, row-level locking, and foreign key integrity.
2. **Character Set & Collation:** `utf8mb4` with `utf8mb4_unicode_ci` for international names and emoji support.
3. **Monetary Values:** `DECIMAL(10, 2)` strictly used for all financial fields (invoices, consultation fees, payments). Never use `FLOAT` or `DOUBLE`.
4. **Time & Date:**
   - Operational event logs and audit timestamps stored in UTC (`TIMESTAMP`).
   - Clinic appointment dates stored as `DATE` (`YYYY-MM-DD`).
   - Slot times stored as `TIME` (`HH:MM:SS`) aligned with the clinic's local timezone (`Asia/Kolkata`).
5. **Soft Deletions / State Flags:** Explicit status columns instead of hard deletes to maintain clinical audit trails.

---

## 2. Entity Relationship Overview

```
                      ┌───────────────────┐
                      │   clinic_settings │ (Single row configuration)
                      └───────────────────┘
                                │
        ┌───────────────────────┼────────────────────────┐
        ▼                       ▼                        ▼
┌──────────────┐      ┌────────────────────┐   ┌───────────────────┐
│clinic_services│     │doctor_availability │   │   blocked_slots   │
└──────────────┘      └────────────────────┘   └───────────────────┘

                              ┌───────────────┐
                              │     users     │
                              └───────┬───────┘
                                      │ 1:1
                                      ▼
                              ┌───────────────┐
                              │   patients    │
                              └───────┬───────┘
                                      │
            ┌─────────────────────────┼─────────────────────────┐
            │ 1:N                     │ 1:N                     │ 1:N
            ▼                         ▼                         ▼
   ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
   │  appointments   │       │ medical_records │       │    invoices     │
   └───┬───────┬─────┘       └────────┬────────┘       └────────┬────────┘
       │       │                      │ 1:1                     │ 1:N
       │       │ 1:1                  ▼                         ▼
       │       │             ┌─────────────────┐       ┌─────────────────┐
       │       ├────────────►│  prescriptions  │       │    payments     │
       │       │             └────────┬────────┘       └─────────────────┘
       │       │                      │ 1:N
       │       │                      ▼
       │       │             ┌─────────────────┐
       │       │             │prescription_meds│
       │       ▼             └─────────────────┘
       │  ┌──────────────┐
       │  │ queue_tokens │
       │  └──────────────┘
       │
       ▼ 1:N
┌──────────────────────┐
│ appointment_history  │
└──────────────────────┘
```

---

## 3. Data Dictionary

### Table 1: `users`
Core authentication and credentials record.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `phone_number` | VARCHAR(15) | NOT NULL, UNIQUE | Mobile phone with country code |
| `email` | VARCHAR(120) | NULL, UNIQUE | Email address |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt salted hash (cost factor 12) |
| `role` | VARCHAR(20) | NOT NULL | `ROLE_ADMIN` or `ROLE_PATIENT` |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `failed_login_attempts` | INT | NOT NULL DEFAULT 0 | Failed login attempt counter |
| `lockout_until` | TIMESTAMP | NULL | Temporary account lockout expiration |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | UTC timestamp |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | UTC timestamp |

### Table 2: `patients`
Detailed demographic and clinical profile of registered patients.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `user_id` | BIGINT | NOT NULL, UNIQUE, FK -> users(id) | Associated user account |
| `patient_number` | VARCHAR(32) | NOT NULL, UNIQUE | Human-readable patient ID (e.g. `DBP-2026-0001`) |
| `full_name` | VARCHAR(120) | NOT NULL | Full name of patient |
| `mobile_number` | VARCHAR(15) | NOT NULL | Contact telephone |
| `email` | VARCHAR(120) | NULL | Patient email address |
| `date_of_birth` | DATE | NULL | Birth date |
| `gender` | VARCHAR(10) | NULL | `FEMALE`, `MALE`, `OTHER` |
| `blood_group` | VARCHAR(5) | NULL | e.g. `O+`, `B+`, `A-` |
| `allergies` | TEXT | NULL | Clinically recorded allergies |
| `medical_history_summary`| TEXT | NULL | Medical history and dermatological background |
| `emergency_contact_name` | VARCHAR(100) | NULL | Name of emergency contact |
| `emergency_contact_phone`| VARCHAR(15) | NULL | Phone of emergency contact |
| `address` | VARCHAR(255) | NULL | Residential address |
| `account_status` | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVE' | `ACTIVE`, `DEACTIVATED` |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

### Table 3: `clinic_settings`
Singleton operational configuration for Derma Bliss Clinic.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `clinic_name` | VARCHAR(120) | NOT NULL | Default: `Derma Bliss Clinic` |
| `doctor_name` | VARCHAR(120) | NOT NULL | `Dr. Ankita Santosh Koli` |
| `doctor_qualifications` | VARCHAR(100) | NOT NULL | `BHMS, PGDMS, CCH, CGO, CSD` |
| `doctor_specialization` | VARCHAR(100) | NOT NULL | `Cosmetologist & Trichologist` |
| `doctor_reg_number` | VARCHAR(50) | NOT NULL | `73892` |
| `contact_number` | VARCHAR(25) | NOT NULL | `+91 789 789 2177` |
| `contact_email` | VARCHAR(120) | NULL | Clinic inquiries email |
| `address` | TEXT | NOT NULL | Clinic location in Thane East |
| `instagram_handle` | VARCHAR(50) | NOT NULL | `@derma_bliss_clinic` |
| `brand_tagline` | VARCHAR(120) | NOT NULL | `DERMA BLISS — SKIN | HAIR | LASER` |
| `timezone` | VARCHAR(50) | NOT NULL DEFAULT 'Asia/Kolkata' | Operating timezone |
| `slot_duration_minutes` | INT | NOT NULL DEFAULT 30 | Standard appointment duration |
| `default_consultation_fee`| DECIMAL(10,2) | NOT NULL DEFAULT 500.00 | Consultation fee in INR |
| `booking_horizon_days` | INT | NOT NULL DEFAULT 30 | Max advance booking days |
| `cancellation_deadline_hours`| INT | NOT NULL DEFAULT 4 | Minimum hours before slot for cancellation |
| `rescheduling_deadline_hours`| INT | NOT NULL DEFAULT 4 | Minimum hours before slot for rescheduling |
| `auto_confirm_bookings` | BOOLEAN | NOT NULL DEFAULT TRUE | Auto-confirm valid reservations |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Setting update timestamp |

### Table 4: `clinic_services`
Official clinic service categories and consultation types.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `service_code` | VARCHAR(30) | NOT NULL, UNIQUE | e.g. `SKIN_CARE`, `HAIR_CARE`, `LASER` |
| `name` | VARCHAR(100) | NOT NULL | Display name |
| `category` | VARCHAR(50) | NOT NULL | `SKIN`, `HAIR`, `LASER` |
| `description` | TEXT | NULL | Detailed clinical service description |
| `estimated_duration_min`| INT | NOT NULL DEFAULT 30 | Standard session duration |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | Service availability flag |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Created timestamp |

### Table 5: `doctor_availability`
Weekly recurring schedule rules for Dr. Ankita Santosh Koli.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `day_of_week` | VARCHAR(10) | NOT NULL | `MONDAY` through `SUNDAY` |
| `start_time` | TIME | NOT NULL | Daily start time (e.g. `10:00:00`) |
| `end_time` | TIME | NOT NULL | Daily closing time (e.g. `19:00:00`) |
| `break_start_time` | TIME | NULL | Afternoon break start (e.g. `13:30:00`) |
| `break_end_time` | TIME | NULL | Afternoon break end (e.g. `15:00:00`) |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | Working day indicator |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Created timestamp |

### Table 6: `blocked_slots`
Doctor vacation days, public holidays, or temporary time blockouts.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `block_date` | DATE | NOT NULL | Target calendar date |
| `start_time` | TIME | NULL | Block start time (NULL if full day) |
| `end_time` | TIME | NULL | Block end time (NULL if full day) |
| `reason` | VARCHAR(255) | NOT NULL | e.g. `Clinic Maintenance`, `Holiday` |
| `is_full_day` | BOOLEAN | NOT NULL DEFAULT FALSE | True if entire day blocked |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Created timestamp |

### Table 7: `appointments`
Core clinical reservations.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `appointment_number` | VARCHAR(32) | NOT NULL, UNIQUE | Human reference (e.g. `APT-2026-0001`) |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Associated patient |
| `service_id` | BIGINT | NULL, FK -> clinic_services(id)| Requested service |
| `appointment_date` | DATE | NOT NULL | Calendar date |
| `start_time` | TIME | NOT NULL | Starting time |
| `end_time` | TIME | NOT NULL | Finishing time |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'CONFIRMED' | `CONFIRMED`, `COMPLETED`, `CANCELLED`, `NO_SHOW` |
| `patient_notes` | TEXT | NULL | Symptoms submitted by patient |
| `internal_notes` | TEXT | NULL | Private administrative/doctor notes |
| `cancellation_reason` | VARCHAR(255) | NULL | Reason for cancellation |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | UTC booking timestamp |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | UTC updated timestamp |

*Index & Integrity Protection:*
- Index: `idx_appointment_date_time (appointment_date, start_time)`
- Index: `idx_patient_appointments (patient_id, appointment_date)`
- Concurrency Control: Protected by transactional write locks (`SELECT ... FOR UPDATE`) and status conflict validation.

### Table 8: `appointment_history`
Immutable audit log of all appointment status modifications and reschedules.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `appointment_id` | BIGINT | NOT NULL, FK -> appointments(id) ON DELETE CASCADE | Target appointment |
| `previous_status` | VARCHAR(20) | NOT NULL | Status before transition |
| `new_status` | VARCHAR(20) | NOT NULL | Status after transition |
| `change_type` | VARCHAR(30) | NOT NULL | `BOOKED`, `RESCHEDULED`, `CANCELLED`, `COMPLETED`, `NO_SHOW` |
| `reason` | VARCHAR(255) | NULL | Note or justification |
| `changed_by_user_id` | BIGINT | NOT NULL, FK -> users(id) | Actor ID |
| `changed_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Timestamp of change |

### Table 9: `medical_records`
Confidential consultation notes recorded by Dr. Ankita Santosh Koli.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `record_number` | VARCHAR(32) | NOT NULL, UNIQUE | Record identifier |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Associated patient |
| `appointment_id` | BIGINT | NULL, FK -> appointments(id) | Related appointment |
| `consultation_date` | DATE | NOT NULL | Date of consultation |
| `symptoms` | TEXT | NOT NULL | Patient reported complaints |
| `clinical_observations`| TEXT | NULL | Physical & dermatological examination notes |
| `diagnosis` | TEXT | NOT NULL | Clinical diagnosis |
| `treatment_plan` | TEXT | NOT NULL | Recommended treatment & procedures |
| `follow_up_recommendation`| VARCHAR(255) | NULL | Next review recommendation |
| `is_released_to_patient` | BOOLEAN | NOT NULL DEFAULT FALSE | Release gate for patient portal |
| `internal_notes` | TEXT | NULL | Confidential doctor notes (never exposed) |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Created timestamp |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Updated timestamp |

### Table 10: `prescriptions`
Prescriptions authored by Dr. Ankita Santosh Koli.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `prescription_number` | VARCHAR(32) | NOT NULL, UNIQUE | e.g. `RX-2026-0001` |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Patient recipient |
| `medical_record_id` | BIGINT | NULL, FK -> medical_records(id)| Originating consultation |
| `prescription_date` | DATE | NOT NULL | Date issued |
| `diagnosis_summary` | VARCHAR(255) | NULL | Summary diagnosis |
| `follow_up_date` | DATE | NULL | Next scheduled review date |
| `general_instructions`| TEXT | NULL | Dietary, application, lifestyle instructions |
| `doctor_signature_hash`| VARCHAR(128) | NOT NULL | Digital integrity hash |
| `is_amended` | BOOLEAN | NOT NULL DEFAULT FALSE | Audit flag if amended |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Issued timestamp |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Amendment timestamp |

### Table 11: `prescription_medicines`
Line-item medications prescribed. (Clinical record, NOT inventory).
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `prescription_id` | BIGINT | NOT NULL, FK -> prescriptions(id) ON DELETE CASCADE | Associated prescription |
| `medicine_name` | VARCHAR(120) | NOT NULL | Name of medication |
| `strength` | VARCHAR(50) | NOT NULL | e.g. `20mg`, `0.1% w/w` |
| `dosage_form` | VARCHAR(50) | NOT NULL | `Cream`, `Tablet`, `Gel`, `Serum`, `Lotion` |
| `frequency` | VARCHAR(50) | NOT NULL | e.g. `Twice daily`, `Once at night` |
| `duration_days` | INT | NOT NULL | Duration in days |
| `timing_instructions`| VARCHAR(100) | NULL | e.g. `After food`, `Apply before bedtime` |
| `specific_notes` | VARCHAR(255) | NULL | Application precautions |

### Table 12: `medical_documents`
Patient-uploaded or doctor-issued clinical reports and photos.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `document_reference` | VARCHAR(64) | NOT NULL, UNIQUE | Secure token reference |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Document owner |
| `appointment_id` | BIGINT | NULL, FK -> appointments(id) | Associated appointment |
| `original_filename` | VARCHAR(255) | NOT NULL | Sanitized uploaded filename |
| `stored_filename` | VARCHAR(255) | NOT NULL, UNIQUE | Randomized UUID storage name |
| `mime_type` | VARCHAR(100) | NOT NULL | Verified MIME type |
| `file_size_bytes` | BIGINT | NOT NULL | Size in bytes |
| `storage_path` | VARCHAR(255) | NOT NULL | Relative private storage path |
| `sha256_hash` | VARCHAR(64) | NOT NULL | SHA-256 integrity hash |
| `is_verified` | BOOLEAN | NOT NULL DEFAULT FALSE | Verified by doctor |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Upload timestamp |

### Table 13: `invoices`
Billing statements for clinic consultations and treatments.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `invoice_number` | VARCHAR(32) | NOT NULL, UNIQUE | Reference (e.g. `INV-2026-0001`) |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Billing recipient |
| `appointment_id` | BIGINT | NULL, FK -> appointments(id) | Related consultation |
| `total_amount` | DECIMAL(10,2)| NOT NULL | Gross charges |
| `discount_amount` | DECIMAL(10,2)| NOT NULL DEFAULT 0.00 | Applied concession |
| `net_amount` | DECIMAL(10,2)| NOT NULL | Net payable amount |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | `PENDING`, `PAID`, `PARTIALLY_PAID`, `CANCELLED` |
| `invoice_date` | DATE | NOT NULL | Billing date |
| `notes` | VARCHAR(255) | NULL | Line item description |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Update timestamp |

### Table 14: `payments`
Recorded payment transactions.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `payment_number` | VARCHAR(32) | NOT NULL, UNIQUE | Receipt reference (e.g. `PAY-2026-0001`) |
| `invoice_id` | BIGINT | NOT NULL, FK -> invoices(id) | Target invoice |
| `amount` | DECIMAL(10,2)| NOT NULL | Amount paid |
| `payment_method` | VARCHAR(20) | NOT NULL | `CASH`, `UPI`, `CARD`, `NET_BANKING` |
| `payment_status` | VARCHAR(20) | NOT NULL DEFAULT 'COMPLETED'| `COMPLETED`, `RECORDED`, `FAILED`, `REFUNDED` |
| `transaction_reference`| VARCHAR(100) | NULL | UPI Txn ID / POS Reference |
| `payment_date` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Settlement timestamp |
| `recorded_by_user_id` | BIGINT | NOT NULL, FK -> users(id) | Admin who logged payment |
| `notes` | VARCHAR(255) | NULL | Payment notes |

### Table 15: `queue_tokens`
Daily clinic queue management and token tracking.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `token_number` | INT | NOT NULL | Sequential daily token (e.g. 1, 2, 3...) |
| `token_date` | DATE | NOT NULL | Queue date |
| `appointment_id` | BIGINT | NOT NULL, UNIQUE, FK -> appointments(id) | Linked appointment |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Patient |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'WAITING' | `WAITING`, `CALLED`, `IN_CONSULTATION`, `COMPLETED`, `NO_SHOW`, `CANCELLED` |
| `called_at` | TIMESTAMP | NULL | Time called by doctor |
| `completed_at` | TIMESTAMP | NULL | Time consultation finished |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

*Constraint:* `UNIQUE KEY uq_date_token (token_date, token_number)`

### Table 16: `notifications`
System and clinic alerts dispatched to patients or administrators.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `user_id` | BIGINT | NOT NULL, FK -> users(id) | Recipient |
| `title` | VARCHAR(120) | NOT NULL | Non-sensitive title |
| `message_preview` | VARCHAR(255) | NOT NULL | Non-sensitive preview (No health data) |
| `notification_type` | VARCHAR(30) | NOT NULL | `APPOINTMENT_CONFIRMED`, `REMINDER`, `RESCHEDULED`, `PRESCRIPTION_READY` |
| `is_read` | BOOLEAN | NOT NULL DEFAULT FALSE | Read state |
| `delivery_status` | VARCHAR(20) | NOT NULL DEFAULT 'SENT' | `PENDING`, `SENT`, `FAILED` |
| `metadata_json` | TEXT | NULL | Internal routing payload |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Dispatch timestamp |

### Table 17: `feedback`
Patient satisfaction ratings and clinic reviews.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `patient_id` | BIGINT | NOT NULL, FK -> patients(id) | Submitting patient |
| `appointment_id` | BIGINT | NULL, FK -> appointments(id) | Associated appointment |
| `rating` | INT | NOT NULL CHECK (rating BETWEEN 1 AND 5) | Star rating (1-5) |
| `comments` | TEXT | NULL | Patient feedback text |
| `is_published` | BOOLEAN | NOT NULL DEFAULT FALSE | Doctor approval for public display |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Feedback timestamp |

### Table 18: `audit_logs`
Security and administrative compliance audit trail.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `user_id` | BIGINT | NULL, FK -> users(id) ON DELETE SET NULL | Acting user |
| `event_type` | VARCHAR(50) | NOT NULL | `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `RECORD_ACCESS`, `PRESCRIPTION_CREATE` |
| `entity_name` | VARCHAR(50) | NOT NULL | e.g. `Appointment`, `Prescription`, `User` |
| `entity_id` | VARCHAR(64) | NULL | Target resource ID |
| `action` | VARCHAR(20) | NOT NULL | `CREATE`, `READ`, `UPDATE`, `DELETE`, `LOGIN` |
| `ip_address` | VARCHAR(45) | NOT NULL | Client IP address (IPv4 or IPv6) |
| `user_agent` | VARCHAR(255) | NULL | Client browser / device identifier |
| `details_json` | TEXT | NULL | Context (Never contains credentials or health data) |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Log timestamp |

### Table 19: `refresh_tokens`
Long-lived cryptographic refresh token storage for JWT sessions.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | PK | Primary Key |
| `user_id` | BIGINT | NOT NULL, FK -> users(id) ON DELETE CASCADE | Associated user |
| `token_hash` | VARCHAR(64) | NOT NULL, UNIQUE | SHA-256 hash of refresh token |
| `device_info` | VARCHAR(255) | NULL | Device / client info |
| `expires_at` | TIMESTAMP | NOT NULL | Absolute expiration time |
| `is_revoked` | BOOLEAN | NOT NULL DEFAULT FALSE | Explicit revocation indicator |
| `created_at` | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Issuance timestamp |
