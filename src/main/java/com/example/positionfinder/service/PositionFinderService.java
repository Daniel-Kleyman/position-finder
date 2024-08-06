package com.example.positionfinder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PositionFinderService {

    private static final Logger LOGGER = Logger.getLogger(PositionFinderService.class.getName());
    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
    private static final String USERNAME = System.getenv("L_USERNAME");
    private static final String PASSWORD = System.getenv("L_PASSWORD");
    private static final List<String> KEYWORDS = List.of(" ");
    private String firstUrl = "https://www.linkedin.com/jobs/search?keywords=&location=Israel&geoId=101620260&f_TPR=r3600&position=1&pageNum=0";
    private boolean morePages = true;
    private Map<String, List<String>> jobDetails = new LinkedHashMap<>();
    private WebDriver driver;
    private WebDriverWait wait;
    private static int jobCount;

    public PositionFinderService() {
        this.driver = initializeWebDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.jobCount = 0;
    }

    @Scheduled(cron = "0 0/3 * * * *") // Runs every 5 minutes
    public void scheduledGetResults() {
        try {
            getResults();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during scheduled task", e);
        }
    }

    public void getResults() {
        Scrolling scroller = new Scrolling(driver, wait);
        long startTime = System.currentTimeMillis();

        try {
            openPage();
            printJobCount();
            scroller.start();
            Thread.sleep((long) (jobCount * 0.6 * 1000));
            scroller.stop();
            LOGGER.info("Scrolling stopped");

            ExtractJobDetails.extractJobDetails(driver, wait, jobDetails);
            WriteToExcel.writeToExcel(jobDetails);

            long endTime = System.currentTimeMillis();
            long totalTime = (endTime - startTime) / 1000;
            LOGGER.info("Extraction completed in " + totalTime + " seconds");
            LOGGER.info("Jobs parsed: " + jobDetails.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
//        finally {
//            if (driver != null) {
//                driver.quit();
//            }
//        }
    }

    private void openPage() {
        while (true) {
            driver.get(firstUrl);
            try {
                WebElement signInButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//a[contains(@class, 'sign-in-form__sign-in-cta') and contains(text(), 'Sign in with email')]")));

                LOGGER.info("Sign-In button found. Reloading the page...");
            } catch (TimeoutException e) {
                LOGGER.info("Sign-In button not found. Proceeding...");
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.severe("Interrupted while waiting: " + ex.getMessage());
            }
        }
    }

    private WebDriver initializeWebDriver() {
        if (CHROME_DRIVER_PATH == null) {
            throw new IllegalStateException("CHROME_DRIVER_PATH environment variable not set");
        }

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        // options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        return driver;
    }

    private void printJobCount() {
        WebElement jobCountElement = driver.findElement(By.cssSelector(".results-context-header__job-count"));
        String jobCountText = jobCountElement.getText().replace("+", "").replace(",", "");

        try {
            jobCount = Integer.parseInt(jobCountText);
        } catch (NumberFormatException e) {
            LOGGER.severe("Failed to parse job count: " + e.getMessage());
            return;
        }

        LOGGER.info("Job count: " + jobCount);
    }

    public static void saveMapToJson(Map<String, List<String>> jobDetails) {
        String filePath = "C:\\Users\\Daniel\\IdeaProjects\\position-finder\\testJson.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(filePath), jobDetails);
            LOGGER.info("Map saved to JSON file: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("Error saving map to JSON file: " + e.getMessage());
        }
    }

    public static Map<String, List<String>> loadMapFromJson() {
        String filePath = "C:\\Users\\Daniel\\IdeaProjects\\position-finder\\testJson.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, List<String>> jobDetailsTest = mapper.readValue(new File(filePath),
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, List.class));
            LOGGER.info("Map loaded from JSON file: " + filePath);
            return jobDetailsTest;
        } catch (IOException e) {
            LOGGER.severe("Error loading map from JSON file: " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }
}

//package com.example.positionfinder.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import org.openqa.selenium.*;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.io.*;
//import java.time.Duration;
//import java.util.*;
//import java.util.NoSuchElementException;
//
//@Service
//public class PositionFinderService {
//
//    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
//    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
//    private static final String USERNAME = System.getenv("L_USERNAME");
//    private static final String PASSWORD = System.getenv("L_PASSWORD");
//    private static final List<String> KEYWORDS = List.of(" ");
//    private String firstUrl = "https://www.linkedin.com/jobs/search?keywords=&location=Israel&geoId=101620260&f_TPR=r60&position=1&pageNum=0";
//    //3.5 hours 12600
//    boolean morePages = true;
//    Map<String, List<String>> jobDetails = new LinkedHashMap<>(); // Use LinkedHashMap to maintain insertion order
//    WebDriver driver;
//    WebDriverWait wait;
//    static int jobCount;
//
//    public PositionFinderService() {
//        this.driver = initializeWebDriver();
//        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        this.jobCount = 0;
//    }
//
//
//    public void getResults() {
//        //    Map<String, List<String>> testJobDetails = loadMapFromJson();
//        //   WriteToExcel.writeToExcel(testJobDetails);
//        Scrolling scroller = new Scrolling(driver, wait);
//        int startTime = (int) System.currentTimeMillis();
//        try {
//            openPage(driver);
//            printJobCount(driver);
//            scroller.start();
//            Thread.sleep((long) (jobCount * 0.6 * 1000));
//            scroller.stop(); // Signal the scrolling thread to stop
//            System.out.println("scrolling stoped");
//            // Extract job details from the current page
//            ExtractJobDetails.extractJobDetails(driver, wait, jobDetails);
//            //        saveMapToJson(jobDetails);
//            WriteToExcel.writeToExcel(jobDetails);
//            int endTime = (int) System.currentTimeMillis();
//            int totalTime = (endTime - startTime) / 1000;
//            System.out.println("extract completed in " + totalTime);
//            // Write job details to Excel
//            //        WriteToExcel.writeToExcel(newJobDetails);
//            System.out.println("jobs parsed " + jobDetails.size());
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            driver.quit();
//        }
//    }
//
//    //    @Scheduled(cron = "0 0 7-23 * * *") // Runs every hour from 07:00 to 23:00
////    public void scheduledGetResults() {
////        getResults();
////    }
//    @Scheduled(cron = "0 0/5 * * * *") // Runs every minute
//    public void scheduledGetResults() {
//        getResults();
//    }
//
//    private void openPage(WebDriver driver) {
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        WebElement signInButton = null;
//
//        while (true) {
//            // Load the page
//            driver.get(firstUrl);
//
//            // Check if the "Sign in with email" button is present
//            try {
//                signInButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
//                        By.xpath("//a[contains(@class, 'sign-in-form__sign-in-cta') and contains(text(), 'Sign in with email')]")));
//
//                // If the Sign-In button is found, reload the page and continue checking
//                System.out.println("Sign-In button found. Reloading the page...");
//            } catch (TimeoutException e) {
//                // If the Sign-In button is not found, exit the loop
//                System.out.println("Sign-In button not found. Proceeding...");
//                break; // Exit the loop if the button is not found
//            }
//
//            // Optionally, wait before retrying
//            try {
//                Thread.sleep(2000); // Wait for 2 seconds before retrying
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//        }
//        // Continue with further actions after the loop
//        //       System.out.println("Page is ready.");
//
//    }
//
//
//    private WebDriver initializeWebDriver() {
//        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
//
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--incognito"); // Add incognito mode
//        //       options.addArguments("--headless");
////        String userDataDir = "C:\\Users\\Daniel\\AppData\\Local\\Google\\Chrome\\User Data";
////        options.addArguments("user-data-dir=" + userDataDir);
////        options.addArguments("profile-directory=Default");
//        WebDriver driver = new ChromeDriver(options);
//        driver.manage().window().maximize(); // Maximize the browser window
//
//        return driver;
//    }
//
//    public static void printJobCount(WebDriver driver) {
//        // Locate the element containing the job count
//        WebElement jobCountElement = driver.findElement(By.cssSelector(".results-context-header__job-count"));
//
//        // Extract the text from the element
//        String jobCountText = jobCountElement.getText();
//        jobCountText = jobCountText.replace("+", "");
//        jobCountText = jobCountText.replace(",", "");
//        // Convert the text to an integer
//
//        try {
//            jobCount = Integer.parseInt(jobCountText);
//        } catch (NumberFormatException e) {
//            System.err.println("Failed to parse job count: " + e.getMessage());
//            return;
//        }
//
//        // Print the job count
//        System.out.println("Job count: " + jobCount);
//
//    }
//
//    // Save the Map to a JSON file
//    public static void saveMapToJson(Map<String, List<String>> jobDetails) {
//        String filePath = "C:\\Users\\Daniel\\IdeaProjects\\position-finder\\testJson.json";
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            mapper.writeValue(new File(filePath), jobDetails);
//            System.out.println("Map saved to JSON file: " + filePath);
//        } catch (IOException e) {
//            System.err.println("Error saving map to JSON file: " + e.getMessage());
//        }
//    }
//
//    // Load the Map from a JSON file
//    public static Map<String, List<String>> loadMapFromJson() {
//        String filePath = "C:\\Users\\Daniel\\IdeaProjects\\position-finder\\testJson.json";
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            Map<String, List<String>> jobDetailsTest = mapper.readValue(new File(filePath),
//                    mapper.getTypeFactory().constructMapType(Map.class, String.class, List.class));
//            System.out.println("Map loaded from JSON file: " + filePath);
//            return jobDetailsTest;
//        } catch (IOException e) {
//            System.err.println("Error loading map from JSON file: " + e.getMessage());
//            return null;
//        }
//
////    private void loginToLinkedIn(WebDriver driver, WebDriverWait wait) {
////        driver.get(L_LOGIN_URL);
////
////        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='username']")));
////        emailField.sendKeys(USERNAME);
////        WebElement passwordField = driver.findElement(By.xpath("//input[@id='password']"));
////        passwordField.sendKeys(PASSWORD);
////
////        WebElement signInButton = driver.findElement(By.xpath("//button[@type='submit']"));
////        signInButton.click();
////    }
//
//    }
//
//}
