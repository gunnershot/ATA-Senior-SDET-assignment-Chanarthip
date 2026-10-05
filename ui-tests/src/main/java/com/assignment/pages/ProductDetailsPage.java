package com.assignment.pages;

import com.assignment.models.Product;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.math.BigDecimal;

public class ProductDetailsPage extends BasePage {

    private final Locator itemName;
    private final Locator itemDescription;
    private final Locator itemPrice;
    private final Locator backButton;

    public ProductDetailsPage(Page page) {
        super(page);
        this.itemName = page.getByTestId("inventory-item-name");
        this.itemDescription = page.getByTestId("inventory-item-desc");
        this.itemPrice = page.getByTestId("inventory-item-price");
        this.backButton = page.getByTestId("back-to-products");
    }

    public Locator itemName() {
        return itemName;
    }

    public Locator itemDescription() {
        return itemDescription;
    }

    public Locator itemPrice() {
        return itemPrice;
    }

    public Locator backButton() {
        return backButton;
    }

    public BigDecimal price() {
        String raw = itemPrice.textContent().replace("$", "").trim();
        return new BigDecimal(raw);
    }

    public Locator addButton(Product product) {
        return page.getByTestId("add-to-cart-" + product.slug());
    }

    public Locator removeButton(Product product) {
        return page.getByTestId("remove-" + product.slug());
    }

    @Step("Click Back to products")
    public InventoryPage backToProducts() {
        backButton.click();
        return new InventoryPage(page);
    }
}

