package com.assignment.utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import com.assignment.base.BaseTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JUnit 5 TestWatcher that attaches Playwright artifacts to Allure on failure:
 * - Full-page Screenshot
 * - Playwright Trace (.zip)
 * On success, trace is stopped without writing to disk.
 */
public class TestFailureListener implements TestWatcher {

    private static final Path TRACE_DIR = Paths.get("allure-results/traces");

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable cause) {
        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof BaseTest baseTest) {
            Page page = baseTest.getPage();
            BrowserContext browserContext = baseTest.getContext();

            // 1. Capture Full-Page Screenshot on failure
            if (page != null && !page.isClosed()) {
                try {
                    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.addAttachment("Failure Screenshot - " + extensionContext.getDisplayName(),
                            "image/png", new ByteArrayInputStream(screenshot), "png");
                } catch (Exception e) {
                    System.err.println("Failed to capture failure screenshot: " + e.getMessage());
                }
            }

            // 2. Stop Tracing & Attach trace.zip on failure
            if (browserContext != null) {
                try {
                    Files.createDirectories(TRACE_DIR);
                    String testName = extensionContext.getRequiredTestMethod().getName();
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
        }
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof BaseTest baseTest) {
            BrowserContext browserContext = baseTest.getContext();
            if (browserContext != null) {
                try {
                    browserContext.tracing().stop();
                } catch (Exception ignored) {
                }
            }
        }
    }
}

