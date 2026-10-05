package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.assignment.models.Product;
import io.qameta.allure.Step;

public class CartPage extends BasePage {

    private final Locator items;
    private final Locator itemNames;
    private final Locator itemDescriptions;
    private final Locator checkoutButton;

    public CartPage(Page page) {
        super(page);
        this.items = page.getByTestId("inventory-item");
        this.itemNames = page.getByTestId("inventory-item-name");
        this.itemDescriptions = page.getByTestId("inventory-item-desc");
        this.checkoutButton = page.getByTestId("checkout");
    }

    public Locator items() {
        return items;
    }

    public Locator itemNames() {
        return itemNames;
    }

    public Locator itemDescriptions() {
        return itemDescriptions;
    }

    public Locator itemDescription(Product product) {
        return items.filter(new Locator.FilterOptions().setHasText(product.displayName()))
                .getByTestId("inventory-item-desc");
    }

    public Locator removeButton(Product product) {
        return page.getByTestId("remove-" + product.slug());
    }

    @Step("Remove {product} from cart")
    public CartPage remove(Product product) {
        removeButton(product).click();
        return this;
    }

    @Step("Click Checkout")
    public CheckoutInfoPage checkout() {
        checkoutButton.click();
        return new CheckoutInfoPage(page);
    }
}

