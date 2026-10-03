# Test Coverage & Scenario Matrix (SauceDemo)

This document satisfies **Part 0: Test coverage and scenario design** and forms the specification basis for the **Part 1: UI Automation (Playwright + Java)** suite.

---

## Target Scope & Accounts

- **Application:** [https://www.saucedemo.com/](https://www.saucedemo.com/)
- **Features in Scope:** Authentication, Product Catalog & Inventory, Shopping Cart, and Checkout Flow.
- **Accounts in Scope (all with password `secret_sauce`):**
  1. `standard_user`: Base functional user behavior.
  2. `locked_out_user`: Authentication rejection and error messaging.
  3. `problem_user`: Diagnostic testing for visual/DOM defects, state mutations, and input misrouting.
  4. `performance_glitch_user`: Latency tolerance, functional timeouts, and SLA threshold resilience.

---

## Scenario Matrix (20 Scenarios)

| Scenario ID | Feature Area | Account | Input / Condition | Expected Outcome | Category | Priority | Automated Test Method |
|---|---|---|---|---|---|:---:|---|
| **UI-01** | Authentication | `standard_user` | Valid username & `secret_sauce` | Redirects to `/inventory.html`, header displays "Products", 6 inventory items visible. | Happy Path | P0 | `LoginTest#shouldDisplayInventoryWhenStandardUserLogsInWithValidCredentials` |
| **UI-02** | Authentication | `locked_out_user` | Valid username & `secret_sauce` | Login rejected, remains on login page, displays exact error: `Epic sadface: Sorry, this user has been locked out.` | Negative | P0 | `LoginTest#shouldRejectLoginAndDisplayErrorMessageWhenUserIsLockedOut` |
| **UI-03** | Authentication | `standard_user` | Valid username & invalid password (`wrong_password`) | Login rejected, displays `Epic sadface: Username and password do not match any user in this service`. | Negative | P0 | `LoginTest#shouldRejectLoginAndDisplayMismatchErrorWhenPasswordIsInvalid` |
| **UI-04** | Authentication | Unauthenticated | Blank username (`""`) & valid password | Form validation blocks submit, displays `Epic sadface: Username is required`. `[AMBIGUITY: Max username length and trim behavior are unspecified.]` | Boundary | P1 | `LoginTest#shouldDisplayRequiredErrorWhenUsernameIsOmitted` |
| **UI-05** | Authentication | Unauthenticated | Valid username & blank password (`""`) | Form validation blocks submit, displays `Epic sadface: Password is required`. | Boundary | P1 | `LoginTest#shouldDisplayRequiredErrorWhenPasswordIsOmitted` |
| **UI-06** | Access Control | Unauthenticated | Direct URL navigation to `/inventory.html` without session cookie | Access blocked, redirected to login page with error `Epic sadface: You can only access '/inventory.html' when you are logged in.`. `[AMBIGUITY: Session timeout duration and server-side token revocation are unspecified.]` | Edge Case | P0 | `LoginTest#shouldPreventAccessAndRedirectToLoginWhenVisitingInventoryWithoutAuthentication` |
| **UI-07** | Cart | `standard_user` | Click "Add to cart" on single product (Sauce Labs Backpack) | Button toggles to "Remove", shopping cart badge displays "1", cart item list contains the item. | Happy Path | P0 | `CartTest#shouldUpdateBadgeAndCartWhenAddingOneItem` |
| **UI-08** | Cart | `standard_user` | Add two distinct products (Backpack & Bike Light) | Shopping cart badge increments to "2", both products listed in cart with correct individual prices. `[AMBIGUITY: Maximum quantity per line item is not supported in UI.]` | Boundary | P0 | `CartTest#shouldUpdateBadgeWhenAddingTwoDifferentItems` |
| **UI-09** | Cart | `standard_user` | Add item, then click "Remove" directly from inventory page | Button reverts to "Add to cart", cart badge is removed from DOM. | Happy Path | P1 | `CartTest#shouldRemoveItemFromInventoryAndClearBadge` |
| **UI-10** | Cart | `standard_user` | Add single item, navigate to `/cart.html`, click "Remove" | Item row removed from cart list, shopping cart badge cleared. | Boundary | P1 | `CartTest#shouldRemoveLastItemInCartAndClearBadge` |
| **UI-11** | Cart & Session | `standard_user` | Add item to cart, perform page reload (`F5`) | Cart badge remains "1", cart persistence verified across reload. `[AMBIGUITY: State is maintained via localStorage/sessionStorage without backend sync.]` | Edge Case | P1 | `CartTest#shouldPersistCartItemsAfterPageRefresh` |
| **UI-12** | Checkout | `standard_user` | Complete checkout with valid First Name, Last Name, Postal Code | Navigates through Info -> Overview -> Complete page with header `Thank you for your order!`. | E2E Happy | P0 | `CheckoutTest#shouldCompleteCheckoutWithFullInformation` |
| **UI-13** | Checkout | `standard_user` | Submit `/checkout-step-one.html` with missing individual fields | Enforces field validation sequentially: `Error: First Name is required`, `Error: Last Name is required`, `Error: Postal Code is required`. | Negative | P1 | `CheckoutTest#shouldEnforceRequiredFieldsAtCheckoutInfo` |
| **UI-14** | Checkout | `standard_user` | Cart with Backpack ($29.99) & Bike Light ($9.99) at Overview | Item subtotal matches exact sum ($39.98), tax matches calculated rate ($3.20), Total equals Subtotal + Tax ($43.18) using BigDecimal precision. | Integrity | P0 | `CheckoutTest#shouldCalculateAccurateTotalForMultipleItems` |
| **UI-15** | Checkout | `standard_user` | Click "Cancel" on `/checkout-step-one.html` | User returns to `/cart.html`, cart items remain intact. | Edge Case | P1 | `CheckoutTest#shouldReturnToCartWhenCancelingCheckoutInfo` |
| **UI-16** | Checkout | `standard_user` | Navigate directly to checkout with empty cart (0 items) | **Observed Application Defect:** SauceDemo allows empty cart checkout ($0.00) through completion without preventing order placement. `[AMBIGUITY / DEFECT: E-commerce standard expects empty cart checkout to be disabled.]` | Boundary / Defect | P1 | `CheckoutTest#shouldPreventCheckoutWithEmptyCart` |
| **UI-17** | Checkout | `standard_user` | Fill checkout info using whitespace only (`"   "`) | **Observed Application Defect:** Whitespace bypasses required field validation, navigating to overview page instead of displaying validation error. | Edge / Defect | P1 | `CheckoutTest#shouldEnforceWhitespaceValidationAtCheckoutInfo` |
| **UI-18** | Problem User | `problem_user` | Add item (Backpack), then click "Remove" button | **Account-Specific Defect:** Remove button event listener fails to update DOM/state; cart badge remains permanently stuck at "1". | Diagnostic | P1 | `ProblemUserTest#shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem` |
| **UI-19** | Problem User | `problem_user` | Fill First Name ("John") and Last Name ("Doe") on Checkout Info | **Account-Specific Defect:** Form input routing is miswired. Typing into `lastName` overwrites `firstName` input, leaving `lastName` blank and triggering `Error: Last Name is required`. | Diagnostic | P1 | `ProblemUserTest#shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation` |
| **UI-20** | Performance & Resilience | `performance_glitch_user` | Standard login with simulated backend latency | Login succeeds, catalog renders within SLA threshold (10,000 ms) despite artificial ~5,000 ms backend delay. | Resilience | P1 | `PerformanceGlitchUserTest#shouldCompleteLoginWithinSlaThresholdWhenPerformanceGlitchUserLogsIn` |

---

## Explicit Differences: `problem_user` and `performance_glitch_user`

### 1. `problem_user` vs `standard_user`
- **Catalog Image Integrity:** For `standard_user`, each item displays its unique product image. For `problem_user`, multiple item images point to the dog image asset (`/static/media/sl-404.168b1cce.jpg`).
- **Cart Mutation Failure (UI-18):** On `standard_user`, clicking "Remove" deletes the item and clears the badge. On `problem_user`, the remove click handler fails silently; the badge remains at "1".
- **Form State Crossing (UI-19):** On `standard_user`, form fields bind to their respective data attributes. On `problem_user`, the `lastName` input event is mapped to update the `firstName` element, making form completion impossible.
- **Testing Approach:** Instead of using soft assertions or ignoring errors, we write strict assertions verifying the *exact defective behavior* and tag the tests with `@Issue` and `@Tag("Diagnostic")`.

### 2. `performance_glitch_user` vs `standard_user`
- **Latency Profile:** `standard_user` requests complete in < 500 ms. `performance_glitch_user` incurs a deterministic ~5-second server-side delay on the `/inventory.html` API route.
- **Testing Approach:** We do not use arbitrary `Thread.sleep`. We rely on Playwright's auto-waiting web-first assertions combined with an explicit SLA threshold check (`durationMs < CONFIG.getPerformanceTimeoutMs()`) to verify both functional correctness and non-functional resilience.
