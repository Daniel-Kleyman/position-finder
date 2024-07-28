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
import java.util.NoSuchElementException;

public class PositionFinderService {

    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
    private static final String USERNAME = System.getenv("L_USERNAME");
    private static final String PASSWORD = System.getenv("L_PASSWORD");
    private static final List<String> KEYWORDS = List.of(" ");
    private String firstUrl = "https://www.linkedin.com/jobs/search/?f_TPR=r86400&keywords=&origin=JOB_SEARCH_PAGE_JOB_FILTER";
    boolean morePages = true;
    Map<String, String> jobDetails = new LinkedHashMap<>(); // Use LinkedHashMap to maintain insertion order

    public void getResults() {

        WebDriver driver = initializeWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
//            if (!isFeedPageOpen(driver, wait)) {
//                loginToLinkedIn(driver, wait);
//            }

            int start = 0;

            while (morePages) {
                // Fetch the page with the current start parameter
                filterByDatePosted(driver, start);
                // Scroll down to load more job cards
                scrollToLoadMore(driver, wait);
                // Extract job details from the current page
                extractJobDetails(driver, wait, jobDetails);
                System.out.println("jobs found " + jobDetails.size());
                // Check if the "No matching jobs found" message is present
                morePages = !isNoJobsFound(driver);

                // Increment start parameter for the next page
                start += 25;
            }
            System.out.println("jobs parsed " + jobDetails.size());
            // Write job details to Excel
            WriteToExcel.writeToExcel(jobDetails);

        } finally {
            driver.quit();
        }
    }

    private void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Locate the scrollable container using the provided XPath
        WebElement scrollableContainer = driver.findElement(By.xpath("//*[@id='main']/div/div[2]/div[1]/div"));

        // Define the step size for scrolling. Start with an initial value and adjust based on your needs.
        int stepSize = 800; // Example value in pixels
        long previousHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", scrollableContainer);
        boolean moreContentToLoad = true;

        while (moreContentToLoad) {
            // Scroll down incrementally in steps
            for (int i = 0; i < 1; i++) { // Number of intermediate stops
                js.executeScript("arguments[0].scrollTop += arguments[1];", scrollableContainer, stepSize);

                // Wait for new content to load
                try {
                    Thread.sleep(2000); // Wait for 2 seconds between scrolls
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Check if the page height has increased (indicating new content has loaded)
                long newHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", scrollableContainer);
                if (newHeight == previousHeight) {
                    // If the height hasn't changed, there may be no more new content
                    moreContentToLoad = false;
                    break;
                }
                previousHeight = newHeight;
            }
        }
    }

    private void filterByDatePosted(WebDriver driver, int start) {
        String url = firstUrl;
        if (start > 0) {
            url = String.format("https://www.linkedin.com/jobs/search/?f_TPR=r86400&keywords=&origin=JOB_SEARCH_PAGE_JOB_FILTER&start=%d", start);
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

//    private boolean isFeedPageOpen(WebDriver driver, WebDriverWait wait) {
//        try {
//            driver.get("https://www.linkedin.com/feed/");
//            WebElement searchInputField = wait.until(ExpectedConditions.visibilityOfElementLocated(
//                    By.xpath("//input[@class='search-global-typeahead__input' and @placeholder='Search']")));
//            return searchInputField.isDisplayed();
//        } catch (TimeoutException e) {
//            return false;
//        }
//    }

    private void loginToLinkedIn(WebDriver driver, WebDriverWait wait) {
        driver.get(L_LOGIN_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='username']")));
        emailField.sendKeys(USERNAME);
        WebElement passwordField = driver.findElement(By.xpath("//input[@id='password']"));
        passwordField.sendKeys(PASSWORD);

        WebElement signInButton = driver.findElement(By.xpath("//button[@type='submit']"));
        signInButton.click();
    }

    private void extractJobDetails(WebDriver driver, WebDriverWait wait, Map<String, String> jobDetails) {
        try {
            // Wait until the job container is visible
            WebElement jobContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'jobs-search-results')]")));

            // Find all job cards on the page
            List<WebElement> jobCards = driver.findElements(By.xpath("//div[@data-job-id]"));
            if (jobCards.isEmpty()) {
                // Log if no job cards are found
                System.err.println("No job cards found.");
                return;
            }

            // Loop through each job card
            for (WebElement jobCard : jobCards) {
                try {
                    // Extract job title
                    WebElement titleElement;
                    try {
                        titleElement = jobCard.findElement(By.xpath(".//a[contains(@class, 'job-card-list__title')]"));
                    } catch (NoSuchElementException e) {
                        System.err.println("Job title element not found in a job card.");
                        continue; // Skip this job card if the title is missing
                    }
                    String title = titleElement.getText();

                    // Extract job URL
                    WebElement urlElement;
                    try {
                        urlElement = jobCard.findElement(By.xpath(".//a[contains(@class, 'job-card-list__title')]"));
                    } catch (NoSuchElementException e) {
                        System.err.println("Job URL element not found in a job card.");
                        continue; // Skip this job card if the URL is missing
                    }
                    String url = urlElement.getAttribute("href");

                    // Only add job details if the URL is not already in the map
                    jobDetails.putIfAbsent(url, title);
                } catch (NoSuchElementException e) {
                    // Log error if elements are not found within a job card
                    System.err.println("Error extracting details from job card: " + e.getMessage());
                }
            }
        } catch (TimeoutException e) {
            // Log error if the job container element is not found within the timeout period
            System.err.println("Timeout while waiting for job container: " + e.getMessage());
        } catch (Exception e) {
            // Log any other unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private boolean isNoJobsFound(WebDriver driver) {
        List<WebElement> jobCards = driver.findElements(By.xpath("//div[@data-job-id]"));
        return jobCards.isEmpty();
    }
}