package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {
    List<BlockedSlot> findByBlockDate(LocalDate blockDate);
    List<BlockedSlot> findByBlockDateBetween(LocalDate startDate, LocalDate endDate);
}
