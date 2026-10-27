package com.dermabliss.clinic.repository;

import com.dermabliss.clinic.entity.ClinicSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicSettingRepository extends JpaRepository<ClinicSetting, Long> {
    Optional<ClinicSetting> findFirstByOrderByIdAsc();
}
