package com.example.positionfinder.gpt;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class ExtractGptResult {
    public static int extractGptResult(WebDriver driver, WebDriverWait wait, String prompt) {

        return extractProcess(driver, wait, prompt);

    }

    private static int extractProcess(WebDriver driver, WebDriverWait wait, String promptText) {
        System.out.println("Waiting for page to load...");

        try {
            // Locate and interact with the prompt input field
            WebElement promptField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea#prompt-textarea")));
            promptField.clear();
            promptField.sendKeys(promptText);
            System.out.println("Entered prompt text: " + promptText);

            // Locate and click the send button
            WebElement sendButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='send-button']")));
            sendButton.click();
            System.out.println("Clicked send button.");

            // Wait for the result <p> to be visible
            WebElement resultParagraph = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.markdown.prose.w-full.break-words.dark\\:prose-invert.light > p")));

            // Extract and process the result text
            String resultText = resultParagraph.getText();

            // Check if the result is a 2-digit number
            if (resultText.matches("\\d{2}")) {
                // Convert the result text to an integer
                int resultNumber = Integer.parseInt(resultText);
                System.out.println("Result integer extracted: " + resultNumber);
                return resultNumber;
            } else {
                System.err.println("Unexpected result text: " + resultText);
                return -1; // Return -1 or another default value to indicate failure
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return -1; // Return -1 or another default value to indicate failure
        }
    }

}
