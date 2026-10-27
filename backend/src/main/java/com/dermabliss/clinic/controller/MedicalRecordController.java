package com.dermabliss.clinic.controller;

import com.dermabliss.clinic.dto.ApiResponse;
import com.dermabliss.clinic.dto.MedicalRecordDto;
import com.dermabliss.clinic.security.CustomUserDetails;
import com.dermabliss.clinic.service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MedicalRecordDto>>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MedicalRecordDto> list = medicalRecordService.getMyRecords(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(list, "My medical records retrieved"));
    }
}
