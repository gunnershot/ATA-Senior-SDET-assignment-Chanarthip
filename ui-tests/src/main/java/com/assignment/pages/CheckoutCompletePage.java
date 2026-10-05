package com.assignment.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Checkout complete page: order confirmation.
 */
public class CheckoutCompletePage extends BasePage {

    private final Locator header;
    private final Locator backHomeButton;
    private final Locator generatePdfButton;

    public CheckoutCompletePage(Page page) {
        super(page);
        this.header = page.getByTestId("complete-header");
        this.backHomeButton = page.getByTestId("back-to-products");
        this.generatePdfButton = page.getByTestId("generate-pdf-order");
    }

    public Locator header() {
        return header;
    }

    public Locator backHomeButton() {
        return backHomeButton;
    }

    public Locator generatePdfButton() {
        return generatePdfButton;
    }

    @Step("Click Back Home")
    public InventoryPage backHome() {
        backHomeButton.click();
        return new InventoryPage(page);
    }

    @Step("Generate and download order PDF receipt")
    public Path downloadOrderPdf() {
        Download download = page.waitForDownload(() -> generatePdfButton.click());
        try {
            Path tempFile = Files.createTempFile("order-receipt-", ".pdf");
            download.saveAs(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save downloaded order receipt PDF", e);
        }
    }
}

