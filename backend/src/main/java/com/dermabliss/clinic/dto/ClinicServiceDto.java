package com.dermabliss.clinic.dto;

public class ClinicServiceDto {
    private Long id;
    private String serviceCode;
    private String name;
    private String category;
    private String description;
    private int estimatedDurationMin;
    private boolean active;

    public ClinicServiceDto() {}

    public ClinicServiceDto(Long id, String serviceCode, String name, String category, String description, int estimatedDurationMin, boolean active) {
        this.id = id;
        this.serviceCode = serviceCode;
        this.name = name;
        this.category = category;
        this.description = description;
        this.estimatedDurationMin = estimatedDurationMin;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
