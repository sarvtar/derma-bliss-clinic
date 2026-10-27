package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.ClinicServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicServiceRepository extends JpaRepository<ClinicServiceEntity, Long> {
    List<ClinicServiceEntity> findByIsActiveTrueOrderByCategoryAscNameAsc();
    Optional<ClinicServiceEntity> findByServiceCode(String serviceCode);
}
