package com.smartroad.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroad.backend.model.EmergencyAlert;

public interface EmergencyAlertRepository
        extends JpaRepository<EmergencyAlert, Long> {

    List<EmergencyAlert> findByStatus(String status);

    List<EmergencyAlert> findByDriverName(String driverName);

    List<EmergencyAlert> findByDriverNameAndStatus(
            String driverName,
            String status);

    List<EmergencyAlert> findByDriverNameOrderByIdDesc(
            String driverName);

}