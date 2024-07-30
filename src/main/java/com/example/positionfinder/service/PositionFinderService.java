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
    private String firstUrl = "https://www.linkedin.com/jobs/search?keywords=Java&location=Israel&geoId=101620260&f_TPR=r86400&position=1&pageNum=0";
    boolean morePages = true;
    Map<String, String> jobDetails = new LinkedHashMap<>(); // Use LinkedHashMap to maintain insertion order
    WebDriver driver = initializeWebDriver();
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    public void getResults() {

        try {
//            if (!isFeedPageOpen(driver, wait)) {
//                loginToLinkedIn(driver, wait);
//            }
            openPage(driver);
            // Scroll down to load more job cards
            Scrolling.scrollToLoadMore(driver, wait);
            // Extract job details from the current page
        //    extractJobDetails(driver, wait, jobDetails);
            System.out.println("jobs found " + jobDetails.size());
            // Write job details to Excel
            WriteToExcel.writeToExcel(jobDetails);
            System.out.println("jobs parsed " + jobDetails.size());
        } finally {
            //     driver.quit();
        }
    }

    private void openPage(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement signInButton = null;

        while (true) {
            // Load the page
            driver.get(firstUrl);

            // Check if the "Sign in with email" button is present
            try {
                signInButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//a[contains(@class, 'sign-in-form__sign-in-cta') and contains(text(), 'Sign in with email')]")));

                // If the Sign-In button is found, reload the page and continue checking
                System.out.println("Sign-In button found. Reloading the page...");
            } catch (TimeoutException e) {
                // If the Sign-In button is not found, exit the loop
                System.out.println("Sign-In button not found. Proceeding...");
                break; // Exit the loop if the button is not found
            }

            // Optionally, wait before retrying
            try {
                Thread.sleep(2000); // Wait for 2 seconds before retrying
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        // Continue with further actions after the loop
        System.out.println("Page is ready.");
    }


    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito"); // Add incognito mode
//        String userDataDir = "C:\\Users\\Daniel\\AppData\\Local\\Google\\Chrome\\User Data";
//        options.addArguments("user-data-dir=" + userDataDir);
//        options.addArguments("profile-directory=Default");

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

//    private void loginToLinkedIn(WebDriver driver, WebDriverWait wait) {
//        driver.get(L_LOGIN_URL);
//
//        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='username']")));
//        emailField.sendKeys(USERNAME);
//        WebElement passwordField = driver.findElement(By.xpath("//input[@id='password']"));
//        passwordField.sendKeys(PASSWORD);
//
//        WebElement signInButton = driver.findElement(By.xpath("//button[@type='submit']"));
//        signInButton.click();
//    }

    private void extractJobDetails(WebDriver driver, WebDriverWait wait, Map<String, String> jobDetails) {
//        while (jobDetails.size() < 57) {
//            extractProcess(driver, wait, jobDetails);
//        }
//        extractProcess(driver, wait, jobDetails);
    }

    private void extractProcess(WebDriver driver, WebDriverWait wait, Map<String, String> jobDetails) {
        // Wait for a given period to allow the page to load
        System.out.println("Waiting for page to load...");
        try {
            Thread.sleep(10000); // Convert seconds to milliseconds
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            // Wait until the job container is visible /html/body/div[1]/div/main/section[2]
            WebElement jobContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[contains(@class, 'jobs-search__results-list')]")));
            System.out.println("container is found");
            //   Find all job cards on the page
            List<WebElement> jobCards = driver.findElements(By.xpath("//div[contains(@class, 'job-search-card')]"));
            if (jobCards.isEmpty()) {
                // Log if no job cards are found
                System.err.println("No job cards found.");
                return;
            }
            System.out.println("job card is found");
            // Loop through each job card
            for (WebElement jobCard : jobCards) {
                try {
                    // Extract job title
                    WebElement titleElement;
                    try {
                        titleElement = jobCard.findElement(By.xpath("//div[contains(@class, 'job-search-card')]//h3[contains(@class, 'base-search-card__title')]"));
                        System.out.println("job title is found");
                    } catch (NoSuchElementException e) {
                        System.err.println("Job title element not found in a job card.");
                        continue; // Skip this job card if the title is missing
                    }
                    String title = titleElement.getText();

                    // Extract job URL
                    WebElement urlElement;
                    try {
                        //urlElement = jobCard.findElement(By.xpath("//div[contains(@class, 'job-search-card')]//a[contains(@class, 'base-card__full-link')]/@href\n"));
                        urlElement = jobCard.findElement(By.cssSelector("a.base-card__full-link"));
                        System.out.println("url is found");
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
