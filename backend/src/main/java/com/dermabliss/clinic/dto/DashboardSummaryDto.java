package com.dermabliss.clinic.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDto {
    private long todayAppointmentsCount;
    private long waitingQueueCount;
    private long totalPatientsCount;
    private BigDecimal todayRevenue;
    private List<AppointmentDto> todayAppointments;
    private List<QueueTokenDto> activeQueue;

    public DashboardSummaryDto() {}

    public long getTodayAppointmentsCount() { return todayAppointmentsCount; }
    public void setTodayAppointmentsCount(long todayAppointmentsCount) { this.todayAppointmentsCount = todayAppointmentsCount; }

    public long getWaitingQueueCount() { return waitingQueueCount; }
    public void setWaitingQueueCount(long waitingQueueCount) { this.waitingQueueCount = waitingQueueCount; }

    public long getTotalPatientsCount() { return totalPatientsCount; }
    public void setTotalPatientsCount(long totalPatientsCount) { this.totalPatientsCount = totalPatientsCount; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public List<AppointmentDto> getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(List<AppointmentDto> todayAppointments) { this.todayAppointments = todayAppointments; }

    public List<QueueTokenDto> getActiveQueue() { return activeQueue; }
    public void setActiveQueue(List<QueueTokenDto> activeQueue) { this.activeQueue = activeQueue; }
}
