package com.example.positionfinder.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileOutputStream;
import java.io.IOException;

public class PositionFinderService {
    public void getResults() {
        String pageText = getPageText();
        writeToExcel(pageText);
    }

    private String getPageText() {
        // Set the path to your WebDriver executable
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Daniel\\IdeaProjects\\chromedriver.exe");

        // Initialize WebDriver
        WebDriver driver = new ChromeDriver();
        String text;
        try {
            // Open the page
            driver.get("https://www.bbc.com");

            // Get the page source
            String pageSource = driver.getPageSource();

            // Use Jsoup to parse the HTML and extract text
            Document document = Jsoup.parse(pageSource);
            text = document.body().text();

            // Print the page title and all text
            System.out.println("Page Title: " + driver.getTitle());
            System.out.println("Page Text: " + text);

        } finally {
            // Close WebDriver
            driver.quit();
        }
        return text;
    }
    private void writeToExcel(String text) {
        // Create a Workbook and a Sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Positions");

        // Create a Row and a Cell
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        // Set the cell value to the extracted text
        cell.setCellValue(text);

        // Write the Workbook to a file
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
