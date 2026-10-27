package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);
    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);
    Optional<Prescription> findByMedicalRecordId(Long medicalRecordId);
}
