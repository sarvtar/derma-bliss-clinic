package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Feedback> findByIsPublishedTrueOrderByCreatedAtDesc();
}
