package com.example.positionfinder.service;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ExtractJobDetails {
    public static void extractJobDetails(WebDriver driver, WebDriverWait wait, Map<String, String> jobDetails) {

           extractProcess(driver, wait, jobDetails);

    }

    private static void extractProcess(WebDriver driver, WebDriverWait wait, Map<String, String> jobDetails) {
        // Wait for a given period to allow the page to load
        System.out.println("Waiting for page to load...");
        try {
            Thread.sleep(1000); // Convert seconds to milliseconds
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
