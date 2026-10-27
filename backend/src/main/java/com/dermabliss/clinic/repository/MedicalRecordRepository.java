package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByRecordNumber(String recordNumber);
    List<MedicalRecord> findByPatientIdOrderByConsultationDateDesc(Long patientId);
    List<MedicalRecord> findByPatientIdAndIsReleasedToPatientTrueOrderByConsultationDateDesc(Long patientId);
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
}
