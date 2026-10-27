package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.ClinicProfileDto;
import com.dermabliss.clinic.dto.ClinicServiceDto;
import com.dermabliss.clinic.dto.UpdateSettingsRequest;
import com.dermabliss.clinic.entity.ClinicSetting;
import com.dermabliss.clinic.entity.ClinicServiceEntity;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.repository.ClinicServiceRepository;
import com.dermabliss.clinic.repository.ClinicSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClinicInformationService {

    private final ClinicSettingRepository clinicSettingRepository;
    private final ClinicServiceRepository clinicServiceRepository;

    public ClinicInformationService(ClinicSettingRepository clinicSettingRepository,
                                    ClinicServiceRepository clinicServiceRepository) {
        this.clinicSettingRepository = clinicSettingRepository;
        this.clinicServiceRepository = clinicServiceRepository;
    }

    @Transactional(readOnly = true)
    public ClinicProfileDto getClinicProfile() {
        ClinicSetting setting = clinicSettingRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Clinic settings not found"));

        ClinicProfileDto dto = new ClinicProfileDto();
        dto.setClinicName(setting.getClinicName());
        dto.setDoctorName(setting.getDoctorName());
        dto.setDoctorQualifications(setting.getDoctorQualifications());
        dto.setDoctorSpecialization(setting.getDoctorSpecialization());
        dto.setRegistrationNumber(setting.getDoctorRegNumber());
        dto.setContactNumber(setting.getContactNumber());
        dto.setContactEmail(setting.getContactEmail());
        dto.setAddress(setting.getAddress());
        dto.setInstagramHandle(setting.getInstagramHandle());
        dto.setBrandTagline(setting.getBrandTagline());
        dto.setTimezone(setting.getTimezone());
        dto.setDefaultConsultationFee(setting.getDefaultConsultationFee());
        dto.setSlotDurationMinutes(setting.getSlotDurationMinutes());
        dto.setBookingHorizonDays(setting.getBookingHorizonDays());
        dto.setCancellationDeadlineHours(setting.getCancellationDeadlineHours());
        dto.setReschedulingDeadlineHours(setting.getReschedulingDeadlineHours());
        dto.setAutoConfirmBookings(setting.isAutoConfirmBookings());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ClinicServiceDto> getActiveServices() {
        return clinicServiceRepository.findByIsActiveTrueOrderByCategoryAscNameAsc()
                .stream()
                .map(this::mapToServiceDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClinicProfileDto updateClinicSettings(UpdateSettingsRequest req) {
        ClinicSetting setting = clinicSettingRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Clinic settings not found"));

        if (req.getClinicName() != null && !req.getClinicName().isBlank()) setting.setClinicName(req.getClinicName().trim());
        if (req.getDoctorName() != null && !req.getDoctorName().isBlank()) setting.setDoctorName(req.getDoctorName().trim());
        if (req.getDoctorQualifications() != null && !req.getDoctorQualifications().isBlank()) setting.setDoctorQualifications(req.getDoctorQualifications().trim());
        if (req.getDoctorSpecialization() != null && !req.getDoctorSpecialization().isBlank()) setting.setDoctorSpecialization(req.getDoctorSpecialization().trim());
        if (req.getDoctorRegNumber() != null && !req.getDoctorRegNumber().isBlank()) setting.setDoctorRegNumber(req.getDoctorRegNumber().trim());
        if (req.getBrandTagline() != null && !req.getBrandTagline().isBlank()) setting.setBrandTagline(req.getBrandTagline().trim());
        if (req.getContactNumber() != null) setting.setContactNumber(req.getContactNumber());
        if (req.getContactEmail() != null) setting.setContactEmail(req.getContactEmail());
        if (req.getAddress() != null) setting.setAddress(req.getAddress());
        if (req.getInstagramHandle() != null) setting.setInstagramHandle(req.getInstagramHandle());
        if (req.getSlotDurationMinutes() > 0) setting.setSlotDurationMinutes(req.getSlotDurationMinutes());
        if (req.getDefaultConsultationFee() != null) setting.setDefaultConsultationFee(req.getDefaultConsultationFee());
        if (req.getBookingHorizonDays() > 0) setting.setBookingHorizonDays(req.getBookingHorizonDays());
        if (req.getCancellationDeadlineHours() >= 0) setting.setCancellationDeadlineHours(req.getCancellationDeadlineHours());
        if (req.getReschedulingDeadlineHours() >= 0) setting.setReschedulingDeadlineHours(req.getReschedulingDeadlineHours());
        setting.setAutoConfirmBookings(req.isAutoConfirmBookings());

        clinicSettingRepository.save(setting);
        return getClinicProfile();
    }

    private ClinicServiceDto mapToServiceDto(ClinicServiceEntity entity) {
        return new ClinicServiceDto(
                entity.getId(),
                entity.getServiceCode(),
                entity.getName(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getEstimatedDurationMin(),
                entity.isActive()
        );
    }
}
