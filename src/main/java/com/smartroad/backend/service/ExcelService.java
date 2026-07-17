package com.smartroad.backend.service;

import java.io.OutputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import com.smartroad.backend.model.AccidentReport;

@Service
public class ExcelService {

    /**
     * Stream-export reports to the provided OutputStream using a low-memory SXSSFWorkbook.
     */
    public void exportToStream(Iterator<AccidentReport> reportsIterator, OutputStream out)
            throws Exception {

        SXSSFWorkbook workbook = new SXSSFWorkbook(100); // keep 100 rows in memory

        Sheet sheet = workbook.createSheet("Accident Reports");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Reporter");
        header.createCell(2).setCellValue("Location");
        header.createCell(3).setCellValue("Severity");
        header.createCell(4).setCellValue("Status");

        int rowNum = 1;

        while (reportsIterator.hasNext()) {
            AccidentReport report = reportsIterator.next();

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(report.getId());
            row.createCell(1).setCellValue(report.getFullName());
            row.createCell(2).setCellValue(report.getLocation());
            row.createCell(3).setCellValue(report.getSeverity());
            row.createCell(4).setCellValue(report.getStatus());
        }

        workbook.write(out);
        out.flush();

        // dispose of temporary files backing this workbook on disk
        workbook.dispose();
        workbook.close();
    }

}