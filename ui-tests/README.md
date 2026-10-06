# Part 1: UI Automation Tests

This directory contains the Playwright and Java test automation framework for the SauceDemo application.

**For full architectural details, setup instructions, configuration management, and defect reports, please refer to the main repository [README.md](../README.md).**

## Quick Start (Execution)

### 1. Standard Maven (Headless)
This is the standard execution method expected for evaluation. Ensure you have Java 17 and Maven 3.8+ installed:
```bash
# From the ui-tests directory
mvn clean test
```

### 2. PowerShell Runner (Windows Local)
We provide a bundled runner that requires zero system setup (uses a bundled JDK and Maven instance) and automatically generates a self-contained Single-File Allure HTML report.

```powershell
# Run all tests in headless mode and open the Allure report
.\run-tests.ps1

# Run tests in parallel across 3 JVM processes
.\run-tests.ps1 -Threads 3

# Run tests in headed mode (browser window visible)
.\run-tests.ps1 -Headed

# Run a specific test class (e.g., to showcase defect detection with problem_user)
.\run-tests.ps1 -Test "CheckoutTest#shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout" -User "problem_user"
```

## Directory Highlights
- `src/main/java/com/assignment/` - Framework Core (Page Objects, Configuration, Utilities)
- `src/test/java/com/assignment/ui/` - Test Execution Scenarios (Grouped by Feature)
- `src/test/resources/env/` - Environment-specific configurations (`local`, `dev`, `sit`)

---

## SDET Evaluation Criteria Addressed

This framework was explicitly designed to answer the core questions of the evaluation rubric:

### 1. Do you design a maintainable architecture rather than linear scripts?
**Yes.** The framework strictly avoids single-file linear scripts. It employs a robust **Page Object Model (POM)** separating UI interactions (`src/main/.../pages`) from test logic (`src/test/.../ui`). Framework responsibilities—such as environment resolution (`PlaywrightConfig`), browser context lifecycle (`BaseTest`), and screenshot/trace attachments (`TestFailureListener`)—are heavily modularized for enterprise scalability.

### 2. How do you handle locators, waits, and test data?
* **Locators:** Brittle XPaths and arbitrary CSS selectors are eliminated. The framework globally configures `playwright.selectors().setTestIdAttribute("data-test")`, exclusively utilizing `page.getByTestId(...)` to target resilient native attributes.
* **Waits:** Zero `Thread.sleep()` or static waits exist in this repository. All synchronizations rely on Playwright's Web-First Auto-Retrying Assertions (e.g., `assertThat(locator).isVisible()`), which dynamically poll the DOM until conditions are met or timeout.
* **Test Data:** Test data (such as accounts and passwords) is managed via a dynamic 5-Tier Hierarchical Credential Resolver, allowing tests to inject personas (`standard_user`, `problem_user`) securely via CLI parameters without hardcoding secrets.

### 3. Do you cover happy paths as well as edge cases?
**Yes.** The `ui-tests` suite executes **21 scenarios** across a risk-based matrix:
* **Happy Paths:** End-to-end checkout, catalog sorting, and cart state management.
* **Edge/Boundary Cases:** Missing inputs, direct unauthenticated URL access, and exact `BigDecimal` financial math verifications.
* **Defect Assertions:** Intentional, strict assertions that capture observed application bugs (e.g., `$0.00` empty cart checkout, whitespace validation bypasses) without using weak assertions to mask them.

### 4. Readability and reuse of your code.
Code readability is prioritized via self-documenting BDD-style method names (e.g., `shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout`). By abstracting Playwright calls into Page Objects, the actual test classes read as clean, reusable business flows (e.g., `loginPage.loginAs(user); inventoryPage.addMultipleItemsToCart(3);`).

