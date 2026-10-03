package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.math.BigDecimal;
import java.util.List;

/**
 * Checkout step two: order overview with price and tax summary.
 */
public class CheckoutOverviewPage extends BasePage {

    private final Locator items;
    private final Locator itemNames;
    private final Locator itemPrices;
    private final Locator subtotal;
    private final Locator tax;
    private final Locator total;
    private final Locator finishButton;
    private final Locator cancelButton;

    public CheckoutOverviewPage(Page page) {
        super(page);
        this.items = page.getByTestId("inventory-item");
        this.itemNames = page.getByTestId("inventory-item-name");
        this.itemPrices = page.getByTestId("inventory-item-price");
        this.subtotal = page.getByTestId("subtotal-label");
        this.tax = page.getByTestId("tax-label");
        this.total = page.getByTestId("total-label");
        this.finishButton = page.getByTestId("finish");
        this.cancelButton = page.getByTestId("cancel");
    }

    public Locator items() { return items; }
    public Locator itemNames() { return itemNames; }
    public Locator finishButton() { return finishButton; }

    public List<BigDecimal> itemPrices() {
        return itemPrices.allInnerTexts().stream().map(BasePage::money).toList();
    }

    public BigDecimal subtotal() { return money(subtotal.innerText()); }
    public BigDecimal tax() { return money(tax.innerText()); }
    public BigDecimal total() { return money(total.innerText()); }

    @Step("Click Finish")
    public CheckoutCompletePage finish() {
        finishButton.click();
        return new CheckoutCompletePage(page);
    }

    @Step("Click Cancel")
    public InventoryPage cancel() {
        cancelButton.click();
        return new InventoryPage(page);
    }
}

