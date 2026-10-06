---
name: run-ui-tests
description: >-
  Commands, parameters, and runbooks for executing SauceDemo UI automation tests (Playwright + Java 17). Use this skill when the user asks to run UI tests, filter by test class or method, filter by tags, run defect showcase tests, inject user personas, run headed/headless, execute in parallel, or generate Allure single-file HTML reports.
---

# Run UI Automation Tests

When executing SauceDemo Playwright UI automation tests, use the automated PowerShell runner script `run-tests.ps1` or the Maven Wrapper `mvnw.cmd` located inside the `ui-tests/` directory.

---

## 1. Primary Execution Method: `run-tests.ps1`

The runner script `ui-tests/run-tests.ps1` manages runtime environment resolution, test execution, raw result archiving, and generates a self-contained Allure Single-File HTML report (`allure-report/index.html`).

> **Note:** Always execute from the `ui-tests/` directory:
> ```powershell
> cd ui-tests
> ```

### Standard Runs

- **Run All 21 Standard UI Tests (Headless, Default):**
  ```powershell
  cd ui-tests
  .\run-tests.ps1
  ```
  Runs all 21 regression scenarios headlessly across Chromium, archives results, generates the single-file Allure report, and opens it automatically in the default browser.

- **Run Headless without Auto-opening Browser (Ideal for CI/Background Tasks):**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -NoOpen
  ```

- **Run in Headed Mode (Browser Window Visible):**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Headed
  ```

---

### Environment Configuration & Switching

Switch environments dynamically (loads `src/test/resources/env/<env>.json`):

- **Run on Default Environment (`local` - points to real SauceDemo):**
  ```powershell
  cd ui-tests
  .\run-tests.ps1
  ```

- **Run on CI/CD Default (`dev`) or Remote Target Environments (`sit`, `staging`, `uat`):**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Env "dev"
  .\run-tests.ps1 -Env "sit"
  .\run-tests.ps1 -Env "staging"
  .\run-tests.ps1 -Env "uat"
  ```

- **Via Maven Wrapper CLI:**
  ```powershell
  cd ui-tests
  .\mvnw.cmd test -Denv=sit
  ```

---

### Filtering Tests

- **Run by Test Class:**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Test "LoginTest"
  .\run-tests.ps1 -Test "CheckoutTest"
  .\run-tests.ps1 -Test "CartTest"
  .\run-tests.ps1 -Test "PerformanceGlitchUserTest"
  .\run-tests.ps1 -Test "ProductDetailsTest"
  .\run-tests.ps1 -Test "CatalogTest"
  ```

- **Run Specific Test Method:**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Test "*#shouldCompleteOrderSuccessfullyWhenCheckingOutWithValidInformation"
  ```

- **Run by Priority / Category Tag:**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Tag "Smoke"
  .\run-tests.ps1 -Tag "Regression"
  .\run-tests.ps1 -Tag "P0"
  .\run-tests.ps1 -Tag "P1"
  .\run-tests.ps1 -Tag "Edge"
  .\run-tests.ps1 -Tag "Boundary"
  .\run-tests.ps1 -Tag "Resilience"
  ```

- **Exclude Specific Tags (via -ExcludeTag):**
  ```powershell
  cd ui-tests
  # Run regression tests excluding slow or performance-intensive tests
  .\run-tests.ps1 -Tag "Regression" -ExcludeTag "Resilience"

  # Run all tests excluding edge cases
  .\run-tests.ps1 -ExcludeTag "Edge"
  ```

---

### Defect Showcase & Diagnostic Tests

Tests demonstrating discovered SauceDemo bugs are tagged with `@Tag("NotRun")` and individual IDs (`DEF-01` to `DEF-08`). The runner automatically clears Surefire's exclusion filter when targeting these tags.

- **Run All 8 Defect Showcase Tests (All Fail as Expected):**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Tag "NotRun" -NoOpen
  ```

- **Run Specific Defect by ID:**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Tag "DEF-01"  # Broken Remove button on inventory
  .\run-tests.ps1 -Tag "DEF-02"  # Form input misrouting (Last Name overwriting First Name)
  .\run-tests.ps1 -Tag "DEF-03"  # Empty cart checkout allowed
  .\run-tests.ps1 -Tag "DEF-04"  # Whitespace-only bypasses input validation
  .\run-tests.ps1 -Tag "DEF-05"  # Broken product images (404 dog asset)
  .\run-tests.ps1 -Tag "DEF-06"  # Non-numeric postal code bypasses format validation
  .\run-tests.ps1 -Tag "DEF-07"  # Product title link misdirection
  .\run-tests.ps1 -Tag "DEF-08"  # Partial add-to-cart failure (limits to 3 items)
  ```

---

### User Persona Injection

Dynamically override the logged-in user persona without altering test code:

```powershell
cd ui-tests
# Injects problem_user into full catalog checkout to trigger failures and capture traces
.\run-tests.ps1 -Test "CheckoutTest#shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout" -User "problem_user"

# Injects performance_glitch_user
.\run-tests.ps1 -Test "CheckoutTest#shouldCompleteOrderSuccessfullyWhenCheckingOutWithValidInformation" -User "performance_glitch_user"
```

Available personas: `standard_user`, `problem_user`, `performance_glitch_user`, `locked_out_user`.

---

### Cross-Browser & Parallel Execution

- **Target Specific Browser:**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Browser "firefox"
  .\run-tests.ps1 -Browser "webkit"
  ```

- **Run Test Classes in Parallel Across Multiple JVM Forks:**
  ```powershell
  cd ui-tests
  .\run-tests.ps1 -Threads 3
  ```

---

### Report Generation Only

To regenerate or view the Allure Single-File HTML report from existing test results without re-executing tests:

```powershell
cd ui-tests
.\run-tests.ps1 -ReportOnly
```

The self-contained report is generated at:
`ui-tests/allure-report/index.html`

---

## 2. Direct Maven Wrapper Execution (`mvnw.cmd`)

If preferred, Maven commands can be run directly using the bundled wrapper:

- **Run All Standard Tests:**
  ```powershell
  cd ui-tests
  .\mvnw.cmd test
  ```

- **Run by Tag:**
  ```powershell
  cd ui-tests
  .\mvnw.cmd test -Dgroups="Smoke"
  .\mvnw.cmd test -Dgroups="P0"
  ```

- **Run Defect Tests via Maven:**
  ```powershell
  cd ui-tests
  .\mvnw.cmd test -Dgroups="NotRun" -DexcludedGroups=DummyGroupToClearExclusion
  ```

