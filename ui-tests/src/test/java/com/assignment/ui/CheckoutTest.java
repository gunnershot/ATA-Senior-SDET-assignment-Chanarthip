package com.assignment.ui;

import com.assignment.base.BaseTest;

import com.assignment.models.CheckoutInfo;
import com.assignment.models.Product;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.assignment.pages.CartPage;
import com.assignment.pages.CheckoutCompletePage;
import com.assignment.pages.CheckoutInfoPage;
import com.assignment.pages.CheckoutOverviewPage;

import com.assignment.utils.PdfUtils;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Feature("Checkout & Order Integrity")
class CheckoutTest extends BaseTest {

    // =========================================================================
    // 1. Happy Path & End-to-End Order Flows
    // =========================================================================

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
        CartPage cart = cartWith(Product.BACKPACK);
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
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Price & Tax Integrity")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-13] Should dynamically calculate subtotal, 8% tax, and total when checking out entire product catalog (6 items)")
    void shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout() {
        CheckoutInfo customer = CheckoutInfo.valid();

        // 1. Add all 6 products in catalog to cart
        CartPage cart = cartWith(Product.values());
        assertThat(cart.items()).hasCount(Product.values().length);

        // 2. Proceed to Checkout Overview
        CheckoutOverviewPage overview = cart.checkout()
                .fill(customer)
                .continueToOverview();

        // 3. Validate all 6 products are rendered in overview
        assertThat(overview.items()).hasCount(Product.values().length);

        // 4. Dynamic Calculation: Sum expected subtotal from Product enum definitions
        BigDecimal expectedSubtotal = BigDecimal.ZERO;
        for (Product product : Product.values()) {
            expectedSubtotal = expectedSubtotal.add(product.price());
        }

        // 5. Dynamic Calculation: 8% Tax with RoundingMode.HALF_UP (standard SauceDemo tax algorithm)
        BigDecimal expectedTax = expectedSubtotal.multiply(new BigDecimal("0.08"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = expectedSubtotal.add(expectedTax);

        // 6. Assert UI overview prices match dynamic calculations
        BigDecimal actualSubtotal = overview.subtotal();
        BigDecimal actualTax = overview.tax();
        BigDecimal actualTotal = overview.total();

        assertEquals(0, expectedSubtotal.compareTo(actualSubtotal),
                "Subtotal on UI must exactly match dynamic sum of all 6 products ($" + expectedSubtotal + ")");
        assertEquals(0, expectedTax.compareTo(actualTax),
                "Tax on UI must match dynamic 8% tax calculation ($" + expectedTax + ")");
        assertEquals(0, expectedTotal.compareTo(actualTotal),
                "Total on UI must equal Subtotal + Tax ($" + expectedTotal + ")");

        // 7. Complete order and verify receipt PDF contains all 6 items and accurate dynamic totals
        CheckoutCompletePage complete = overview.finish();
        assertThat(complete.header()).hasText("Thank you for your order!");

        Path pdfFile = complete.downloadOrderPdf();
        assertNotNull(pdfFile, "Downloaded receipt PDF path must not be null");
        assertTrue(Files.exists(pdfFile), "Downloaded receipt PDF file must exist");

        // Attach PDF receipt to Allure report
        try (InputStream stream = Files.newInputStream(pdfFile)) {
            Allure.addAttachment("Full Catalog Order Receipt PDF", "application/pdf", stream, ".pdf");
        } catch (Exception ignored) {
        }

        String pdfText = PdfUtils.extractText(pdfFile);

        // Assert all 6 products are listed in the PDF receipt
        for (Product product : Product.values()) {
            assertTrue(pdfText.contains(product.displayName()),
                    "PDF receipt must list item: " + product.displayName());
        }

        // Assert customer shipping info and dynamic totals in PDF
        assertTrue(pdfText.contains(customer.firstName() + " " + customer.lastName()),
                "PDF receipt must contain customer full name: " + customer.firstName() + " " + customer.lastName());
        assertTrue(pdfText.contains(customer.postalCode()),
                "PDF receipt must contain customer postal code: " + customer.postalCode());
        assertTrue(pdfText.contains("Item total $" + expectedSubtotal),
                "PDF receipt must contain dynamic item total: $" + expectedSubtotal);
        assertTrue(pdfText.contains("Tax $" + expectedTax),
                "PDF receipt must contain dynamic tax: $" + expectedTax);
        assertTrue(pdfText.contains("Total $" + expectedTotal),
                "PDF receipt must contain dynamic grand total: $" + expectedTotal);
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
        CartPage cart = cartWith(Product.BACKPACK, Product.BIKE_LIGHT);

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

    // =========================================================================
    // 2. Order Confirmation & PDF Receipt Verification
    // =========================================================================

    @Test
    @Tag("UI-15")
    @Tag("P1")
    @Tag("Regression")
    @Story("Order Receipt PDF")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-15] Should generate and verify PDF order receipt contains accurate customer and order details upon checkout completion")
    void shouldGenerateAndVerifyOrderPdfReceiptUponCheckoutCompletion() {
        CheckoutInfo customer = CheckoutInfo.valid();
        CartPage cart = cartWith(Product.BACKPACK);
        CheckoutCompletePage complete = cart.checkout()
                .fill(customer)
                .continueToOverview()
                .finish();

        assertThat(complete.header()).hasText("Thank you for your order!");
        assertThat(complete.generatePdfButton()).isVisible();

        Path pdfFile = complete.downloadOrderPdf();
        assertNotNull(pdfFile, "Downloaded PDF path must not be null");
        assertTrue(Files.exists(pdfFile), "Downloaded PDF file must exist on disk");

        // Attach downloaded PDF to Allure report for full auditability
        try (InputStream stream = Files.newInputStream(pdfFile)) {
            Allure.addAttachment("Order Receipt PDF", "application/pdf", stream, ".pdf");
        } catch (Exception ignored) {
        }

        String pdfText = PdfUtils.extractText(pdfFile);

        // Verify Brand & Receipt Header
        assertTrue(pdfText.contains("Swag Labs"), "PDF receipt must contain brand 'Swag Labs'");
        assertTrue(pdfText.contains("Order Receipt"), "PDF receipt must contain title 'Order Receipt'");

        // Verify Customer Shipping Info (John Doe, 10110)
        assertTrue(pdfText.contains(customer.firstName() + " " + customer.lastName()),
                "PDF receipt must contain customer full name: " + customer.firstName() + " " + customer.lastName());
        assertTrue(pdfText.contains(customer.postalCode()),
                "PDF receipt must contain customer postal code: " + customer.postalCode());

        // Verify Item Details & Pricing
        assertTrue(pdfText.contains(Product.BACKPACK.displayName()),
                "PDF receipt must contain ordered product name: " + Product.BACKPACK.displayName());
        assertTrue(pdfText.contains("Item total $29.99"), "PDF receipt must contain correct item total ($29.99)");
        assertTrue(pdfText.contains("Tax $2.40"), "PDF receipt must contain correct tax ($2.40)");
        assertTrue(pdfText.contains("Total $32.39"), "PDF receipt must contain correct total ($32.39)");
        assertTrue(pdfText.contains("Thank you for your order!"), "PDF receipt must contain thank you message");
    }

    // =========================================================================
    // 3. Navigation & User Journey Controls
    // =========================================================================

    @Test
    @Tag("UI-16")
    @Tag("Edge")
    @Tag("P1")
    @Tag("Regression")
    @Story("Checkout Navigation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-16] Should return to cart page with items intact when canceling checkout at information step (Assumption A3)")
    void shouldNavigateBackToCartAndPreserveItemsWhenCancelingAtInformationStep() {
        CartPage cart = cartWith(Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        CartPage returnedCart = infoPage.cancel();

        assertThat(page).hasURL(Pattern.compile(".*/cart\\.html$"));
        assertThat(returnedCart.items()).hasCount(1);
        assertThat(returnedCart.itemNames()).hasText(new String[]{Product.BACKPACK.displayName()});
    }

    // =========================================================================
    // 4. Form Validation & Negative Scenarios
    // =========================================================================

    @Test
    @Tag("UI-17")
    @Tag("Negative")
    @Tag("P1")
    @Tag("Regression")
    @Story("Field Validation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-17] Should display specific field validation error when First Name, Last Name, or Postal Code is omitted")
    void shouldDisplayRequiredValidationErrorWhenAnyCheckoutFieldIsMissing() {
        CartPage cart = cartWith(Product.BACKPACK);
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

    // =========================================================================
    // 5. Observed Defects & Boundary Anomalies
    // =========================================================================

    @Test
    @Tag("DEF-03")
    @Tag("NotRun")
    @Tag("Boundary")
    @Tag("P1")
    @Tag("Defect-A4")
    @Issue("A4")
    @Story("Empty Cart Boundary")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[DEF-03] Should document observed defect where system permits completing checkout with an empty cart (Defect A4)")
    void shouldVerifySystemPermitsCheckoutWhenCartIsEmpty() {
        login();
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
    @Tag("DEF-04")
    @Tag("NotRun")
    @Tag("Edge")
    @Tag("P1")
    @Tag("Defect-A5")
    @Issue("A5")
    @Story("Whitespace Input Validation")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("[DEF-04] Should document observed defect where whitespace-only fields bypass validation to overview page (Defect A5)")
    void shouldVerifyWhitespaceInputBypassesValidationWhenSubmitted() {
        CartPage cart = cartWith(Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        infoPage.fill(new CheckoutInfo("   ", "   ", "   "));
        infoPage.continueToOverview();

        // Specific observed behavior: Whitespace-only string bypasses client validation
        assertThat(page).hasURL(Pattern.compile(".*/checkout-step-two\\.html$"));
        Allure.step("DISCOVERED DEFECT [A5]: Whitespace-only strings ('   ') bypass required validation on checkout");
    }

    @Test
    @Tag("DEF-06")
    @Tag("NotRun")
    @Tag("Edge")
    @Tag("P1")
    @Tag("Defect-A7")
    @Issue("A7")
    @Story("Postal Code Format Validation")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("[DEF-06] Should document observed defect where non-numeric postal code (\"ABCDE\") bypasses validation to overview page (Defect A7)")
    void shouldVerifyNonNumericPostalCodeBypassesValidationWhenSubmitted() {
        CartPage cart = cartWith(Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        infoPage.fill(new CheckoutInfo("John", "Doe", "ABCDE"));
        infoPage.continueToOverview();

        // Specific observed behavior: Non-numeric postal code string bypasses format validation
        assertThat(page).hasURL(Pattern.compile(".*/checkout-step-two\\.html$"));
        Allure.step("DISCOVERED DEFECT [A7]: Non-numeric postal code ('ABCDE') bypasses format validation and allows proceeding to checkout overview");
    }

    @Test
    @Tag("UI-21")
    @Tag("Edge")
    @Tag("P2")
    @Tag("Regression")
    @Story("Name Validation")
    @Severity(SeverityLevel.TRIVIAL)
    @DisplayName("[UI-21] Should allow special characters and numbers in First Name and Last Name given unspecified validation rules (Ambiguity)")
    void shouldAllowSpecialCharactersInNameFieldsGivenUnspecifiedValidationRules() {
        CartPage cart = cartWith(Product.BACKPACK);
        CheckoutInfoPage infoPage = cart.checkout();

        infoPage.fill(new CheckoutInfo("John123!@#", "Doe$%^987", "12345"));
        infoPage.continueToOverview();

        // Observed behavior: Special characters are permitted in both name fields. Marked as AMBIGUITY.
        assertThat(page).hasURL(Pattern.compile(".*/checkout-step-two\\.html$"));
        Allure.step("AMBIGUITY: Allowed character sets for First and Last Name are unspecified. System allows 'John123!@#' and 'Doe$%^987'.");
    }
}


