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
import com.assignment.pages.CheckoutCompletePage;
import com.assignment.pages.CheckoutInfoPage;
import com.assignment.pages.CheckoutOverviewPage;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Feature("Checkout & Order Integrity")
class CheckoutTest extends BaseTest {

    @Test
    @Tag("UI-12")
    @Tag("E2E")
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Checkout Flow")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-12] Should complete checkout order and display confirmation header when submitting valid customer information")
    void shouldCompleteOrderSuccessfullyWhenCheckingOutWithValidInformation() {
        CartPage cart = cartWith(User.STANDARD, Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        CheckoutOverviewPage overview = infoPage
                .fill(CheckoutInfo.valid())
                .continueToOverview();

        CheckoutCompletePage complete = overview.finish();

        assertThat(complete.header()).hasText("Thank you for your order!");
        assertThat(page).hasURL(Pattern.compile(".*/checkout-complete\\.html$"));
    }

    @Test
    @Tag("UI-13")
    @Tag("Negative")
    @Tag("P1")
    @Tag("Regression")
    @Story("Field Validation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-13] Should display specific field validation error when First Name, Last Name, or Postal Code is omitted")
    void shouldDisplayRequiredValidationErrorWhenAnyCheckoutFieldIsMissing() {
        CartPage cart = cartWith(User.STANDARD, Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        // 1. Missing First Name
        infoPage.fill(new CheckoutInfo(null, "Doe", "12345")).continueToOverview();
        assertThat(infoPage.error()).hasText("Error: First Name is required");

        // 2. Missing Last Name
        page.reload();
        infoPage.fill(new CheckoutInfo("John", null, "12345")).continueToOverview();
        assertThat(infoPage.error()).hasText("Error: Last Name is required");

        // 3. Missing Postal Code
        page.reload();
        infoPage.fill(new CheckoutInfo("John", "Doe", null)).continueToOverview();
        assertThat(infoPage.error()).hasText("Error: Postal Code is required");
    }

    @Test
    @Tag("UI-14")
    @Tag("Integrity")
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Price & Tax Integrity")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-14] Should verify price integrity where subtotal equals sum of item prices and total equals subtotal plus tax")
    void shouldCalculateSubtotalAndTotalAccuratelyWhenCheckingOutTwoItems() {
        CartPage cart = cartWith(User.STANDARD, Product.BACKPACK, Product.BIKE_LIGHT);

        CheckoutOverviewPage overview = cart.checkout()
                .fill(CheckoutInfo.valid())
                .continueToOverview();

        assertThat(overview.items()).hasCount(2);
        assertThat(overview.itemNames()).hasText(new String[]{
                Product.BACKPACK.displayName(), Product.BIKE_LIGHT.displayName()
        });

        // Price integrity calculation using exact BigDecimal arithmetic
        BigDecimal expectedSubtotal = BigDecimal.ZERO;
        for (BigDecimal price : overview.itemPrices()) {
            expectedSubtotal = expectedSubtotal.add(price);
        }

        BigDecimal actualSubtotal = overview.subtotal();
        BigDecimal actualTax = overview.tax();
        BigDecimal actualTotal = overview.total();

        assertEquals(0, expectedSubtotal.compareTo(actualSubtotal),
                "Subtotal must equal sum of item prices");
        assertEquals(0, actualSubtotal.add(actualTax).compareTo(actualTotal),
                "Total must equal Subtotal + Tax");
    }

    @Test
    @Tag("UI-15")
    @Tag("Edge")
    @Tag("P1")
    @Tag("Regression")
    @Story("Checkout Navigation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-15] Should return to cart page with items intact when canceling checkout at information step (Assumption A3)")
    void shouldNavigateBackToCartAndPreserveItemsWhenCancelingAtInformationStep() {
        CartPage cart = cartWith(User.STANDARD, Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        CartPage returnedCart = infoPage.cancel();

        assertThat(page).hasURL(Pattern.compile(".*/cart\\.html$"));
        assertThat(returnedCart.items()).hasCount(1);
        assertThat(returnedCart.itemNames()).hasText(new String[]{Product.BACKPACK.displayName()});
    }

    @Test
    @Tag("UI-16")
    @Tag("Boundary")
    @Tag("P1")
    @Tag("Regression")
    @Tag("Defect-A4")
    @Issue("A4")
    @Story("Empty Cart Boundary")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-16] Should document observed defect where system permits completing checkout with an empty cart (Defect A4)")
    void shouldVerifySystemPermitsCheckoutWhenCartIsEmpty() {
        loginAs(User.STANDARD);
        CartPage cart = new CartPage(page);
        page.navigate(CONFIG.getBaseUrl() + "/cart.html");

        // Verify cart is empty initially
        assertThat(cart.items()).hasCount(0);

        CheckoutInfoPage infoPage = cart.checkout();
        CheckoutOverviewPage overview = infoPage
                .fill(CheckoutInfo.valid())
                .continueToOverview();

        // Specific observed behavior: SauceDemo allows empty cart orders to complete
        CheckoutCompletePage complete = overview.finish();
        assertThat(complete.header()).hasText("Thank you for your order!");
        Allure.step("DISCOVERED DEFECT [A4]: SauceDemo allows placing order with empty cart (0 items, $0.00 total)");
    }

    @Test
    @Tag("UI-17")
    @Tag("Edge")
    @Tag("P1")
    @Tag("Regression")
    @Tag("Defect-A5")
    @Issue("A5")
    @Story("Whitespace Input Validation")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("[UI-17] Should document observed defect where whitespace-only fields bypass validation to overview page (Defect A5)")
    void shouldVerifyWhitespaceInputBypassesValidationWhenSubmitted() {
        CartPage cart = cartWith(User.STANDARD, Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        infoPage.fill(new CheckoutInfo("   ", "   ", "   "));
        infoPage.continueToOverview();

        // Specific observed behavior: Whitespace-only string bypasses client validation
        assertThat(page).hasURL(Pattern.compile(".*/checkout-step-two\\.html$"));
        Allure.step("DISCOVERED DEFECT [A5]: Whitespace-only strings ('   ') bypass required validation on checkout");
    }
}

