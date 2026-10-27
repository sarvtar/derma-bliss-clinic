package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.QueueStatus;
import com.dermabliss.clinic.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {
    List<QueueToken> findByTokenDateOrderByTokenNumberAsc(LocalDate date);
    List<QueueToken> findByTokenDateAndStatusOrderByTokenNumberAsc(LocalDate date, QueueStatus status);
    Optional<QueueToken> findByAppointmentId(Long appointmentId);
    Optional<QueueToken> findByTokenDateAndTokenNumber(LocalDate date, int tokenNumber);

    @Query("SELECT COALESCE(MAX(q.tokenNumber), 0) FROM QueueToken q WHERE q.tokenDate = :date")
    int findMaxTokenNumberByTokenDate(@Param("date") LocalDate date);

    long countByTokenDateAndStatus(LocalDate date, QueueStatus status);
}
