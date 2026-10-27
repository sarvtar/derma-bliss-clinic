package com.dermabliss.clinic.service;

import com.dermabliss.clinic.entity.*;
import com.dermabliss.clinic.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Service
public class BootstrapService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapService.class);

    private final UserRepository userRepository;
    private final ClinicSettingRepository clinicSettingRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.bootstrap.admin-phone:+917897892177}")
    private String adminPhone;

    @Value("${security.bootstrap.admin-password:ChangeMeInProdDoctor2026!}")
    private String adminPassword;

    public BootstrapService(UserRepository userRepository,
                            ClinicSettingRepository clinicSettingRepository,
                            ClinicServiceRepository clinicServiceRepository,
                            DoctorAvailabilityRepository doctorAvailabilityRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clinicSettingRepository = clinicSettingRepository;
        this.clinicServiceRepository = clinicServiceRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        bootstrapAdminUser();
        bootstrapClinicSettings();
        bootstrapClinicServices();
        bootstrapDoctorSchedule();
    }

    private void bootstrapAdminUser() {
        if (!userRepository.existsByPhoneNumber(adminPhone)) {
            log.info("Bootstrapping default Admin account for Dr. Ankita Santosh Koli ({})", adminPhone);
            User admin = new User(
                    adminPhone,
                    "dr.ankita@dermablissclinic.com",
                    passwordEncoder.encode(adminPassword),
                    Role.ROLE_ADMIN
            );
            admin.setStatus("ACTIVE");
            userRepository.save(admin);
            log.info("Admin account successfully bootstrapped.");
        } else {
            log.info("Admin account ({}) already exists.", adminPhone);
        }
    }

    private void bootstrapClinicSettings() {
        if (clinicSettingRepository.count() == 0) {
            log.info("Seeding default clinic settings for Derma Bliss Clinic...");
            ClinicSetting s = new ClinicSetting();
            s.setClinicName("Derma Bliss Clinic");
            s.setDoctorName("Dr. Ankita Santosh Koli");
            s.setDoctorQualifications("BHMS, PGDMS, CCH, CGO, CSD");
            s.setDoctorSpecialization("Cosmetologist & Trichologist");
            s.setDoctorRegNumber("73892");
            s.setContactNumber("+91 789 789 2177");
            s.setContactEmail("dr.ankita@dermablissclinic.com");
            s.setAddress("DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.");
            s.setInstagramHandle("@derma_bliss_clinic");
            s.setBrandTagline("DERMA BLISS — SKIN | HAIR | LASER");
            s.setTimezone("Asia/Kolkata");
            s.setSlotDurationMinutes(15);
            s.setDefaultConsultationFee(new BigDecimal("800.00"));
            s.setBookingHorizonDays(30);
            s.setCancellationDeadlineHours(4);
            s.setReschedulingDeadlineHours(4);
            s.setAutoConfirmBookings(true);
            clinicSettingRepository.save(s);
            log.info("Clinic settings initialized.");
        }
    }

    private void bootstrapClinicServices() {
        if (clinicServiceRepository.count() == 0) {
            log.info("Seeding initial clinical services...");
            List<ClinicServiceEntity> services = List.of(
                    createService("REG-PATIENT", "Regular Patient Consultation", "REGULAR_PATIENT", "Routine clinical checkup, follow-up assessment, and treatment monitoring.", 30),
                    createService("SKN-HYDRA", "HydraFacial MD & Glow Infusion", "SKIN_CARE", "Medical grade deep hydradermabrasion with hyaluronic antioxidant serum.", 45),
                    createService("LSR-CARBON", "Carbon Laser Peel (Hollywood Peel)", "LASER_TREATMENTS", "Q-Switched Nd:YAG laser carbon nano-particle pore purification.", 45),
                    createService("HAR-PRP", "PRP Hair Restoration Therapy", "HAIR_CARE", "Autologous platelet-rich plasma scalp microinjections with peptide growth factors.", 60),
                    createService("SKN-PEEL", "Chemical Peel (Glycolic & Salicylic 35%)", "SKIN_CARE", "Controlled medical exfoliation targeting acne and hyperpigmentation.", 30),
                    createService("LSR-FACE", "Laser Hair Reduction — Full Face", "LASER_TREATMENTS", "Triple-wavelength Diode laser with chilled sapphire crystal cooling.", 45),
                    createService("HAR-MESO", "MesoHair Anti-Dandruff Infusion", "HAIR_CARE", "Targeted peptide botanical mesotherapy for stubborn seborrheic dandruff.", 45)
            );
            clinicServiceRepository.saveAll(services);
            log.info("Seeded {} clinical services.", services.size());
        } else if (clinicServiceRepository.findByServiceCode("REG-PATIENT").isEmpty()) {
            clinicServiceRepository.save(
                    createService("REG-PATIENT", "Regular Patient Consultation", "REGULAR_PATIENT", "Routine clinical checkup, follow-up assessment, and treatment monitoring.", 30)
            );
            log.info("Added missing Regular Patient service to existing clinic services.");
        }
    }

    private ClinicServiceEntity createService(String code, String name, String category, String desc, int duration) {
        ClinicServiceEntity e = new ClinicServiceEntity();
        e.setServiceCode(code);
        e.setName(name);
        e.setCategory(category);
        e.setDescription(desc);
        e.setEstimatedDurationMin(duration);
        e.setActive(true);
        return e;
    }

    private void bootstrapDoctorSchedule() {
        if (doctorAvailabilityRepository.count() == 0) {
            log.info("Seeding doctor availability for Dr. Ankita Santosh Koli...");
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
            for (String day : days) {
                DoctorAvailability a = new DoctorAvailability();
                a.setDayOfWeek(day);
                a.setStartTime(LocalTime.of(10, 0));
                a.setEndTime(LocalTime.of(21, 0));
                a.setBreakStartTime(LocalTime.of(13, 0));
                a.setBreakEndTime(LocalTime.of(17, 0));
                a.setActive(true);
                doctorAvailabilityRepository.save(a);
            }
            log.info("Doctor availability seeded for Monday through Saturday.");
        }
    }
}
