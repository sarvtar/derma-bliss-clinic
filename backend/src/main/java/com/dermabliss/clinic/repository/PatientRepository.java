package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByMobileNumber(String mobileNumber);
    Optional<Patient> findByPatientNumber(String patientNumber);
    Optional<Patient> findTopByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.mobileNumber LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(p.patientNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Patient> searchPatients(@Param("query") String query);
}
