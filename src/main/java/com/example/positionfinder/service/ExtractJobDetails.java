package com.example.positionfinder.service;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExtractJobDetails {
    public static void extractJobDetails(WebDriver driver, WebDriverWait wait, Map<String, List<String>> jobDetails) {

        extractProcess(driver, wait, jobDetails);

    }

    private static void extractProcess(WebDriver driver, WebDriverWait wait, Map<String, List<String>> jobDetails) {
        System.out.println("Waiting for page to load...");
        int jobsVisibleOnPage = 0;

        try {
            // Wait until the job container is visible
            WebElement jobContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[contains(@class, 'jobs-search__results-list')]")));

            // Find all job cards on the page
            List<WebElement> jobCards = driver.findElements(By.xpath("//div[contains(@class, 'job-search-card')]"));
            if (jobCards.isEmpty()) {
                System.err.println("No job cards found.");
                return;
            }

            for (int i = 0; i < jobCards.size(); i++) {
                jobsVisibleOnPage++;
                WebElement jobCard = jobCards.get(i);
                String title = "";
                String url = "";
                List<String> details = new ArrayList<>();
                try {
                    // Extract job title and URL
                    try {
                        WebElement titleElement = jobCard.findElement(By.xpath(".//h3[contains(@class, 'base-search-card__title')]"));
                        title = titleElement.getText();
                        if (filterTitle(title)) {
                            System.out.println("Job title excluded: " + title);
                            continue; // Skip this job card if title matches filter criteria
                        }
                        details.add(title);
                        //             System.out.println("Title added: " + title);

                        WebElement urlElement = jobCard.findElement(By.cssSelector("a.base-card__full-link"));
                        url = urlElement.getAttribute("href");

                    } catch (NoSuchElementException e) {
                        System.err.println("Job title or URL element not found in a job card.");
                        continue; // Skip this job card if title or URL is missing
                    }

                    boolean showMoreVisible = false;
                    while (!showMoreVisible) {
                        // Click on the job card to expand it
                        jobCard.click();
                        try {
                            // Try to find and click the "Show more" button
                            WebElement showMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.show-more-less-html__button")));
                            showMoreButton.click();
                            showMoreVisible = true; // Exit loop if the button was clicked
                            System.out.println("Clicked 'Show more' button.");
                        } catch (NoSuchElementException | TimeoutException e) {
                            // If the "Show more" button is not found, collapse the previous job card if possible
                            if (i > 0) {
                                WebElement previousJobCard = jobCards.get(i - 1);
                                previousJobCard.click(); // Collapse the previous job card
                                System.out.println("Collapsed previous job card.");
                            }
                            // Retry the current job card by clicking it again
                            jobCard.click();
                        }
                    }

                    // Extract company name
                    try {
                        WebElement companyElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.topcard__org-name-link")));
                        String companyName = companyElement.getText();
                        details.add(companyName);
                        //                System.out.println("Company name added: " + companyName);
                    } catch (NoSuchElementException | TimeoutException e) {
                        System.err.println("Company name element not found: " + e.getMessage());
                        details.add("Company name not available");
                    }

                    // Extract city
                    try {
                        WebElement cityElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.topcard__flavor.topcard__flavor--bullet")));
                        String city = cityElement.getText().trim();
                        details.add(city);
                        //                    System.out.println("City added: " + city);
                    } catch (NoSuchElementException | TimeoutException e) {
                        System.err.println("City element not found: " + e.getMessage());
                        details.add("City not available");
                    }
// Use a shorter wait for the expanded content
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                    WebElement expandedContent = null;
                    try {
                        expandedContent = shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.show-more-less-html__markup")));
                        String extendedText = expandedContent.getText();
                        if (!filterDescription(extendedText)) {
                            System.out.println("Extended text excluded.");
                            continue; // Skip this job card if the extended text does not match filter criteria
                        }
                        details.add(extendedText);
                        //                    System.out.println("Extended text added: " + extendedText);
                    } catch (TimeoutException e) {
                        System.err.println("Extended content not found within 1 second.");
                        details.add("Extended text not available");
                    }
                    // Add job details to the map
                    jobDetails.putIfAbsent(url, details);
//                    if (filterDetails(details)) {
//                        jobDetails.putIfAbsent(url, details);
//                        System.out.println("Job details added to map for URL: " + url);
//                    }

                } catch (Exception e) {
                    System.err.println("Unexpected error extracting details from job card: " + e.getMessage());
                }
            }
        } catch (TimeoutException e) {
            System.err.println("Timeout while waiting for job container: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
        System.out.println("Jobs visible: " + jobsVisibleOnPage);
    }

    private static boolean filterDetails(List<String> details) {
        Set<String> excludeKeywords = Set.of("senior", "lead", "leader", "devops", "manager", "qa", "mechanical", "infrastructure", "integration", "civil",
                "principal", "customer", "embedded", "system", " verification", "electrical", "support", "complaint", "solution", "solutions", "simulation", "technical",
                "manufacturing", "validation", "finops", "hardware", "devsecops", "motion", "machine Learning", "design", "sr.", "quality", "architect");
        // Convert the job title to lower case for case-insensitive comparison
        String jobTitle = details.get(0).toLowerCase();
        String aboutJob = details.get(3).toLowerCase();
        // Exclude entries if the job title contains any of the excludeKeywords
        boolean shouldExclude = excludeKeywords.stream()
                .anyMatch(keyword -> jobTitle.contains(keyword));
        // Include only entries that contain at least one of the includeKeywords

        boolean shouldAlsoInclude = aboutJob.contains("java");

        return !shouldExclude && shouldAlsoInclude;
    }

    private static boolean filterTitle(String jobTitle) {
        Set<String> excludeKeywords = Set.of("senior", "lead", "leader", "devops", "manager", "qa", "mechanical", "infrastructure", "integration", "civil",
                "principal", "customer", "embedded", "system", " verification", "electrical", "support", "complaint", "solution", "solutions", "simulation", "technical",
                "manufacturing", "validation", "finops", "hardware", "devsecops", "motion", "machine Learning", "design", "sr.", "quality");
        String jobTitle1 = jobTitle.toLowerCase();
        return excludeKeywords.stream()
                .anyMatch(keyword -> jobTitle1.contains(keyword));

    }

    private static boolean filterDescription(String aboutJob) {
        String aboutJob1 = aboutJob.toLowerCase();
        return aboutJob1.contains("java");

    }
}
