package com.example.positionfinder.service;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;


import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WriteToExcel {
    public static void writeToExcel(Map<String, String> jobDetails) {
        String filePath = "C:\\Users\\Daniel\\Desktop\\CV\\Positions.xlsx";
        Workbook workbook;
        Sheet sheet;
        saveUnfilteredMapToExcel(jobDetails);
        Map<String, String> filteredTestMap = filterMap(jobDetails);
        // Check if the file exists
        if (Files.exists(Paths.get(filePath))) {
            // File exists, so read it
            try (FileInputStream fis = new FileInputStream(filePath)) {
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0); // Assuming data is on the first sheet
            } catch (IOException e) {
                e.printStackTrace();
                return; // Exit if there's an error reading the existing file
            }
        } else {
            // File does not exist, so create a new one
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Positions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue(""); // Blank column
            headerRow.createCell(1).setCellValue("Date");
            headerRow.createCell(2).setCellValue("Source");
            headerRow.createCell(3).setCellValue("Position");
            headerRow.createCell(4).setCellValue("Company");
            headerRow.createCell(5).setCellValue("Location");
            headerRow.createCell(6).setCellValue("Link");

            // Set column widths
            sheet.setColumnWidth(0, 1000);
            sheet.setColumnWidth(3, 10000); // Index 3 is the 4th column (Position)
            sheet.setColumnWidth(6, 10000);
        }

        // Determine the row index to start writing new data
        int rowIndex = sheet.getLastRowNum() + 2;

        for (Map.Entry<String, String> entry : filteredTestMap.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(""); // Blank column
            row.createCell(1).setCellValue(getCurrentDate());
            row.createCell(2).setCellValue("Linkedin");
            row.createCell(3).setCellValue(entry.getKey()); // Position
            row.createCell(4).setCellValue(""); // Company
            row.createCell(5).setCellValue(""); // Location
            //          row.createCell(6).setCellValue(entry.getValue()); // Link
            // Create hyperlink for the URL from entry.getValue()
            Cell linkCell = row.createCell(6);
            String url = entry.getValue();
            Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(url);
            linkCell.setHyperlink(hyperlink);
            linkCell.setCellValue(url);
        }

        // Write changes to the file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
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

    private static void saveUnfilteredMapToExcel(Map<String, String> jobDetails) {
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
        sheet.setColumnWidth(0, 1000);
        sheet.setColumnWidth(3, 10000); // Index 3 is the 4th column (Position)
        sheet.setColumnWidth(6, 10000);

        int rowIndex = 1;
        for (Map.Entry<String, String> entry : jobDetails.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(""); // Blank column
            row.createCell(1).setCellValue(getCurrentDate());
            row.createCell(2).setCellValue("Linkedin");
            row.createCell(3).setCellValue(entry.getValue()); // Position
            row.createCell(4).setCellValue(""); // Company
            row.createCell(5).setCellValue(""); // Location
            row.createCell(6).setCellValue(entry.getKey()); // Link
        }

        try (FileOutputStream fileOut = new FileOutputStream("C:\\Users\\Daniel\\Desktop\\CV\\PositionsUnfiltered.xlsx")) {
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
        Set<String> excludeKeywords = Set.of("senior", "lead", "leader", "devops", "manager", "engineering");
        Set<String> includeKeywords = Set.of("developer", "engineer");
        return jobDetails.entrySet().stream()
                // Convert the key to lower case for case-insensitive comparison
                .filter(entry -> {
                    String keyLowerCase = entry.getKey().toLowerCase();
                    // Exclude entries if the key contains any of the excludeKeywords
                    boolean shouldExclude = excludeKeywords.stream()
                            .anyMatch(keyword -> keyLowerCase.contains(keyword));
                    // Include only entries that contain at least one of the includeKeywords
                    boolean shouldInclude = includeKeywords.stream()
                            .anyMatch(keyword -> keyLowerCase.contains(keyword));
                    // Include the entry if it should be included and not excluded
                    return !shouldExclude && shouldInclude;
                })
                // Collect the results into a new map
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

