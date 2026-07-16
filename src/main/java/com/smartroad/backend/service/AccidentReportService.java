package com.smartroad.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartroad.backend.model.AccidentReport;
import com.smartroad.backend.model.EmergencyAlert;
import com.smartroad.backend.repository.AccidentReportRepository;

@Service
public class AccidentReportService {

    @Autowired
    private AccidentReportRepository repository;

    @Autowired
    private EmergencyAlertService emergencyAlertService;

    public AccidentReport save(AccidentReport report) {

        AccidentReport savedReport = repository.save(report);

        if (savedReport.getSeverity().equalsIgnoreCase("High")
                || savedReport.getSeverity().equalsIgnoreCase("Critical")) {

            EmergencyAlert alert = new EmergencyAlert();
            
            alert.setReportId(savedReport.getId());

            alert.setLocation(savedReport.getLocation());
            alert.setDescription(savedReport.getDescription());
            alert.setSeverity(savedReport.getSeverity());
            alert.setLatitude(savedReport.getLatitude());
            alert.setLongitude(savedReport.getLongitude());
            alert.setReportId(savedReport.getId());
            alert.setStatus("Pending");      // IMPORTANT
            alert.setDriverName(null);

            emergencyAlertService.save(alert);
        }

        return savedReport;
    }

    public List<AccidentReport> getAll() {
        return repository.findAll();
    }

    public AccidentReport getById(Long id) {
        return repository.findById(id).orElse(null);
    }
}