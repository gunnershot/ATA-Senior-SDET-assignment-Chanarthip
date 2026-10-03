package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

/**
 * Checkout complete page: order confirmation.
 */
public class CheckoutCompletePage extends BasePage {

    private final Locator header;
    private final Locator backHomeButton;

    public CheckoutCompletePage(Page page) {
        super(page);
        this.header = page.getByTestId("complete-header");
        this.backHomeButton = page.getByTestId("back-to-products");
    }

    public Locator header() {
        return header;
    }

    public Locator backHomeButton() {
        return backHomeButton;
    }

    @Step("Click Back Home")
    public InventoryPage backHome() {
        backHomeButton.click();
        return new InventoryPage(page);
    }
}

