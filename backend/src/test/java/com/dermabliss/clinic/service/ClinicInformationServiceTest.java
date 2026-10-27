package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.ClinicProfileDto;
import com.dermabliss.clinic.dto.ClinicServiceDto;
import com.dermabliss.clinic.dto.UpdateSettingsRequest;
import com.dermabliss.clinic.entity.ClinicServiceEntity;
import com.dermabliss.clinic.entity.ClinicSetting;
import com.dermabliss.clinic.repository.ClinicServiceRepository;
import com.dermabliss.clinic.repository.ClinicSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicInformationServiceTest {

    @Mock
    private ClinicSettingRepository clinicSettingRepository;

    @Mock
    private ClinicServiceRepository clinicServiceRepository;

    @InjectMocks
    private ClinicInformationService clinicInformationService;

    private ClinicSetting mockSetting;

    @BeforeEach
    void setUp() {
        mockSetting = new ClinicSetting();
        mockSetting.setId(1L);
        mockSetting.setClinicName("Derma Bliss Clinic");
        mockSetting.setDoctorName("Dr. Ankita Santosh Koli");
        mockSetting.setDoctorQualifications("BHMS, PGDMS, CCH, CGO, CSD");
        mockSetting.setDoctorSpecialization("Cosmetologist & Trichologist");
        mockSetting.setDoctorRegNumber("73892");
        mockSetting.setContactNumber("+91 789 789 2177");
        mockSetting.setContactEmail("contact@dermablissclinic.com");
        mockSetting.setAddress("DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603");
        mockSetting.setInstagramHandle("@derma_bliss_clinic");
        mockSetting.setBrandTagline("DERMA BLISS — SKIN | HAIR | LASER");
        mockSetting.setTimezone("Asia/Kolkata");
        mockSetting.setDefaultConsultationFee(new BigDecimal("700.00"));
        mockSetting.setSlotDurationMinutes(30);
        mockSetting.setBookingHorizonDays(30);
        mockSetting.setAutoConfirmBookings(true);
    }

    @Test
    @DisplayName("Should retrieve Dr. Ankita Santosh Koli's official clinic profile")
    void getClinicProfile_Success() {
        when(clinicSettingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(mockSetting));

        ClinicProfileDto profile = clinicInformationService.getClinicProfile();

        assertNotNull(profile);
        assertEquals("Derma Bliss Clinic", profile.getClinicName());
        assertEquals("Dr. Ankita Santosh Koli", profile.getDoctorName());
        assertEquals("73892", profile.getRegistrationNumber());
        assertEquals("Cosmetologist & Trichologist", profile.getDoctorSpecialization());
        assertEquals("+91 789 789 2177", profile.getContactNumber());
        assertEquals("@derma_bliss_clinic", profile.getInstagramHandle());
        assertEquals(30, profile.getSlotDurationMinutes());
        assertEquals(new BigDecimal("700.00"), profile.getDefaultConsultationFee());
    }

    @Test
    @DisplayName("Should list active clinical dermatology & cosmetology services")
    void getActiveServices_Success() {
        ClinicServiceEntity s1 = new ClinicServiceEntity();
        s1.setId(1L);
        s1.setServiceCode("SKIN-REV");
        s1.setName("Acne & Scar Laser Revision");
        s1.setCategory("Skin Care");
        s1.setActive(true);

        ClinicServiceEntity s2 = new ClinicServiceEntity();
        s2.setId(2L);
        s2.setServiceCode("HAIR-PRP");
        s2.setName("PRP Hair Regrowth Therapy");
        s2.setCategory("Hair Care");
        s2.setActive(true);

        when(clinicServiceRepository.findByIsActiveTrueOrderByCategoryAscNameAsc())
                .thenReturn(List.of(s1, s2));

        List<ClinicServiceDto> services = clinicInformationService.getActiveServices();

        assertNotNull(services);
        assertEquals(2, services.size());
        assertEquals("Acne & Scar Laser Revision", services.get(0).getName());
        assertEquals("Hair Care", services.get(1).getCategory());
    }

    @Test
    @DisplayName("Should update clinic settings")
    void updateClinicSettings_Success() {
        when(clinicSettingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(mockSetting));
        when(clinicSettingRepository.save(any(ClinicSetting.class))).thenAnswer(i -> i.getArgument(0));

        UpdateSettingsRequest req = new UpdateSettingsRequest();
        req.setContactNumber("+91 789 789 9999");
        req.setDefaultConsultationFee(new BigDecimal("800.00"));

        ClinicProfileDto updated = clinicInformationService.updateClinicSettings(req);

        assertNotNull(updated);
        assertEquals("+91 789 789 9999", updated.getContactNumber());
        assertEquals(new BigDecimal("800.00"), updated.getDefaultConsultationFee());
        verify(clinicSettingRepository).save(mockSetting);
    }
}
