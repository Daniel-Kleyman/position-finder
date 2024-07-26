package com.example.positionfinder.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;

public class PositionFinderService {

    // Access environment variables
    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
    private static final String USERNAME = System.getenv("L_USERNAME");
    private static final String PASSWORD = System.getenv("L_PASSWORD");
    private static final List<String> KEYWORDS = List.of("Java");
    private static final String COOKIES_FILE_PATH = "cookies.json";

    public void getResults() {
        List<String[]> jobDetails = new ArrayList<>();
        WebDriver driver = initializeWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            if (!isFeedPageOpen(driver, wait)) {
                loginToLinkedIn(driver, wait);
            } else {
                driver.get("https://www.linkedin.com/feed/");
            }

            searchForJobs(driver, wait);
            clickSeeAllJobResults(driver, wait);
            filterByDatePosted(driver, wait);

            // Extract job details
            do {
                extractJobDetails(driver, wait, jobDetails);
                // Click "Next" button if available
            } while (clickNextButton(driver, wait));

            // Write job details to Excel
            writeToExcel(jobDetails);

        } finally {
            //  driver.quit();
        }
    }

    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();

        // Set the path to your Chrome user data directory
        String userDataDir = "C:\\Users\\Daniel\\AppData\\Local\\Google\\Chrome\\User Data";
        options.addArguments("user-data-dir=" + userDataDir);

        // Optionally specify the profile to use, e.g., 'Default'
        options.addArguments("profile-directory=Default");

        // Initialize WebDriver with ChromeOptions
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
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search']")));
        searchBox.sendKeys("Java" + Keys.RETURN);
    }

    private void clickSeeAllJobResults(WebDriver driver, WebDriverWait wait) {
        WebElement seeAllJobsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'See all job results in Israel')]")));
        seeAllJobsButton.click();
    }

    private void filterByDatePosted(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement datePostedButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='searchFilter_timePostedRange']")));
            datePostedButton.click();

            WebElement past24HoursOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Past 24 hours']")));
            past24HoursOption.click();

            WebElement showButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(@class, 'artdeco-button__text') and contains(text(), 'Show') and contains(text(), 'results')]")));
            showButton.click();
        } catch (TimeoutException e) {
            System.err.println("TimeoutException: Element could not be found or interacted with.");
            e.printStackTrace();
        }
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

    private boolean clickNextButton(WebDriver driver, WebDriverWait wait) {
        List<WebElement> nextButtons = driver.findElements(By.xpath("//button[contains(@aria-label, 'Next')]"));
        if (nextButtons.size() > 0) {
            WebElement nextButton = nextButtons.get(0);
            if (nextButton.isDisplayed()) {
                nextButton.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-job-id]")));
                return true;
            }
        }
        return false;
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
