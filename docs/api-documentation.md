# Derma Bliss Clinic — REST API Specification (v1)

Base URL: `/api/v1`  
Protocol: HTTPS  
Standard Headers:
- `Content-Type: application/json`
- `Authorization: Bearer <access_token>` (for protected routes)

---

## 1. Authentication Endpoints (`/api/v1/auth`)

### 1.1 Patient Registration
- **Endpoint:** `POST /api/v1/auth/register`
- **Access:** Public
- **Request Body:**
```json
{
  "fullName": "Pooja Sharma",
  "phoneNumber": "+919876543210",
  "email": "pooja.sharma@example.com",
  "password": "StrongPassword123!",
  "gender": "FEMALE",
  "dateOfBirth": "1995-04-12"
}
```
- **Responses:**
  - `201 Created`: Returns User & Patient profile with initial tokens.
  - `400 Bad Request`: Validation failure.
  - `409 Conflict`: Phone number or email already registered.

### 1.2 User Login (Admin & Patient)
- **Endpoint:** `POST /api/v1/auth/login`
- **Access:** Public
- **Request Body:**
```json
{
  "phoneNumber": "+919876543210",
  "password": "StrongPassword123!"
}
```
- **Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5c...",
  "refreshToken": "7d9a1f2e8c4b...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "role": "ROLE_PATIENT",
    "name": "Pooja Sharma",
    "phoneNumber": "+919876543210"
  }
}
```
- **Error Response `401 Unauthorized`:**
```json
{
  "timestamp": "2026-10-25T07:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid phone number or password",
  "path": "/api/v1/auth/login"
}
```

### 1.3 Refresh Access Token
- **Endpoint:** `POST /api/v1/auth/refresh`
- **Access:** Public
- **Request Body:** `{ "refreshToken": "7d9a1f2e8c4b..." }`
- **Response `200 OK`:** New access token and rotated refresh token.

### 1.4 Secure Logout
- **Endpoint:** `POST /api/v1/auth/logout`
- **Access:** Authenticated
- **Response `200 OK`:** Revokes active refresh token in database.

---

## 2. Public Clinic Information (`/api/v1/clinic`)

### 2.1 Get Clinic Profile
- **Endpoint:** `GET /api/v1/clinic`
- **Access:** Public
- **Response `200 OK`:**
```json
{
  "clinicName": "Derma Bliss Clinic",
  "doctorName": "Dr. Ankita Santosh Koli",
  "doctorQualifications": "BHMS, PGDMS, CCH, CGO, CSD",
  "doctorSpecialization": "Cosmetologist & Trichologist",
  "registrationNumber": "73892",
  "contactNumber": "+91 789 789 2177",
  "address": "DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.",
  "instagramHandle": "@derma_bliss_clinic",
  "brandTagline": "DERMA BLISS — SKIN | HAIR | LASER",
  "timezone": "Asia/Kolkata",
  "defaultConsultationFee": 500.00,
  "slotDurationMinutes": 30
}
```

### 2.2 Get Clinic Services
- **Endpoint:** `GET /api/v1/clinic/services`
- **Access:** Public
- **Response `200 OK`:** List of active services (Skin Care, Hair Care, Laser Treatments).

---

## 3. Appointment Booking Endpoints (`/api/v1/appointments`)

### 3.1 Get Available Time Slots
- **Endpoint:** `GET /api/v1/appointments/available-slots?date=2026-10-26`
- **Access:** Authenticated (Patient or Admin)
- **Response `200 OK`:**
```json
{
  "date": "2026-10-26",
  "slotDurationMinutes": 30,
  "slots": [
    { "startTime": "10:00:00", "endTime": "10:30:00", "available": true },
    { "startTime": "10:30:00", "endTime": "11:00:00", "available": false },
    { "startTime": "11:00:00", "endTime": "11:30:00", "available": true }
  ]
}
```

### 3.2 Book Appointment
- **Endpoint:** `POST /api/v1/appointments`
- **Access:** Authenticated (`ROLE_PATIENT`)
- **Request Body:**
```json
{
  "serviceId": 1,
  "appointmentDate": "2026-10-26",
  "startTime": "11:00:00",
  "patientNotes": "Consultation for persistent acne"
}
```
- **Responses:**
  - `201 Created`: Appointment automatically confirmed (`status: "CONFIRMED"`).
  - `409 Conflict`: Slot already reserved by a concurrent booking.
  - `400 Bad Request`: Date is in the past, holiday, or outside working hours.

### 3.3 My Appointments
- **Endpoint:** `GET /api/v1/appointments/my`
- **Access:** Authenticated (`ROLE_PATIENT`)
- **Response `200 OK`:** List of appointments belonging to current patient.

### 3.4 Reschedule Appointment
- **Endpoint:** `PATCH /api/v1/appointments/{id}/reschedule`
- **Access:** Authenticated (Patient owner or Admin)
- **Request Body:**
```json
{
  "newDate": "2026-10-28",
  "newStartTime": "14:00:00",
  "reason": "Work conflict"
}
```

### 3.5 Cancel Appointment
- **Endpoint:** `PATCH /api/v1/appointments/{id}/cancel`
- **Access:** Authenticated (Patient owner or Admin)
- **Request Body:** `{ "reason": "Feeling unwell" }`

---

## 4. Patient Clinical Records (`/api/v1/patient`)

### 4.1 My Prescriptions
- **Endpoint:** `GET /api/v1/prescriptions/my`
- **Access:** Authenticated (`ROLE_PATIENT`)
- **Response `200 OK`:** List of digital prescriptions issued to the patient.

### 4.2 Download Prescription PDF
- **Endpoint:** `GET /api/v1/prescriptions/{id}/pdf`
- **Access:** Authenticated (`ROLE_PATIENT` owner or `ROLE_ADMIN`)
- **Response `200 OK`:** `application/pdf` stream with doctor's verified credentials.

### 4.3 My Medical Records
- **Endpoint:** `GET /api/v1/medical-records/my`
- **Access:** Authenticated (`ROLE_PATIENT`)
- **Response `200 OK`:** Consultation summaries explicitly released to patient.

### 4.4 Upload Medical Report
- **Endpoint:** `POST /api/v1/medical-documents/upload`
- **Access:** Authenticated (`ROLE_PATIENT` or `ROLE_ADMIN`)
- **Multipart Form:** `file` (PDF/JPEG/PNG, max 10MB), `appointmentId` (optional).

---

## 5. Doctor & Admin Endpoints (`/api/v1/admin`)

All `/api/v1/admin/**` endpoints strictly require `ROLE_ADMIN`.

### 5.1 Dashboard Summary
- **Endpoint:** `GET /api/v1/admin/dashboard`
- **Response `200 OK`:** Today's appointments, queue count, revenue, upcoming follow-ups.

### 5.2 Clinic Appointments Management
- **Endpoint:** `GET /api/v1/admin/appointments?date=2026-10-26&status=CONFIRMED`
- **Response `200 OK`:** Filterable paginated appointments list.

### 5.3 Mark Appointment Completed / No-Show
- **Endpoint:** `PATCH /api/v1/admin/appointments/{id}/complete`
- **Endpoint:** `PATCH /api/v1/admin/appointments/{id}/no-show`

### 5.4 Issue Digital Prescription
- **Endpoint:** `POST /api/v1/admin/prescriptions`
- **Request Body:**
```json
{
  "patientId": 12,
  "appointmentId": 45,
  "prescriptionDate": "2026-10-26",
  "diagnosisSummary": "Acne Vulgaris Grade II",
  "followUpDate": "2026-11-26",
  "generalInstructions": "Wash face twice daily with gentle cleanser. Use broad-spectrum sunscreen.",
  "medicines": [
    {
      "medicineName": "Clindamycin Gel",
      "strength": "1% w/w",
      "dosageForm": "Gel",
      "frequency": "Twice daily",
      "durationDays": 30,
      "timingInstructions": "Apply thin layer morning and night",
      "specificNotes": "Avoid eye contour area"
    }
  ]
}
```

### 5.5 Queue Operations
- **Endpoint:** `POST /api/v1/admin/queue/call-next`
- **Endpoint:** `PATCH /api/v1/admin/queue/{tokenId}/complete`

### 5.6 Reports & Analytics
- **Endpoint:** `GET /api/v1/admin/reports/revenue?startDate=2026-10-01&endDate=2026-10-31`
- **Endpoint:** `GET /api/v1/admin/reports/appointments-summary`

### 5.7 Audit Logs
- **Endpoint:** `GET /api/v1/admin/audit-logs?page=0&size=20`
