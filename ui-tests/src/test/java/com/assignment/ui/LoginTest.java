package com.assignment.ui;

import com.assignment.base.BaseTest;

import com.assignment.models.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.assignment.pages.InventoryPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Feature("Authentication & Access Control")
class LoginTest extends BaseTest {

    @Test
    @Tag("UI-01")
    @Tag("Happy")
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Standard Login")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-01] Should navigate to inventory catalog when standard_user logs in with valid credentials")
    void shouldDisplayInventoryWhenStandardUserLogsInWithValidCredentials() {
        InventoryPage inventory = loginAs(User.STANDARD);

        assertThat(page).hasURL(Pattern.compile(".*/inventory\\.html$"));
        assertThat(inventory.title()).hasText("Products");
        assertThat(inventory.inventoryList()).isVisible();
    }

    @Test
    @Tag("UI-02")
    @Tag("Negative")
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Locked Out User")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-02] Should reject login and display locked out error when locked_out_user attempts to log in")
    void shouldRejectLoginAndDisplayErrorMessageWhenUserIsLockedOut() {
        loginPage.submit(User.LOCKED_OUT.username(), User.LOCKED_OUT.password());

        assertThat(loginPage.error()).hasText("Epic sadface: Sorry, this user has been locked out.");
        assertThat(page).not().hasURL(Pattern.compile(".*/inventory\\.html$"));
    }

    @Test
    @Tag("UI-03")
    @Tag("Negative")
    @Tag("P0")
    @Tag("Regression")
    @Story("Invalid Credentials")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-03] Should reject login and display credentials mismatch error when standard_user enters incorrect password")
    void shouldRejectLoginAndDisplayMismatchErrorWhenPasswordIsInvalid() {
        loginPage.submit(User.STANDARD.username(), "wrong_password");

        assertThat(loginPage.error())
                .hasText("Epic sadface: Username and password do not match any user in this service");
        assertThat(page).not().hasURL(Pattern.compile(".*/inventory\\.html$"));
    }

    @Test
    @Tag("UI-04")
    @Tag("Boundary")
    @Tag("P1")
    @Tag("Regression")
    @Story("Form Validation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-04] Should display username required validation error when username field is left blank")
    void shouldDisplayRequiredErrorWhenUsernameIsOmitted() {
        loginPage.submit(null, User.STANDARD.password());

        assertThat(loginPage.error()).hasText("Epic sadface: Username is required");
    }

    @Test
    @Tag("UI-05")
    @Tag("Boundary")
    @Tag("P1")
    @Tag("Regression")
    @Story("Form Validation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-05] Should display password required validation error when password field is left blank")
    void shouldDisplayRequiredErrorWhenPasswordIsOmitted() {
        loginPage.submit(User.STANDARD.username(), null);

        assertThat(loginPage.error()).hasText("Epic sadface: Password is required");
    }

    @Test
    @Tag("UI-06")
    @Tag("Edge")
    @Tag("P0")
    @Tag("Regression")
    @Story("Access Control")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("[UI-06] Should prevent direct inventory access and redirect with error when user is unauthenticated (Assumption A1)")
    void shouldPreventAccessAndRedirectToLoginWhenVisitingInventoryWithoutAuthentication() {
        loginPage.openProtected("/inventory.html");

        assertThat(loginPage.error())
                .hasText("Epic sadface: You can only access '/inventory.html' when you are logged in.");
        assertThat(loginPage.loginButton()).isVisible();
    }
}



