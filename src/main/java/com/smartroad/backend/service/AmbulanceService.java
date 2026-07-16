package com.smartroad.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartroad.backend.model.Ambulance;
import com.smartroad.backend.repository.AmbulanceRepository;

@Service
public class AmbulanceService {

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    // Save ambulance
    public Ambulance save(Ambulance ambulance) {
        return ambulanceRepository.save(ambulance);
    }

   

    // Get available ambulances
    public List<Ambulance> getAvailable() {
        return ambulanceRepository.findByAvailableTrue();
    }

    // Accept emergency request
    public Ambulance acceptRequest(Long id) {

        Ambulance ambulance =
                ambulanceRepository.findById(id).orElse(null);

        if (ambulance != null) {

            ambulance.setAvailable(false);

            ambulanceRepository.save(ambulance);
        }

        return ambulance;
    }

    // Mark ambulance available again
    public Ambulance completeRequest(Long id) {

        Ambulance ambulance =
                ambulanceRepository.findById(id).orElse(null);

        if (ambulance != null) {

            ambulance.setAvailable(true);

            ambulanceRepository.save(ambulance);
        }

        return ambulance;
    }
    
    public List<Ambulance> getAll() {

        return ambulanceRepository.findAll();

    }

    public Ambulance getById(Long id) {

        return ambulanceRepository.findById(id).orElseThrow();

    }
    
    public Ambulance login(String email, String password) {

        Ambulance ambulance =
                ambulanceRepository.findByEmail(email).orElse(null);

        if (ambulance != null &&
                ambulance.getPassword().equals(password)) {

            return ambulance;

        }

        return null;

    }
    
    public Ambulance findNearestAvailable(
            Double latitude,
            Double longitude) {

        List<Ambulance> ambulances =
                ambulanceRepository.findByAvailableTrue();

        Ambulance nearest = null;

        double minDistance = Double.MAX_VALUE;

        for (Ambulance ambulance : ambulances) {

            if (ambulance.getLatitude() == null ||
                ambulance.getLongitude() == null)
                continue;

            double distance =
                    Math.sqrt(
                        Math.pow(latitude - ambulance.getLatitude(), 2)
                      + Math.pow(longitude - ambulance.getLongitude(), 2)
                    );

            if (distance < minDistance) {

                minDistance = distance;

                nearest = ambulance;

            }
        }

        return nearest;
    }
    
 }