package com.smartroad.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartroad.backend.model.AccidentReport;
import com.smartroad.backend.model.EmergencyAlert;
import com.smartroad.backend.repository.AccidentReportRepository;
import com.smartroad.backend.repository.EmergencyAlertRepository;

@Service
public class EmergencyAlertService {

    @Autowired
    private EmergencyAlertRepository repository;

    @Autowired
    private AccidentReportRepository accidentReportRepository;

    public EmergencyAlert save(EmergencyAlert alert) {

        return repository.save(alert);

    }

    public List<EmergencyAlert> getPendingAlerts() {

        return repository.findByStatus("Pending");

    }

    public EmergencyAlert acceptAlert(Long id, String driverName) {

        EmergencyAlert alert =
                repository.findById(id).orElse(null);

        if (alert != null) {

            alert.setStatus("Accepted");

            alert.setDriverName(driverName);

            alert.setAcceptedTime(LocalDateTime.now());

            repository.save(alert);

            // Update report status also
            if (alert.getReportId() != null) {

                AccidentReport report =
                        accidentReportRepository
                        .findById(alert.getReportId())
                        .orElse(null);

                if (report != null) {

                    report.setStatus("Ambulance Assigned");

                    accidentReportRepository.save(report);

                }
            }

        }

        return alert;

    }

    public EmergencyAlert complete(Long id) {

        EmergencyAlert alert =
                repository.findById(id).orElse(null);

        if (alert != null) {

            alert.setStatus("Completed");

            repository.save(alert);

            // Update Accident Report
            if (alert.getReportId() != null) {

                AccidentReport report =
                        accidentReportRepository
                        .findById(alert.getReportId())
                        .orElse(null);

                if (report != null) {

                    report.setStatus("Resolved");

                    accidentReportRepository.save(report);
                }
            }
        }

        return alert;
    }

    public EmergencyAlert declineAlert(Long id) {

        EmergencyAlert alert =
                repository.findById(id).orElse(null);

        if (alert != null) {

            alert.setStatus("Declined");

            repository.save(alert);

            // Optionally update related report status if linked
            if (alert.getReportId() != null) {

                AccidentReport report =
                        accidentReportRepository
                        .findById(alert.getReportId())
                        .orElse(null);

                if (report != null) {

                    report.setStatus("Pending");

                    accidentReportRepository.save(report);
                }
            }
        }

        return alert;
    }

    public List<EmergencyAlert> getDriverAlerts(String driverName) {

        return repository.findByDriverName(driverName);

    }

    public List<EmergencyAlert> getCompletedAlerts(String driverName) {

        return repository.findByDriverNameAndStatus(
                driverName,
                "Completed");

    }

    public List<EmergencyAlert> getHistory(String driverName) {

        return repository.findByDriverNameOrderByIdDesc(driverName);

    }

}