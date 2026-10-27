package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.CreatePrescriptionRequest;
import com.dermabliss.clinic.dto.MedicineDto;
import com.dermabliss.clinic.dto.PrescriptionDto;
import com.dermabliss.clinic.entity.*;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.repository.MedicalRecordRepository;
import com.dermabliss.clinic.repository.PatientRepository;
import com.dermabliss.clinic.repository.PrescriptionRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AuditService auditService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientRepository patientRepository,
                               MedicalRecordRepository medicalRecordRepository,
                               AuditService auditService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PrescriptionDto createPrescription(CreatePrescriptionRequest req, Long doctorUserId) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + req.getPatientId()));

        MedicalRecord record = null;
        if (req.getMedicalRecordId() != null) {
            record = medicalRecordRepository.findById(req.getMedicalRecordId()).orElse(null);
        }

        String rxNumber = "RX-" + req.getPrescriptionDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Cryptographic doctor digital verification hash
        String signaturePayload = "DR_ANKITA_SANTOOSH_Koli_REG_73892_" + rxNumber + "_" + req.getPrescriptionDate();
        String signatureHash = computeSha256(signaturePayload);

        Prescription prescription = new Prescription();
        prescription.setPrescriptionNumber(rxNumber);
        prescription.setPatient(patient);
        prescription.setMedicalRecord(record);
        prescription.setPrescriptionDate(req.getPrescriptionDate());
        prescription.setDiagnosisSummary(req.getDiagnosisSummary());
        prescription.setFollowUpDate(req.getFollowUpDate());
        prescription.setGeneralInstructions(req.getGeneralInstructions());
        prescription.setDoctorSignatureHash(signatureHash);
        prescription.setAmended(false);

        if (req.getMedicines() != null) {
            for (MedicineDto mDto : req.getMedicines()) {
                PrescriptionMedicine med = new PrescriptionMedicine();
                med.setMedicineName(mDto.getMedicineName());
                med.setStrength(mDto.getStrength());
                med.setDosageForm(mDto.getDosageForm());
                med.setFrequency(mDto.getFrequency());
                med.setDurationDays(mDto.getDurationDays());
                med.setTimingInstructions(mDto.getTimingInstructions());
                med.setSpecificNotes(mDto.getSpecificNotes());
                prescription.addMedicine(med);
            }
        }

        prescription = prescriptionRepository.save(prescription);
        auditService.log(null, "PRESCRIPTION_ISSUED", "prescriptions", rxNumber, "CREATE", null, null, "Digital prescription generated");

        return mapToDto(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionDto getPrescription(Long prescriptionId, Long currentUserId, boolean isAdmin) {
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));

        validateOwnership(rx, currentUserId, isAdmin);
        return mapToDto(rx);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getMyPrescriptions(Long currentUserId) {
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patient.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] generatePrescriptionPdf(Long prescriptionId, Long currentUserId, boolean isAdmin) {
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));

        validateOwnership(rx, currentUserId, isAdmin);

        Patient patient = rx.getPatient();
        String patientAgeStr = patient.getDateOfBirth() != null ?
                Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears() + " yrs" : "N/A";

        StringBuilder medsTable = new StringBuilder();
        int idx = 1;
        for (PrescriptionMedicine m : rx.getMedicines()) {
            medsTable.append("<tr>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0;'>").append(idx++).append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0; font-weight: bold;'>").append(m.getMedicineName()).append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0;'>").append(m.getStrength()).append(" (").append(m.getDosageForm()).append(")</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0;'>").append(m.getFrequency()).append("</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0;'>").append(m.getDurationDays()).append(" days</td>")
                    .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0;'>").append(m.getTimingInstructions() != null ? m.getTimingInstructions() : "-").append("</td>")
                    .append("</tr>");
        }

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>" +
                "body { font-family: sans-serif; margin: 30px; color: #2D3748; line-height: 1.5; }" +
                ".header { border-bottom: 2px solid #C8A97E; padding-bottom: 15px; margin-bottom: 20px; }" +
                ".clinic-title { font-size: 24px; font-weight: bold; color: #1A202C; margin-bottom: 4px; }" +
                ".clinic-tagline { font-size: 11px; letter-spacing: 2px; color: #B08D57; font-weight: bold; }" +
                ".doctor-info { text-align: right; float: right; }" +
                ".patient-box { background: #FDFBF7; border: 1px solid #E2E8F0; border-radius: 6px; padding: 12px; margin-bottom: 20px; }" +
                ".table { width: 100%; border-collapse: collapse; margin-top: 15px; }" +
                ".table th { background: #F7FAFC; padding: 8px; text-align: left; border-bottom: 2px solid #CBD5E0; font-size: 12px; }" +
                ".footer { margin-top: 40px; border-top: 1px solid #E2E8F0; padding-top: 15px; font-size: 10px; color: #718096; }" +
                ".signature-box { float: right; text-align: center; margin-top: 30px; }" +
                "</style></head><body>" +
                "<div class='header'>" +
                "<div style='overflow: hidden;'>" +
                "<div class='doctor-info'>" +
                "<div style='font-size: 16px; font-weight: bold;'>Dr. Ankita Santosh Koli</div>" +
                "<div style='font-size: 11px; color: #4A5568;'>BHMS, PGDMS, CCH, CGO, CSD</div>" +
                "<div style='font-size: 12px; color: #B08D57; font-weight: bold;'>Cosmetologist &amp; Trichologist &#8226; Reg. 73892</div>" +
                "</div>" +
                "<div>" +
                "<div class='clinic-title'>Derma Bliss Clinic</div>" +
                "<div class='clinic-tagline'>DERMA BLISS &#8226; SKIN | HAIR | LASER</div>" +
                "<div style='font-size: 10px; color: #718096; margin-top: 4px;'>" +
                "Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603<br/>Phone: +91 789 789 2177 &#8226; Instagram: @derma_bliss_clinic" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class='patient-box'>" +
                "<table style='width: 100%; font-size: 13px;'>" +
                "<tr>" +
                "<td><strong>Prescription #:</strong> " + rx.getPrescriptionNumber() + "</td>" +
                "<td><strong>Date:</strong> " + rx.getPrescriptionDate() + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td><strong>Patient Name:</strong> " + patient.getFullName() + " (" + patient.getPatientNumber() + ")</td>" +
                "<td><strong>Age / Gender:</strong> " + patientAgeStr + " / " + (patient.getGender() != null ? patient.getGender() : "N/A") + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td colspan='2'><strong>Diagnosis:</strong> " + (rx.getDiagnosisSummary() != null ? rx.getDiagnosisSummary() : "Dermatology Consultation") + "</td>" +
                "</tr>" +
                "</table>" +
                "</div>" +
                "<h3>Prescribed Medications (Rx)</h3>" +
                "<table class='table'>" +
                "<thead><tr><th>#</th><th>Medicine</th><th>Strength &amp; Form</th><th>Frequency</th><th>Duration</th><th>Timing</th></tr></thead>" +
                "<tbody>" + medsTable + "</tbody>" +
                "</table>" +
                (rx.getGeneralInstructions() != null ? "<div style='margin-top: 20px;'><strong>General Instructions:</strong><p style='font-size: 12px; color: #4A5568;'>" + rx.getGeneralInstructions() + "</p></div>" : "") +
                (rx.getFollowUpDate() != null ? "<div style='margin-top: 10px; font-size: 13px;'><strong>Recommended Follow-up Date:</strong> " + rx.getFollowUpDate() + "</div>" : "") +
                "<div class='signature-box'>" +
                "<div style='font-family: cursive; font-size: 18px; color: #2B6CB0;'>Dr. Ankita S. Koli</div>" +
                "<div style='font-size: 11px; font-weight: bold;'>Dr. Ankita Santosh Koli</div>" +
                "<div style='font-size: 10px; color: #718096;'>Reg. No. 73892</div>" +
                "<div style='font-size: 8px; color: #A0AEC0; margin-top: 4px;'>Digitally Signed: " + rx.getDoctorSignatureHash().substring(0, 16) + "...</div>" +
                "</div>" +
                "<div style='clear: both;'></div>" +
                "<div class='footer'>" +
                "Derma Bliss Clinic Management System &#8226; Thane East &#8226; Patient care is our supreme priority." +
                "</div>" +
                "</body></html>";

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate digital prescription PDF: " + e.getMessage(), e);
        }
    }

    private void validateOwnership(Prescription rx, Long currentUserId, boolean isAdmin) {
        if (!isAdmin) {
            Patient patient = patientRepository.findByUserId(currentUserId).orElse(null);
            if (patient == null || !rx.getPatient().getId().equals(patient.getId())) {
                throw new AccessDeniedException("Access denied to requested medical prescription");
            }
        }
    }

    private PrescriptionDto mapToDto(Prescription rx) {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(rx.getId());
        dto.setPrescriptionNumber(rx.getPrescriptionNumber());
        dto.setPatientId(rx.getPatient().getId());
        dto.setPatientName(rx.getPatient().getFullName());
        dto.setPatientMobile(rx.getPatient().getMobileNumber());
        dto.setPatientGender(rx.getPatient().getGender());
        if (rx.getPatient().getDateOfBirth() != null) {
            dto.setPatientAge(Period.between(rx.getPatient().getDateOfBirth(), LocalDate.now()).getYears());
        }
        if (rx.getMedicalRecord() != null) {
            dto.setMedicalRecordId(rx.getMedicalRecord().getId());
        }
        dto.setPrescriptionDate(rx.getPrescriptionDate());
        dto.setDiagnosisSummary(rx.getDiagnosisSummary());
        dto.setFollowUpDate(rx.getFollowUpDate());
        dto.setGeneralInstructions(rx.getGeneralInstructions());
        dto.setDoctorSignatureHash(rx.getDoctorSignatureHash());
        dto.setAmended(rx.isAmended());
        dto.setCreatedAt(rx.getCreatedAt());

        if (rx.getMedicines() != null) {
            dto.setMedicines(rx.getMedicines().stream().map(m -> {
                MedicineDto mDto = new MedicineDto();
                mDto.setId(m.getId());
                mDto.setMedicineName(m.getMedicineName());
                mDto.setStrength(m.getStrength());
                mDto.setDosageForm(m.getDosageForm());
                mDto.setFrequency(m.getFrequency());
                mDto.setDurationDays(m.getDurationDays());
                mDto.setTimingInstructions(m.getTimingInstructions());
                mDto.setSpecificNotes(m.getSpecificNotes());
                return mDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    private String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "DIGITAL_SIG_" + UUID.randomUUID();
        }
    }
}
