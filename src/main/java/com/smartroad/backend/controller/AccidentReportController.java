package com.smartroad.backend.controller;

import com.smartroad.backend.model.AccidentReport;
import com.smartroad.backend.repository.AccidentReportRepository;
import com.smartroad.backend.service.AccidentReportService;
import com.smartroad.backend.service.EmailService;
import com.smartroad.backend.model.User;
import com.smartroad.backend.repository.UserRepository;
import com.smartroad.backend.service.NotificationService;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;

import com.smartroad.backend.service.ExcelService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class AccidentReportController {

	
	@Autowired
	private AccidentReportRepository reportRepository;
	
	
	@Autowired
	private AccidentReportService accidentReportService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExcelService excelService;

    @GetMapping("/test-mail")
    public String testMail() {

        emailService.sendMail(
                "your_email@gmail.com",
                "Test Email",
                "Email service is working!");

        return "Mail Sent";
    }

    // CREATE REPORT
    @PostMapping
    public AccidentReport createReport(
            @RequestBody AccidentReport report) {

        report.setStatus("Pending");
        report.setDate(LocalDate.now().toString());

        AccidentReport saved =
                accidentReportService.save(report);

        notificationService.sendNotification(
                "🚨 New Accident Report from "
                        + saved.getFullName()
                        + " (" + saved.getSeverity() + ")"
        );

        return saved;
    }

    // GET ALL REPORTS
    @GetMapping
    public List<AccidentReport> getAllReports() {
        return reportRepository.findAll();
    }

    // GET REPORTS OF A USER
    @GetMapping("/user/{fullName}")
    public List<AccidentReport> getUserReports(
            @PathVariable String fullName) {

        return reportRepository.findByFullName(fullName);
    }

    // UPDATE STATUS
    @PutMapping("/{id}/status")
    public AccidentReport updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        AccidentReport report =
                reportRepository.findById(id).orElse(null);

        if (report != null) {

            report.setStatus(status);

            AccidentReport updatedReport =
                    reportRepository.save(report);

            // Send email when report is resolved
            if ("Resolved".equalsIgnoreCase(status)) {

                User user =
                        userRepository.findByFullName(
                                report.getFullName());

                if (user != null) {

                    String subject =
                            "🚨 Accident Report Status Updated";

                    String body =
                            "Dear " + user.getFullName() + ",\n\n"

                            + "Your accident report has been reviewed.\n\n"

                            + "Location : "
                            + report.getLocation() + "\n"

                            + "Severity : "
                            + report.getSeverity() + "\n"

                            + "Status : "
                            + report.getStatus() + "\n\n"

                            + "Admin Response :\n"
                            + (report.getAdminResponse() == null
                                    ? "No response provided."
                                    : report.getAdminResponse())

                            + "\n\n"

                            + "Thank you for helping improve road safety.\n\n"

                            + "Smart Road Accident Reporting System";

                    emailService.sendMail(
                            user.getEmail(),
                            subject,
                            body);

                }

            }

            return updatedReport;

        }

        return null;

    }

    // UPDATE ADMIN RESPONSE
    @PutMapping("/{id}/response")
    public AccidentReport updateResponse(
            @PathVariable Long id,
            @RequestParam String response) {

        AccidentReport report =
                reportRepository.findById(id).orElse(null);

        if (report != null) {

            report.setAdminResponse(response);

            return reportRepository.save(report);
        }

        return null;
    }

    // DELETE REPORT
    @DeleteMapping("/{id}")
    public void deleteReport(@PathVariable Long id) {
        reportRepository.deleteById(id);
    }

    // IMAGE UPLOAD
    @PostMapping("/upload")
    public String uploadImage(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        Files.createDirectories(Paths.get("uploads"));

        String fileName =
                System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path path = Paths.get("uploads/" + fileName);

        Files.write(path, file.getBytes());

        return fileName;
    }

    // HOTSPOTS
    @GetMapping("/hotspots")
    public List<Object[]> getHotspots() {
        return reportRepository.getHotspots();
    }

    // PDF DOWNLOAD
    @GetMapping("/pdf")
    public void downloadPdf(HttpServletResponse response)
            throws Exception {

        response.setContentType("application/pdf");

        Document document = new Document();

        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        document.add(new Paragraph("Accident Reports"));

        PdfPTable table = new PdfPTable(5);

        table.addCell("ID");
        table.addCell("Reporter");
        table.addCell("Location");
        table.addCell("Severity");
        table.addCell("Status");

        List<AccidentReport> reports = reportRepository.findAll();

        for (AccidentReport report : reports) {

            table.addCell(String.valueOf(report.getId()));
            table.addCell(report.getFullName());
            table.addCell(report.getLocation());
            table.addCell(report.getSeverity());
            table.addCell(report.getStatus());
        }

        document.add(table);

        document.close();
    }

    // DASHBOARD STATS
    @GetMapping("/stats")
    public Map<String, Long> getStats() {

        List<AccidentReport> reports = reportRepository.findAll();

        long total = reports.size();

        long pending = reports.stream()
                .filter(r -> "Pending".equals(r.getStatus()))
                .count();

        long resolved = reports.stream()
                .filter(r -> "Resolved".equals(r.getStatus()))
                .count();

        long critical = reports.stream()
                .filter(r -> "Critical".equals(r.getSeverity()))
                .count();

        Map<String, Long> stats = new HashMap<>();

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("resolved", resolved);
        stats.put("critical", critical);

        return stats;
    }
    
    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> downloadExcel()
            throws Exception {

        List<AccidentReport> reports =
                reportRepository.findAll();

        ByteArrayInputStream in =
                excelService.export(reports);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "Content-Disposition",
                "attachment; filename=AccidentReports.xlsx"
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));

    }
    
    @GetMapping("/monthly")
    public List<Object[]> getMonthlyReports() {

        return reportRepository.getMonthlyReports();

    }
}