package com.dermabliss.clinic.dto;

import java.math.BigDecimal;

public class UpdateSettingsRequest {
    private String clinicName;
    private String doctorName;
    private String doctorQualifications;
    private String doctorSpecialization;
    private String doctorRegNumber;
    private String brandTagline;
    private String contactNumber;
    private String contactEmail;
    private String address;
    private String instagramHandle;
    private int slotDurationMinutes;
    private BigDecimal defaultConsultationFee;
    private int bookingHorizonDays;
    private int cancellationDeadlineHours;
    private int reschedulingDeadlineHours;
    private boolean autoConfirmBookings;

    public UpdateSettingsRequest() {}

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorQualifications() { return doctorQualifications; }
    public void setDoctorQualifications(String doctorQualifications) { this.doctorQualifications = doctorQualifications; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public String getDoctorRegNumber() { return doctorRegNumber; }
    public void setDoctorRegNumber(String doctorRegNumber) { this.doctorRegNumber = doctorRegNumber; }

    public String getBrandTagline() { return brandTagline; }
    public void setBrandTagline(String brandTagline) { this.brandTagline = brandTagline; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }

    public int getSlotDurationMinutes() { return slotDurationMinutes; }
    public void setSlotDurationMinutes(int slotDurationMinutes) { this.slotDurationMinutes = slotDurationMinutes; }

    public BigDecimal getDefaultConsultationFee() { return defaultConsultationFee; }
    public void setDefaultConsultationFee(BigDecimal defaultConsultationFee) { this.defaultConsultationFee = defaultConsultationFee; }

    public int getBookingHorizonDays() { return bookingHorizonDays; }
    public void setBookingHorizonDays(int bookingHorizonDays) { this.bookingHorizonDays = bookingHorizonDays; }

    public int getCancellationDeadlineHours() { return cancellationDeadlineHours; }
    public void setCancellationDeadlineHours(int cancellationDeadlineHours) { this.cancellationDeadlineHours = cancellationDeadlineHours; }

    public int getReschedulingDeadlineHours() { return reschedulingDeadlineHours; }
    public void setReschedulingDeadlineHours(int reschedulingDeadlineHours) { this.reschedulingDeadlineHours = reschedulingDeadlineHours; }

    public boolean isAutoConfirmBookings() { return autoConfirmBookings; }
    public void setAutoConfirmBookings(boolean autoConfirmBookings) { this.autoConfirmBookings = autoConfirmBookings; }
}
