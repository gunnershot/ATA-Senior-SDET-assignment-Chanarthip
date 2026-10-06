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
    @Tag("UI-20")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-01")
    @Story("Problem User Cart Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-20] Should document diagnostic defect where clicking remove fails to decrement badge for problem_user")
    void shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem() {
        InventoryPage inventory = loginAs(User.PROBLEM).add(Product.BACKPACK);
        assertThat(inventory.cartBadge()).hasText("1");

        // Specific observed behavior: Remove button fails to remove item for problem_user
        inventory.remove(Product.BACKPACK);

        // Strict assertion verifying the observed defect: Badge remains 1 instead of disappearing
        assertThat(inventory.cartBadge()).hasText("1");
        Allure.step("DISCOVERED DEFECT: problem_user cannot remove items from inventory. Cart badge remains 1.");
    }

    @Test
    @Tag("UI-21")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-02")
    @Story("Problem User Form Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-21] Should document diagnostic defect where typing last name misroutes and overwrites first name for problem_user")
    void shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation() {
        CartPage cart = cartWith(User.PROBLEM, Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        // problem_user form defect: typing into lastName field updates the firstName input
        infoPage.fill(new CheckoutInfo("John", "Doe", "12345"));

        // Strict assertions on observed defect state:
        assertThat(infoPage.firstName()).hasValue("Doe");
        assertThat(infoPage.lastName()).hasValue("");

        // Attempting to continue fails with Last Name is required because it routed into first name
        infoPage.continueToOverview();
        assertThat(infoPage.error()).hasText("Error: Last Name is required");

        Allure.step("DISCOVERED DEFECT: problem_user checkout input fields are miswired. Typing into last name sets first name to 'Doe' and leaves last name blank.");
    }

    @Test
    @Tag("UI-25")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-03")
    @Story("Problem User Catalog Image Defect")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("[UI-25] Should document diagnostic defect where catalog product images display 404 fallback dog image for problem_user (DEF-05)")
    void shouldDocumentBrokenProductImagesWhenProblemUserViewsCatalog() {
        loginAs(User.PROBLEM);
        com.microsoft.playwright.Locator images = page.locator("img.inventory_item_img");
        int count = images.count();
        org.junit.jupiter.api.Assertions.assertTrue(count > 0, "Inventory images should be present in DOM");

        // Specific observed behavior: Every item image src points to fallback 404 dog image asset
        for (int i = 0; i < count; i++) {
            String src = images.nth(i).getAttribute("src");
            org.junit.jupiter.api.Assertions.assertTrue(src != null && src.contains("sl-404"),
                    "Image " + i + " must contain 'sl-404' fallback path for problem_user. Actual: " + src);
        }
        Allure.step("DISCOVERED DEFECT [DEF-05]: problem_user catalog item images are broken and render fallback 'sl-404' asset.");
    }

    @Test
    @Tag("UI-26")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-04")
    @Story("Problem User Navigation Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-26] Should document diagnostic defect where clicking Backpack title navigates to Fleece Jacket details for problem_user (DEF-07)")
    void shouldDocumentWrongProductNavigationWhenProblemUserClicksItemName() {
        InventoryPage inventory = loginAs(User.PROBLEM);
        com.assignment.pages.ProductDetailsPage details = inventory.openProductDetails(Product.BACKPACK);

        // Specific observed behavior: Clicking Backpack link opens Fleece Jacket instead
        assertThat(details.itemName()).hasText(Product.FLEECE_JACKET.displayName());
        Allure.step("DISCOVERED DEFECT [DEF-07]: Clicking 'Sauce Labs Backpack' opens 'Sauce Labs Fleece Jacket' for problem_user.");
    }

    @Test
    @Tag("UI-27")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-05")
    @Story("Problem User Add to Cart Limitation")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-27] Should document diagnostic defect where problem_user can only add 3 of 6 items to cart (DEF-08)")
    void shouldDocumentAddLimitDefectWhenProblemUserAttemptsToAddAllCatalogItems() {
        InventoryPage inventory = loginAs(User.PROBLEM);
        inventory.addAll();

        // Specific observed behavior: problem_user can only add 3 items to cart out of 6
        assertThat(inventory.cartBadge()).hasText("3");
        CartPage cart = inventory.openCart();
        assertThat(cart.items()).hasCount(3);
        Allure.step("DISCOVERED DEFECT [DEF-08]: problem_user is limited to adding only 3 of 6 items to cart.");
    }
}
