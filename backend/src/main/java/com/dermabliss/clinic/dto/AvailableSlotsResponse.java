package com.dermabliss.clinic.dto;

import java.time.LocalDate;
import java.util.List;

public class AvailableSlotsResponse {
    private LocalDate date;
    private int slotDurationMinutes;
    private boolean isClinicOpen;
    private List<TimeSlotDto> slots;

    public AvailableSlotsResponse() {}

    public AvailableSlotsResponse(LocalDate date, int slotDurationMinutes, boolean isClinicOpen, List<TimeSlotDto> slots) {
        this.date = date;
        this.slotDurationMinutes = slotDurationMinutes;
        this.isClinicOpen = isClinicOpen;
        this.slots = slots;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getSlotDurationMinutes() { return slotDurationMinutes; }
    public void setSlotDurationMinutes(int slotDurationMinutes) { this.slotDurationMinutes = slotDurationMinutes; }

    public boolean isClinicOpen() { return isClinicOpen; }
    public void setClinicOpen(boolean clinicOpen) { isClinicOpen = clinicOpen; }

    public List<TimeSlotDto> getSlots() { return slots; }
    public void setSlots(List<TimeSlotDto> slots) { this.slots = slots; }
}
