package com.dermabliss.clinic.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class QueueTokenDto {
    private Long id;
    private int tokenNumber;
    private LocalDate tokenDate;
    private Long appointmentId;
    private String appointmentNumber;
    private Long patientId;
    private String patientName;
    private String patientMobile;
    private String status;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public QueueTokenDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(int tokenNumber) { this.tokenNumber = tokenNumber; }

    public LocalDate getTokenDate() { return tokenDate; }
    public void setTokenDate(LocalDate tokenDate) { this.tokenDate = tokenDate; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getAppointmentNumber() { return appointmentNumber; }
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientMobile() { return patientMobile; }
    public void setPatientMobile(String patientMobile) { this.patientMobile = patientMobile; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
