package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.CreateMedicalRecordRequest;
import com.dermabliss.clinic.dto.MedicalRecordDto;
import com.dermabliss.clinic.entity.Appointment;
import com.dermabliss.clinic.entity.MedicalRecord;
import com.dermabliss.clinic.entity.Patient;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.repository.AppointmentRepository;
import com.dermabliss.clinic.repository.MedicalRecordRepository;
import com.dermabliss.clinic.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PatientRepository patientRepository,
                                AppointmentRepository appointmentRepository,
                                AuditService auditService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MedicalRecordDto createRecord(CreateMedicalRecordRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + req.getPatientId()));

        Appointment appointment = null;
        if (req.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(req.getAppointmentId()).orElse(null);
        }

        String recordNumber = "MR-" + req.getConsultationDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        MedicalRecord record = new MedicalRecord();
        record.setRecordNumber(recordNumber);
        record.setPatient(patient);
        record.setAppointment(appointment);
        record.setConsultationDate(req.getConsultationDate());
        record.setSymptoms(req.getSymptoms());
        record.setClinicalObservations(req.getClinicalObservations());
        record.setDiagnosis(req.getDiagnosis());
        record.setTreatmentPlan(req.getTreatmentPlan());
        record.setFollowUpRecommendation(req.getFollowUpRecommendation());
        record.setReleasedToPatient(req.isReleaseToPatient());
        record.setInternalNotes(req.getInternalNotes());

        record = medicalRecordRepository.save(record);
        auditService.log(null, "RECORD_CREATED", "medical_records", recordNumber, "CREATE", null, null, "Consultation record saved");

        return mapToDto(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordDto> getMyRecords(Long currentUserId) {
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return medicalRecordRepository.findByPatientIdAndIsReleasedToPatientTrueOrderByConsultationDateDesc(patient.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordDto> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByConsultationDateDesc(patientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalRecordDto releaseRecordToPatient(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found: " + recordId));

        record.setReleasedToPatient(true);
        record = medicalRecordRepository.save(record);
        return mapToDto(record);
    }

    private MedicalRecordDto mapToDto(MedicalRecord record) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setId(record.getId());
        dto.setRecordNumber(record.getRecordNumber());
        dto.setPatientId(record.getPatient().getId());
        dto.setPatientName(record.getPatient().getFullName());
        dto.setPatientMobile(record.getPatient().getMobileNumber());
        if (record.getAppointment() != null) {
            dto.setAppointmentId(record.getAppointment().getId());
        }
        dto.setConsultationDate(record.getConsultationDate());
        dto.setSymptoms(record.getSymptoms());
        dto.setClinicalObservations(record.getClinicalObservations());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentPlan(record.getTreatmentPlan());
        dto.setFollowUpRecommendation(record.getFollowUpRecommendation());
        dto.setReleasedToPatient(record.isReleasedToPatient());
        dto.setInternalNotes(record.getInternalNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
