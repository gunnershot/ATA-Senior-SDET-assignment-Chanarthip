# API Automation Tests (Part 2)

This module contains the REST API test automation suite for https://gorest.co.in/public/v2, developed using Java, REST Assured, and JUnit 5.

## Prerequisites

- Java 17 or higher
- (Optional) Maven. However, a Maven Wrapper (mvnw) is provided, meaning you don't need Maven installed globally to run the tests.

## Setup

1. **Configure Environment Variables:**
   A GoRest API Token is required to run the tests. 
   - Copy the provided .env.example file to .env:
     `bash
     cp .env.example .env
     `
   - Open .env and replace your_gorest_api_token_here with your actual GoRest Bearer token.

## Running the Tests

Navigate to the `api-tests` directory and use the Maven Wrapper:

### Standard Sequential / Default Execution

#### On Windows (PowerShell / CMD)
```powershell
cd api-tests
.\mvnw.cmd clean test
```

#### On macOS / Linux
```bash
cd api-tests
./mvnw clean test
```

---

## Parallel Execution

The API suite is configured for JUnit 5 parallel execution via `src/test/resources/junit-platform.properties`. Test methods and classes execute concurrently in a shared thread pool.

### Thread Safety & State Isolation
- **Dynamic Data Generation:** Each test scenario generates unique user emails and names via DataFaker (`UserClient.createUniqueUser()`).
- **Idempotency & Teardown:** Tests isolate their state and automatically delete created users upon completion, preventing collision across concurrent threads.

### Run with Custom Parallel Thread Count (e.g. 4 Threads)

#### On Windows (PowerShell / CMD)
```powershell
cd api-tests
.\mvnw.cmd test -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
```

#### On macOS / Linux
```bash
cd api-tests
./mvnw test -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
```

---

## Features

- **Environment-based Configuration**: Uses dotenv-java to securely inject the API token without hardcoding it in the codebase.
- **Robust Model Layer**: Uses POJOs (Plain Old Java Objects) and Lombok for request/response serialization (no raw JSON strings).
- **Dynamic Data Generation**: Uses DataFaker to generate unique names and emails per test run.
- **Shared Assertions**: Centralized validation logic with custom logging that prints response details if an assertion fails (reducing test output noise).
- **State Cleanup**: Automatically deletes created users after the test finishes, ensuring idempotency.
