package com.assignment.utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import com.assignment.base.BaseTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JUnit 5 Extension that captures Playwright artifacts on failure:
 * - Full-page Screenshot (saved to target/screenshots/ and attached to Allure)
 * - Playwright Trace (.zip attached to Allure)
 * 
 * Uses AfterTestExecutionCallback so capture occurs before @AfterEach closes the BrowserContext.
 */
public class TestFailureListener implements AfterTestExecutionCallback, TestWatcher {

    private static final Path SCREENSHOT_DIR = Paths.get("target/screenshots");
    private static final Path TRACE_DIR = Paths.get("allure-results/traces");

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        Object testInstance = extensionContext.getRequiredTestInstance();
        if (!(testInstance instanceof BaseTest baseTest)) {
            return;
        }

        Page page = baseTest.getPage();
        BrowserContext browserContext = baseTest.getContext();

        if (extensionContext.getExecutionException().isPresent()) {
            String testName = extensionContext.getRequiredTestMethod().getName();
            String displayName = extensionContext.getDisplayName();

            // 1. Capture Full-Page Screenshot on failure
            if (page != null && !page.isClosed()) {
                try {
                    Files.createDirectories(SCREENSHOT_DIR);
                    Path screenshotFile = SCREENSHOT_DIR.resolve(testName + "-failure.png");
                    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                            .setFullPage(true)
                            .setPath(screenshotFile));

                    Allure.addAttachment("Failure Screenshot - " + displayName,
                            "image/png", new ByteArrayInputStream(screenshot), "png");
                    System.out.println("📸 Failure screenshot saved to: " + screenshotFile.toAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to capture failure screenshot: " + e.getMessage());
                }
            }

            // 2. Stop Tracing & Attach trace.zip on failure
            if (browserContext != null) {
                try {
                    Files.createDirectories(TRACE_DIR);
                    Path traceFile = TRACE_DIR.resolve(testName + "-trace.zip");
                    browserContext.tracing().stop(new Tracing.StopOptions().setPath(traceFile));

                    if (Files.exists(traceFile)) {
                        try (InputStream is = Files.newInputStream(traceFile)) {
                            Allure.addAttachment("Playwright Trace - " + testName, "application/zip", is, "zip");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to export Playwright trace: " + e.getMessage());
                }
            }
        } else {
            // Success: stop tracing without writing to disk
            if (browserContext != null) {
                try {
                    browserContext.tracing().stop();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
