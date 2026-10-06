# Part 3: CI/CD Pipeline Fixes & Extensions

This directory contains the GitHub Actions workflow (`ci.yml`) used to automatically execute the UI and API test suites.

## Bugs Found in the Starter File and Fixes

The provided `starter-kit/ci-broken.yml` file contained several issues that prevented the pipeline from running correctly and reliably reporting test results. The following bugs were identified and fixed:

1. **Missing Code Checkout:** The starter pipeline lacked the `actions/checkout@v4` step. Without this, the runner failed immediately because the project files didn't exist in the CI environment.
   * *Fix:* Added `actions/checkout@v4` as the initial step in both test jobs.
2. **Incompatible Java Version:** The starter pipeline provisioned Java 8, whereas both the Playwright and REST Assured test suites rely on modern Java 17 features.
   * *Fix:* Upgraded `actions/setup-java@v4` to use `java-version: '17'`.
3. **Missing Working Directories:** Maven commands were configured to execute at the repository root rather than targeting the specific `ui-tests` and `api-tests` modules.
   * *Fix:* Configured `defaults.run.working-directory` explicitly for each job.
4. **Masked UI Test Failures:** The UI test command included a shell bypass (`mvn clean test || true`). This suppressed legitimate test failures, returning a `0` exit code and falsely greening builds even if assertions failed.
   * *Fix:* Removed `|| true` to ensure builds fail reliably when defects are caught.
5. **System Maven Dependency:** The starter file relied on a globally installed `mvn`, which may not match the required versions defined in the project.
   * *Fix:* Standardized execution to use the project-bound Maven wrappers (`./mvnw`) across both jobs.
6. **Missing Playwright OS Dependencies:** Headless browser execution failed on Linux runners due to missing Chromium binaries and OS-level shared libraries.
   * *Fix:* Added an explicit `./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"` step before test execution.
7. **Playwright Headless Override:** The local default runs headfully (`headless: false` in `playwright.json`), which crashes on Linux runners without an X display.
   * *Fix:* Passed `-Dheadless=true` dynamically during CI UI test execution.
8. **Missing API Secrets:** API test execution was missing the `GOREST_API_TOKEN` environment variable, causing immediate 401 Unauthorized failures.
   * *Fix:* Mapped `GOREST_API_TOKEN: ${{ secrets.GOREST_API_TOKEN }}` via GitHub Action Secrets.
9. **Environment Variable Collision:** UI tests failed with `Unknown user: runner` because `PlaywrightConfig` attempted to map the native OS environment variable `USER` to SauceDemo accounts via a generic `user` property override fallback.
   * *Fix:* Removed the `override("user", ...)` fallback in `PlaywrightConfig.java`, forcing the test suite to rely exclusively on the namespaced `targetUser` property.

---

## SDET Evaluation Questionnaire

**1. Can you read and debug someone else’s CI YAML, not only write one from a blank file?**
Yes. By methodically reading the raw execution logs from the broken starter file, I was able to trace back pipeline failures to their specific YAML configuration flaws. For example, a "file not found" error pointed to a missing checkout step, while compile errors pointed to the mismatched Java 8 setup. I applied surgical fixes to the YAML to unblock the pipeline step-by-step.

**2. Do you understand how configuration choices affect whether a pipeline actually runs and reports results correctly?**
Yes. Configuration context is critical. For instance, without explicitly defining the `working-directory` at the job or step level, Maven cannot locate the `pom.xml` files for the sub-modules. Furthermore, understanding artifact paths ensures results are reported properly; if the `actions/upload-artifact` path doesn't precisely match the `pom.xml` output directory (e.g., `target/allure-results`), the test reports are lost when the runner shuts down.

**3. Do you fail builds properly instead of masking failures?**
Yes. Masking failures is a critical anti-pattern in CI/CD. The starter file contained `mvn clean test || true` for the UI suite, which forces the shell to return a successful exit code (0) regardless of the test outcome. I immediately removed `|| true`. A CI pipeline must be a reliable gatekeeper; if an assertion fails (like the observed defects tested in `problem_user`), the build must legitimately fail and block downstream deployment.

