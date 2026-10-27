package com.dermabliss.clinic.controller;

import com.dermabliss.clinic.dto.ApiResponse;
import com.dermabliss.clinic.dto.ClinicProfileDto;
import com.dermabliss.clinic.dto.ClinicServiceDto;
import com.dermabliss.clinic.service.ClinicInformationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clinic")
public class ClinicController {

    private final ClinicInformationService clinicInformationService;

    public ClinicController(ClinicInformationService clinicInformationService) {
        this.clinicInformationService = clinicInformationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ClinicProfileDto>> getClinicProfile() {
        ClinicProfileDto profile = clinicInformationService.getClinicProfile();
        return ResponseEntity.ok(ApiResponse.success(profile, "Clinic profile retrieved"));
    }

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ClinicServiceDto>>> getClinicServices() {
        List<ClinicServiceDto> services = clinicInformationService.getActiveServices();
        return ResponseEntity.ok(ApiResponse.success(services, "Active services retrieved"));
    }
}
