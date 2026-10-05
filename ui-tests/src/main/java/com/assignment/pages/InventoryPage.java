package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.assignment.models.Product;
import io.qameta.allure.Step;

public class InventoryPage extends BasePage {

    private final Locator inventoryList;

    public InventoryPage(Page page) {
        super(page);
        this.inventoryList = page.getByTestId("inventory-list");
    }

    public Locator inventoryList() {
        return inventoryList;
    }

    public Locator addButton(Product product) {
        return page.getByTestId("add-to-cart-" + product.slug());
    }

    public Locator removeButton(Product product) {
        return page.getByTestId("remove-" + product.slug());
    }

    @Step("Add {product} to cart")
    public InventoryPage add(Product product) {
        addButton(product).click();
        return this;
    }

    @Step("Add all available products to cart")
    public InventoryPage addAll() {
        for (Product product : Product.values()) {
            add(product);
        }
        return this;
    }

    @Step("Remove {product} from cart (inventory page)")
    public InventoryPage remove(Product product) {
        removeButton(product).click();
        return this;
    }

    @Step("Open cart")
    public CartPage openCart() {
        cartLink().click();
        return new CartPage(page);
    }
}

