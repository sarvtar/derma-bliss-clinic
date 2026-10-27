package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByPatientIdOrderByInvoiceDateDesc(Long patientId);
    Optional<Invoice> findByAppointmentId(Long appointmentId);

    @Query("SELECT COALESCE(SUM(i.netAmount), 0) FROM Invoice i WHERE i.invoiceDate = :date AND i.status = 'PAID'")
    BigDecimal sumPaidAmountByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(i.netAmount), 0) FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate AND i.status = 'PAID'")
    BigDecimal sumPaidAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
