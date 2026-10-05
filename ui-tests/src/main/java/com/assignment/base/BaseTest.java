package com.assignment.base;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.assignment.config.PlaywrightConfig;
import com.assignment.models.Product;
import com.assignment.models.User;
import com.assignment.utils.TestFailureListener;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import com.assignment.pages.CartPage;
import com.assignment.pages.InventoryPage;
import com.assignment.pages.LoginPage;

/**
 * Isolated BaseTest managing Playwright browser and isolated browser context lifecycle.
 * - Each test class owns an isolated Browser instance.
 * - Fresh BrowserContext and Page per test ensures no cookies/state leakage between tests.
 * - Automatic failure capture (Full-page Screenshot + Playwright Trace) via TestFailureListener.
 * - Configured with official Playwright test-id attribute 'data-test'.
 */
@Epic("SauceDemo UI")
@ExtendWith(TestFailureListener.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    protected static final PlaywrightConfig CONFIG = PlaywrightConfig.get();

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    protected Page page;
    protected LoginPage loginPage;

    @BeforeAll
    void launchBrowser() {
        playwright = Playwright.create();
        // Official Playwright Best Practice: set test-id attribute to match SauceDemo data-test
        playwright.selectors().setTestIdAttribute("data-test");

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(CONFIG.isHeadless())
                .setSlowMo(CONFIG.getSlowMo());

        browser = switch (CONFIG.getBrowser().toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };

        PlaywrightAssertions.setDefaultAssertionTimeout(CONFIG.getDefaultTimeoutMs());
    }

    @BeforeEach
    void openFreshContext() {
        context = browser.newContext();
        context.setDefaultTimeout(CONFIG.getDefaultTimeoutMs());

        // Start tracing on the fresh context
        try {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        } catch (Exception ignored) {
        }

        page = context.newPage();
        loginPage = new LoginPage(page).open();
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception ignored) {
            }
            context = null;
        }
        page = null;
    }

    @AfterAll
    void closeBrowser() {
        if (playwright != null) {
            try {
                playwright.close(); // Closes browser and Playwright process cleanly
            } catch (Exception ignored) {
            }
            playwright = null;
            browser = null;
        }
    }

    public Page getPage() {
        return page;
    }

    public BrowserContext getContext() {
        return context;
    }

    // ---- Shared arrange helpers ----

    protected User defaultUser() {
        return CONFIG.getTargetUser();
    }

    protected InventoryPage login() {
        return loginAs(defaultUser());
    }

    protected InventoryPage loginAs(User user) {
        return loginPage.loginAs(user);
    }

    protected CartPage cartWith(Product... products) {
        return cartWith(defaultUser(), products);
    }

    protected CartPage cartWith(User user, Product... products) {
        InventoryPage inventory = loginAs(user);
        for (Product p : products) {
            inventory.add(p);
        }
        return inventory.openCart();
    }
}

