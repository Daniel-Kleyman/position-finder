package com.example.positionfinder.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestWriteToExcel {

    public static void writeToExcel(Map<String, String> jobDetails) {
        Map<String, String> filteredTestMap = filterMap(jobDetails);
        //           Map<String, String> filteredMap = filterMap(jobDetails);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Positions");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue(""); // Blank column
        headerRow.createCell(1).setCellValue("Date");
        headerRow.createCell(2).setCellValue("Source");
        headerRow.createCell(3).setCellValue("Position");
        headerRow.createCell(4).setCellValue("Company");
        headerRow.createCell(5).setCellValue("Location");
        headerRow.createCell(6).setCellValue("Link");
        // Set column widths
        int defaultWidth = 2560; // This is a rough approximation of the default column width in POI
        int widerWidth = 3 * defaultWidth; // Make the Position column 3 times wider
        sheet.setColumnWidth(3, widerWidth); // Index 3 is the 4th column (Position)

        int rowIndex = 1;
        for (Map.Entry<String, String> entry : jobDetails.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(""); // Blank column
            row.createCell(1).setCellValue(getCurrentDate());
            row.createCell(2).setCellValue("Linkedin");
            row.createCell(3).setCellValue(entry.getKey()); // Position
            row.createCell(4).setCellValue(""); // Company
            row.createCell(5).setCellValue(""); // Location
            row.createCell(6).setCellValue(entry.getValue()); // Link
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

    private static String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.now().format(formatter);
    }

    private static Map<String, String> filterMap(Map<String, String> jobDetails) {
        Set<String> excludeKeywords = Set.of("senior", "lead", "leader", "devops");
        Set<String> includeKeywords = Set.of("developer", "engineer");
        return jobDetails.entrySet().stream()
                // Convert the key to lower case for case-insensitive comparison
                .filter(entry -> excludeKeywords.stream()
                        .noneMatch(keyword -> entry.getKey().toLowerCase().contains(keyword)))
                // Further filter to include only keys that contain at least one of the includeKeywords
                .filter(entry -> includeKeywords.stream()
                        .anyMatch(keyword -> entry.getKey().toLowerCase().contains(keyword)))
                // Collect the results into a new map
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

