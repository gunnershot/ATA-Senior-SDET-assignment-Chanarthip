package com.assignment.ui;

import com.assignment.base.BaseTest;
import com.assignment.models.Product;
import com.assignment.models.User;
import com.assignment.pages.InventoryPage;
import com.assignment.pages.ProductDetailsPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Feature("Product Catalog & Details")
class ProductDetailsTest extends BaseTest {

    @Test
    @Tag("UI-19")
    @Tag("Happy")
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Product Details View")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-19] Should navigate to product details and display matching name, description, and price when clicking item name")
    void shouldDisplayAccurateProductDetailsWhenClickingItemNameFromInventory() {
        Product targetProduct = Product.BACKPACK;

        // 1. Log in and arrive at Inventory
        InventoryPage inventory = login();
        assertThat(page).hasURL(Pattern.compile(".*/inventory\\.html$"));

        // 2. Click inventory-item-name on inventory page
        ProductDetailsPage detailsPage = inventory.openProductDetails(targetProduct);

        // 3. Assert navigation to product details page
        assertThat(page).hasURL(Pattern.compile(".*/inventory-item\\.html\\?id=\\d+$"));

        // 4. Assert data-test attributes and values: name, description, price
        assertThat(detailsPage.itemName()).hasText(targetProduct.displayName());
        assertThat(detailsPage.itemDescription()).hasText(targetProduct.description());
        assertThat(detailsPage.itemPrice()).hasText("$" + targetProduct.price());
        assertEquals(0, targetProduct.price().compareTo(detailsPage.price()),
                "Parsed details price must match Product definition");

        // 5. Assert Back to products navigates back to inventory page
        InventoryPage returnedInventory = detailsPage.backToProducts();
        assertThat(page).hasURL(Pattern.compile(".*/inventory\\.html$"));
        assertThat(returnedInventory.inventoryList()).isVisible();
    }
}



