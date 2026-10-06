package com.assignment.ui;

import com.assignment.base.BaseTest;

import com.assignment.models.CheckoutInfo;
import com.assignment.models.Product;
import com.assignment.models.User;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.assignment.pages.CartPage;
import com.assignment.pages.CheckoutInfoPage;
import com.assignment.pages.InventoryPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Feature("Problem User Diagnostics")
class ProblemUserTest extends BaseTest {

    @Test
    @Tag("DEF-01")
    @Tag("NotRun")
    @Tag("Diagnostic")
    @Tag("P1")
    @Issue("SAUCE-PROBLEM-USER-01")
    @Story("Problem User Cart Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[DEF-01] Should document diagnostic defect where clicking remove fails to decrement badge for problem_user")
    void shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem() {
        InventoryPage inventory = loginAs(User.PROBLEM).add(Product.BACKPACK);
        assertThat(inventory.cartBadge()).hasText("1");

        inventory.remove(Product.BACKPACK);

        // Expectation: The cart badge should disappear
        assertThat(inventory.cartBadge()).not().isVisible();
    }

    @Test
    @Tag("DEF-02")
    @Tag("NotRun")
    @Tag("Diagnostic")
    @Tag("P1")
    @Issue("SAUCE-PROBLEM-USER-02")
    @Story("Problem User Form Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[DEF-02] Should document diagnostic defect where typing last name misroutes and overwrites first name for problem_user")
    void shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation() {
        CartPage cart = cartWith(User.PROBLEM, Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        infoPage.fill(new CheckoutInfo("John", "Doe", "12345"));

        // Expectation: Fields should retain the values inputted
        assertThat(infoPage.firstName()).hasValue("John");
        assertThat(infoPage.lastName()).hasValue("Doe");
        
        infoPage.continueToOverview();
        assertThat(infoPage.error()).not().isVisible();
    }

    @Test
    @Tag("DEF-05")
    @Tag("NotRun")
    @Tag("Diagnostic")
    @Tag("P1")
    @Issue("SAUCE-PROBLEM-USER-03")
    @Story("Problem User Catalog Image Defect")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("[DEF-05] Should document diagnostic defect where catalog product images display 404 fallback dog image for problem_user (DEF-05)")
    void shouldDocumentBrokenProductImagesWhenProblemUserViewsCatalog() {
        loginAs(User.PROBLEM);
        com.microsoft.playwright.Locator images = page.locator("img.inventory_item_img");
        int count = images.count();
        org.junit.jupiter.api.Assertions.assertTrue(count > 0, "Inventory images should be present in DOM");

        // Expectation: Every item image src should NOT point to fallback 404 dog image asset
        for (int i = 0; i < count; i++) {
            String src = images.nth(i).getAttribute("src");
            org.junit.jupiter.api.Assertions.assertFalse(src != null && src.contains("sl-404"),
                    "Image " + i + " must NOT contain 'sl-404' fallback path. Actual: " + src);
        }
    }

    @Test
    @Tag("DEF-07")
    @Tag("NotRun")
    @Tag("Diagnostic")
    @Tag("P1")
    @Issue("SAUCE-PROBLEM-USER-04")
    @Story("Problem User Navigation Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[DEF-07] Should document diagnostic defect where clicking Backpack title navigates to Fleece Jacket details for problem_user (DEF-07)")
    void shouldDocumentWrongProductNavigationWhenProblemUserClicksItemName() {
        InventoryPage inventory = loginAs(User.PROBLEM);
        com.assignment.pages.ProductDetailsPage details = inventory.openProductDetails(Product.BACKPACK);

        // Expectation: Clicking Backpack link should open Backpack details
        assertThat(details.itemName()).hasText(Product.BACKPACK.displayName());
    }

    @Test
    @Tag("DEF-08")
    @Tag("NotRun")
    @Tag("Diagnostic")
    @Tag("P1")
    @Issue("SAUCE-PROBLEM-USER-05")
    @Story("Problem User Add to Cart Limitation")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[DEF-08] Should document diagnostic defect where problem_user can only add 3 of 6 items to cart (DEF-08)")
    void shouldDocumentAddLimitDefectWhenProblemUserAttemptsToAddAllCatalogItems() {
        InventoryPage inventory = loginAs(User.PROBLEM);
        inventory.addAll();

        // Expectation: All 6 items should be added to the cart
        assertThat(inventory.cartBadge()).hasText("6");
        CartPage cart = inventory.openCart();
        assertThat(cart.items()).hasCount(6);
    }
}
