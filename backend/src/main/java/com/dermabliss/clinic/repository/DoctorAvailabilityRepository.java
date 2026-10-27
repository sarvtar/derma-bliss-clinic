package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    Optional<DoctorAvailability> findByDayOfWeekAndIsActiveTrue(String dayOfWeek);
    List<DoctorAvailability> findAllByOrderByIdAsc();
}
