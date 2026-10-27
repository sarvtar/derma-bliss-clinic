package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.AppointmentDto;
import com.dermabliss.clinic.dto.AvailableSlotsResponse;
import com.dermabliss.clinic.dto.BookAppointmentRequest;
import com.dermabliss.clinic.entity.*;
import com.dermabliss.clinic.exception.InvalidOperationException;
import com.dermabliss.clinic.exception.SlotConflictException;
import com.dermabliss.clinic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private BlockedSlotRepository blockedSlotRepository;

    @Mock
    private ClinicSettingRepository clinicSettingRepository;

    @Mock
    private ClinicServiceRepository clinicServiceRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private QueueTokenRepository queueTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AppointmentService appointmentService;

    private ClinicSetting testSetting;
    private DoctorAvailability testAvailability;
    private Patient testPatient;
    private User testUser;

    @BeforeEach
    void setUp() {
        testSetting = new ClinicSetting();
        testSetting.setSlotDurationMinutes(30);
        testSetting.setDoctorName("Dr. Ankita Santosh Koli");

        testAvailability = new DoctorAvailability();
        testAvailability.setDayOfWeek("MONDAY");
        testAvailability.setStartTime(LocalTime.of(10, 0));
        testAvailability.setEndTime(LocalTime.of(19, 0));
        testAvailability.setBreakStartTime(LocalTime.of(13, 30));
        testAvailability.setBreakEndTime(LocalTime.of(15, 0));
        testAvailability.setActive(true);

        testUser = new User();
        testUser.setId(10L);
        testUser.setPhoneNumber("+919876543210");
        testUser.setRole(Role.ROLE_PATIENT);

        testPatient = new Patient();
        testPatient.setId(100L);
        testPatient.setUser(testUser);
        testPatient.setFullName("Sneha Patil");
        testPatient.setMobileNumber("+919876543210");
        testPatient.setPatientNumber("PT-000100");
    }

    @Test
    @DisplayName("Should return available slots respecting doctor working hours and lunch break")
    void getAvailableSlots_WorkingDayWithLunchBreak() {
        // Monday date in future
        LocalDate testDate = LocalDate.now().plusWeeks(1);
        while (testDate.getDayOfWeek().getValue() != 1) { // ensure Monday
            testDate = testDate.plusDays(1);
        }

        when(clinicSettingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testSetting));
        when(availabilityRepository.findByDayOfWeekAndIsActiveTrue("MONDAY")).thenReturn(Optional.of(testAvailability));
        when(blockedSlotRepository.findByBlockDate(testDate)).thenReturn(Collections.emptyList());
        when(appointmentRepository.findByAppointmentDateAndStatusOrderByStartTimeAsc(eq(testDate), eq(AppointmentStatus.CONFIRMED)))
                .thenReturn(Collections.emptyList());

        AvailableSlotsResponse response = appointmentService.getAvailableSlots(testDate);

        assertNotNull(response);
        assertTrue(response.isClinicOpen());
        assertEquals(30, response.getSlotDurationMinutes());
        assertFalse(response.getSlots().isEmpty());

        // Verify slot during lunch break (13:30 - 14:00) is marked unavailable
        response.getSlots().stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(13, 30)))
                .findFirst()
                .ifPresent(slot -> {
                    assertFalse(slot.isAvailable());
                    assertTrue(slot.getReason().contains("Doctor lunch / break"));
                });

        // Verify morning slot (10:00 - 10:30) is available
        response.getSlots().stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(10, 0)))
                .findFirst()
                .ifPresent(slot -> {
                    assertTrue(slot.isAvailable());
                    assertNull(slot.getReason());
                });
    }

    @Test
    @DisplayName("Should throw SlotConflictException when double-booking is attempted on same slot")
    void bookAppointment_DoubleBookingConflict_ThrowsException() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime slotTime = LocalTime.of(11, 0);
        BookAppointmentRequest req = new BookAppointmentRequest(null, null, futureDate, slotTime, "Consultation");

        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(testPatient));
        when(clinicSettingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testSetting));

        // Simulate pessimistic lock finding an existing overlapping appointment
        Appointment existing = new Appointment();
        existing.setId(99L);
        when(appointmentRepository.findConflictingAppointments(eq(futureDate), eq(slotTime), eq(LocalTime.of(11, 30))))
                .thenReturn(List.of(existing));

        SlotConflictException ex = assertThrows(SlotConflictException.class, () ->
                appointmentService.bookAppointment(req, 10L, false));

        assertTrue(ex.getMessage().contains("has just been reserved"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(queueTokenRepository, never()).save(any(QueueToken.class));
    }

    @Test
    @DisplayName("Should book appointment, assign queue token, and create history trail")
    void bookAppointment_Success() {
        LocalDate futureDate = LocalDate.now().plusDays(3);
        LocalTime slotTime = LocalTime.of(11, 30);
        BookAppointmentRequest req = new BookAppointmentRequest(null, null, futureDate, slotTime, "Skin allergy checkup");

        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(testPatient));
        when(clinicSettingRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testSetting));
        when(appointmentRepository.findConflictingAppointments(eq(futureDate), eq(slotTime), eq(LocalTime.of(12, 0))))
                .thenReturn(Collections.emptyList());

        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(500L);
        savedAppointment.setAppointmentNumber("APT-20261028-1A2B");
        savedAppointment.setPatient(testPatient);
        savedAppointment.setAppointmentDate(futureDate);
        savedAppointment.setStartTime(slotTime);
        savedAppointment.setEndTime(LocalTime.of(12, 0));
        savedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        savedAppointment.setPatientNotes("Skin allergy checkup");

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);
        when(queueTokenRepository.findMaxTokenNumberByTokenDate(futureDate)).thenReturn(4); // previous tokens = 4
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

        AppointmentDto result = appointmentService.bookAppointment(req, 10L, false);

        assertNotNull(result);
        assertEquals("APT-20261028-1A2B", result.getAppointmentNumber());
        assertEquals("Sneha Patil", result.getPatientName());
        assertEquals(5, result.getQueueTokenNumber()); // 4 + 1
        assertEquals("CONFIRMED", result.getStatus());

        verify(appointmentRepository).save(any(Appointment.class));
        verify(queueTokenRepository).save(any(QueueToken.class));
        verify(appointmentHistoryRepository).save(any(AppointmentHistory.class));
    }

    @Test
    @DisplayName("Should prevent past date appointment booking")
    void bookAppointment_PastDate_ThrowsInvalidOperationException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        BookAppointmentRequest req = new BookAppointmentRequest(null, null, pastDate, LocalTime.of(10, 0), null);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                appointmentService.bookAppointment(req, 10L, false));

        assertTrue(ex.getMessage().contains("Cannot book appointment for past dates"));
        verify(appointmentRepository, never()).findConflictingAppointments(any(), any(), any());
    }

    @Test
    @DisplayName("Should enforce anti-IDOR check on appointment cancellation")
    void cancelAppointment_OtherPatientAttempt_ThrowsAccessDeniedException() {
        Appointment appt = new Appointment();
        appt.setId(200L);
        appt.setPatient(testPatient); // belongs to testPatient (user ID 10)
        appt.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(200L)).thenReturn(Optional.of(appt));

        // Different patient (user ID 99) tries to cancel appt of patient ID 100
        Patient hackerPatient = new Patient();
        hackerPatient.setId(999L);
        when(patientRepository.findByUserId(99L)).thenReturn(Optional.of(hackerPatient));

        assertThrows(AccessDeniedException.class, () ->
                appointmentService.cancelAppointment(200L, new com.dermabliss.clinic.dto.CancelAppointmentRequest("Malicious cancel"), 99L, false));

        verify(appointmentRepository, never()).save(appt);
    }
}
