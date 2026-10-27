package com.dermabliss.clinic.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_settings")
public class ClinicSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_name", nullable = false, length = 120)
    private String clinicName;

    @Column(name = "doctor_name", nullable = false, length = 120)
    private String doctorName;

    @Column(name = "doctor_qualifications", nullable = false, length = 100)
    private String doctorQualifications;

    @Column(name = "doctor_specialization", nullable = false, length = 100)
    private String doctorSpecialization;

    @Column(name = "doctor_reg_number", nullable = false, length = 50)
    private String doctorRegNumber;

    @Column(name = "contact_number", nullable = false, length = 25)
    private String contactNumber;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "instagram_handle", nullable = false, length = 50)
    private String instagramHandle;

    @Column(name = "brand_tagline", nullable = false, length = 120)
    private String brandTagline;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";

    @Column(name = "slot_duration_minutes", nullable = false)
    private int slotDurationMinutes = 30;

    @Column(name = "default_consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultConsultationFee = new BigDecimal("500.00");

    @Column(name = "booking_horizon_days", nullable = false)
    private int bookingHorizonDays = 30;

    @Column(name = "cancellation_deadline_hours", nullable = false)
    private int cancellationDeadlineHours = 4;

    @Column(name = "rescheduling_deadline_hours", nullable = false)
    private int reschedulingDeadlineHours = 4;

    @Column(name = "auto_confirm_bookings", nullable = false)
    private boolean autoConfirmBookings = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ClinicSetting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }

    public String getBrandTagline() { return brandTagline; }
    public void setBrandTagline(String brandTagline) { this.brandTagline = brandTagline; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
