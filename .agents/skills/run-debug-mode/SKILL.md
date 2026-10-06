---
name: run-debug-mode
description: >-
  Commands, techniques, and runbooks for executing UI and API automation tests in Debug Mode. Use this skill when the user asks to debug tests, run in headed mode, enable slowMo, inspect Playwright Traces, attach JVM debuggers, view verbose API HTTP request/response logs, or run sequentially in single-threaded mode.
---

# Run Automation Tests in Debug Mode

This guide provides targeted instructions for debugging both **Playwright UI Tests** (`ui-tests/`) and **REST Assured API Tests** (`api-tests/`).

---

## 1. UI Tests Debug Mode (`ui-tests/`)

### A. Headed Execution with Slow Motion (`slowMo`)
Run tests with the browser window open and slow down operations so step-by-step actions are visible:

```powershell
cd ui-tests

# Run in Headed mode (Browser UI visible)
.\run-tests.ps1 -Test "LoginTest" -Headed

# Run specific method with slowMo (e.g. 500ms delay per action)
.\mvnw.cmd test -Dtest="CheckoutTest#shouldCompleteOrderSuccessfullyWhenCheckingOutWithValidInformation" -Dheadless=false -DslowMo=500
```

### B. Playwright Inspector & Step-by-Step Stepping
Launch Playwright Inspector GUI allowing step-by-step execution, locator picking, and console inspection:

```powershell
cd ui-tests
$env:PWDEBUG="1"
.\mvnw.cmd test -Dtest="LoginTest#shouldDisplayInventoryWhenStandardUserLogsInWithValidCredentials"
$env:PWDEBUG="0" # Reset after debug
```

### C. Viewing Playwright Traces
Every test execution records a Playwright Trace (Snapshots, DOM, Console, Network) stored in `target/allure-results/` upon test failure or completion:

```powershell
cd ui-tests
# Inspect generated trace zip using Playwright CLI
.\mvnw.cmd exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="show-trace target/allure-results/<test-trace>.zip"
```

### D. JVM Remote Debugging (Attach IDE Debugger on Port 5005)
Pause test execution and wait for IntelliJ / VS Code debugger to attach:

```powershell
cd ui-tests
.\mvnw.cmd test -Dtest="CheckoutTest" -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
```

---

## 2. API Tests Debug Mode (`api-tests/`)

### A. Single-Threaded Sequential Execution (Isolate Race Conditions)
Disable JUnit 5 parallelism to execute scenarios one-by-one with clean, un-interleaved console output:

```powershell
cd api-tests
# Disable parallel execution completely
.\mvnw.cmd test -Djunit.jupiter.execution.parallel.enabled=false

# Run single targeted test
.\mvnw.cmd test -Dtest="UserApiTests#testGetUserSuccess" -Djunit.jupiter.execution.parallel.enabled=false
```

### B. Verbose HTTP & Wire Logging
Enable detailed Request/Response wire logs in console and log file:

```powershell
cd api-tests
# Set log level to DEBUG
.\mvnw.cmd test -DlogLevel=DEBUG -Dtest="UserApiTests#testCreateUserWithoutToken"
```
Logs print complete request method, URI, headers (authorization masked), JSON payload, and full HTTP response headers/body.

### C. JVM Remote Debugging (Attach IDE Debugger on Port 5005)
Pause API test execution and wait for debugger attachment:

```powershell
cd api-tests
.\mvnw.cmd test -Dtest="UserApiTests#testGetAllUsersDefault" -Dmaven.surefire.debug="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
```

---

## Quick Reference Summary Table

| Goal | Suite | Key Parameter / Environment Variable |
|---|---|---|
| **Visible Browser** | UI | `-Headed` or `-Dheadless=false` |
| **Action Delay (SlowMo)** | UI | `-DslowMo=500` (delay in ms) |
| **Playwright Inspector GUI** | UI | `$env:PWDEBUG="1"` |
| **Playwright Trace Viewer** | UI | `mvnw exec:java ... show-trace <trace.zip>` |
| **Single-Threaded Execution** | API | `-Djunit.jupiter.execution.parallel.enabled=false` |
| **Verbose HTTP Logging** | API | `-DlogLevel=DEBUG` |
| **IDE Remote Debugger** | Both | `-Dmaven.surefire.debug="-agentlib:jdwp=...5005"` |

