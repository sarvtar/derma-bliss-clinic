package com.dermabliss.clinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "prescription_medicines")
public class PrescriptionMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonIgnore
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 120)
    private String medicineName;

    @Column(name = "strength", nullable = false, length = 50)
    private String strength;

    @Column(name = "dosage_form", nullable = false, length = 50)
    private String dosageForm;

    @Column(name = "frequency", nullable = false, length = 50)
    private String frequency;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "timing_instructions", length = 100)
    private String timingInstructions;

    @Column(name = "specific_notes", length = 255)
    private String specificNotes;

    public PrescriptionMedicine() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getTimingInstructions() { return timingInstructions; }
    public void setTimingInstructions(String timingInstructions) { this.timingInstructions = timingInstructions; }

    public String getSpecificNotes() { return specificNotes; }
    public void setSpecificNotes(String specificNotes) { this.specificNotes = specificNotes; }
}
