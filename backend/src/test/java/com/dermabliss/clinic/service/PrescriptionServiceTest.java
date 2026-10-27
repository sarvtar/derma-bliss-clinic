package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.CreatePrescriptionRequest;
import com.dermabliss.clinic.dto.MedicineDto;
import com.dermabliss.clinic.dto.PrescriptionDto;
import com.dermabliss.clinic.entity.Patient;
import com.dermabliss.clinic.entity.Prescription;
import com.dermabliss.clinic.entity.PrescriptionMedicine;
import com.dermabliss.clinic.entity.User;
import com.dermabliss.clinic.repository.MedicalRecordRepository;
import com.dermabliss.clinic.repository.PatientRepository;
import com.dermabliss.clinic.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Patient testPatient;
    private Prescription testPrescription;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(25L);

        testPatient = new Patient();
        testPatient.setId(205L);
        testPatient.setUser(user);
        testPatient.setFullName("Kavita Rao");
        testPatient.setMobileNumber("+919811223344");
        testPatient.setPatientNumber("PT-000205");
        testPatient.setGender("FEMALE");
        testPatient.setDateOfBirth(LocalDate.of(1996, 8, 14));

        testPrescription = new Prescription();
        testPrescription.setId(301L);
        testPrescription.setPrescriptionNumber("RX-20261025-ABCD");
        testPrescription.setPatient(testPatient);
        testPrescription.setPrescriptionDate(LocalDate.now());
        testPrescription.setDiagnosisSummary("Acne Vulgaris Grade II");
        testPrescription.setDoctorSignatureHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

        PrescriptionMedicine med = new PrescriptionMedicine();
        med.setMedicineName("Clindamycin Gel 1%");
        med.setStrength("1%");
        med.setDosageForm("Gel");
        med.setFrequency("Twice Daily");
        med.setDurationDays(14);
        med.setTimingInstructions("Apply locally morning and night after gentle face wash");
        testPrescription.addMedicine(med);
    }

    @Test
    @DisplayName("Should create digital prescription with doctor signature hash and medicines")
    void createPrescription_Success() {
        CreatePrescriptionRequest req = new CreatePrescriptionRequest();
        req.setPatientId(205L);
        req.setPrescriptionDate(LocalDate.now());
        req.setDiagnosisSummary("Alopecia Areata");
        req.setGeneralInstructions("Avoid harsh shampoos; follow up in 3 weeks");
        req.setFollowUpDate(LocalDate.now().plusWeeks(3));

        MedicineDto medDto = new MedicineDto(
                null, "Minoxidil Topical Solution 5%", "5%", "Lotion", "Once Daily", 30, "Night time application", "Massage gently"
        );
        req.setMedicines(List.of(medDto));

        when(patientRepository.findById(205L)).thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription p = invocation.getArgument(0);
            p.setId(302L);
            return p;
        });

        PrescriptionDto dto = prescriptionService.createPrescription(req, 1L);

        assertNotNull(dto);
        assertTrue(dto.getPrescriptionNumber().startsWith("RX-"));
        assertEquals("Kavita Rao", dto.getPatientName());
        assertEquals("Alopecia Areata", dto.getDiagnosisSummary());
        assertNotNull(dto.getDoctorSignatureHash());
        assertEquals(64, dto.getDoctorSignatureHash().length(), "SHA-256 hash must be 64 hexadecimal characters");
        assertEquals(1, dto.getMedicines().size());
        assertEquals("Minoxidil Topical Solution 5%", dto.getMedicines().get(0).getMedicineName());

        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should render prescription HTML to valid PDF binary stream starting with %PDF")
    void generatePdfBytes_GeneratesValidPdf() {
        when(prescriptionRepository.findById(301L)).thenReturn(Optional.of(testPrescription));

        byte[] pdfBytes = prescriptionService.generatePrescriptionPdf(301L, 1L, true);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "Generated PDF must contain valid binary document data");

        // Validate standard PDF magic number (%PDF)
        assertEquals(0x25, pdfBytes[0]); // '%'
        assertEquals(0x50, pdfBytes[1]); // 'P'
        assertEquals(0x44, pdfBytes[2]); // 'D'
        assertEquals(0x46, pdfBytes[3]); // 'F'
    }

    @Test
    @DisplayName("Should enforce anti-IDOR when unauthorized patient tries to download prescription")
    void getPrescription_UnauthorizedPatient_ThrowsAccessDenied() {
        when(prescriptionRepository.findById(301L)).thenReturn(Optional.of(testPrescription));

        // Authenticated user is someone else (userId 999, patientId 888)
        Patient differentPatient = new Patient();
        differentPatient.setId(888L);
        when(patientRepository.findByUserId(999L)).thenReturn(Optional.of(differentPatient));

        assertThrows(AccessDeniedException.class, () ->
                prescriptionService.getPrescription(301L, 999L, false));
    }
}
