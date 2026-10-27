package com.dermabliss.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MedicineDto {

    private Long id;

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    @NotBlank(message = "Strength is required (e.g. 500mg, 1% w/w)")
    private String strength;

    @NotBlank(message = "Dosage form is required (e.g. Tablet, Gel, Cream, Serum)")
    private String dosageForm;

    @NotBlank(message = "Frequency is required (e.g. Once daily, Twice daily)")
    private String frequency;

    @NotNull(message = "Duration in days is required")
    private Integer durationDays;

    private String timingInstructions;
    private String specificNotes;

    public MedicineDto() {}

    public MedicineDto(Long id, String medicineName, String strength, String dosageForm, String frequency, Integer durationDays, String timingInstructions, String specificNotes) {
        this.id = id;
        this.medicineName = medicineName;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.timingInstructions = timingInstructions;
        this.specificNotes = specificNotes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getTimingInstructions() { return timingInstructions; }
    public void setTimingInstructions(String timingInstructions) { this.timingInstructions = timingInstructions; }

    public String getSpecificNotes() { return specificNotes; }
    public void setSpecificNotes(String specificNotes) { this.specificNotes = specificNotes; }
}
