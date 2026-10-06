# ATA Senior SDET Assignment — Chanarthip

Enterprise-grade Test Automation Framework for **Part 1: UI Automation** on [SauceDemo](https://www.saucedemo.com), engineered with **Playwright for Java 1.46.0**, **JUnit 5 Jupiter**, **Maven**, and **Allure Report** (Single-File HTML).

---

## Table of Contents

- [Executive Summary & Part 1 Compliance](#executive-summary--part-1-compliance)
- [Framework Architecture & Directory Layout](#framework-architecture--directory-layout)
- [Prerequisites & Environment Setup](#prerequisites--environment-setup)
- [Configuration Management](#configuration-management)
- [Test Execution Guide](#test-execution-guide)
  - [Standard CLI Execution (Evaluator / CI)](#1-standard-cli-execution-evaluator--ci)
  - [Automated PowerShell Runner (Local Windows)](#2-automated-powershell-runner-local-windows)
- [Allure Single-File HTML Report](#allure-single-file-html-report)
- [Test Coverage & Scenario Matrix](#test-coverage--scenario-matrix)
- [Observed Application Defects & Diagnostic Strategy](#observed-application-defects--diagnostic-strategy)
- [Senior SDET Architectural Decisions](#senior-sdet-architectural-decisions)
- [Assumptions & Surfaced Ambiguities](#assumptions--surfaced-ambiguities)
- [Part 3: CI/CD Pipeline Fixes & Extensions](#part-3-cicd-pipeline-fixes--extensions)
- [Part 4: AI-Assisted Workflow Notes](#part-4-ai-assisted-workflow-notes)

---

## Executive Summary & Part 1 Compliance

> **Note for Part 2:** Please see [api-tests/README.md](api-tests/README.md) for instructions on running the API automation suite.

This repository satisfies all requirements established in **Part 1: UI automation (Playwright and Java)** of the `SDET-QA-Assignment.docx` specification:

| Evaluation Criterion | Implementation Details | Status |
|---|---|:---:|
| **Official Playwright Library** | Utilizes official `com.microsoft.playwright:playwright` (v1.46.0) with Java 17 and JUnit 5. | Verified |
| **Submission Command Standard** | Fully executable via `(cd ui-tests && mvn clean test)` out-of-the-box in headless mode. | Verified |
| **Account Coverage** | Complete end-to-end and boundary coverage for all four mandated accounts: `standard_user`, `locked_out_user`, `problem_user`, and `performance_glitch_user`. | Verified |
| **Negative Scenarios** | Multiple negative scenarios covering locked-out rejection, bad credentials, missing fields, and direct URL access. | Verified |
| **Strict Defect Assertions** | Zero `Thread.sleep`, zero broad retries, zero soft assertions papering over bugs. Explicit assertions target observed defect behavior with `@Issue` and `@Tag("Diagnostic")`. | Verified |
| **Clean Lifecycle & Isolation** | `@TestInstance(PER_CLASS)` with fresh, isolated `BrowserContext` and `Page` created in `@BeforeEach` to prevent state leakage. | Verified |
| **Resilient Locators** | Configured with `playwright.selectors().setTestIdAttribute("data-test")` invoking `page.getByTestId(...)`. | Verified |

---

## Framework Architecture & Directory Layout

Following industry-standard modular separation of concerns, framework infrastructure code resides under `src/main/java/com/assignment`, while test suites reside under `src/test/java/com/assignment`:

```
ATA-Senior-SDET-assignment-Chanarthip/
├── test-coverage/                          # Part 0: Test design & scenario matrix
│   ├── scenarios.md                        # Complete 20-scenario specification matrix
│   ├── assumptions.md                      # Engineering assumptions and surfaced ambiguities
│   └── findings.md                         # Discovered defect reports and diagnostic strategy
├── ui-tests/                               # Part 1: UI Automation (Playwright + Java)
│   ├── jdk-17.0.10+7/                      # Bundled OpenJDK 17 (Zero machine setup required)
│   ├── apache-maven-3.9.6/                 # Bundled Apache Maven 3.9.6
│   ├── run-tests.ps1                       # Automated Test Runner & Allure Single-File Generator
│   ├── pom.xml                             # Dependencies & Allure plugins
│   ├── .env                                # Local secrets (Git-ignored)
│   ├── src/
│   │   ├── main/java/com/assignment/       # Core Framework Layer
│   │   │   ├── base/
│   │   │   │   └── BaseTest.java           # Playwright Context, Tracing, and Lifecycle Management
│   │   │   ├── config/
│   │   │   │   ├── PlaywrightConfig.java   # JSON & CLI Override Config Loader
│   │   │   │   ├── Credentials.java        # Env & Dotenv Secret Reader
│   │   │   │   └── AppConfig.java          # Backward-compatibility facade
│   │   │   ├── models/
│   │   │   │   ├── User.java               # Accounts Enum (Standard, LockedOut, Problem, Glitch)
│   │   │   │   ├── Product.java            # Products & Data-Test Slugs Enum
│   │   │   │   └── CheckoutInfo.java       # Immutable Form Record
│   │   │   ├── pages/                      # Page Object Model (POM)
│   │   │   │   ├── BasePage.java           # Shared Page Object base with getByTestId locators
│   │   │   │   ├── LoginPage.java
│   │   │   │   ├── InventoryPage.java
│   │   │   │   ├── CartPage.java
│   │   │   │   ├── CheckoutInfoPage.java
│   │   │   │   ├── CheckoutOverviewPage.java
│   │   │   │   └── CheckoutCompletePage.java
│   │   │   └── utils/
│   │   │       ├── TestFailureListener.java# JUnit 5 TestWatcher (Captures Screenshot & Trace on failure)
│   │   │       ├── KnownDefect.java        # Annotation for intentional defect assertions
│   │   │       └── KnownDefectExtension.java
│   │   └── test/
│   │       ├── java/com/assignment/ui/     # Test Execution Layer
│   │       │   ├── LoginTest.java          # UI-01 to UI-06 (Happy, Negative, Boundary, Edge)
│   │       │   ├── CartTest.java           # UI-07 to UI-11 (Add, Remove, State Retention)
│   │       │   ├── CheckoutTest.java       # UI-12 to UI-17, UI-21, DEF-03, DEF-04, DEF-06
│   │       │   ├── PerformanceGlitchUserTest.java # UI-18 (Resilience & E2E Checkout Flow)
│   │       │   ├── ProductDetailsTest.java # UI-19 (Navigation & Product Details)
│   │       │   ├── CatalogTest.java        # UI-20 (Catalog Sorting Ambiguity)
│   │       │   └── ProblemUserTest.java    # DEF-01, DEF-02, DEF-05, DEF-07, DEF-08 (Defect Showcase)
│   │       └── resources/
│   │           ├── playwright.json         # Environment Settings (Default: headless = true)
│   │           └── junit-platform.properties# JUnit 5 Execution settings
│   ├── allure-results/                     # Raw Allure event files
│   ├── allure-report/                      # Portable Single-File HTML Report
│   └── allure-archive/                     # Historical timestamped run archives
├── api-tests/                              # Part 2: REST Assured API Suite
├── .github/workflows/                      # Part 3: CI/CD Pipeline
└── README.md
```

---

## Prerequisites & Environment Setup

### 1. Zero Machine Dependency (Windows Local)
The repository includes a bundled **OpenJDK 17** (`jdk-17.0.10+7`) and **Apache Maven 3.9.6** within `ui-tests/`. No system environment variable configuration is necessary when executing via `run-tests.ps1`.

If executing via standard system CLI, ensure Java 17+ and Maven 3.8+ are installed.

### 2. Browser Binaries Installation
Playwright automatically downloads browser binaries on its initial run. To pre-install or update browser binaries explicitly:
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

### 3. Credentials Configuration
The application password is read via `Credentials.java`:
1. **Local `.env` file (Ignored in `.gitignore`):**
   ```env
   SAUCE_PASSWORD=secret_sauce
   ```
2. **Environment Variable (Standard for CI pipelines):**
   ```bash
   export SAUCE_PASSWORD=secret_sauce
   ```
*(Fallback default `secret_sauce` is embedded safely for headless evaluation runs).*

---

## Configuration Management

Runtime parameters are centralized in `ui-tests/src/test/resources/playwright.json`:
```json
{
  "browser": "chromium",
  "headless": true,
  "slowMo": 0,
  "baseUrl": "https://www.saucedemo.com",
  "defaultTimeoutMs": 10000,
  "performanceTimeoutMs": 10000
}
```

### Dynamic CLI Overrides
Any setting in `playwright.json` can be dynamically overridden from the command line without editing source files:
```bash
mvn test -Dheadless=false -Dbrowser=firefox
```

---

## Test Execution Guide

Navigate to `ui-tests/` directory:
```bash
cd ui-tests
```

### 1. Standard CLI Execution (Evaluator / CI)
To run the full suite headlessly as required by the assignment specification:

#### Using Maven Wrapper (No global Maven installation required)
- **Windows (PowerShell / CMD):**
  ```powershell
  .\mvnw.cmd clean test
  ```
- **macOS / Linux:**
  ```bash
  ./mvnw clean test
  ```

#### Using Global Maven (if installed)
```bash
mvn clean test
```

To run a specific test class or tag:
```bash
mvn test -Dtest=LoginTest
mvn test -Dgroups="P0"
```

### 2. Parallel Test Execution

Both the UI and API suites are designed with parallel execution capabilities to accelerate test feedback loops:

#### A. UI Tests (Playwright + Maven Surefire Process Forking)
In Playwright UI tests, each test class (`LoginTest`, `CartTest`, `CheckoutTest`, etc.) controls an isolated browser lifecycle. To ensure 100% thread and process isolation without browser window contention or memory leaks, parallel execution is handled via Maven Surefire process forks (`-DforkCount=<N>`):

- **Windows (Maven Wrapper):**
  ```powershell
  # Run 3 test classes in parallel across 3 JVM processes
  .\mvnw.cmd test -DforkCount=3
  ```
- **macOS / Linux (Maven Wrapper):**
  ```bash
  ./mvnw test -DforkCount=3
  ```
- **Windows Automated PowerShell Runner:**
  ```powershell
  .\run-tests.ps1 -Threads 3
  ```

#### B. API Tests (REST Assured + JUnit 5 Concurrency)
API tests run concurrently using JUnit 5 native threading. Tests are completely thread-safe due to dynamic test data generation (DataFaker) and isolated user teardown:

- **Windows (Maven Wrapper):**
  ```powershell
  cd ..\api-tests
  .\mvnw.cmd test -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
  ```
- **macOS / Linux (Maven Wrapper):**
  ```bash
  cd ../api-tests
  ./mvnw test -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
  ```

#### C. CI/CD Parallel Execution (GitHub Actions)
In the GitHub Actions CI pipeline (`.github/workflows/ci.yml`), tests can be triggered manually via **Run workflow** (`workflow_dispatch`), allowing you to specify the `threads` parameter (e.g. `2`, `3`, `4`). The pipeline dynamically injects:
- `-DforkCount=${threads}` for UI tests
- `-Djunit.jupiter.execution.parallel.config.fixed.parallelism=${threads}` for API tests

---

### 3. Automated PowerShell Runner (Local Windows)
The included `run-tests.ps1` runner automatically wires the bundled JDK/Maven, executes tests, archives raw results, and builds the self-contained Single-File Allure Report:

```powershell
# Run all 22 Scenarios (Headless by default) + auto-generate Allure report
.\run-tests.ps1

# Run in parallel across 3 JVM fork processes
.\run-tests.ps1 -Threads 3

# Run with browser window visible (Headed mode)
.\run-tests.ps1 -Headed

# Clean build before test execution
.\run-tests.ps1 -Clean

# Run a specific test class on a specific browser (e.g. chromium, firefox, webkit)
.\run-tests.ps1 -Test "LoginTest" -Browser "firefox" -Headed

# Run by priority or category tag (e.g. P0, P1, Happy, Negative, Diagnostic)
.\run-tests.ps1 -Tag "P0"

# Exclude specific tags (e.g. exclude Edge cases or slow tests)
.\run-tests.ps1 -Tag "Regression" -ExcludeTag "Edge"

# Dynamic User Injection (Showcase defect detection by injecting problem_user)
.\run-tests.ps1 -Test "CheckoutTest#shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout" -User "problem_user"

# Execute headlessly without launching the HTML report in a browser window (Ideal for automated scripts)
.\run-tests.ps1 -NoOpen
```

---

### 4. Dynamic User Injection & Defect Showcase (Evaluation / Demonstration)

The test framework supports runtime **User Persona Injection** without touching test source code:
- **Default Execution:** Test scenarios utilize `standard_user` loaded from `playwright.json` (`"targetUser": "standard_user"`).
- **Runtime Injection via CLI:** Inject any user persona dynamically via `-User <username>` (PowerShell) or `-Duser=<username>` (Maven CLI). Supported personas: `standard_user`, `problem_user`, `performance_glitch_user`, `locked_out_user`.

#### Defect Showcase Scenario (`UI-13` with `problem_user`):
To showcase that our test suite actively catches application defects, halts flawed checkout flows, and attaches rich diagnostics (Full-page Screenshots and Playwright Traces) to Allure reports:

```powershell
# Step 1: Run UI-13 injecting problem_user (Demonstrates defect detection)
.\run-tests.ps1 -Test "CheckoutTest#shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout" -User "problem_user"

# Step 2: The test cleanly fails because problem_user cannot add all 6 items to the cart.
# Allure generates an interactive report with screenshot and trace attached.

# Step 3: Rerun with standard_user to demonstrate the passing baseline
.\run-tests.ps1 -Test "CheckoutTest#shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout" -User "standard_user"
```

---

## Allure Single-File HTML Report

The suite produces a self-contained, single-file HTML report:
```
ui-tests/allure-report/index.html
```

- **Zero Web Server Requirement:** Unlike standard Allure reports that require `allure serve` to bypass CORS restrictions, this single-file report can be opened directly in Google Chrome, Microsoft Edge, or Firefox (`file:///...`).
- **Failure Artifacts:** When a test fails, `TestFailureListener` automatically attaches a **Full-Page Screenshot** and a **Playwright Trace (.zip)** to the corresponding Allure test step. Traces can be dragged and dropped directly into [trace.playwright.dev](https://trace.playwright.dev) for DOM-level replay.
- **Regenerate Report Manually:**
  ```powershell
  .\run-tests.ps1 -ReportOnly
  ```

---

## Test Coverage & Scenario Matrix

The suite covers **21 automated scenarios** across 6 feature classes:

| ID | Class | Method | Category | Priority | Expected Outcome | Result |
|---|---|---|---|:---:|---|:---:|
| **UI-01** | `LoginTest` | `shouldDisplayInventoryWhenStandardUserLogsInWithValidCredentials` | Happy | P0 | Inventory catalog renders 6 products upon valid login. | ✅ PASS |
| **UI-02** | `LoginTest` | `shouldRejectLoginAndDisplayErrorMessageWhenUserIsLockedOut` | Negative | P0 | Rejection displays: `Epic sadface: Sorry, this user has been locked out.` | ✅ PASS |
| **UI-03** | `LoginTest` | `shouldRejectLoginAndDisplayMismatchErrorWhenPasswordIsInvalid` | Negative | P0 | Rejection displays: `Epic sadface: Username and password do not match...` | ✅ PASS |
| **UI-04** | `LoginTest` | `shouldDisplayRequiredErrorWhenUsernameIsOmitted` | Boundary | P1 | Validates required username: `Epic sadface: Username is required`. | ✅ PASS |
| **UI-05** | `LoginTest` | `shouldDisplayRequiredErrorWhenPasswordIsOmitted` | Boundary | P1 | Validates required password: `Epic sadface: Password is required`. | ✅ PASS |
| **UI-06** | `LoginTest` | `shouldPreventAccessAndRedirectToLoginWhenVisitingInventoryWithoutAuthentication` | Edge | P0 | Direct access to `/inventory.html` redirects to `/` with error notice. | ✅ PASS |
| **UI-07** | `CartTest` | `shouldIncrementBadgeAndDisplayItemInCartWhenSingleItemIsAdded` | Happy | P0 | Adding 1 item toggles button to Remove, sets cart badge to `1`, and asserts catalog description. | ✅ PASS |
| **UI-08** | `CartTest` | `shouldIncrementBadgeToTwoAndListDistinctItemsWhenAddingTwoDifferentItems` | Boundary | P0 | Adding 2 distinct items increments badge to `2` without item duplication. | ✅ PASS |
| **UI-09** | `CartTest` | `shouldRemoveItemFromCartAndClearBadgeWhenRemovedFromInventoryPage` | Happy | P1 | Clicking Remove on inventory page removes badge from DOM and empties cart. | ✅ PASS |
| **UI-10** | `CartTest` | `shouldEmptyCartAndHideBadgeWhenRemovingLastItemFromCartPage` | Boundary | P1 | Removing the last item in `/cart.html` clears the badge and renders cart empty. | ✅ PASS |
| **UI-11** | `CartTest` | `shouldRetainCartStateWhenPageIsRefreshed` | Edge | P1 | Session persistence: cart items and badge persist across page reload (`F5`). | ✅ PASS |
| **UI-12** | `CheckoutTest` | `shouldCompleteOrderSuccessfullyWhenCheckingOutWithValidInformation` | E2E | P0 | Full checkout journey ends with `Thank you for your order!`. | ✅ PASS |
| **UI-13** | `CheckoutTest` | `shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout` | Integrity / E2E | P0 | Adds all 6 catalog items, verifies dynamic subtotal ($129.94), 8% tax ($10.40), total ($140.34) and checks order PDF receipt. | ✅ PASS |
| **UI-14** | `CheckoutTest` | `shouldCalculateSubtotalAndTotalAccuratelyWhenCheckingOutTwoItems` | Integrity | P0 | Exact arithmetic validation: Subtotal ($39.98) + Tax ($3.20) = Total ($43.18). | ✅ PASS |
| **UI-15** | `CheckoutTest` | `shouldGenerateAndVerifyOrderPdfReceiptUponCheckoutCompletion` | Integration / PDF | P1 | Validates PDF generation, downloads receipt via Playwright, parses text via PDFBox, and asserts customer details, items, and totals match order. | ✅ PASS |
| **UI-16** | `CheckoutTest` | `shouldNavigateBackToCartAndPreserveItemsWhenCancelingAtInformationStep` | Edge | P1 | Canceling checkout information step returns user to `/cart.html` with cart items intact. | ✅ PASS |
| **UI-17** | `CheckoutTest` | `shouldDisplayRequiredValidationErrorWhenAnyCheckoutFieldIsMissing` | Negative | P1 | Validates First Name, Last Name, and Postal Code sequentially. | ✅ PASS |
| **UI-18** | `PerformanceGlitchUserTest` | `shouldCompleteFullCheckoutFlowWithinSlaThresholdWhenPerformanceGlitchUserLogsIn` | Resilience / E2E | P1 | Validates that delayed login completes within 10-second SLA limit, adds product to cart, completes checkout, and receives order confirmation. | ✅ PASS |
| **UI-19** | `ProductDetailsTest` | `shouldDisplayAccurateProductDetailsWhenClickingItemNameFromInventory` | Happy | P0 | Clicking product title opens `/inventory-item.html?id=4` and accurately displays Name, Description, and Price. | ✅ PASS |
| **UI-20** | `CatalogTest` | `shouldSortProductsDescendingByNameWhenZToAIsSelected` | Happy | P1 | Sorting Z-A correctly reorders catalog (Highlights algorithm ambiguity). | ✅ PASS |
| **UI-21** | `CheckoutTest` | `shouldAllowSpecialCharactersInNameFieldsGivenUnspecifiedValidationRules` | Edge | P2 | System permits special characters in First and Last Name (Highlights validation ambiguity). | ✅ PASS |

---

## Observed Application Defects & Diagnostic Strategy

The assignment brief instructs:
> *"Write assertions that are specific to the behaviour you observed. Do not use broad retries, Thread.sleep or weak assertions to paper over a defect."*

In accordance with this directive, defects are neither ignored nor masked. Complete Jira-format bug reports and root cause analyses are documented in [`test-coverage/findings.md`](test-coverage/findings.md). Strict assertions verify the exact anomalous state across 8 discovered defects:

1. **Defect DEF-01 (DEF-01 - Broken Remove Button on Cart):** On `problem_user`, clicking "Remove" fails silently; badge remains permanently stuck at `"1"`.
2. **Defect DEF-02 (DEF-02 - Form Input Misrouting):** On `problem_user`, typing into `lastName` overwrites `firstName`, leaving `lastName` blank and triggering an `Error: Last Name is required`.
3. **Defect DEF-03 (DEF-03 - Empty Cart Checkout Allowed):** SauceDemo permits advancing through Overview to Complete ($0.00 total) without empty cart validation. Tagged with `@Issue("A4")`.
4. **Defect DEF-04 (DEF-04 - Whitespace Validation Bypass):** Whitespace (`"   "`) bypasses validation without `.trim()`, advancing user directly to Overview. Tagged with `@Issue("A5")`.
5. **Defect DEF-05 (DEF-05 - Broken Product Image Links):** On `problem_user`, all catalog product images point to the 404 dog error placeholder asset (`sl-404.168b1cce.jpg`).
6. **Defect DEF-06 (DEF-06 - Non-Numeric Postal Code Accepted):** Non-numeric strings (e.g. `"ABCDE"`) bypass format validation and successfully navigate to Overview. Tagged with `@Issue("A7")`.
7. **Defect DEF-07 (DEF-07 - Product Title Link Misdirection):** On `problem_user`, clicking the link for "Sauce Labs Backpack" incorrectly opens the details page of "Sauce Labs Fleece Jacket".
8. **Defect DEF-08 (DEF-08 - Partial Add-to-Cart Failure):** On `problem_user`, only 3 of the 6 items can be added to the cart; the remaining items fail to respond to click events.

### Defect Showcase (Observed Defects & Diagnostics)

This table isolates the explicit assertion tests designed to capture application defects across both standard_user and problem_user.

| ID | Class | Method | Category | Priority | Expected Outcome | Result |
|---|---|---|---|:---:|---|:---:|
| **DEF-03** | CheckoutTest | shouldVerifySystemPermitsCheckoutWhenCartIsEmpty | Defect / Boundary | P1 | **Observed Defect A4 / DEF-03:** Application allows $0.00 checkout on empty cart. | ✅ PASS (Defect Asserted) |
| **DEF-04** | CheckoutTest | shouldVerifyWhitespaceInputBypassesValidationWhenSubmitted | Defect / Edge | P1 | **Observed Defect A5 / DEF-04:** Whitespace bypasses required field check. | ✅ PASS (Defect Asserted) |
| **DEF-01** | ProblemUserTest | shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem | Diagnostic | P1 | **DEF-01:** Remove button event listener fails to update DOM/state. | ✅ PASS (Defect Asserted) |
| **DEF-02** | ProblemUserTest | shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation | Diagnostic | P1 | **DEF-02:** Form input routing miswired (lastName overwrites firstName). | ✅ PASS (Defect Asserted) |
| **DEF-06** | CheckoutTest | shouldVerifyNonNumericPostalCodeBypassesValidationWhenSubmitted | Defect / Edge | P1 | **Observed Defect A7 / DEF-06:** Non-numeric postal code bypasses format validation. | ✅ PASS (Defect Asserted) |
| **DEF-05** | ProblemUserTest | shouldDocumentBrokenProductImagesWhenProblemUserViewsCatalog | Diagnostic | P2 | **DEF-05:** Product card images are broken (404 fallback). | ✅ PASS (Defect Asserted) |
| **DEF-07** | ProblemUserTest | shouldDocumentWrongProductNavigationWhenProblemUserClicksItemName | Diagnostic | P0 | **DEF-07:** Clicking item erroneously navigates to wrong details page. | ✅ PASS (Defect Asserted) |
| **DEF-08** | ProblemUserTest | shouldDocumentAddLimitDefectWhenProblemUserAttemptsToAddAllCatalogItems | Diagnostic | P1 | **DEF-08:** Add to cart fails after 3 items on problem_user. | ✅ PASS (Defect Asserted) |


---

## Senior SDET Architectural Decisions

1. **Thread-Safe Isolated Context Lifecycle:**
   - Test classes utilize `@TestInstance(Lifecycle.PER_CLASS)` to maintain a single browser process per class.
   - Each test method receives a newly created `BrowserContext` and `Page` in `@BeforeEach`, ensuring 100% cookie and cache isolation with zero cross-test state pollution.
2. **Web-First Auto-Waiting & Auto-Retrying Assertions:**
   - Avoided JUnit 5 `assertTrue(...)` on static element states.
   - Replaced with Playwright web-first assertions (`assertThat(locator)...`), which automatically poll until DOM elements satisfy expected conditions or time out.
3. **Official Data-Test Test-ID Locators:**
   - Set `playwright.selectors().setTestIdAttribute("data-test")` globally in `BaseTest`.
   - All Page Objects locate interactive components via `page.getByTestId(...)` rather than fragile CSS classes or XPath hierarchies.
4. **High-Precision Financial Math:**
   - In `CheckoutOverviewPage` (UI-14), currency calculations strictly employ `java.math.BigDecimal` with `RoundingMode.HALF_UP` to prevent IEEE 754 floating-point drift (e.g., `0.1 + 0.2 != 0.3`).
5. **Automatic Screenshot & Trace on Assertion Failure:**
   - Implemented via `AfterTestExecutionCallback` in `TestFailureListener` to guarantee capture immediately when an assertion fails before `@AfterEach` context teardown.
   - Automatically captures full-page screenshot saved to `target/screenshots/<testName>-failure.png` and attaches PNG screenshot + Playwright trace ZIP directly to the Allure report.
   - On passing tests, traces are cleanly stopped without writing to disk, minimizing CI storage footprint.

6. **API Layered Architecture & Reusability (Part 2):**
   - **Base Layer (`BaseApiTest`):** Centralizes RestAssured configuration, environment variables, and authentication (`GOREST_API_TOKEN`). Test methods never deal with raw token injection.
   - **Client Layer (`UserClient`):** Encapsulates HTTP routing and methods (GET, POST, PUT, DELETE) so test classes remain clean and expressive.
   - **Data/Model Layer:** Replaces raw JSON strings with Jackson POJOs (`UserRequest`, `UserResponse`) for strict type safety and field-level assertions.
7. **Dynamic API Test Data & Idempotent Teardown:**
   - **DataFaker:** Every test dynamically generates unique user payloads (Names, Emails) to prevent collision in parallel execution.
   - **State Isolation:** All created user IDs are tracked in a thread-safe list. The `@AfterEach` lifecycle hook iterates and issues `DELETE` requests to purge the environment, ensuring the database remains clean (Idempotency).

---

## Assumptions & Surfaced Ambiguities

Documented comprehensively in [`test-coverage/assumptions.md`](test-coverage/assumptions.md):
- **Assumption A1 (Access Control):** Direct navigation to `/inventory.html` without session state must redirect to `/` with an explicit error.
- **Assumption A2 (Latency SLA):** `performance_glitch_user` has an acceptable non-functional latency threshold of 10,000 ms.
- **Assumption A3 (Tax Calculation):** Tax rate is approximately 8%, calculated via `BigDecimal`.
- **Assumption A4 (Empty Cart Checkout):** Classified as an observed application defect rather than intentional behavior.
- **Assumption A5 (Whitespace Sanitization):** Form inputs lack `.trim()` sanitization, classified as an input validation defect.
- **Assumption A6 (State Persistence):** Cart persistence across page reload (`F5`) is client-side and verified as an edge case.
- **Assumption A7 (Postal Code Validation):** Arbitrary alphanumeric characters bypass validation, treated as an input validation defect.
- **Assumption A8 (Multi-Environment & Secret Management):** Multi-environment (`local`, `sit`, `staging`, `uat`) architecture with dynamic resolution; zero committed secrets by resolving passwords via environment variables.

---

## Part 2: API Automation Tests

The API test suite (pi-tests) validates the GoRest API using **REST Assured** and **JUnit 5**, focusing on robust HTTP client architecture and dynamic state management.

### Directory Structure
`
api-tests/
├── src/
│   ├── main/java/com/assignment/api/
│   │   ├── base/
│   │   │   └── BaseApiTest.java        # RestAssured config, token injection, Auth management
│   │   ├── clients/
│   │   │   └── UserClient.java         # API Routing (GET, POST, PUT, DELETE) & HTTP abstraction
│   │   ├── models/
│   │   │   ├── UserRequest.java        # Jackson POJO for request payloads
│   │   │   └── UserResponse.java       # Jackson POJO for response mapping and assertions
│   │   └── utils/
│   │       └── DataFaker.java          # Dynamic test data generation (Faker)
│   └── test/java/com/assignment/api/
│       └── UserCrudTest.java           # Comprehensive CRUD operations & boundary coverage
`

### Key Architectural Characteristics
1. **Model-Driven Payloads:** Raw JSON strings are completely avoided. We use Jackson POJOs (UserRequest, UserResponse) to enforce strict type safety and structured assertions.
2. **Client Abstraction:** Test classes like UserCrudTest never execute raw HTTP calls. They invoke declarative methods from UserClient (e.g. client.createUser(payload)).
3. **Idempotency & Isolation:** 
   - Uses DataFaker to generate unique email addresses dynamically, preventing database collision during parallel test runs.
   - All tests track created entities in a thread-safe list. The @AfterEach lifecycle hook iterates and issues DELETE requests to purge the environment, ensuring tests do not leak state or pollute the API database.

## Part 3: CI/CD Pipeline Fixes & Extensions

The `starter-kit/ci-broken.yml` file contained several issues that prevented the pipeline from running correctly and reliably reporting test results. The following bugs were identified and fixed:

1. **Missing Code Checkout:** The starter pipeline lacked `actions/checkout@v4`, causing the runner to fail immediately with missing project files.
   * *Fix:* Added `actions/checkout@v4` as the initial step in each test job.
2. **Incompatible Java Version:** The starter pipeline used Java 8, whereas both test suites rely on modern Java 17 features.
   * *Fix:* Upgraded `actions/setup-java@v4` to `java-version: '17'`.
3. **Missing Working Directories:** Maven commands were executed at repository root rather than targeting `ui-tests` and `api-tests` modules.
   * *Fix:* Configured `defaults.run.working-directory` explicitly for each job.
4. **Masked UI Test Failures:** The UI test command included `|| true`, suppressing legitimate test failures and falsely greening builds.
   * *Fix:* Removed `|| true` to ensure builds fail reliably when assertions fail.
5. **System Maven Dependency:** Relied on global `mvn` which may not match wrapper versions.
   * *Fix:* Standardized on `./mvnw` across both UI and API test jobs.
6. **Missing Playwright OS Dependencies:** Headless browser execution failed on Linux runners due to missing browser binaries and OS-level libraries.
   * *Fix:* Added `./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"` step.
7. **Playwright Headless Override:** Local default runs headfully (`headless: false` in `playwright.json`), which crashes on Linux runners without an X display.
   * *Fix:* Passed `-Dheadless=true` dynamically during CI UI test execution.
8. **Missing API Secrets:** API test execution was missing the `GOREST_API_TOKEN` environment variable.
   * *Fix:* Mapped `GOREST_API_TOKEN: ${{ secrets.GOREST_API_TOKEN }}` via GitHub Secrets.
9. **Allure Result Directory Misalignment:** UI tests saved raw results to project base instead of `target/allure-results`.
   * *Fix:* Standardized `ui-tests/pom.xml` to output to `${project.build.directory}/allure-results`.
10. **Pipeline Parameterization & Parallel Execution:** Extended workflow with `workflow_dispatch` inputs supporting suite selection (`all`, `ui`, `api`), tag filtering, environment configuration, parallel execution (`threads`), and dynamic Log4j2 verbosity toggles.

---

## Part 4: AI-Assisted Workflow Notes

- **AI Tools & Collaborative Design:**  
  I utilized Google Antigravity with the interactive `/grill-me` design interview workflow as an architectural sparring partner. Instead of using AI as a blind code generator, I leveraged `/grill-me` to systematically evaluate trade-offs, plan the clean separation between framework infrastructure (`src/main/java/com/assignment`) and test suites (`src/test/java/com/assignment/ui`), refine Playwright locator strategies (`getByTestId`), and establish balanced JUnit 5 tag boundaries (`Smoke` vs `Regression`).

- **Concrete Failure & Verification (Calibrated Trust):**  
  While executing a structural refactor to rename packages in bulk, the AI assistant generated a batch PowerShell string-replacement script with an uninitialized variable (`$newContent`), which wiped the Java files down to 0 bytes. Because I practice strict verification and run continuous sanity builds, I detected the empty build immediately (`0 tests executed`). Rather than re-prompting blindly, I took manual command: I audited the session transcripts to extract the latest verified source code revisions, restored the repository, and verified every file with targeted test runs. This concrete failure reinforced why Senior SDETs must treat all AI outputs as unverified drafts.

- **What Was Deliberately Not Delegated:**  
  I deliberately did not delegate the core test design thinking, equivalence partitioning, or the defect assertion strategy. AI tools exhibit a strong bias toward generating naive happy paths, adding arbitrary `Thread.sleep` to paper over timing issues, or using weak assertions to make tests pass green. Designing strict, non-flaky assertions for defective states—such as `problem_user`'s broken remove button and SauceDemo's empty-cart checkout allowance—required intentional human engineering judgment.





