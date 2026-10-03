package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.assignment.models.CheckoutInfo;
import io.qameta.allure.Step;

/**
 * Checkout step one: "Your Information".
 */
public class CheckoutInfoPage extends BasePage {

    private final Locator firstName;
    private final Locator lastName;
    private final Locator postalCode;
    private final Locator continueButton;
    private final Locator cancelButton;
    private final Locator error;

    public CheckoutInfoPage(Page page) {
        super(page);
        this.firstName = page.getByTestId("firstName");
        this.lastName = page.getByTestId("lastName");
        this.postalCode = page.getByTestId("postalCode");
        this.continueButton = page.getByTestId("continue");
        this.cancelButton = page.getByTestId("cancel");
        this.error = page.getByTestId("error");
    }

    /** Types each non-null field. null leaves the field empty, used for required-field tests. */
    @Step("Fill checkout information {info}")
    public CheckoutInfoPage fill(CheckoutInfo info) {
        if (info.firstName() != null) firstName.fill(info.firstName());
        if (info.lastName() != null) lastName.fill(info.lastName());
        if (info.postalCode() != null) postalCode.fill(info.postalCode());
        return this;
    }

    @Step("Click Continue")
    public CheckoutOverviewPage continueToOverview() {
        continueButton.click();
        return new CheckoutOverviewPage(page);
    }

    @Step("Click Cancel")
    public CartPage cancel() {
        cancelButton.click();
        return new CartPage(page);
    }

    public Locator firstName() { return firstName; }
    public Locator lastName() { return lastName; }
    public Locator postalCode() { return postalCode; }
    public Locator continueButton() { return continueButton; }
    public Locator error() { return error; }
}

