package com.dermabliss.clinic.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelAppointmentRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;

    public CancelAppointmentRequest() {}

    public CancelAppointmentRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
