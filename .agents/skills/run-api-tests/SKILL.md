---
name: run-api-tests
description: >-
  Commands, parameters, and runbooks for executing GoRest API automation tests (REST Assured + JUnit 5 + Java 17). Use this skill when the user asks to run API tests, filter by test class or method, filter by tags, run in parallel with custom threads, or generate Allure reports.
---

# Run API Automation Tests

When executing GoRest REST API automation tests, use the Maven Wrapper `mvnw.cmd` located inside the `api-tests/` directory.

> **Note:** Always execute from the `api-tests/` directory:
> ```powershell
> cd api-tests
> ```

---

## 1. Test Execution Commands (`mvnw.cmd`)

The API test suite runs with JUnit 5 Jupiter and REST Assured, utilizing thread-safe state tracking, dynamic data generation (`DataFaker`), and automatic teardown cleanup (`DELETE /users/{id}`).

### Standard Runs

- **Run All 16 API Tests (Full Regression Suite):**
  ```powershell
  cd api-tests
  .\mvnw.cmd clean test
  ```
  Runs all 16 API test scenarios, clears stale artifacts, serializes/deserializes Jackson POJOs, validates JSON schemas, and records Allure results to `target/allure-results`.

- **Fast Re-run without Clean:**
  ```powershell
  cd api-tests
  .\mvnw.cmd test
  ```

---

### Filtering Tests

- **Run by Test Class:**
  ```powershell
  cd api-tests
  .\mvnw.cmd test -Dtest="UserApiTests"
  ```

- **Run Specific Test Method:**
  ```powershell
  cd api-tests
  # Run single method
  .\mvnw.cmd test -Dtest="UserApiTests#testGetUserSuccess"

  # Run multiple specific methods
  .\mvnw.cmd test -Dtest="UserApiTests#testGetAllUsersWithoutToken,UserApiTests#testGetUserByIdWithoutToken"
  ```

- **Run by Category / Tag:**
  The suite is tagged with JUnit 5 tags:
  ```powershell
  cd api-tests
  # Happy path scenarios (CRUD, Pagination, Filters)
  .\mvnw.cmd test -Dgroups="HappyPath"

  # Negative & validation boundary scenarios (Duplicate, missing fields, 401, 404, 422)
  .\mvnw.cmd test -Dgroups="Negative"

  # Security & public access scenarios (No-token verification: API-15, API-16)
  .\mvnw.cmd test -Dgroups="Security"

  # Core regression suite
  .\mvnw.cmd test -Dgroups="Regression"
  ```

---

### Parallel Execution & Thread Configuration

The API suite supports JUnit 5 parallel execution configured via `src/test/resources/junit-platform.properties`. Tests run concurrently with dynamic data isolation.

- **Run with Default Parallel Execution:**
  ```powershell
  cd api-tests
  .\mvnw.cmd test
  ```

- **Run with Custom Parallel Thread Count (e.g. 4 Threads):**
  ```powershell
  cd api-tests
  .\mvnw.cmd test -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
  ```

- **Run in Single Thread (Sequential / Debug Mode):**
  ```powershell
  cd api-tests
  .\mvnw.cmd test -Djunit.jupiter.execution.parallel.enabled=false
  ```

---

## 2. Allure Report Generation

Raw Allure results are captured with masked authentication headers via `MaskedAllureRestAssured` filter into `api-tests/target/allure-results`.

### Generate Portable Report via Allure CLI

To generate the standalone HTML report using the shared Allure CLI in the workspace:

```powershell
# From api-tests/ directory
..\ui-tests\.allure\allure-2.25.0\bin\allure.bat generate target/allure-results --single-file --clean -o allure-report
Start-Process allure-report\index.html
```

### Serve Allure Report via Maven Plugin

```powershell
cd api-tests
.\mvnw.cmd allure:serve
```

---

## 3. Environment & Token Configuration

A valid GoRest Bearer token is required for write operations (`POST`, `PUT`, `DELETE`).

- **Via `.env` file in `api-tests/`:**
  ```properties
  GOREST_API_TOKEN=your_token_here
  GOREST_BASE_URL=https://gorest.co.in/public/v2
  ```

- **Via Environment Variable / CLI Override:**
  ```powershell
  $env:GOREST_API_TOKEN="your_token_here"
  .\mvnw.cmd test
  ```
