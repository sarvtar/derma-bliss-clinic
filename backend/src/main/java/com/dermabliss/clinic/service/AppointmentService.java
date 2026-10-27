package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.*;
import com.dermabliss.clinic.entity.*;
import com.dermabliss.clinic.exception.InvalidOperationException;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.exception.SlotConflictException;
import com.dermabliss.clinic.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentHistoryRepository appointmentHistoryRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final BlockedSlotRepository blockedSlotRepository;
    private final ClinicSettingRepository clinicSettingRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final PatientRepository patientRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AppointmentHistoryRepository appointmentHistoryRepository,
                              DoctorAvailabilityRepository availabilityRepository,
                              BlockedSlotRepository blockedSlotRepository,
                              ClinicSettingRepository clinicSettingRepository,
                              ClinicServiceRepository clinicServiceRepository,
                              PatientRepository patientRepository,
                              QueueTokenRepository queueTokenRepository,
                              UserRepository userRepository,
                              AuditService auditService) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentHistoryRepository = appointmentHistoryRepository;
        this.availabilityRepository = availabilityRepository;
        this.blockedSlotRepository = blockedSlotRepository;
        this.clinicSettingRepository = clinicSettingRepository;
        this.clinicServiceRepository = clinicServiceRepository;
        this.patientRepository = patientRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public AvailableSlotsResponse getAvailableSlots(LocalDate date) {
        ClinicSetting setting = clinicSettingRepository.findFirstByOrderByIdAsc()
                .orElse(new ClinicSetting());

        int durationMin = setting.getSlotDurationMinutes() > 0 ? setting.getSlotDurationMinutes() : 30;

        DayOfWeek dow = date.getDayOfWeek();
        Optional<DoctorAvailability> availabilityOpt = availabilityRepository.findByDayOfWeekAndIsActiveTrue(dow.name());

        if (availabilityOpt.isEmpty()) {
            return new AvailableSlotsResponse(date, durationMin, false, List.of());
        }

        DoctorAvailability availability = availabilityOpt.get();
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findByBlockDate(date);
        boolean isFullDayBlocked = blockedSlots.stream().anyMatch(BlockedSlot::isFullDay);

        if (isFullDayBlocked) {
            return new AvailableSlotsResponse(date, durationMin, false, List.of());
        }

        List<Appointment> bookedAppointments = appointmentRepository.findByAppointmentDateAndStatusOrderByStartTimeAsc(
                date, AppointmentStatus.CONFIRMED);

        List<TimeSlotDto> slots = new ArrayList<>();
        LocalTime current = availability.getStartTime();
        LocalTime dayEnd = availability.getEndTime();
        LocalTime breakStart = availability.getBreakStartTime();
        LocalTime breakEnd = availability.getBreakEndTime();

        while (current.plusMinutes(durationMin).isBefore(dayEnd) || current.plusMinutes(durationMin).equals(dayEnd)) {
            LocalTime slotEnd = current.plusMinutes(durationMin);

            boolean inBreak = false;
            if (breakStart != null && breakEnd != null) {
                if ((current.isBefore(breakEnd) && slotEnd.isAfter(breakStart))) {
                    inBreak = true;
                }
            }

            boolean isBlocked = false;
            String reason = null;
            if (inBreak) {
                isBlocked = true;
                reason = "Doctor lunch / break";
            } else {
                for (BlockedSlot bs : blockedSlots) {
                    if (bs.getStartTime() != null && bs.getEndTime() != null) {
                        if (current.isBefore(bs.getEndTime()) && slotEnd.isAfter(bs.getStartTime())) {
                            isBlocked = true;
                            reason = bs.getReason();
                            break;
                        }
                    }
                }
            }

            boolean isBooked = false;
            if (!isBlocked) {
                for (Appointment app : bookedAppointments) {
                    if (current.isBefore(app.getEndTime()) && slotEnd.isAfter(app.getStartTime())) {
                        isBooked = true;
                        reason = "Slot already booked";
                        break;
                    }
                }
            }

            boolean available = !isBlocked && !isBooked;
            slots.add(new TimeSlotDto(current, slotEnd, available, reason));
            current = slotEnd;
        }

        return new AvailableSlotsResponse(date, durationMin, true, slots);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AppointmentDto bookAppointment(BookAppointmentRequest req, Long currentUserId, boolean isAdmin) {
        if (req.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Cannot book appointment for past dates");
        }

        Patient patient;
        if (isAdmin && req.getPatientId() != null) {
            patient = patientRepository.findById(req.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + req.getPatientId()));
        } else {
            patient = patientRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + currentUserId));
        }

        ClinicSetting setting = clinicSettingRepository.findFirstByOrderByIdAsc()
                .orElse(new ClinicSetting());
        int durationMin = setting.getSlotDurationMinutes() > 0 ? setting.getSlotDurationMinutes() : 30;
        LocalTime endTime = req.getStartTime().plusMinutes(durationMin);

        // 1. Concurrency Check with Pessimistic Lock
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                req.getAppointmentDate(), req.getStartTime(), endTime);

        if (!conflicts.isEmpty()) {
            throw new SlotConflictException("The selected slot (" + req.getStartTime() + " - " + endTime + ") has just been reserved. Please select another time slot.");
        }

        ClinicServiceEntity service = null;
        if (req.getServiceId() != null) {
            service = clinicServiceRepository.findById(req.getServiceId()).orElse(null);
        }

        String apptNumber = "APT-" + req.getAppointmentDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber(apptNumber);
        appointment.setPatient(patient);
        appointment.setService(service);
        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setStartTime(req.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setPatientNotes(req.getPatientNotes());
        appointment = appointmentRepository.save(appointment);

        // 2. Automatically generate Daily In-Clinic Queue Token
        int maxToken = queueTokenRepository.findMaxTokenNumberByTokenDate(req.getAppointmentDate());
        QueueToken token = new QueueToken();
        token.setTokenNumber(maxToken + 1);
        token.setTokenDate(req.getAppointmentDate());
        token.setAppointment(appointment);
        token.setPatient(patient);
        token.setStatus(QueueStatus.WAITING);
        queueTokenRepository.save(token);

        // 3. Record Audit Trail
        User user = userRepository.findById(currentUserId).orElse(null);
        AppointmentHistory history = new AppointmentHistory();
        history.setAppointment(appointment);
        history.setPreviousStatus("NONE");
        history.setNewStatus("CONFIRMED");
        history.setChangeType("BOOKING_CREATED");
        history.setReason(isAdmin ? "Booked by Doctor" : "Self-booked by patient");
        history.setChangedByUser(user);
        appointmentHistoryRepository.save(history);

        auditService.log(user, "APPOINTMENT_BOOKED", "appointments", appointment.getAppointmentNumber(), "CREATE", null, null, "Appointment booked");

        return mapToDto(appointment, token.getTokenNumber());
    }

    @Transactional
    public AppointmentDto rescheduleAppointment(Long appointmentId, RescheduleRequest req, Long currentUserId, boolean isAdmin) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        validateOwnership(appointment, currentUserId, isAdmin);

        ClinicSetting setting = clinicSettingRepository.findFirstByOrderByIdAsc()
                .orElse(new ClinicSetting());
        int durationMin = setting.getSlotDurationMinutes() > 0 ? setting.getSlotDurationMinutes() : 30;
        LocalTime newEndTime = req.getNewStartTime().plusMinutes(durationMin);

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                req.getNewDate(), req.getNewStartTime(), newEndTime);

        // Exclude the appointment being rescheduled itself
        boolean hasConflict = conflicts.stream().anyMatch(a -> !a.getId().equals(appointmentId));
        if (hasConflict) {
            throw new SlotConflictException("The selected new slot is unavailable. Please choose another time.");
        }

        String oldStatus = appointment.getStatus().name();
        appointment.setAppointmentDate(req.getNewDate());
        appointment.setStartTime(req.getNewStartTime());
        appointment.setEndTime(newEndTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointment = appointmentRepository.save(appointment);

        // Audit log
        User user = userRepository.findById(currentUserId).orElse(null);
        AppointmentHistory history = new AppointmentHistory();
        history.setAppointment(appointment);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus("RESCHEDULED");
        history.setChangeType("RESCHEDULED");
        history.setReason(req.getReason());
        history.setChangedByUser(user);
        appointmentHistoryRepository.save(history);

        return mapToDto(appointment, null);
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long appointmentId, CancelAppointmentRequest req, Long currentUserId, boolean isAdmin) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        validateOwnership(appointment, currentUserId, isAdmin);

        String oldStatus = appointment.getStatus().name();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(req.getReason());
        appointment = appointmentRepository.save(appointment);

        queueTokenRepository.findByAppointmentId(appointmentId).ifPresent(token -> {
            token.setStatus(QueueStatus.CANCELLED);
            queueTokenRepository.save(token);
        });

        User user = userRepository.findById(currentUserId).orElse(null);
        AppointmentHistory history = new AppointmentHistory();
        history.setAppointment(appointment);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus("CANCELLED");
        history.setChangeType("CANCELLED");
        history.setReason(req.getReason());
        history.setChangedByUser(user);
        appointmentHistoryRepository.save(history);

        return mapToDto(appointment, null);
    }

    @Transactional
    public AppointmentDto markCompleted(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);

        queueTokenRepository.findByAppointmentId(appointmentId).ifPresent(token -> {
            token.setStatus(QueueStatus.COMPLETED);
            token.setCompletedAt(java.time.LocalDateTime.now());
            queueTokenRepository.save(token);
        });

        return mapToDto(appointment, null);
    }

    @Transactional
    public AppointmentDto markNoShow(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment = appointmentRepository.save(appointment);

        queueTokenRepository.findByAppointmentId(appointmentId).ifPresent(token -> {
            token.setStatus(QueueStatus.CANCELLED);
            queueTokenRepository.save(token);
        });

        return mapToDto(appointment, null);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointments(Long currentUserId) {
        Patient patient = patientRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId())
                .stream()
                .map(a -> mapToDto(a, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForAdmin(LocalDate date, AppointmentStatus status) {
        List<Appointment> list = appointmentRepository.searchAppointments(date, status);
        return list.stream()
                .map(a -> {
                    Integer tokenNum = queueTokenRepository.findByAppointmentId(a.getId())
                            .map(QueueToken::getTokenNumber).orElse(null);
                    return mapToDto(a, tokenNum);
                })
                .collect(Collectors.toList());
    }

    private void validateOwnership(Appointment appointment, Long currentUserId, boolean isAdmin) {
        if (!isAdmin) {
            Patient patient = patientRepository.findByUserId(currentUserId).orElse(null);
            if (patient == null || !appointment.getPatient().getId().equals(patient.getId())) {
                throw new AccessDeniedException("Access denied to requested medical appointment");
            }
        }
    }

    private AppointmentDto mapToDto(Appointment a, Integer tokenNumber) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(a.getId());
        dto.setAppointmentNumber(a.getAppointmentNumber());
        dto.setPatientId(a.getPatient().getId());
        dto.setPatientName(a.getPatient().getFullName());
        dto.setPatientMobile(a.getPatient().getMobileNumber());
        if (a.getService() != null) {
            dto.setServiceId(a.getService().getId());
            dto.setServiceName(a.getService().getName());
        }
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus().name());
        dto.setPatientNotes(a.getPatientNotes());
        dto.setInternalNotes(a.getInternalNotes());
        dto.setCancellationReason(a.getCancellationReason());
        dto.setQueueTokenNumber(tokenNumber);
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
