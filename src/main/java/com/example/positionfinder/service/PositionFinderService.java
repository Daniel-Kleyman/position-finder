package com.example.positionfinder.service;

import org.apache.commons.io.FileUtils;
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
    private String firstUrl = "https://www.linkedin.com/jobs/search/?f_TPR=r86400&keywords=java&origin=JOB_SEARCH_PAGE_JOB_FILTER";

    public void getResults() {
        List<String[]> jobDetails = new ArrayList<>();
        WebDriver driver = initializeWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            if (!isFeedPageOpen(driver, wait)) {
                loginToLinkedIn(driver, wait);
            }

            int start = 0;
            boolean morePages = true;

            while (morePages) {
                // Fetch the page with the current start parameter
                filterByDatePosted(driver, start);
// Scroll down to load more job cards
                scrollToLoadMore(driver, wait);
                // Extract job details from the current page
                extractJobDetails(driver, wait, jobDetails);

                // Check if the "No matching jobs found" message is present
                morePages = !isNoJobsFound(driver);

                // Increment start parameter for the next page
                start += 25;
            }
            System.out.println("jobs parsed " + jobDetails.size());
            // Write job details to Excel
            writeToExcel(jobDetails);

        } finally {
            driver.quit();
        }
    }

    private void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Locate the scrollable container using the provided XPath
        WebElement scrollableContainer = driver.findElement(By.xpath("//*[@id='main']/div/div[2]/div[1]/div"));

        // Get the current scroll height of the container
        long previousHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", scrollableContainer);

        while (true) {
            // Scroll down within the container
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollableContainer);

            // Wait for new content to load
            try {
                Thread.sleep(3000); // Wait for 3 seconds, adjust as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Calculate the new scroll height and compare with the previous height
            long newHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", scrollableContainer);
            if (newHeight == previousHeight) {
                // Break the loop if the height hasn't changed (no more content to load)
                break;
            }
            previousHeight = newHeight;

            // Optionally, you can add a WebDriverWait to ensure that new elements are loaded
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-job-id]")));
            } catch (TimeoutException e) {
                System.out.println("Timeout while waiting for new job elements to load.");
                break;
            }
        }
    }

    private void filterByDatePosted(WebDriver driver, int start) {
        String url = firstUrl;
        if (start > 0) {
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
        try {
            // Wait until the job container or an element within it is visible
            WebElement jobContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'jobs-search-results')]")));

            // Find all job cards on the page
            List<WebElement> jobCards = driver.findElements(By.xpath("//div[@data-job-id]"));
            if (jobCards.isEmpty()) {
                // Handle the case where no job cards are found
                System.out.println("No job cards found.");
                return;
            }

            // Loop through each job card
            for (WebElement jobCard : jobCards) {
                try {
                    // Extract job title
                    WebElement titleElement = jobCard.findElement(By.xpath(".//strong"));
                    String title = titleElement.getText();

                    // Extract job URL
                    WebElement urlElement = jobCard.findElement(By.xpath(".//a[contains(@class, 'job-card-list__title')]"));
                    String url = urlElement.getAttribute("href");
                    jobDetails.add(new String[]{title, url});
                    // Check if the job title contains any of the keywords
                    // for (String keyword : KEYWORDS) {
                    //     if (title.toLowerCase().contains(keyword.toLowerCase())) {
                    //         // Add job details to the list if the keyword is found
                    //         jobDetails.add(new String[]{title, url});
                    //         break; // Exit the loop after finding a match
                    //     }
                    // }
                } catch (NoSuchElementException e) {
                    // Handle cases where expected elements are not found
                    System.out.println("Element not found within job card.");
                }
            }
        } catch (TimeoutException e) {
            // Handle timeout if the element is not found within the timeout period
            System.out.println("Timeout while waiting for job details: " + e.getMessage());
        }
    }

    private boolean isNoJobsFound(WebDriver driver) {
        //   List<WebElement> noJobsElements = driver.findElements(By.xpath("//h1[@class='t-24 t-black t-normal text-align-center' and contains(text(), 'No matching jobs found.')]"));
        //  List<WebElement> noJobsElements = driver.findElements(By.xpath("//h1[contains(text(), 'No matching jobs found.')]"));
        List<WebElement> jobCards = driver.findElements(By.xpath("//div[@data-job-id]"));
        return jobCards.isEmpty();
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