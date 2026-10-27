package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.AppointmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentHistoryRepository extends JpaRepository<AppointmentHistory, Long> {
    List<AppointmentHistory> findByAppointmentIdOrderByChangedAtDesc(Long appointmentId);
}
