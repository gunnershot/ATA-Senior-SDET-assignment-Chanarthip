package com.assignment.ui;

import com.assignment.base.BaseTest;

import com.assignment.models.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.assignment.pages.InventoryPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Feature("Performance & Resilience")
class PerformanceGlitchUserTest extends BaseTest {

    @Test
    @Tag("UI-18")
    @Tag("Resilience")
    @Tag("P1")
    @Tag("Regression")
    @Story("Performance Glitch Handling")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-18] Should successfully reach inventory within 10-second SLA threshold when performance_glitch_user logs in")
    void shouldCompleteLoginWithinSlaThresholdWhenPerformanceGlitchUserLogsIn() {
        long startTime = System.currentTimeMillis();

        InventoryPage inventory = loginAs(User.PERFORMANCE_GLITCH);

        assertThat(page).hasURL(Pattern.compile(".*/inventory\\.html$"));
        assertThat(inventory.title()).hasText("Products");
        assertThat(inventory.inventoryList()).isVisible();

        long durationMs = System.currentTimeMillis() - startTime;
        double timeoutThresholdMs = CONFIG.getPerformanceTimeoutMs();

        System.out.printf("[UI-18] performance_glitch_user login completed in %d ms (threshold: %.0f ms)%n",
                durationMs, timeoutThresholdMs);

        assertTrue(durationMs < timeoutThresholdMs,
                String.format("Login duration (%d ms) exceeded functional threshold (%.0f ms)",
                        durationMs, timeoutThresholdMs));
    }
}

