package com.example.positionfinder.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.*;

public class PositionFinderService {

    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
    private static final String USERNAME = System.getenv("L_USERNAME");
    private static final String PASSWORD = System.getenv("L_PASSWORD");
    private static final List<String> KEYWORDS = List.of("Java");
    private String firstUrl = "https://www.linkedin.com/jobs/search/?f_TPR=r86400&keywords=java&origin=JOB_SEARCH_PAGE_JOB_FILTER";

    public void getResults() {
        List<String[]> jobDetails = new ArrayList<>();
        WebDriver driver = initializeWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            if (!isFeedPageOpen(driver, wait)) {
                loginToLinkedIn(driver, wait);
            }

            int start = 0;
            boolean morePages = true;

            while (morePages) {
                // Fetch the page with the current start parameter
                filterByDatePosted(driver, start);

                // Extract job details from the current page
                extractJobDetails(driver, wait, jobDetails);

                // Check if the "No matching jobs found" message is present
                morePages = !isNoJobsFound(driver);

                // Increment start parameter for the next page
                start += 25;
            }

            // Write job details to Excel
            writeToExcel(jobDetails);

        } finally {
      //      driver.quit();
        }
    }

    private void filterByDatePosted(WebDriver driver, int start) {
        String url = firstUrl;
        if(start > 0){
            url = String.format("https://www.linkedin.com/jobs/search/?f_TPR=r86400&keywords=java&origin=JOB_SEARCH_PAGE_JOB_FILTER&start=%d", start);
        }
        driver.get(url);
    }

    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        String userDataDir = "C:\\Users\\Daniel\\AppData\\Local\\Google\\Chrome\\User Data";
        options.addArguments("user-data-dir=" + userDataDir);
        options.addArguments("profile-directory=Default");

        return new ChromeDriver(options);
    }

    private boolean isFeedPageOpen(WebDriver driver, WebDriverWait wait) {
        try {
            driver.get("https://www.linkedin.com/feed/");
            WebElement searchInputField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@class='search-global-typeahead__input' and @placeholder='Search']")));
            return searchInputField.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void loginToLinkedIn(WebDriver driver, WebDriverWait wait) {
        driver.get(L_LOGIN_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='username']")));
        emailField.sendKeys(USERNAME);
        WebElement passwordField = driver.findElement(By.xpath("//input[@id='password']"));
        passwordField.sendKeys(PASSWORD);

        WebElement signInButton = driver.findElement(By.xpath("//button[@type='submit']"));
        signInButton.click();
    }

    private void extractJobDetails(WebDriver driver, WebDriverWait wait, List<String[]> jobDetails) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-job-id]")));

        List<WebElement> jobCards = driver.findElements(By.xpath("//div[@data-job-id]"));
        for (WebElement jobCard : jobCards) {
            WebElement titleElement = jobCard.findElement(By.xpath(".//a[contains(@class, 'job-card-list__title')]"));
            String title = titleElement.getText();
            String url = titleElement.getAttribute("href");

            for (String keyword : KEYWORDS) {
                if (title.toLowerCase().contains(keyword.toLowerCase())) {
                    jobDetails.add(new String[]{title, url});
                    break;
                }
            }
        }
    }

    private boolean isNoJobsFound(WebDriver driver) {
        List<WebElement> noJobsElements = driver.findElements(By.xpath("//h1[@class='t-24 t-black t-normal text-align-center' and contains(text(), 'No matching jobs found.')]"));
        return !noJobsElements.isEmpty();
    }

    private void writeToExcel(List<String[]> jobDetails) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Positions");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Job Title");
        headerRow.createCell(1).setCellValue("Job URL");

        int rowIndex = 1;
        for (String[] jobDetail : jobDetails) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(jobDetail[0]);
            row.createCell(1).setCellValue(jobDetail[1]);
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
