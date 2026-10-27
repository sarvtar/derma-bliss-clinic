package com.dermabliss.clinic.controller;

import com.dermabliss.clinic.dto.ApiResponse;
import com.dermabliss.clinic.dto.PrescriptionDto;
import com.dermabliss.clinic.security.CustomUserDetails;
import com.dermabliss.clinic.service.PrescriptionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PrescriptionDto>>> getMyPrescriptions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PrescriptionDto> list = prescriptionService.getMyPrescriptions(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(list, "Prescriptions retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionDto>> getPrescription(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRole());
        PrescriptionDto dto = prescriptionService.getPrescription(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(dto, "Prescription details retrieved"));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(userDetails.getRole());
        byte[] pdf = prescriptionService.generatePrescriptionPdf(id, userDetails.getId(), isAdmin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Prescription-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
