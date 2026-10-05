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
│   │       │   ├── CheckoutTest.java       # UI-12 to UI-17 (E2E, Integrity, Validation, Defect)
│   │       │   ├── ProblemUserTest.java    # UI-18, UI-19 (Diagnostic Defect Assertions)
│   │       │   └── PerformanceGlitchUserTest.java # UI-20 (Resilience & Latency SLA)
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
```bash
mvn clean test
```

To run a specific test class or tag:
```bash
mvn test -Dtest=LoginTest
mvn test -Dgroups="P0"
```

### 2. Automated PowerShell Runner (Local Windows)
The included `run-tests.ps1` runner automatically wires the bundled JDK/Maven, executes tests, archives raw results, and builds the self-contained Single-File Allure Report:

```powershell
# Run all 20 Scenarios (Headless by default) + auto-generate Allure report
.\run-tests.ps1

# Run with browser window visible (Headed mode)
.\run-tests.ps1 -Headed

# Clean build before test execution
.\run-tests.ps1 -Clean

# Run a specific test class on a specific browser (e.g. chromium, firefox, webkit)
.\run-tests.ps1 -Test "LoginTest" -Browser "firefox" -Headed

# Run by priority or category tag (e.g. P0, P1, Happy, Negative, Diagnostic)
.\run-tests.ps1 -Tag "P0"

# Generate report from latest results without re-executing tests
.\run-tests.ps1 -ReportOnly

# Execute headlessly without launching the HTML report in a browser window (Ideal for automated scripts)
.\run-tests.ps1 -NoOpen
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

The suite covers **20 automated scenarios** across 5 feature classes:

| ID | Class | Method | Category | Priority | Expected Outcome | Result |
|---|---|---|---|:---:|---|:---:|
| **UI-01** | `LoginTest` | `shouldDisplayInventoryWhenStandardUserLogsInWithValidCredentials` | Happy | P0 | Inventory catalog renders 6 products upon valid login. | ✅ PASS |
| **UI-02** | `LoginTest` | `shouldRejectLoginAndDisplayErrorMessageWhenUserIsLockedOut` | Negative | P0 | Rejection displays: `Epic sadface: Sorry, this user has been locked out.` | ✅ PASS |
| **UI-03** | `LoginTest` | `shouldRejectLoginAndDisplayMismatchErrorWhenPasswordIsInvalid` | Negative | P0 | Rejection displays: `Epic sadface: Username and password do not match...` | ✅ PASS |
| **UI-04** | `LoginTest` | `shouldDisplayRequiredErrorWhenUsernameIsOmitted` | Boundary | P1 | Validates required username: `Epic sadface: Username is required`. | ✅ PASS |
| **UI-05** | `LoginTest` | `shouldDisplayRequiredErrorWhenPasswordIsOmitted` | Boundary | P1 | Validates required password: `Epic sadface: Password is required`. | ✅ PASS |
| **UI-06** | `LoginTest` | `shouldPreventAccessAndRedirectToLoginWhenVisitingInventoryWithoutAuthentication` | Edge | P0 | Direct access to `/inventory.html` redirects to `/` with error notice. | ✅ PASS |
| **UI-07** | `CartTest` | `shouldUpdateBadgeAndCartWhenAddingOneItem` | Happy | P0 | Adding 1 item toggles button to Remove and sets cart badge to `1`. | ✅ PASS |
| **UI-08** | `CartTest` | `shouldUpdateBadgeWhenAddingTwoDifferentItems` | Boundary | P0 | Adding 2 distinct items increments badge to `2`. | ✅ PASS |
| **UI-09** | `CartTest` | `shouldRemoveItemFromInventoryAndClearBadge` | Happy | P1 | Clicking Remove on inventory page removes badge from DOM. | ✅ PASS |
| **UI-10** | `CartTest` | `shouldRemoveLastItemInCartAndClearBadge` | Boundary | P1 | Removing the last item in `/cart.html` clears the badge. | ✅ PASS |
| **UI-11** | `CartTest` | `shouldPersistCartItemsAfterPageRefresh` | Edge | P1 | Session persistence: cart items and badge persist across page reload (`F5`). | ✅ PASS |
| **UI-12** | `CheckoutTest` | `shouldCompleteCheckoutWithFullInformation` | E2E | P0 | Full checkout journey ends with `Thank you for your order!`. | ✅ PASS |
| **UI-13** | `CheckoutTest` | `shouldEnforceRequiredFieldsAtCheckoutInfo` | Negative | P1 | Validates First Name, Last Name, and Postal Code sequentially. | ✅ PASS |
| **UI-14** | `CheckoutTest` | `shouldCalculateAccurateTotalForMultipleItems` | Integrity | P0 | Exact arithmetic validation: Subtotal ($39.98) + Tax ($3.20) = Total ($43.18). | ✅ PASS |
| **UI-15** | `CheckoutTest` | `shouldReturnToCartWhenCancelingCheckoutInfo` | Edge | P1 | Canceling checkout information step returns user to `/cart.html` safely. | ✅ PASS |
| **UI-16** | `CheckoutTest` | `shouldPreventCheckoutWithEmptyCart` | Defect / Boundary | P1 | **Observed Defect A4:** Application allows $0.00 checkout on empty cart. | ✅ PASS (Defect Asserted) |
| **UI-17** | `CheckoutTest` | `shouldEnforceWhitespaceValidationAtCheckoutInfo` | Defect / Edge | P1 | **Observed Defect A5:** Whitespace (`"   "`) bypasses required field check. | ✅ PASS (Defect Asserted) |
| **UI-18** | `ProblemUserTest` | `shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem` | Diagnostic | P1 | **Observed Defect:** `problem_user` Remove button fails to decrement badge. | ✅ PASS (Defect Asserted) |
| **UI-19** | `ProblemUserTest` | `shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation` | Diagnostic | P1 | **Observed Defect:** `problem_user` Last Name input routes into First Name field. | ✅ PASS (Defect Asserted) |
| **UI-20** | `PerformanceGlitchUserTest` | `shouldCompleteLoginWithinSlaThresholdWhenPerformanceGlitchUserLogsIn` | Resilience | P1 | Validates that delayed login completes within 10-second SLA limit. | ✅ PASS |

---

## Observed Application Defects & Diagnostic Strategy

The assignment brief instructs:
> *"Write assertions that are specific to the behaviour you observed. Do not use broad retries, Thread.sleep or weak assertions to paper over a defect."*

In accordance with this directive, defects are neither ignored nor masked. Strict assertions verify the exact anomalous state, accompanied by Allure diagnostic logs:

1. **Defect UI-16 (Empty Cart Checkout Allowed):**
   - *Expected:* Checkout should be blocked if the cart contains 0 items.
   - *Observed & Asserted:* SauceDemo permits advancing through Overview to Complete ($0.00 total) without validation. Asserted strictly and tagged with `@Issue("A4")`.
2. **Defect UI-17 (Whitespace Validation Bypass):**
   - *Expected:* Pure whitespace (`"   "`) in required fields should trigger validation errors.
   - *Observed & Asserted:* Whitespace bypasses validation and navigates to the Overview screen. Asserted and tagged with `@Issue("A5")`.
3. **Defect UI-18 (`problem_user` Remove Button Defect):**
   - *Expected:* Clicking "Remove" must remove the item and clear the cart badge.
   - *Observed & Asserted:* On `problem_user`, the remove click handler fails silently; the badge remains permanently stuck at `"1"`.
4. **Defect UI-19 (`problem_user` Input Misrouting):**
   - *Expected:* First Name and Last Name inputs must accept independent values.
   - *Observed & Asserted:* On `problem_user`, typing into `lastName` overwrites `firstName`, leaving `lastName` blank and triggering an `Error: Last Name is required`.

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
5. **Dynamic Tracing and Artifact Capture:**
   - Tracing starts dynamically in `@BeforeEach`. `TestFailureListener` captures and stores the trace only when a failure occurs, optimizing disk usage while preserving granular debugging data.

---

## Assumptions & Surfaced Ambiguities

Documented comprehensively in [`test-coverage/assumptions.md`](test-coverage/assumptions.md):
- **Assumption A1 (Access Control):** Direct navigation to `/inventory.html` without session state must redirect to `/` with an explicit error.
- **Assumption A2 (Latency SLA):** `performance_glitch_user` has an acceptable non-functional latency threshold of 10,000 ms.
- **Assumption A3 (Tax Calculation):** Tax rate is approximately 8%, calculated via `BigDecimal`.
- **Assumption A4 (Empty Cart Checkout):** Classified as an observed application defect rather than intentional behavior.
- **Assumption A5 (Whitespace Sanitization):** Form inputs lack `.trim()` sanitization, classified as an input validation defect.
- **Assumption A6 (State Persistence):** Cart persistence across page reload (`F5`) is client-side and verified as an edge case.

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


