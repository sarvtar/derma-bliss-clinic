package com.dermabliss.clinic.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionDto {
    private Long id;
    private String prescriptionNumber;
    private Long patientId;
    private String patientName;
    private String patientMobile;
    private String patientGender;
    private Integer patientAge;
    private Long medicalRecordId;
    private LocalDate prescriptionDate;
    private String diagnosisSummary;
    private LocalDate followUpDate;
    private String generalInstructions;
    private String doctorSignatureHash;
    private boolean isAmended;
    private List<MedicineDto> medicines;
    private LocalDateTime createdAt;

    public PrescriptionDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrescriptionNumber() { return prescriptionNumber; }
    public void setPrescriptionNumber(String prescriptionNumber) { this.prescriptionNumber = prescriptionNumber; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientMobile() { return patientMobile; }
    public void setPatientMobile(String patientMobile) { this.patientMobile = patientMobile; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public Long getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(Long medicalRecordId) { this.medicalRecordId = medicalRecordId; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public String getDiagnosisSummary() { return diagnosisSummary; }
    public void setDiagnosisSummary(String diagnosisSummary) { this.diagnosisSummary = diagnosisSummary; }

    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }

    public String getGeneralInstructions() { return generalInstructions; }
    public void setGeneralInstructions(String generalInstructions) { this.generalInstructions = generalInstructions; }

    public String getDoctorSignatureHash() { return doctorSignatureHash; }
    public void setDoctorSignatureHash(String doctorSignatureHash) { this.doctorSignatureHash = doctorSignatureHash; }

    public boolean isAmended() { return isAmended; }
    public void setAmended(boolean amended) { isAmended = amended; }

    public List<MedicineDto> getMedicines() { return medicines; }
    public void setMedicines(List<MedicineDto> medicines) { this.medicines = medicines; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
