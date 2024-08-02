package com.example.positionfinder.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Scrolling implements Runnable {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private volatile boolean running = true; // Control flag for thread execution
    Map<String, String> jobDetails;

    public Scrolling(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        this.jobDetails = new LinkedHashMap<>();
    }

    @Override
    public void run() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Define the step size for scrolling
        int stepSize = 800; // Example value in pixels

        while (running) {
            // Get the initial height of the page


            // Perform incremental scrolling and check for new content
            while (running) {
                // Check if the "See more jobs" button is present and click it
                List<WebElement> seeMoreButtons = driver.findElements(By.xpath("//button[contains(@class, 'infinite-scroller__show-more-button')]"));

                if (!seeMoreButtons.isEmpty()) {
                    WebElement seeMoreButton = seeMoreButtons.get(0);
                    if (seeMoreButton.isDisplayed() && seeMoreButton.isEnabled()) {
                        seeMoreButton.click();
                        System.out.println("Clicked 'See more jobs' button.");
                        try {
                            Thread.sleep(3000); // Wait for 3 seconds to allow content to load after clicking
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Scroll down incrementally in steps
                js.executeScript("window.scrollBy(0, arguments[0]);", stepSize);
                js.executeScript("window.scrollBy(0, arguments[0]);", -stepSize/3);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                js.executeScript("window.scrollBy(0, arguments[0]);", stepSize/3);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
       //         ExtractJobDetails.extractJobDetails(driver, wait, jobDetails);
                // Wait for new content to load
//                try {
//                    Thread.sleep(3000); // Wait for 3 seconds between scrolls
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }


    // Method to stop the thread
    public void stop() {
        running = false;
    }

    // Method to start the thread
    public void start() {
        new Thread(this).start();
    }
}




