package com.smartroad.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartroad.backend.model.EmergencyAlert;
import com.smartroad.backend.service.EmergencyAlertService;

@RestController
@RequestMapping("/api/emergency-alerts")
public class EmergencyAlertController {

    @Autowired
    private EmergencyAlertService emergencyAlertService;

    @GetMapping("/pending")
    public List<EmergencyAlert> getPendingAlerts() {

        return emergencyAlertService.getPendingAlerts();

    }

    @PutMapping("/accept/{id}")
    public EmergencyAlert acceptAlert(

            @PathVariable Long id,

            @RequestParam String driverName) {

        return emergencyAlertService.acceptAlert(id, driverName);

    }

    @PutMapping("/complete/{id}")
    public EmergencyAlert complete(

            @PathVariable Long id) {

        return emergencyAlertService.complete(id);

    }
    
    @PutMapping("/decline/{id}")
    public EmergencyAlert decline(

            @PathVariable Long id) {

        return emergencyAlertService.declineAlert(id);

    }
    
    @GetMapping("/driver/{driverName}")
    public List<EmergencyAlert> getDriverAlerts(
            @PathVariable String driverName) {

        return emergencyAlertService.getDriverAlerts(driverName);

    }

    
    @GetMapping("/history/{driverName}")
    public List<EmergencyAlert> history(
            @PathVariable String driverName){

        return emergencyAlertService.getHistory(driverName);

    }
}