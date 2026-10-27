package com.dermabliss.clinic.dto;

public class UserDto {
    private Long id;
    private String phoneNumber;
    private String email;
    private String fullName;
    private String role;
    private Long patientId;
    private String patientNumber;

    public UserDto() {}

    public UserDto(Long id, String phoneNumber, String email, String fullName, String role, Long patientId, String patientNumber) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.patientId = patientId;
        this.patientNumber = patientNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientNumber() { return patientNumber; }
    public void setPatientNumber(String patientNumber) { this.patientNumber = patientNumber; }
}
