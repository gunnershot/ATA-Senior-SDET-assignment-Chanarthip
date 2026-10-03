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
    @Tag("UI-18")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-01")
    @Story("Problem User Cart Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-18] Should document diagnostic defect where clicking remove fails to decrement badge for problem_user")
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
    @Tag("UI-19")
    @Tag("Diagnostic")
    @Tag("P1")
    @Tag("Regression")
    @Issue("SAUCE-PROBLEM-USER-02")
    @Story("Problem User Form Defect")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-19] Should document diagnostic defect where typing last name misroutes and overwrites first name for problem_user")
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
}

