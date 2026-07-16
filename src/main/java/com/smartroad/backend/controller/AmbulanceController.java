package com.smartroad.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartroad.backend.model.Ambulance;
import com.smartroad.backend.service.AmbulanceService;

@RestController
@RequestMapping("/api/ambulance")
@CrossOrigin(origins = "http://localhost:5173")
public class AmbulanceController {

    @Autowired
    private AmbulanceService ambulanceService;

    @PostMapping("/register")
    public Ambulance register(@RequestBody Ambulance ambulance) {
        return ambulanceService.save(ambulance);
    }

    @GetMapping
    public List<Ambulance> getAll() {
        return ambulanceService.getAll();
    }

    @GetMapping("/available")
    public List<Ambulance> getAvailable() {
        return ambulanceService.getAvailable();
    }

    @PutMapping("/accept/{id}")
    public Ambulance acceptAlert(@PathVariable Long id) {

        Ambulance ambulance = ambulanceService.getById(id);

        ambulance.setAvailable(false);

        return ambulanceService.save(ambulance);
    }

    @PostMapping("/login")
    public Ambulance login(@RequestBody Ambulance ambulance) {

        return ambulanceService.login(
                ambulance.getEmail(),
                ambulance.getPassword());
    }

    @PutMapping("/{id}/location")
    public Ambulance updateLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        Ambulance ambulance = ambulanceService.getById(id);

        ambulance.setLatitude(latitude);
        ambulance.setLongitude(longitude);

        return ambulanceService.save(ambulance);
    }

    @GetMapping("/tracking")
    public List<Ambulance> tracking() {
        return ambulanceService.getAll();
    }

}