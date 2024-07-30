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
    WebDriver driver;
    WebDriverWait wait;

    public PositionFinderService() {
        this.driver = initializeWebDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }


    public void getResults() {

        try {
//            if (!isFeedPageOpen(driver, wait)) {
//                loginToLinkedIn(driver, wait);
//            }
            openPage(driver);
            // Scroll down to load more job cards
            Scrolling.scrollToLoadMore(driver, wait);
            // Extract job details from the current page
            //           ExtractJobDetails.extractJobDetails(driver, wait, jobDetails);
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
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize(); // Maximize the browser window

        return driver;
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
