package com.smartroad.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartroad.backend.repository.AccidentReportRepository;
import com.smartroad.backend.repository.AmbulanceRepository;
import com.smartroad.backend.repository.UserRepository;

@Service
public class AnalyticsService {

    @Autowired
    private AccidentReportRepository reportRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Long> getDashboardStats() {

        Map<String, Long> stats = new HashMap<>();

        long totalReports = reportRepository.count();

        long pendingReports =
                reportRepository.findAll()
                .stream()
                .filter(r -> "Pending".equalsIgnoreCase(r.getStatus()))
                .count();

        long resolvedReports =
                reportRepository.findAll()
                .stream()
                .filter(r -> "Resolved".equalsIgnoreCase(r.getStatus()))
                .count();

        long criticalReports =
                reportRepository.findAll()
                .stream()
                .filter(r -> "Critical".equalsIgnoreCase(r.getSeverity()))
                .count();

        long totalAmbulances =
                ambulanceRepository.count();

        long availableAmbulances =
                ambulanceRepository.findByAvailableTrue().size();

        long busyAmbulances =
                totalAmbulances - availableAmbulances;

        long totalUsers =
                userRepository.count();

        stats.put("totalReports", totalReports);
        stats.put("pendingReports", pendingReports);
        stats.put("resolvedReports", resolvedReports);
        stats.put("criticalReports", criticalReports);
        stats.put("totalAmbulances", totalAmbulances);
        stats.put("availableAmbulances", availableAmbulances);
        stats.put("busyAmbulances", busyAmbulances);
        stats.put("totalUsers", totalUsers);

        return stats;
    }

}