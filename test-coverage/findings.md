# Discovered Defects & Senior SDET Diagnostic Strategy

This document details all application defects discovered during exploratory testing and automated verification of [SauceDemo](https://www.saucedemo.com). In strict accordance with the `SDET-QA-Assignment.docx` directive:

> *"Write assertions that are specific to the behaviour you observed. Do not use broad retries, Thread.sleep or weak assertions to paper over a defect."*

All defects are categorized, strictly asserted in automated suites, and documented with complete steps to reproduce, root cause analysis, and evidence artifacts.

---

## 1. Summary Defect Matrix

| Defect ID | Feature Area | Affected User | Defect Summary | Severity | Priority | Automated Test Scenario |
|---|---|---|---|:---:|:---:|---|
| **DEF-01** | Cart | `problem_user` | Inventory "Remove" button fails to mutate DOM; cart badge remains permanently stuck at "1". | Critical | P0 | `ProblemUserTest#shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem` (UI-20) |
| **DEF-02** | Checkout | `problem_user` | Checkout form input miswired: typing into "Last Name" field overwrites "First Name" and leaves Last Name empty. | Blocker | P0 | `ProblemUserTest#shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation` (UI-21) |
| **DEF-03** | Checkout | `standard_user` | System permits completing full checkout and order placement with an empty cart ($0.00 total). | Major | P1 | `CheckoutTest#shouldVerifySystemPermitsCheckoutWhenCartIsEmpty` (UI-18) |
| **DEF-04** | Checkout | `standard_user` | Purely whitespace input (`"   "`) bypasses required field validation and advances to overview screen. | Minor | P2 | `CheckoutTest#shouldVerifyWhitespaceInputBypassesValidationWhenSubmitted` (UI-19) |
| **DEF-05** | Catalog | `problem_user` | Catalog product image assets are broken and render fallback 404 dog image (`sl-404.168b1cce.jpg`). | Minor | P2 | `ProblemUserTest#shouldDocumentBrokenProductImagesWhenProblemUserViewsCatalog` (UI-25) |
| **DEF-06** | Checkout | `standard_user` | Non-numeric postal code input (`"ABCDE"`) bypasses format validation and advances to overview screen. | Minor | P2 | `CheckoutTest#shouldVerifyNonNumericPostalCodeBypassesValidationWhenSubmitted` (UI-24) |
| **DEF-07** | Catalog | `problem_user` | Clicking product title link redirects to an incorrect product details page (e.g. Backpack opens Fleece Jacket). | Critical | P0 | `ProblemUserTest#shouldDocumentWrongProductNavigationWhenProblemUserClicksItemName` (UI-26) |
| **DEF-08** | Catalog | `problem_user` | Add-to-cart failure limit: attempting to add all 6 catalog items only successfully adds 3 items to the cart. | Major | P1 | `ProblemUserTest#shouldDocumentAddLimitDefectWhenProblemUserAttemptsToAddAllCatalogItems` (UI-27) |

---

## 2. Detailed Defect Reports (Jira Format)

---

### [DEF-01] Inventory "Remove" Button Event Listener Fails to Remove Item
- **Defect ID:** `DEF-01` (`SAUCE-PROBLEM-USER-01`)
- **Severity:** Critical (Blocks basic cart mutation)
- **Priority:** P0
- **Affected User:** `problem_user`
- **Environment:** `https://www.saucedemo.com/inventory.html`
- **Preconditions:** User is logged in as `problem_user`.
- **Steps to Reproduce:**
  1. Login with `problem_user` / `secret_sauce`.
  2. Click "Add to cart" on `Sauce Labs Backpack`.
  3. Verify cart badge increments to `"1"`.
  4. Click "Remove" button on `Sauce Labs Backpack`.
- **Expected Result:**
  - Item button toggles back to "Add to cart".
  - Shopping cart badge is removed from DOM (hidden).
- **Actual Result:**
  - The click handler on the Remove button fails silently.
  - The cart badge remains permanently visible with text `"1"`.
- **Evidence / Failure Screenshot:** `target/screenshots/shouldRemoveItemFromCartAndClearBadgeWhenRemovedFromInventoryPage-failure.png`
- **Automated Verification:** `ProblemUserTest#shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem` (UI-20)
- **Root Cause Analysis:** Event handler attached to `remove-sauce-labs-backpack` is missing or lacks the state dispatch call to decrement cart state in `problem_user` bundle.

---

### [DEF-02] Checkout Form Input Binding Routing Miswired Between First and Last Name
- **Defect ID:** `DEF-02` (`SAUCE-PROBLEM-USER-02`)
- **Severity:** Blocker (Completely halts checkout funnel)
- **Priority:** P0
- **Affected User:** `problem_user`
- **Environment:** `https://www.saucedemo.com/checkout-step-one.html`
- **Preconditions:** User is logged in as `problem_user` with at least 1 item in cart.
- **Steps to Reproduce:**
  1. Navigate to `/cart.html` and click "Checkout".
  2. In the "First Name" field, enter `"John"`.
  3. In the "Last Name" field, enter `"Doe"`.
  4. In the "Zip/Postal Code" field, enter `"12345"`.
  5. Click "Continue".
- **Expected Result:**
  - `firstName` retains `"John"`, `lastName` retains `"Doe"`.
  - Form submits successfully and user navigates to `/checkout-step-two.html`.
- **Actual Result:**
  - Typing into the `lastName` input fires an onChange event that updates the `firstName` DOM element value to `"Doe"`.
  - The `lastName` input remains completely empty (`""`).
  - Clicking "Continue" triggers validation rejection: `Error: Last Name is required`.
- **Evidence / Failure Screenshot:** `target/screenshots/shouldDisplayRequiredValidationErrorWhenAnyCheckoutFieldIsMissing-failure.png`
- **Automated Verification:** `ProblemUserTest#shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation` (UI-21)
- **Root Cause Analysis:** Inverted React/DOM state binding: the `name="lastName"` element's onChange handler updates state key `firstName` instead of `lastName`.

---

### [DEF-03] System Permits Zero-Item ($0.00) Order Placement with Empty Cart
- **Defect ID:** `DEF-03` (`Defect-A4`)
- **Severity:** Major (Business logic anomaly)
- **Priority:** P1
- **Affected User:** `standard_user`, all users
- **Environment:** `https://www.saucedemo.com/cart.html`
- **Preconditions:** User is authenticated and cart is empty (0 items).
- **Steps to Reproduce:**
  1. Login as `standard_user`.
  2. Navigate directly to `/cart.html` with no items added.
  3. Click "Checkout".
  4. Fill valid customer information (e.g. John Doe, 12345) and click "Continue".
  5. On Overview page, observe Item Total: $0.00, Tax: $0.00, Total: $0.00.
  6. Click "Finish".
- **Expected Result:**
  - "Checkout" button should either be disabled when cart is empty, or navigating to `/checkout-step-one.html` should show an error: `"Cart is empty"`.
- **Actual Result:**
  - The application allows advancing through all checkout steps and places an order for $0.00 displaying `Thank you for your order!`.
- **Automated Verification:** `CheckoutTest#shouldVerifySystemPermitsCheckoutWhenCartIsEmpty` (UI-18)
- **Root Cause Analysis:** Lack of cart item count guard condition on `/cart.html` and backend checkout initialization endpoint.

---

### [DEF-04] Whitespace-Only Input Bypasses Required Field Validation
- **Defect ID:** `DEF-04` (`Defect-A5`)
- **Severity:** Minor (Input sanitization omission)
- **Priority:** P2
- **Affected User:** `standard_user`
- **Environment:** `https://www.saucedemo.com/checkout-step-one.html`
- **Preconditions:** User is on checkout step one with an item in cart.
- **Steps to Reproduce:**
  1. Add an item to cart and proceed to `/checkout-step-one.html`.
  2. In "First Name", enter `"   "` (3 whitespace spaces).
  3. In "Last Name", enter `"   "`.
  4. In "Zip/Postal Code", enter `"   "`.
  5. Click "Continue".
- **Expected Result:**
  - Form validation trims whitespace and rejects inputs with `Error: First Name is required`.
- **Actual Result:**
  - Whitespace-only strings satisfy the `if (value)` check without `.trim()`, advancing the user directly to `/checkout-step-two.html`.
- **Automated Verification:** `CheckoutTest#shouldVerifyWhitespaceInputBypassesValidationWhenSubmitted` (UI-19)
- **Root Cause Analysis:** Form validation relies on simple falsy string check `!field` rather than checking trimmed length `!field.trim()`.

---

### [DEF-05] Catalog Product Images Fail to Load and Display Fallback 404 Dog Image
- **Defect ID:** `DEF-05` (`SAUCE-PROBLEM-USER-03`)
- **Severity:** Minor (UI cosmetic defect)
- **Priority:** P2
- **Affected User:** `problem_user`
- **Environment:** `https://www.saucedemo.com/inventory.html`
- **Preconditions:** User is logged in as `problem_user`.
- **Steps to Reproduce:**
  1. Login with `problem_user` / `secret_sauce`.
  2. Inspect the product catalog cards on `/inventory.html`.
- **Expected Result:**
  - Each product card renders its corresponding product image (e.g. `sauce-backpack-1200x1500.0a0b85a3.jpg`).
- **Actual Result:**
  - Every catalog card points its `src` attribute to the fallback error asset: `/static/media/sl-404.168b1cce.jpg` (dog image).
- **Automated Verification:** `ProblemUserTest#shouldDocumentBrokenProductImagesWhenProblemUserViewsCatalog` (UI-25)
- **Root Cause Analysis:** Product data model array for `problem_user` overrides `imgUrl` properties with the 404 placeholder URL.

---

### [DEF-06] Non-Numeric Zip/Postal Code Bypasses Format Validation
- **Defect ID:** `DEF-06` (`Defect-A7`)
- **Severity:** Minor (Format validation omission)
- **Priority:** P2
- **Affected User:** `standard_user`
- **Environment:** `https://www.saucedemo.com/checkout-step-one.html`
- **Preconditions:** User is on checkout step one with an item in cart.
- **Steps to Reproduce:**
  1. Add an item to cart and proceed to `/checkout-step-one.html`.
  2. In "First Name", enter `"John"`.
  3. In "Last Name", enter `"Doe"`.
  4. In "Zip/Postal Code", enter `"ABCDE"` (pure alphabetic string).
  5. Click "Continue".
- **Expected Result:**
  - System enforces numeric/zip code format regex and prompts `Error: Postal Code must be numeric` or similar.
- **Actual Result:**
  - Form accepts arbitrary non-numeric text and proceeds to Overview (`/checkout-step-two.html`).
- **Automated Verification:** `CheckoutTest#shouldVerifyNonNumericPostalCodeBypassesValidationWhenSubmitted` (UI-24)
- **Root Cause Analysis:** Absence of regex pattern validation (`/^[0-9]+$/`) or HTML5 input `pattern` attribute on the Postal Code input.

---

### [DEF-07] Catalog Product Title Link Navigates to Wrong Product Details Page
- **Defect ID:** `DEF-07` (`SAUCE-PROBLEM-USER-04`)
- **Severity:** Critical (Navigation & Data Integrity failure)
- **Priority:** P0
- **Affected User:** `problem_user`
- **Environment:** `https://www.saucedemo.com/inventory.html`
- **Preconditions:** User is logged in as `problem_user`.
- **Steps to Reproduce:**
  1. Login as `problem_user`.
  2. Click on the product name `"Sauce Labs Backpack"` (`id=4`).
- **Expected Result:**
  - Navigates to `/inventory-item.html?id=4` displaying "Sauce Labs Backpack" details ($29.99).
- **Actual Result:**
  - System routes to `/inventory-item.html?id=5`, rendering "Sauce Labs Fleece Jacket" ($49.99) instead of the Backpack.
- **Evidence / Failure Screenshot:** `target/screenshots/shouldDisplayAccurateProductDetailsWhenClickingItemNameFromInventory-failure.png`
- **Automated Verification:** `ProblemUserTest#shouldDocumentWrongProductNavigationWhenProblemUserClicksItemName` (UI-26)
- **Root Cause Analysis:** Hardcoded or corrupted href anchor link binding on catalog cards for `problem_user`.

---

### [DEF-08] Catalog Add-to-Cart Action Fails to Add More Than 3 Distinct Items
- **Defect ID:** `DEF-08` (`SAUCE-PROBLEM-USER-05`)
- **Severity:** Major (Limits purchase capacity)
- **Priority:** P1
- **Affected User:** `problem_user`
- **Environment:** `https://www.saucedemo.com/inventory.html`
- **Preconditions:** User is logged in as `problem_user`.
- **Steps to Reproduce:**
  1. Login as `problem_user`.
  2. Sequentially click "Add to cart" on all 6 catalog items.
  3. Observe shopping cart badge and navigate to `/cart.html`.
- **Expected Result:**
  - Shopping cart badge displays `"6"`, and `/cart.html` lists all 6 items.
- **Actual Result:**
  - Only 3 items can be added to the cart; the remaining 3 "Add to cart" buttons fail to toggle. Cart badge is capped at `"3"`.
- **Evidence / Failure Screenshot:** `target/screenshots/shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout-failure.png`
- **Automated Verification:** `ProblemUserTest#shouldDocumentAddLimitDefectWhenProblemUserAttemptsToAddAllCatalogItems` (UI-27)
- **Root Cause Analysis:** Event handlers for items 4-6 in the catalog array for `problem_user` are disconnected or throw silent exceptions.

---

## 3. Senior SDET Diagnostic Strategy

### Guideline Adherence
> *"Write assertions that are specific to the behaviour you observed. Do not use broad retries, Thread.sleep or weak assertions to paper over a defect."*

Instead of masking defects with soft assertions, skips, or arbitrary delays, our test architecture follows two complementary approaches:

1. **Explicit Diagnostic Test Suite (`ProblemUserTest` & Defect Tests):**
   - Scenarios `UI-18`, `UI-19`, `UI-20`, `UI-21`, `UI-24`, `UI-25`, `UI-26`, `UI-27` strictly assert the **exact anomalous state** (e.g. badge remaining `"1"`, `firstName` taking the value of `lastName`, or navigation opening the Fleece Jacket).
   - Tagged with `@Tag("Diagnostic")`, `@Issue`, and annotated with detailed Allure step explanations.
2. **Dynamic Runtime Persona Injection (`-User problem_user`):**
   - When regression suites (`CartTest`, `CheckoutTest`, `ProductDetailsTest`) run with `-User problem_user`, standard business assertions immediately trip and fail on the defects.
   - `TestFailureListener` (via `AfterTestExecutionCallback`) automatically captures full-page PNG screenshots and full Playwright trace ZIP archives to provide developers with instant root-cause diagnostics.
