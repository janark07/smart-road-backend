package com.smartroad.backend.controller;

import com.smartroad.backend.model.AccidentReport;
import com.smartroad.backend.repository.AccidentReportRepository;
import com.smartroad.backend.service.AccidentReportService;
import com.smartroad.backend.service.EmailService;
import com.smartroad.backend.model.User;
import com.smartroad.backend.repository.UserRepository;
import com.smartroad.backend.service.NotificationService;
import com.smartroad.backend.service.S3Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.draw.LineSeparator;
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
    
    @Autowired
    private S3Service s3Service;

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
    public String uploadImage(@RequestParam("file") MultipartFile file)
            throws IOException {

        return s3Service.uploadFile(file);
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
        response.setHeader("Content-Disposition", "attachment; filename=accident_reports_ledger.pdf");

        // A4 page with margins 36pt left/right, 54pt top/bottom
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        
        // Attach Header & Footer Page Events
        writer.setPageEvent(new HeaderFooterPageEvent());

        document.open();

        // ------------------ FONTS DEFINITION ------------------
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(15, 23, 42));
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(107, 114, 128));
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(51, 65, 85));
        Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(51, 65, 85));
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(15, 23, 42));

        // ------------------ TITLE / HEADER SECTION ------------------
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65f, 35f});
        headerTable.setSpacingAfter(15f);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("SMART ROAD SAFETY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(30, 58, 138))));
        Paragraph titlePara = new Paragraph("Incident Register Ledger", titleFont);
        titlePara.setSpacingAfter(4f);
        leftCell.addElement(titlePara);
        leftCell.addElement(new Paragraph("Official report generated from civilian emergency dispatcher grids.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new BaseColor(100, 116, 139))));
        headerTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph datePara = new Paragraph("Date Generated: " + java.time.LocalDate.now().toString(), metaFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(datePara);
        
        Paragraph timePara = new Paragraph("Time Generated: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")), metaFont);
        timePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(timePara);
        
        Paragraph statusPara = new Paragraph("Status: ACTIVE RESPONSE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(16, 185, 129)));
        statusPara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(statusPara);
        
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Horizontal Separator Line
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(new BaseColor(229, 231, 235));
        separator.setLineWidth(1f);
        document.add(new Chunk(separator));
        document.add(new Paragraph(" "));

        // Fetch data
        List<AccidentReport> reports = reportRepository.findAll();

        // ------------------ DASHBOARD / STATISTICS WIDGETS ------------------
        long totalCount = reports.size();
        long criticalCount = reports.stream().filter(r -> "Critical".equalsIgnoreCase(r.getSeverity())).count();
        long pendingCount = reports.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count();
        long resolvedCount = reports.stream().filter(r -> "Resolved".equalsIgnoreCase(r.getStatus())).count();

        PdfPTable statsTable = new PdfPTable(4);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(20f);
        statsTable.setWidths(new float[]{25f, 25f, 25f, 25f});

        addStatsCard(statsTable, "TOTAL REPORTS", String.valueOf(totalCount), new BaseColor(30, 58, 138));
        addStatsCard(statsTable, "CRITICAL SEVERITY", String.valueOf(criticalCount), new BaseColor(220, 53, 69));
        addStatsCard(statsTable, "PENDING ACTIONS", String.valueOf(pendingCount), new BaseColor(245, 158, 11));
        addStatsCard(statsTable, "RESOLVED INCIDENTS", String.valueOf(resolvedCount), new BaseColor(16, 185, 129));

        document.add(statsTable);

        // Section Title
        Paragraph secTitle = new Paragraph("Detailed Incident Ledger Reports", sectionFont);
        secTitle.setSpacingAfter(10f);
        document.add(secTitle);

        // ------------------ REPORTS DATA TABLE ------------------
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{6f, 18f, 32f, 15f, 15f, 14f});
        table.setHeaderRows(1);

        // Add styled table headers
        String[] headers = {"ID", "Reporter", "Location & Coordinates", "Severity", "Status", "Date"};
        for (String headerText : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(headerText, headerFont));
            hCell.setBackgroundColor(new BaseColor(15, 23, 42)); // Deep Navy
            hCell.setPadding(8f);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hCell.setBorderColor(new BaseColor(51, 65, 85));
            table.addCell(hCell);
        }

        // Add table row cells
        int rowIndex = 0;
        for (AccidentReport report : reports) {
            BaseColor rowBg = (rowIndex % 2 == 0) ? BaseColor.WHITE : new BaseColor(248, 250, 252); // Alternating `#F8FAFC`
            BaseColor borderColor = new BaseColor(226, 232, 240); // Light border color

            // ID
            PdfPCell cellId = new PdfPCell(new Phrase(String.valueOf(report.getId()), bodyBoldFont));
            cellId.setBackgroundColor(rowBg);
            cellId.setPadding(6f);
            cellId.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellId.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellId.setBorderColor(borderColor);
            table.addCell(cellId);

            // Reporter
            PdfPCell cellReporter = new PdfPCell(new Phrase(report.getFullName(), bodyFont));
            cellReporter.setBackgroundColor(rowBg);
            cellReporter.setPadding(6f);
            cellReporter.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellReporter.setBorderColor(borderColor);
            table.addCell(cellReporter);

            // Location (display address with coordinates if available)
            String locString = report.getLocation();
            if (report.getLatitude() != null && report.getLongitude() != null) {
                locString += String.format("\n(Lat: %.4f, Lng: %.4f)", report.getLatitude(), report.getLongitude());
            }
            PdfPCell cellLocation = new PdfPCell(new Phrase(locString, bodyFont));
            cellLocation.setBackgroundColor(rowBg);
            cellLocation.setPadding(6f);
            cellLocation.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellLocation.setBorderColor(borderColor);
            table.addCell(cellLocation);

            // Severity Badge
            String sev = report.getSeverity() != null ? report.getSeverity() : "Low";
            PdfPCell cellSeverity = new PdfPCell();
            cellSeverity.setBackgroundColor(rowBg);
            cellSeverity.setPadding(6f);
            cellSeverity.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellSeverity.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellSeverity.setBorderColor(borderColor);
            
            Paragraph sevPara = new Paragraph(sev.toUpperCase(), getBadgeFont(sev));
            PdfPTable badgeTable = new PdfPTable(1);
            badgeTable.setWidthPercentage(90);
            PdfPCell badgeCell = new PdfPCell(sevPara);
            badgeCell.setBackgroundColor(getBadgeBg(sev));
            badgeCell.setPadding(3f);
            badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeCell.setBorder(Rectangle.NO_BORDER);
            badgeTable.addCell(badgeCell);
            cellSeverity.addElement(badgeTable);
            table.addCell(cellSeverity);

            // Status Badge
            String stat = report.getStatus() != null ? report.getStatus() : "Pending";
            PdfPCell cellStatus = new PdfPCell();
            cellStatus.setBackgroundColor(rowBg);
            cellStatus.setPadding(6f);
            cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellStatus.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellStatus.setBorderColor(borderColor);
            
            Paragraph statPara = new Paragraph(stat.toUpperCase(), getStatusFont(stat));
            PdfPTable statBadgeTable = new PdfPTable(1);
            statBadgeTable.setWidthPercentage(90);
            PdfPCell statBadgeCell = new PdfPCell(statPara);
            statBadgeCell.setBackgroundColor(getStatusBg(stat));
            statBadgeCell.setPadding(3f);
            statBadgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statBadgeCell.setBorder(Rectangle.NO_BORDER);
            statBadgeTable.addCell(statBadgeCell);
            cellStatus.addElement(statBadgeTable);
            table.addCell(cellStatus);

            // Date
            String reportDate = report.getDate() != null ? report.getDate() : "";
            PdfPCell cellDate = new PdfPCell(new Phrase(reportDate, bodyFont));
            cellDate.setBackgroundColor(rowBg);
            cellDate.setPadding(6f);
            cellDate.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellDate.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellDate.setBorderColor(borderColor);
            table.addCell(cellDate);

            rowIndex++;
        }

        document.add(table);
        document.close();
    }

    private void addStatsCard(PdfPTable table, String label, String value, BaseColor accentColor) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(100, 116, 139));
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, accentColor);
        
        PdfPCell cardCell = new PdfPCell();
        cardCell.setBackgroundColor(new BaseColor(248, 250, 252));
        cardCell.setPadding(8f);
        cardCell.setBorderColor(new BaseColor(226, 232, 240));
        cardCell.setBorderWidth(1f);
        
        // Left accent line
        cardCell.setBorderWidthLeft(3f);
        cardCell.setBorderColorLeft(accentColor);
        
        cardCell.addElement(new Paragraph(label, labelFont));
        cardCell.addElement(new Paragraph(value, valFont));
        
        table.addCell(cardCell);
    }

    private BaseColor getBadgeBg(String severity) {
        if ("Critical".equalsIgnoreCase(severity)) return new BaseColor(254, 226, 226); // Light Red
        if ("High".equalsIgnoreCase(severity)) return new BaseColor(255, 237, 213); // Light Orange
        if ("Medium".equalsIgnoreCase(severity)) return new BaseColor(219, 234, 254); // Light Blue
        return new BaseColor(241, 245, 249); // Light Gray
    }

    private Font getBadgeFont(String severity) {
        if ("Critical".equalsIgnoreCase(severity)) return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(153, 27, 27));
        if ("High".equalsIgnoreCase(severity)) return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(154, 52, 18));
        if ("Medium".equalsIgnoreCase(severity)) return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(30, 64, 175));
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(71, 85, 105)); // Low
    }

    private BaseColor getStatusBg(String status) {
        if ("Resolved".equalsIgnoreCase(status)) return new BaseColor(209, 250, 229); // Light Green
        if ("Responding".equalsIgnoreCase(status)) return new BaseColor(243, 232, 255); // Light Purple
        return new BaseColor(254, 243, 199); // Light Yellow
    }

    private Font getStatusFont(String status) {
        if ("Resolved".equalsIgnoreCase(status)) return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(6, 95, 70));
        if ("Responding".equalsIgnoreCase(status)) return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(91, 33, 182));
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new BaseColor(146, 64, 14)); // Pending
    }

    // Static event helper to generate header and footer numbers dynamically
    static class HeaderFooterPageEvent extends com.itextpdf.text.pdf.PdfPageEventHelper {
        private com.itextpdf.text.pdf.PdfTemplate t;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
            
            // Header Title
            cb.beginText();
            try {
                cb.setFontAndSize(com.itextpdf.text.pdf.BaseFont.createFont(com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD, com.itextpdf.text.pdf.BaseFont.CP1252, com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED), 7);
            } catch (Exception e) {
                // Font load fallback
            }
            cb.setColorFill(new com.itextpdf.text.BaseColor(107, 114, 128));
            cb.showTextAligned(Element.ALIGN_LEFT, "SMART CITY SAFETY NETWORK | OFFICIAL INCIDENT REGISTER", document.left(), document.top() + 10, 0);
            cb.endText();

            // Header line
            cb.setLineWidth(0.5f);
            cb.setColorStroke(new com.itextpdf.text.BaseColor(229, 231, 235));
            cb.moveTo(document.left(), document.top() + 5);
            cb.lineTo(document.right(), document.top() + 5);
            cb.stroke();

            // Footer line
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();

            // Footer texts
            cb.beginText();
            try {
                cb.setFontAndSize(com.itextpdf.text.pdf.BaseFont.createFont(com.itextpdf.text.pdf.BaseFont.HELVETICA, com.itextpdf.text.pdf.BaseFont.CP1252, com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED), 7);
            } catch (Exception e) {
                // Font load fallback
            }
            cb.showTextAligned(Element.ALIGN_LEFT, "CONFIDENTIAL - Department of Smart Emergency Management", document.left(), document.bottom() - 22, 0);
            
            String pageText = String.format("Page %d of ", writer.getPageNumber());
            cb.showTextAligned(Element.ALIGN_RIGHT, pageText, document.right() - 15, document.bottom() - 22, 0);
            cb.endText();
            
            cb.addTemplate(t, document.right() - 15, document.bottom() - 22);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            t.beginText();
            try {
                t.setFontAndSize(com.itextpdf.text.pdf.BaseFont.createFont(com.itextpdf.text.pdf.BaseFont.HELVETICA, com.itextpdf.text.pdf.BaseFont.CP1252, com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED), 7);
            } catch (Exception e) {
                // Font load fallback
            }
            t.setColorFill(new com.itextpdf.text.BaseColor(107, 114, 128));
            t.showText(String.valueOf(writer.getPageNumber()));
            t.endText();
        }
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