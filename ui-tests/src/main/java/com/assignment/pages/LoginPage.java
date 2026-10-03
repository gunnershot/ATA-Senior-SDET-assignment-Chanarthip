package com.assignment.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.assignment.models.User;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {

    private final Locator username;
    private final Locator password;
    private final Locator loginButton;
    private final Locator error;

    public LoginPage(Page page) {
        super(page);
        this.username = page.getByTestId("username");
        this.password = page.getByTestId("password");
        this.loginButton = page.getByTestId("login-button");
        this.error = page.getByTestId("error");
    }

    @Step("Open login page")
    public LoginPage open() {
        open("/");
        return this;
    }

    @Step("Open protected path {path} directly")
    public LoginPage openProtected(String path) {
        open(path);
        return this;
    }

    @Step("Log in as {user}")
    public InventoryPage loginAs(User user) {
        submit(user.username(), user.password());
        return new InventoryPage(page);
    }

    /** Submits the form as-is. A null value leaves that field empty. */
    @Step("Submit login credentials (username='{user}')")
    public LoginPage submit(String user, String pass) {
        if (user != null) username.fill(user);
        if (pass != null) password.fill(pass);
        loginButton.click();
        return this;
    }

    public Locator username() {
        return username;
    }

    public Locator password() {
        return password;
    }

    public Locator loginButton() {
        return loginButton;
    }

    public Locator error() {
        return error;
    }
}

