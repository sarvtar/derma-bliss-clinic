package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.AppointmentDto;
import com.dermabliss.clinic.dto.DashboardSummaryDto;
import com.dermabliss.clinic.dto.QueueTokenDto;
import com.dermabliss.clinic.entity.AppointmentStatus;
import com.dermabliss.clinic.entity.QueueStatus;
import com.dermabliss.clinic.repository.AppointmentRepository;
import com.dermabliss.clinic.repository.InvoiceRepository;
import com.dermabliss.clinic.repository.PatientRepository;
import com.dermabliss.clinic.repository.QueueTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final PatientRepository patientRepository;
    private final InvoiceRepository invoiceRepository;
    private final AppointmentService appointmentService;
    private final QueueService queueService;

    public DashboardService(AppointmentRepository appointmentRepository,
                            QueueTokenRepository queueTokenRepository,
                            PatientRepository patientRepository,
                            InvoiceRepository invoiceRepository,
                            AppointmentService appointmentService,
                            QueueService queueService) {
        this.appointmentRepository = appointmentRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.patientRepository = patientRepository;
        this.invoiceRepository = invoiceRepository;
        this.appointmentService = appointmentService;
        this.queueService = queueService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary() {
        LocalDate today = LocalDate.now();

        long todayAppointments = appointmentRepository.countByAppointmentDate(today);
        long waitingQueue = queueTokenRepository.countByTokenDateAndStatus(today, QueueStatus.WAITING);
        long totalPatients = patientRepository.count();
        BigDecimal todayRevenue = invoiceRepository.sumPaidAmountByDate(today);

        List<AppointmentDto> appointments = appointmentService.getAppointmentsForAdmin(today, null);
        List<QueueTokenDto> queue = queueService.getQueueForDate(today);

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTodayAppointmentsCount(todayAppointments);
        dto.setWaitingQueueCount(waitingQueue);
        dto.setTotalPatientsCount(totalPatients);
        dto.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        dto.setTodayAppointments(appointments);
        dto.setActiveQueue(queue);

        return dto;
    }
}
