package com.example.positionfinder.gpt;

import com.example.positionfinder.constants.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GptFilter {
    public static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
    private final WebDriver driver;
    private WebDriverWait wait;
    private String url = "https://chatgpt.com/";

    public GptFilter() {
        this.driver = initializeWebDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public int getResult(String jobDescription) {
        int result = 0;
        openPage();
        String prompt = Constants.task1 + Constants.CV + Constants.task2 + jobDescription;
        return result;
    }

    private void openPage() {
        driver.get(url);
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
}
