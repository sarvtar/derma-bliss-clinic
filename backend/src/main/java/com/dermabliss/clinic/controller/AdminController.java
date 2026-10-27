package com.dermabliss.clinic.controller;

import com.dermabliss.clinic.dto.*;
import com.dermabliss.clinic.entity.AppointmentStatus;
import com.dermabliss.clinic.entity.AuditLog;
import com.dermabliss.clinic.entity.Patient;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.repository.AuditLogRepository;
import com.dermabliss.clinic.repository.PatientRepository;
import com.dermabliss.clinic.security.CustomUserDetails;
import com.dermabliss.clinic.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;
    private final QueueService queueService;
    private final ClinicInformationService clinicInformationService;
    private final PatientRepository patientRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(DashboardService dashboardService,
                           AppointmentService appointmentService,
                           PrescriptionService prescriptionService,
                           MedicalRecordService medicalRecordService,
                           BillingService billingService,
                           QueueService queueService,
                           ClinicInformationService clinicInformationService,
                           PatientRepository patientRepository,
                           AuditLogRepository auditLogRepository) {
        this.dashboardService = dashboardService;
        this.appointmentService = appointmentService;
        this.prescriptionService = prescriptionService;
        this.medicalRecordService = medicalRecordService;
        this.billingService = billingService;
        this.queueService = queueService;
        this.clinicInformationService = clinicInformationService;
        this.patientRepository = patientRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboard() {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary loaded"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {
        AppointmentStatus apptStatus = status != null && !status.isBlank() ? AppointmentStatus.valueOf(status.toUpperCase()) : null;
        List<AppointmentDto> appointments = appointmentService.getAppointmentsForAdmin(date, apptStatus);
        return ResponseEntity.ok(ApiResponse.success(appointments, "Appointments retrieved"));
    }

    @PatchMapping("/appointments/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentDto>> markAppointmentComplete(@PathVariable Long id) {
        AppointmentDto appointment = appointmentService.markCompleted(id);
        return ResponseEntity.ok(ApiResponse.success(appointment, "Appointment marked as completed"));
    }

    @PatchMapping("/appointments/{id}/no-show")
    public ResponseEntity<ApiResponse<AppointmentDto>> markAppointmentNoShow(@PathVariable Long id) {
        AppointmentDto appointment = appointmentService.markNoShow(id);
        return ResponseEntity.ok(ApiResponse.success(appointment, "Appointment marked as no-show"));
    }

    @PostMapping("/prescriptions")
    public ResponseEntity<ApiResponse<PrescriptionDto>> issuePrescription(
            @Valid @RequestBody CreatePrescriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PrescriptionDto dto = prescriptionService.createPrescription(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Prescription issued successfully"));
    }

    @PostMapping("/medical-records")
    public ResponseEntity<ApiResponse<MedicalRecordDto>> createMedicalRecord(
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        MedicalRecordDto dto = medicalRecordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Medical record created successfully"));
    }

    @PatchMapping("/medical-records/{id}/release")
    public ResponseEntity<ApiResponse<MedicalRecordDto>> releaseMedicalRecord(@PathVariable Long id) {
        MedicalRecordDto dto = medicalRecordService.releaseRecordToPatient(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Record released to patient"));
    }

    @GetMapping("/patients")
    public ResponseEntity<ApiResponse<List<Patient>>> getPatients(
            @RequestParam(required = false) String search) {
        List<Patient> patients = search != null && !search.isBlank() ?
                patientRepository.searchPatients(search) : patientRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(patients, "Patients retrieved"));
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatientById(@PathVariable Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(patient, "Patient profile retrieved"));
    }

    @PostMapping("/invoices")
    public ResponseEntity<ApiResponse<InvoiceDto>> createInvoice(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam(required = false) BigDecimal discountAmount,
            @RequestParam(required = false) String notes) {
        InvoiceDto dto = billingService.createInvoice(patientId, appointmentId, totalAmount, discountAmount, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Invoice generated successfully"));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<InvoiceDto>> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvoiceDto dto = billingService.recordPayment(request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(dto, "Payment recorded successfully"));
    }

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDto>>> getAllInvoices() {
        List<InvoiceDto> invoices = billingService.getAllInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved"));
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<QueueTokenDto>>> getQueue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate qDate = date != null ? date : LocalDate.now();
        List<QueueTokenDto> queue = queueService.getQueueForDate(qDate);
        return ResponseEntity.ok(ApiResponse.success(queue, "Queue tokens retrieved"));
    }

    @PostMapping("/queue/call-next")
    public ResponseEntity<ApiResponse<QueueTokenDto>> callNextQueueToken(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate qDate = date != null ? date : LocalDate.now();
        QueueTokenDto next = queueService.callNextToken(qDate);
        return ResponseEntity.ok(ApiResponse.success(next, next != null ? "Patient called into consultation" : "No waiting patients in queue"));
    }

    @PatchMapping("/queue/{tokenId}/complete")
    public ResponseEntity<ApiResponse<QueueTokenDto>> completeQueueToken(@PathVariable Long tokenId) {
        QueueTokenDto completed = queueService.completeToken(tokenId);
        return ResponseEntity.ok(ApiResponse.success(completed, "Queue token consultation completed"));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<ClinicProfileDto>> updateClinicSettings(
            @RequestBody UpdateSettingsRequest request) {
        ClinicProfileDto updated = clinicInformationService.updateClinicSettings(request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Clinic settings updated successfully"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findTop50ByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs retrieved"));
    }
}
