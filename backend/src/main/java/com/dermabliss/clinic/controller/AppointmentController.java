package com.dermabliss.clinic.controller;

import com.dermabliss.clinic.dto.*;
import com.dermabliss.clinic.security.CustomUserDetails;
import com.dermabliss.clinic.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<AvailableSlotsResponse>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AvailableSlotsResponse slots = appointmentService.getAvailableSlots(date);
        return ResponseEntity.ok(ApiResponse.success(slots, "Available slots retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentDto>> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRole());
        AppointmentDto appointment = appointmentService.bookAppointment(request, userDetails.getId(), isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(appointment, "Appointment confirmed successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AppointmentDto> list = appointmentService.getMyAppointments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(list, "My appointments retrieved"));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentDto>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRole());
        AppointmentDto appointment = appointmentService.rescheduleAppointment(id, request, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(appointment, "Appointment rescheduled successfully"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRole());
        AppointmentDto appointment = appointmentService.cancelAppointment(id, request, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(appointment, "Appointment cancelled"));
    }
}
