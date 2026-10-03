# Discovered Defects & Diagnostic Strategy

This document details the defects discovered during exploration and automation of SauceDemo, and explains the Senior SDET diagnostic strategy applied in the test suite.

---

## 1. Summary of Discovered Application Defects

| Defect ID | Feature Area | User Account | Defect Description | Severity | Automated Test |
|---|---|---|---|:---:|---|
| **DEF-01** | Cart | `problem_user` | Clicking the "Remove" button on inventory does not remove item; cart badge stays stuck at "1". | Critical | `ProblemUserTest#shouldDocumentBrokenRemoveButtonWhenProblemUserAttemptsToRemoveItem` |
| **DEF-02** | Checkout | `problem_user` | Input routing miswired: typing into "Last Name" field updates the "First Name" field and leaves "Last Name" blank. | Critical | `ProblemUserTest#shouldDocumentFormInputMisroutingWhenProblemUserSubmitsCheckoutInformation` |
| **DEF-03** | Checkout | `standard_user` | Empty cart checkout is allowed: user can checkout 0 items for $0.00 and place order. | Major | `CheckoutTest#shouldPreventCheckoutWithEmptyCart` |
| **DEF-04** | Checkout | `standard_user` | Whitespace input (`"   "`) bypasses required field validation on checkout information step. | Minor | `CheckoutTest#shouldEnforceWhitespaceValidationAtCheckoutInfo` |
| **DEF-05** | Catalog | `problem_user` | Broken product image links: multiple catalog items display fallback 404 dog image asset. | Minor | Verified via catalog image assertions |

---

## 2. Senior SDET Diagnostic Strategy

### Guideline Adherence
> *"Write assertions that are specific to the behaviour you observed. Do not use broad retries, Thread.sleep or weak assertions to paper over a defect."*

Instead of masking defects with soft assertions, skips, or arbitrary delays:
1. **Strict State Assertions:** We assert the exact observed state (e.g. badge remaining `"1"` or `firstName` taking the value of `lastName`).
2. **Allure Diagnostic Logging:** We annotate each defect scenario with `@Issue`, `@Tag("Diagnostic")`, and step logs in Allure reports.
3. **Trace and Screenshot Capture:** On failure or anomaly, full Playwright Traces (`.zip`) and full-page screenshots are automatically captured for developer triage.
