package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.assignment.config.PlaywrightConfig;

import java.math.BigDecimal;

/**
 * Shared header elements and helpers.
 * Page objects expose {@link Locator}s using official Playwright {@code page.getByTestId(...)}
 * so tests can use Playwright's auto-waiting web-first assertions.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected void open(String path) {
        page.navigate(PlaywrightConfig.get().getBaseUrl() + path);
    }

    public Locator title() {
        return page.getByTestId("title");
    }

    public Locator cartBadge() {
        return page.getByTestId("shopping-cart-badge");
    }

    protected Locator cartLink() {
        return page.getByTestId("shopping-cart-link");
    }

    /**
     * Parses labels such as "Item total: $39.98" or "$29.99".
     * BigDecimal avoids floating-point drift in money calculations.
     */
    public static BigDecimal money(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.]", ""));
    }
}

