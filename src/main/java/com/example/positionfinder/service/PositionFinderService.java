package com.example.positionfinder.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PositionFinderService {

    // Access environment variables
    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
    private static final String USERNAME = System.getenv("L_USERNAME");
    private static final String PASSWORD = System.getenv("L_PASSWORD");
    private static final List<String> KEYWORDS = List.of("Java");

    public void getResults() {
        List<String[]> jobDetails = new ArrayList<>();
        WebDriver driver = initializeWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            loginToLinkedIn(driver, wait);
            searchForJobs(driver, wait);
            clickSeeAllJobResults(driver, wait);
            applyFilters(driver, wait);

            // Extract job details
            do {
                extractJobDetails(driver, wait, jobDetails);
                // Click "Next" button if available
            } while (clickNextButton(driver, wait));

            // Write job details to Excel
            writeToExcel(jobDetails);

        } finally {
            driver.quit();
        }
    }

    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        // Configure Chrome options to run in incognito mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");

        // Initialize WebDriver with ChromeOptions
        return new ChromeDriver(options);
    }

    private void loginToLinkedIn(WebDriver driver, WebDriverWait wait) {
        driver.get(L_LOGIN_URL);

        // Enter email and password
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='username']")));
        emailField.sendKeys(USERNAME);
        WebElement passwordField = driver.findElement(By.xpath("//input[@id='password']"));
        passwordField.sendKeys(PASSWORD);

        // Click "Sign in" button
        WebElement signInButton = driver.findElement(By.xpath("//button[@type='submit']"));
        signInButton.click();
    }

    private void searchForJobs(WebDriver driver, WebDriverWait wait) {
        // Wait for the search field and enter the keyword
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search']")));
        searchBox.sendKeys("Java" + Keys.RETURN);
    }

    private void clickSeeAllJobResults(WebDriver driver, WebDriverWait wait) {
        // Wait for "See all job results in Israel" button and click it
        WebElement seeAllJobsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'See all job results in Israel')]")));
        seeAllJobsButton.click();
    }

    private void applyFilters(WebDriver driver, WebDriverWait wait) {
        // Wait for the "All filters" button and click it
        WebElement allFiltersButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='All filters']")));
        allFiltersButton.click();

        // Select "Past 24 hours" filter
        WebElement past24HoursCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='f_TPR'][@value='r86400']")));
        past24HoursCheckbox.click();

        // Click "Show results" button
        WebElement showResultsButton = driver.findElement(By.xpath("//button[contains(text(), 'Show results')]"));
        showResultsButton.click();
    }

    private void extractJobDetails(WebDriver driver, WebDriverWait wait, List<String[]> jobDetails) {
        // Wait for job cards to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'job-card-container')]")));

        List<WebElement> jobCards = driver.findElements(By.xpath("//div[contains(@class, 'job-card-container')]"));
        for (WebElement jobCard : jobCards) {
            String title = jobCard.findElement(By.xpath(".//h3[contains(@class, 'job-card-list__title')]")).getText();
            String url = jobCard.findElement(By.xpath(".//a[contains(@class, 'job-card-list__title')]")).getAttribute("href");

            // Filter based on keywords
            for (String keyword : KEYWORDS) {
                if (title.toLowerCase().contains(keyword.toLowerCase())) {
                    jobDetails.add(new String[]{title, url});
                    break;
                }
            }
        }
    }

    private boolean clickNextButton(WebDriver driver, WebDriverWait wait) {
        List<WebElement> nextButtons = driver.findElements(By.xpath("//button[contains(@aria-label, 'Next')]"));
        if (nextButtons.size() > 0) {
            WebElement nextButton = nextButtons.get(0);
            if (nextButton.isDisplayed()) {
                nextButton.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'job-card-container')]")));
                return true;
            }
        }
        return false;
    }

    private void writeToExcel(List<String[]> jobDetails) {
        // Create a Workbook and a Sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Positions");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Job Title");
        headerRow.createCell(1).setCellValue("Job URL");

        // Write job details
        int rowIndex = 1;
        for (String[] jobDetail : jobDetails) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(jobDetail[0]);
            row.createCell(1).setCellValue(jobDetail[1]);
        }

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
