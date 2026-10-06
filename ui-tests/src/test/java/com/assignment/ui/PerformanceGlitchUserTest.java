package com.assignment.ui;

import com.assignment.base.BaseTest;

import com.assignment.models.User;
import com.assignment.models.CheckoutInfo;
import com.assignment.models.Product;
import com.assignment.pages.CartPage;
import com.assignment.pages.CheckoutCompletePage;
import com.assignment.pages.CheckoutInfoPage;
import com.assignment.pages.CheckoutOverviewPage;
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
    @Tag("E2E")
    @Tag("P1")
    @Tag("Regression")
    @Story("Performance Glitch Handling")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-18] Should complete full end-to-end checkout flow (Login -> Add Item -> Checkout -> Confirm) within SLA when performance_glitch_user logs in")
    void shouldCompleteFullCheckoutFlowWithinSlaThresholdWhenPerformanceGlitchUserLogsIn() {
        // Step 1: Login with performance_glitch_user and measure login SLA
        long startTime = System.currentTimeMillis();

        InventoryPage inventory = loginAs(User.PERFORMANCE_GLITCH);

        assertThat(page).hasURL(Pattern.compile(".*/inventory\\.html$"));
        assertThat(inventory.title()).hasText("Products");
        assertThat(inventory.inventoryList()).isVisible();

        long loginDurationMs = System.currentTimeMillis() - startTime;
        double timeoutThresholdMs = CONFIG.getPerformanceTimeoutMs();

        System.out.printf("[UI-18] performance_glitch_user login completed in %d ms (threshold: %.0f ms)%n",
                loginDurationMs, timeoutThresholdMs);

        assertTrue(loginDurationMs < timeoutThresholdMs,
                String.format("Login duration (%d ms) exceeded functional threshold (%.0f ms)",
                        loginDurationMs, timeoutThresholdMs));

        // Step 2: Add item to cart
        inventory.add(Product.BACKPACK);
        assertThat(inventory.cartBadge()).hasText("1");

        // Step 3: Open cart & verify item
        CartPage cart = inventory.openCart();
        assertThat(cart.items()).hasCount(1);

        // Step 4: Checkout Information & Continue to Overview
        CheckoutInfoPage infoPage = cart.checkout();
        CheckoutOverviewPage overview = infoPage
                .fill(CheckoutInfo.valid())
                .continueToOverview();
        assertThat(overview.items()).hasCount(1);

        // Step 5: Finish order and Confirm
        CheckoutCompletePage complete = overview.finish();
        assertThat(complete.header()).hasText("Thank you for your order!");
        assertThat(page).hasURL(Pattern.compile(".*/checkout-complete\\.html$"));
    }
}


