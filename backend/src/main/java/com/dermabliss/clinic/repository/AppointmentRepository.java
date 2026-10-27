package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.Appointment;
import com.dermabliss.clinic.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    List<Appointment> findByAppointmentDateOrderByStartTimeAsc(LocalDate date);

    List<Appointment> findByAppointmentDateAndStatusOrderByStartTimeAsc(LocalDate date, AppointmentStatus status);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    Page<Appointment> findByAppointmentDateOrderByStartTimeAsc(LocalDate date, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date " +
           "AND a.status IN ('CONFIRMED', 'RESCHEDULED') " +
           "AND a.startTime < :endTime AND a.endTime > :startTime")
    List<Appointment> findConflictingAppointments(@Param("date") LocalDate date,
                                                 @Param("startTime") LocalTime startTime,
                                                 @Param("endTime") LocalTime endTime);

    long countByAppointmentDate(LocalDate date);

    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE " +
           "(:date IS NULL OR a.appointmentDate = :date) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.appointmentDate DESC, a.startTime ASC")
    List<Appointment> searchAppointments(@Param("date") LocalDate date, @Param("status") AppointmentStatus status);
}
