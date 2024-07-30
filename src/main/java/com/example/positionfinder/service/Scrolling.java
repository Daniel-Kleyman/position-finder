package com.example.positionfinder.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class Scrolling {

    public static void scrollToLoadMore(WebDriver driver, WebDriverWait wait) {
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
}
