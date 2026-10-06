# Engineering Assumptions & Ambiguities

As outlined in the assignment brief:
> *"You cannot ask clarifying questions while you work. If something is unclear, make a reasonable assumption, carry on, and record the assumption and its impact in your README."*

This document catalogs all assumptions, surfaced ambiguities, and their corresponding impacts on test design and automation architecture.

---

## 1. Catalog of Assumptions & Ambiguities

### [Assumption A1] Session Access Control & Direct URL Navigation
- **Ambiguity:** The specification does not define how unauthenticated requests to protected pages (such as `/inventory.html`, `/cart.html`, or `/checkout-step-one.html`) should be rejected (HTTP 401/403 vs client-side redirect).
- **Observed Behavior:** Navigating directly to `/inventory.html` redirects back to `/` and displays `Epic sadface: You can only access '/inventory.html' when you are logged in.`.
- **Engineering Assumption:** Any direct navigation to a protected URL without a valid session cookie must result in an error notice on the login page and must never leak catalog or user data.
- **Test Implementation:** Covered in `LoginTest#shouldPreventAccessAndRedirectToLoginWhenVisitingInventoryWithoutAuthentication`.

---

### [Assumption A2] Performance Glitch SLA & Latency Tolerances
- **Ambiguity:** `performance_glitch_user` exhibits artificial server-side delay on login. The prompt does not specify the maximum acceptable latency before a login is treated as failed.
- **Observed Behavior:** The request for `/inventory.html` consistently takes between 4,800 ms and 5,500 ms to complete.
- **Engineering Assumption:** We establish a non-functional SLA threshold of 10,000 ms (10 seconds). The test must not use arbitrary `Thread.sleep`; instead, it must measure duration dynamically and assert that the transition occurs strictly within the 10-second SLA limit.
- **Test Implementation:** Configured via `performanceTimeoutMs: 10000` in `playwright.json` and asserted in `PerformanceGlitchUserTest#shouldCompleteLoginWithinSlaThresholdWhenPerformanceGlitchUserLogsIn`.

---

### [Assumption A3] Financial Calculation & Tax Precision (Rounding Rule)
- **Ambiguity:** The tax calculation formula and rounding rule (e.g., rounding up vs. truncating) are not explicitly stated on SauceDemo.
- **Observed Behavior:** Tax is fixed at 8% of the item total. Crucially, the system rounds decimals using standard **HALF_UP** rules (ปัดขึ้นตั้งแต่เลข 5). 
  - *Example:* Adding Fleece Jacket ($49.99): `49.99 * 0.08 = 3.9992`. The system rounds this to **$4.00**, making the Total **$53.99** (`49.99 + 4.00`).
- **Engineering Assumption:** Automation must not use standard 64-bit IEEE 754 floating-point `double` arithmetic (`0.1 + 0.2 != 0.3`) for financial assertions. All price subtotals, tax validations, and grand totals must be parsed and calculated using `java.math.BigDecimal` with `RoundingMode.HALF_UP` to match the application's underlying logic.
- **Test Implementation:** Implemented in `CheckoutOverviewPage` and verified in `CheckoutTest#shouldCalculateAccurateSubtotalAndTaxForFullCatalogCheckout` (UI-13, UI-14).

---

### [Assumption A4] Empty Cart Checkout Handling (Application Defect)
- **Ambiguity:** Standard e-commerce requirements require disabling or blocking checkout when the shopping cart contains 0 items.
- **Observed Behavior:** SauceDemo permits users with an empty cart to proceed through all checkout steps, calculating $0.00 subtotal, $0.00 tax, and completing the order with "Thank you for your order!".
- **Engineering Assumption:** Per the assignment guideline (*"Write assertions that are specific to the behaviour you observed. Do not use broad retries, Thread.sleep or weak assertions to paper over a defect"*), this is classified as an observed functional defect. Rather than writing a weak assertion or skipping the test, we strictly assert that the application allows empty checkout, tagging it with `@Tag("Defect")` and `@Issue("A4")`.
- **Test Implementation:** Documented in `CheckoutTest#shouldPreventCheckoutWithEmptyCart`.

---

### [Assumption A5] Whitespace Input Sanitization (Validation Defect)
- **Ambiguity:** The checkout information form fields require First Name, Last Name, and Postal Code. It is not specified whether string values are trimmed before validation.
- **Observed Behavior:** Entering whitespace strings (e.g. `"   "`) bypasses the required field check and advances the user to the overview page.
- **Engineering Assumption:** In modern web applications, purely whitespace input should be rejected as invalid. We treat this as an input sanitization defect and assert the actual behavior with `@Tag("Defect")` and `@Issue("A5")`.
- **Test Implementation:** Documented in `CheckoutTest#shouldEnforceWhitespaceValidationAtCheckoutInfo`.

---

### [Assumption A6] State Persistence across Navigation and Reload
- **Ambiguity:** The specification does not detail whether cart items are stored in memory, cookies, `localStorage`, or backend database.
- **Observed Behavior:** Cart state is persisted in client storage across page refreshes (`F5`), but does not synchronize across separate browser profiles.
- **Engineering Assumption:** User sessions must maintain state within the same `BrowserContext` across page reloads, but different tests must run in isolated `BrowserContext` instances to guarantee zero state leakage.
- **Test Implementation:** Verified in `CartTest#shouldPersistCartItemsAfterPageRefresh`.

---

### [Assumption A7] Postal Code Format Validation (Validation Defect)
- **Ambiguity:** The specification does not define whether Postal Code accepts only numeric characters or arbitrary alphanumeric strings.
- **Observed Behavior:** Entering purely alphabetic or arbitrary string characters (e.g. `"ABCDE"`) into the Postal Code field bypasses client-side format validation and allows the user to proceed to the overview page.
- **Engineering Assumption:** In standard e-commerce platforms, Postal/Zip Code input should enforce strict format/numeric validation rather than allowing any arbitrary string. We classify this as an observed format validation defect and assert actual behavior with `@Tag("Defect-A7")` and `@Issue("A7")`.
- **Test Implementation:** Verified in `CheckoutTest#shouldVerifyNonNumericPostalCodeBypassesValidationWhenSubmitted` (UI-24).

