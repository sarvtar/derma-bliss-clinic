-- ==============================================================================
-- DERMA BLISS CLINIC MANAGEMENT SYSTEM
-- Flyway Migration V2: Official Seed Data
-- Dr. Ankita Santosh Koli (BHMS, PGDMS, CCH, CGO, CSD | Reg. 73892)
-- ==============================================================================

-- 1. Seed Singleton Official Clinic Configuration
INSERT INTO clinic_settings (
    id,
    clinic_name,
    doctor_name,
    doctor_qualifications,
    doctor_specialization,
    doctor_reg_number,
    contact_number,
    contact_email,
    address,
    instagram_handle,
    brand_tagline,
    timezone,
    slot_duration_minutes,
    default_consultation_fee,
    booking_horizon_days,
    cancellation_deadline_hours,
    rescheduling_deadline_hours,
    auto_confirm_bookings
) VALUES (
    1,
    'Derma Bliss Clinic',
    'Dr. Ankita Santosh Koli',
    'BHMS, PGDMS, CCH, CGO, CSD',
    'Cosmetologist & Trichologist',
    '73892',
    '+91 789 789 2177',
    NULL,
    'DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.',
    '@derma_bliss_clinic',
    'DERMA BLISS — SKIN | HAIR | LASER',
    'Asia/Kolkata',
    30,
    500.00,
    30,
    4,
    4,
    TRUE
) ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 2. Seed Official Clinic Services
INSERT INTO clinic_services (id, service_code, name, category, description, estimated_duration_min, is_active) VALUES
(1, 'REGULAR_PATIENT', 'Regular Patient Consultation', 'REGULAR', 'Routine checkup, regular follow-up assessment, medication review, and general clinical maintenance.', 30, TRUE),
(2, 'SKIN_CARE', 'Skin Care', 'SKIN', 'Comprehensive dermatological consultation, acne management, pigmentation therapy, chemical peels, and skin rejuvenation.', 30, TRUE),
(3, 'HAIR_CARE', 'Hair Care', 'HAIR', 'Specialized trichology consultations, hair fall diagnosis, scalp health treatments, and personalized hair restoration therapies.', 30, TRUE),
(4, 'LASER_TREATMENTS', 'Laser Treatments', 'LASER', 'Advanced aesthetic laser procedures, laser hair reduction, scar resurfacing, and targeted pigmentation laser treatments.', 45, TRUE)
ON DUPLICATE KEY UPDATE is_active = VALUES(is_active);

-- 3. Seed Doctor Weekly Availability Schedule (Dr. Ankita Santosh Koli)
-- Working days: Monday - Saturday (10:00 AM - 7:00 PM, Lunch Break: 1:30 PM - 3:00 PM)
-- Sunday: Clinic Closed
INSERT INTO doctor_availability (id, day_of_week, start_time, end_time, break_start_time, break_end_time, is_active) VALUES
(1, 'MONDAY',    '10:00:00', '19:00:00', '13:30:00', '15:00:00', TRUE),
(2, 'TUESDAY',   '10:00:00', '19:00:00', '13:30:00', '15:00:00', TRUE),
(3, 'WEDNESDAY', '10:00:00', '19:00:00', '13:30:00', '15:00:00', TRUE),
(4, 'THURSDAY',  '10:00:00', '19:00:00', '13:30:00', '15:00:00', TRUE),
(5, 'FRIDAY',    '10:00:00', '19:00:00', '13:30:00', '15:00:00', TRUE),
(6, 'SATURDAY',  '10:00:00', '19:00:00', '13:30:00', '15:00:00', TRUE),
(7, 'SUNDAY',    '10:00:00', '14:00:00', NULL,       NULL,       FALSE)
ON DUPLICATE KEY UPDATE 
    start_time = VALUES(start_time),
    end_time = VALUES(end_time),
    break_start_time = VALUES(break_start_time),
    break_end_time = VALUES(break_end_time),
    is_active = VALUES(is_active);
