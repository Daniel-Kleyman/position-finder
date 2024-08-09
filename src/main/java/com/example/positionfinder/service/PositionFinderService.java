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

    public static final Logger LOGGER = Logger.getLogger(PositionFinderService.class.getName());
    public static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
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

    // @Scheduled(cron = "0 0 * * * *") // Runs at the start of every hour
    @Scheduled(fixedRate = 3600000)
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
        boolean reload = true;
        while (reload) {
            driver.get(firstUrl);
            try {
                WebElement signInButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".results-context-header__job-count")));
                if (signInButton != null) {
                    LOGGER.info("proceeding");
                    reload = false;
                }

            } catch (TimeoutException e) {
                LOGGER.info("reloading page");
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

