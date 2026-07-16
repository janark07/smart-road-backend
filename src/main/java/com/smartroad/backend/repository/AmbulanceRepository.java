package com.smartroad.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroad.backend.model.Ambulance;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {

    Optional<Ambulance> findByEmail(String email);

    Optional<Ambulance> findByVehicleNumber(String vehicleNumber);

    List<Ambulance> findByAvailableTrue();

}