package com.example.positionfinder.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

@Service
public class PositionFinderService {

    private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private static final String L_LOGIN_URL = System.getenv("L_LOGIN_URL");
    private static final String USERNAME = System.getenv("L_USERNAME");
    private static final String PASSWORD = System.getenv("L_PASSWORD");
    private static final List<String> KEYWORDS = List.of(" ");
    private String firstUrl = "https://www.linkedin.com/jobs/search?keywords=&location=Israel&geoId=101620260&f_TPR=r3600&position=1&pageNum=0";
    //3.5 hours 12600
    boolean morePages = true;
    Map<String, List<String>> jobDetails = new LinkedHashMap<>(); // Use LinkedHashMap to maintain insertion order
    WebDriver driver;
    WebDriverWait wait;
    static int jobCount;

    public PositionFinderService() {
        this.driver = initializeWebDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.jobCount = 0;
    }


    public void getResults() {
        //    Map<String, List<String>> testJobDetails = loadMapFromJson();
        //   WriteToExcel.writeToExcel(testJobDetails);
        Scrolling scroller = new Scrolling(driver, wait);
        int startTime = (int) System.currentTimeMillis();
        try {
            openPage(driver);
            printJobCount(driver);
            scroller.start();
            Thread.sleep((long) (jobCount * 0.6 * 1000));
            scroller.stop(); // Signal the scrolling thread to stop
            System.out.println("scrolling stoped");
            // Extract job details from the current page
            ExtractJobDetails.extractJobDetails(driver, wait, jobDetails);
            //        saveMapToJson(jobDetails);
            WriteToExcel.writeToExcel(jobDetails);
            int endTime = (int) System.currentTimeMillis();
            int totalTime = (endTime - startTime) / 1000;
            System.out.println("extract completed in " + totalTime);
            // Write job details to Excel
            //        WriteToExcel.writeToExcel(newJobDetails);
            System.out.println("jobs parsed " + jobDetails.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
    @Scheduled(fixedRate = 3600000) // 3600000 milliseconds = 1 hour
    public void scheduledGetResults() {
        getResults();
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
        //       System.out.println("Page is ready.");

    }


    private WebDriver initializeWebDriver() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito"); // Add incognito mode
 //       options.addArguments("--headless");
//        String userDataDir = "C:\\Users\\Daniel\\AppData\\Local\\Google\\Chrome\\User Data";
//        options.addArguments("user-data-dir=" + userDataDir);
//        options.addArguments("profile-directory=Default");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize(); // Maximize the browser window

        return driver;
    }

    public static void printJobCount(WebDriver driver) {
        // Locate the element containing the job count
        WebElement jobCountElement = driver.findElement(By.cssSelector(".results-context-header__job-count"));

        // Extract the text from the element
        String jobCountText = jobCountElement.getText();
        jobCountText = jobCountText.replace("+", "");
        jobCountText = jobCountText.replace(",", "");
        // Convert the text to an integer

        try {
            jobCount = Integer.parseInt(jobCountText);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse job count: " + e.getMessage());
            return;
        }

        // Print the job count
        System.out.println("Job count: " + jobCount);

    }

    // Save the Map to a JSON file
    public static void saveMapToJson(Map<String, List<String>> jobDetails) {
        String filePath = "C:\\Users\\Daniel\\IdeaProjects\\position-finder\\testJson.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(filePath), jobDetails);
            System.out.println("Map saved to JSON file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving map to JSON file: " + e.getMessage());
        }
    }

    // Load the Map from a JSON file
    public static Map<String, List<String>> loadMapFromJson() {
        String filePath = "C:\\Users\\Daniel\\IdeaProjects\\position-finder\\testJson.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, List<String>> jobDetailsTest = mapper.readValue(new File(filePath),
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, List.class));
            System.out.println("Map loaded from JSON file: " + filePath);
            return jobDetailsTest;
        } catch (IOException e) {
            System.err.println("Error loading map from JSON file: " + e.getMessage());
            return null;
        }

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

    }

}
