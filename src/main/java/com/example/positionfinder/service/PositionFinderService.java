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
    //private String firstUrl = "https://www.linkedin.com/jobs/search/?f_TPR=r86400&keywords=&origin=JOB_SEARCH_PAGE_JOB_FILTER";
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

            driver.get(firstUrl);
            // Scroll down to load more job cards
            //     scrollToLoadMore(driver, wait);
            // Extract job details from the current page
            extractJobDetails(driver, wait, jobDetails);
            System.out.println("jobs found " + jobDetails.size());
            // Write job details to Excel
            WriteToExcel.writeToExcel(jobDetails);
            System.out.println("jobs parsed " + jobDetails.size());
        } finally {
       //     driver.quit();
        }
    }

//    private void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
//
//        // Initialize JavaScript Executor
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//
//        // Locate the scrollable container. Adjust XPath if necessary.
//        WebElement scrollableContainer = driver.findElement(By.xpath("//body")); // Adjust XPath as needed
//
//        // Scroll down to the end of the page
//        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
//
//        // Optionally, you can wait for a while to ensure new content loads
//        try {
//            Thread.sleep(2000); // Wait for 2 seconds to allow content to load
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

//    private void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        boolean moreJobs = true;
//
//        while (moreJobs) {
//            // Scroll to the bottom of the page
//            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
//
//            // Wait for a while to ensure new content loads
//            try {
//                Thread.sleep(7000); // Wait for 7 seconds
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            boolean seeMoreButtonVisible = false;
//
//            // Check if the "See more jobs" button is present and visible
//            try {
//                WebElement seeMoreButton = driver.findElement(By.xpath("//button[@aria-label='See more jobs']"));
//                if (seeMoreButton.isDisplayed()) {
//                    seeMoreButtonVisible = true;
//                    // Click the "See more jobs" button
//                    seeMoreButton.click();
//                    System.out.println("Clicked 'See more jobs' button");
//
//                    // Wait for new content to load
//                    try {
//                        Thread.sleep(7000); // Wait for 7 seconds
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (NoSuchElementException e) {
//                // The "See more jobs" button is not found, move to check the end message
//                System.out.println("No 'See more jobs' button found");
//            }
//
//            // Check if the end message is present
//            try {
//                WebElement endMessage = driver.findElement(By.xpath("//p[contains(@class, 'inline-notification__text') and contains(text(), 'You\'ve viewed all jobs for this search')]"));
//                if (endMessage.isDisplayed()) {
//                    System.out.println("End of job listings reached");
//                    moreJobs = false; // Stop scrolling when the end message is found
//                }
//            } catch (NoSuchElementException endMessageEx) {
//                // If end message is not found, continue scrolling
//                if (!seeMoreButtonVisible) {
//                    System.out.println("No 'See more jobs' button found and end message not displayed, continuing to scroll");
//                }
//            }
//        }
//    }
//private void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
//    JavascriptExecutor js = (JavascriptExecutor) driver;
//
//    // Locate the scrollable container using the provided XPath
//    WebElement scrollableContainer = driver.findElement(By.xpath("//*[@id='main']/div/div[2]/div[1]/div"));
//
//    // Define the step size for scrolling. Start with an initial value and adjust based on your needs.
//    int stepSize = 800; // Example value in pixels
//    long previousHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", scrollableContainer);
//    boolean moreContentToLoad = true;
//
//    while (moreContentToLoad) {
//        // Scroll down incrementally in steps
//        for (int i = 0; i < 1; i++) { // Number of intermediate stops
//            js.executeScript("arguments[0].scrollTop += arguments[1];", scrollableContainer, stepSize);
//
//            // Wait for new content to load
//            try {
//                Thread.sleep(2000); // Wait for 2 seconds between scrolls
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            // Check if the page height has increased (indicating new content has loaded)
//            long newHeight = (Long) js.executeScript("return arguments[0].scrollHeight;", scrollableContainer);
//            if (newHeight == previousHeight) {
//                // If the height hasn't changed, there may be no more new content
//                moreContentToLoad = false;
//                break;
//            }
//            previousHeight = newHeight;
//        }
//    }
//}

    private void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Define the step size for scrolling
        int stepSize = 800; // Example value in pixels
        boolean moreJobs = true;

        while (moreJobs) {
            // Get the initial height of the page
            long previousHeight = (Long) js.executeScript("return document.body.scrollHeight;");

            boolean scrolled = false;

            // Perform incremental scrolling and check for new content
            while (true) {
                // Scroll down incrementally in steps
                js.executeScript("window.scrollBy(0, arguments[0]);", stepSize);
                scrolled = true;

                // Wait for new content to load
                try {
                    Thread.sleep(7000); // Wait for 7 seconds to allow content to load
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Check if the "See more jobs" button is present and click it
                List<WebElement> seeMoreButtons = driver.findElements(By.xpath("//button[contains(@class, 'infinite-scroller__show-more-button')]"));
                if (!seeMoreButtons.isEmpty()) {
                    WebElement seeMoreButton = seeMoreButtons.get(0);
                    if (seeMoreButton.isDisplayed() && seeMoreButton.isEnabled()) {
                        seeMoreButton.click();
                        System.out.println("Clicked 'See more jobs' button.");
                        try {
                            Thread.sleep(7000); // Wait for 7 seconds to allow content to load after clicking
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break; // Break to the outer while loop to check for more jobs
                    }
                }

                // Check if the end message is present
                List<WebElement> endMessages = driver.findElements(By.xpath("//p[contains(@class, 'inline-notification__text') and contains(text(), \"You've viewed all jobs for this search\")]"));
                if (!endMessages.isEmpty()) {
                    System.out.println("End message found. No more jobs to load.");
                    moreJobs = false;
                    break;
                }

                // Check if the page height has increased (indicating new content has loaded)
                long newHeight = (Long) js.executeScript("return document.body.scrollHeight;");
                if (newHeight == previousHeight) {
                    // If the height hasn't changed, there may be no more new content
                    System.out.println("No new content loaded. Ending scrolling.");
                    moreJobs = false;
                    break;
                }
                previousHeight = newHeight;
            }

            // If we did not scroll or load any new content, break the loop
            if (!scrolled) {
                System.out.println("No more new content detected. Ending scrolling.");
                moreJobs = false;
            }
        }
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
        scrollToLoadMore(driver, wait);
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
