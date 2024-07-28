package com.example.positionfinder.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class WriteToExcel {
    public void writeToExcel(Map<String, String> jobDetails) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Positions");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Job Title");
        headerRow.createCell(1).setCellValue(""); // Blank column
        headerRow.createCell(2).setCellValue("Job URL");

        int rowIndex = 1;
        for (Map.Entry<String, String> entry : jobDetails.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getValue()); // Job Title
            row.createCell(1).setCellValue(""); // Blank cell
            row.createCell(2).setCellValue(entry.getKey()); // Job URL
        }

        try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\Daniel\\Desktop\\CV\\Positions.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
