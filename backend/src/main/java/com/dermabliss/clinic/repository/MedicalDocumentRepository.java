package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    Optional<MedicalDocument> findByDocumentReference(String documentReference);
    List<MedicalDocument> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<MedicalDocument> findByStoredFilename(String storedFilename);
}
