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

---

## Part 2 Questionnaire & Architectural Answers

**1. Can you identify the critical API scenarios without being told what to test?**
Yes. For a RESTful API like GoRest, the critical scenarios revolve around CRUD operations and negative pathways:
* **Happy Paths:** Create user (POST) with various valid data combinations (gender/status), Read user by ID (GET) and fetch list with pagination/filters (GET), Update user details (PUT), and Delete user (DELETE).
* **Negative/Boundary Paths:** Attempting to create duplicate emails, missing required fields, omitting authentication tokens (401), invalid JSON formats, and accessing non-existent users (404). 

**2. How do you structure API test code for reuse?**
I employ a layered architecture:
* **Base Layer (`BaseApiTest`)**: Manages the RestAssured global configuration, environment variables (`GOREST_API_TOKEN`), RequestSpecs, and automatic test teardown.
* **Client Layer (`UserClient`)**: Encapsulates specific HTTP calls (POST, GET, PUT, DELETE) and endpoints so the test classes remain clean and don't contain raw HTTP plumbing.
* **Data Layer (`DataGenerator` & POJOs)**: Uses Jackson POJOs (`UserRequest`, `UserResponse`) for serialization/deserialization and `DataFaker` to generate unique payloads dynamically, avoiding hardcoded JSON.

**3. How thoroughly do you validate responses beyond the status code?**
I validate several layers of the response:
* **Status Code:** `200`, `201`, `204`, `401`, `404`, `422`.
* **JSON Schema Validation:** I assert that the response body matches strict JSON schema definitions (`matchesJsonSchemaInClasspath`).
* **Field-Level Data Binding:** Deserializing into POJOs allows strict type checking and precise assertion on fields (e.g., verifying `updatedUser.getName().equals(expectedName)`).
* **Error Array Validation:** For 422 errors, I iterate through the returned `ValidationError[]` to assert that the specific field (e.g., `email`) failed with the correct error message (e.g., `has already been taken`), ensuring we didn't just get a generic failure.

**4. Do you cover error scenarios and edge cases, not only the 2xx path?**
Absolutely. The suite aggressively tests negative conditions, including:
* Missing authorization headers (returns 401).
* Duplicate email constraints (returns 422).
* Invalid data formatting like invalid enums for gender (returns 422).
* Missing required fields via parameterized tests covering `name`, `email`, `gender`, and `status` omission.
* Accessing deleted or non-existent IDs (returns 404).

**5. How do you handle authentication and test data across the full lifecycle of a run?**
* **Authentication:** The `GOREST_API_TOKEN` is injected via `.env` or system environment variables into a global RestAssured `RequestSpecification` in `BaseApiTest`. The test methods don't need to manually attach tokens.
* **Test Data Setup/Teardown:** Test data is generated dynamically per test via `DataFaker` to prevent collisions. Every created user ID is logged to a thread-safe list (`createdUserIds`). The `@AfterEach` lifecycle hook automatically iterates through this list and invokes a DELETE request to purge the environment, guaranteeing idempotency and preventing database bloat.

---

## API Test Scenario Matrix

The suite covers 16 scenarios comprehensively mapped to the GoRest User API:

| ID | Endpoint | Method | Category | Expected Outcome |
|----|----------|--------|----------|------------------|
| **API-01** | `POST /users` | `testCreateUserWithGenderAndStatusCombinations` | Happy Path | Creates users across all valid gender/status combinations (201). |
| **API-02** | `GET /users/{id}` | `testGetUserSuccess` | Happy Path | Successfully retrieves a user by ID (200). |
| **API-03** | `PUT /users/{id}` | `testUpdateUserSuccess` | Happy Path | Updates an existing user's details successfully (200). |
| **API-04** | `DELETE /users/{id}` | `testDeleteUserSuccess` | Happy Path | Deletes the user (204) and verifies subsequent 404. |
| **API-05** | `POST /users` | `testCreateUserDuplicateEmail` | Negative | Rejects duplicate email creation with 422 Unprocessable Entity. |
| **API-06** | `POST /users` | `testCreateUserWithoutToken` | Negative | Prevents creation without a token, returning 401 Unauthorized. |
| **API-07** | `GET /users/{id}` | `testGetNonExistentUser` | Negative | Returns 404 Not Found for non-existent IDs. |
| **API-08** | `PUT /users/{id}` | `testUpdateUserInvalidData` | Negative | Rejects invalid enum values for fields like gender (422). |
| **API-09** | `POST /users` | `testCreateUserInvalidData` | Negative | Parameterized combinations of malformed payloads (422). |
| **API-10** | `GET /users` | `testGetAllUsersDefault` | Happy Path | Validates default pagination and JSON schema. |
| **API-11** | `GET /users` | `testGetAllUsersWithPagination` | Happy Path | Validates `page` and `per_page` query parameters limits. |
| **API-12** | `GET /users` | `testGetAllUsersWithFilters` | Happy Path | Validates data filtering via query strings (e.g., `gender=female`). |
| **API-13** | `POST /users` | `testCreateUserMissingRequiredField` | Negative | Parameterized tests ensuring every required field is validated. |
| **API-14** | `POST /users` | `testCreateUserWithInvalidToken` | Negative | Rejects malformed authorization tokens (401). |
| **API-15** | `GET /users` | `testGetAllUsersWithoutToken` | Security / Happy | Verifies that GET /users is a public endpoint accessible without authentication token (200). |
| **API-16** | `GET /users/{id}` | `testGetUserByIdWithoutToken` | Security / Happy | Verifies that GET /users/{id} retrieves public users without authentication token (200). |
