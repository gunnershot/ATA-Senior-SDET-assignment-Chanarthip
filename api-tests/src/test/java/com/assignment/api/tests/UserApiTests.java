package com.assignment.api.tests;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


import com.assignment.api.base.BaseApiTest;
import com.assignment.api.models.GenericError;
import com.assignment.api.models.UserRequest;
import com.assignment.api.models.UserResponse;
import com.assignment.api.models.ValidationError;
import com.assignment.api.utils.DataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Tag;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("User Management API")
@Feature("CRUD Operations")
public class UserApiTests extends BaseApiTest {
    @ParameterizedTest(name = "1. POST /users - Create user with gender: {0}, status: {1}")
    @org.junit.jupiter.params.provider.CsvSource({
        "male, active",
        "female, active",
        "male, inactive",
        "female, inactive"
    })
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("1. POST /users - Create user with all gender and status combinations")
    @Description("Verify that creating a user works for all combinations of valid genders and statuses.")
    public void testCreateUserWithGenderAndStatusCombinations(String gender, String status) {
        UserRequest request = new UserRequest();
        request.setName(DataGenerator.generateRandomName());
        request.setEmail(DataGenerator.generateRandomEmail());
        request.setGender(gender);
        request.setStatus(status);

        Response response = userClient.createUser(request);
        assertThat(response.statusCode()).isEqualTo(201);

        UserResponse userResponse = response.as(UserResponse.class);
        assertThat(userResponse.getId()).isPositive();
        assertThat(userResponse.getName()).isEqualTo(request.getName());
        assertThat(userResponse.getEmail()).isEqualTo(request.getEmail());
        assertThat(userResponse.getGender()).isEqualTo(gender);
        assertThat(userResponse.getStatus()).isEqualTo(status);
        createdUserIds.add(userResponse.getId());
    }

    @Test
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("2. GET /users/{id} - Get user successfully")
    @Description("Verify that we can retrieve a created user by their ID")
    public void testGetUserSuccess() {
        UserResponse createdUser = setupTestUser();
        
        Response getResponse = userClient.getUser(createdUser.getId());
        assertThat(getResponse.statusCode()).isEqualTo(200);
        
        UserResponse fetchedUser = getResponse.as(UserResponse.class);
        assertThat(fetchedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(fetchedUser.getEmail()).isEqualTo(createdUser.getEmail());
    }

    @Test
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("3. PUT /users/{id} - Update user successfully")
    @Description("Verify that an existing user's details can be updated")
    public void testUpdateUserSuccess() {
        UserResponse createdUser = setupTestUser();
        
        UserRequest updatedData = DataGenerator.generateRandomUser();
        Response updateResponse = userClient.updateUser(createdUser.getId(), updatedData);
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        
        UserResponse updatedUser = updateResponse.as(UserResponse.class);
        assertThat(updatedUser.getName()).isEqualTo(updatedData.getName());
        assertThat(updatedUser.getEmail()).isEqualTo(updatedData.getEmail());
    }

    @Test
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("4. DELETE /users/{id} - Delete user successfully")
    @Description("Verify that a user can be successfully deleted")
    public void testDeleteUserSuccess() {
        UserResponse createdUser = setupTestUser();
        
        Response deleteResponse = userClient.deleteUser(createdUser.getId());
        assertThat(deleteResponse.statusCode()).isEqualTo(204);
        
        Response getResponse = userClient.getUser(createdUser.getId());
        assertThat(getResponse.statusCode()).isEqualTo(404);
        GenericError error = getResponse.as(GenericError.class);
        assertThat(error.getMessage()).contains("Resource not found");
    }

    @Test
    @Tag("Regression")
    @Tag("Negative")
    @DisplayName("5. POST /users - Error on duplicate email")
    @Description("Verify that creating a user with an already existing email returns 422")
    public void testCreateUserDuplicateEmail() {
        UserResponse createdUser = setupTestUser();
        
        UserRequest duplicateEmailUser = new UserRequest(DataGenerator.generateRandomName(), createdUser.getEmail(), "male", "active");
        
        Response duplicateResponse = userClient.createUser(duplicateEmailUser);
        assertThat(duplicateResponse.statusCode()).isEqualTo(422);
        
        ValidationError[] errors = duplicateResponse.as(ValidationError[].class);
        boolean found = false;
        for (ValidationError error : errors) {
            if ("email".equals(error.getField()) && error.getMessage().contains("has already been taken")) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    @Tag("Regression")
    @Tag("Negative")
    @DisplayName("6. POST /users - Error on missing authentication token")
    @Description("Verify that accessing protected endpoints without a token returns 401")
    public void testCreateUserWithoutToken() {
        UserRequest randomUser = DataGenerator.generateRandomUser();
        
        Response response = userClient.createUserWithoutToken(randomUser);
        assertThat(response.statusCode()).isEqualTo(401);
        
        GenericError error = response.as(GenericError.class);
        assertThat(error.getMessage()).contains("Authentication failed");
    }

    @Test
    @Tag("Regression")
    @Tag("Negative")
    @DisplayName("7. GET /users/{id} - Error on non-existent user")
    @Description("Verify that retrieving a user with an invalid ID returns 404")
    public void testGetNonExistentUser() {
        Response response = userClient.getUser(999999999L);
        assertThat(response.statusCode()).isEqualTo(404);
        
        GenericError error = response.as(GenericError.class);
        assertThat(error.getMessage()).contains("Resource not found");
    }

    @Test
    @Tag("Regression")
    @Tag("Negative")
    @DisplayName("8. PUT /users/{id} - Error on invalid data format")
    @Description("Verify that updating a user with invalid gender returns 422")
    public void testUpdateUserInvalidData() {
        UserResponse createdUser = setupTestUser();
        
        UserRequest invalidUpdate = new UserRequest("Test Name", "test@example.com", "invalid_gender", "active");
                
        Response updateResponse = userClient.updateUser(createdUser.getId(), invalidUpdate);
        assertThat(updateResponse.statusCode()).isEqualTo(422);
        
        ValidationError[] errors = updateResponse.as(ValidationError[].class);
        boolean found = false;
        for (ValidationError error : errors) {
            if ("gender".equals(error.getField()) && error.getMessage().contains("can't be blank")) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @ParameterizedTest(name = "Example {index}: Invalid {1}")
    @MethodSource("com.assignment.api.providers.UserDataProvider#invalidUserDataProvider")
    @Tag("Regression")
    @Tag("Negative")
    @DisplayName("9. POST /users - Create a user with invalid data combinations")
    @Description("Verify that API returns 422 Unprocessable Entity and correct validation errors for different bad data.")
    public void testCreateUserInvalidData(UserRequest invalidUser, String expectedErrorField, String expectedErrorMessage) {
        Response response = userClient.createUser(invalidUser);
        assertThat(response.statusCode()).isEqualTo(422);
        
        ValidationError[] errors = response.as(ValidationError[].class);
        boolean found = false;
        for (ValidationError error : errors) {
            if (expectedErrorField.equals(error.getField()) && error.getMessage().contains(expectedErrorMessage)) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("10. GET /users - Get all users (Default)")
    @Description("Verify that GET /users without parameters returns the default list of users with correct data format.")
    public void testGetAllUsersDefault() {
        Response response = userClient.getUsers(null);
        assertThat(response.statusCode()).isEqualTo(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemas/users-schema.json"));
        
        List<UserResponse> users = response.jsonPath().getList("$", UserResponse.class);
        assertThat(users)
            .as("Default users list should not be empty")
            .isNotEmpty();
            
        assertThat(users.size())
            .as("Default pagination should return up to 10 users")
            .isLessThanOrEqualTo(10);
            
        UserResponse firstUser = users.get(0);
        assertThat(firstUser.getId()).isNotNull().isPositive();
        assertThat(firstUser.getName()).isNotBlank();
        assertThat(firstUser.getEmail()).isNotBlank().contains("@");
        assertThat(firstUser.getGender()).matches("male|female");
        assertThat(firstUser.getStatus()).matches("active|inactive");
    }

    @Test
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("11. GET /users - Get users with pagination")
    @Description("Verify that GET /users works correctly with page and per_page parameters.")
    public void testGetAllUsersWithPagination() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", 2);
        queryParams.put("per_page", 5);

        Response response = userClient.getUsers(queryParams);
        assertThat(response.statusCode()).isEqualTo(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemas/users-schema.json"));
        
        List<UserResponse> users = response.jsonPath().getList("$", UserResponse.class);
        assertThat(users)
            .as("Paginated users list should not be empty")
            .isNotEmpty();
            
        assertThat(users.size())
            .as("Pagination per_page should be respected")
            .isLessThanOrEqualTo(5);
    }

    @Test
    @Tag("Regression")
    @Tag("HappyPath")
    @DisplayName("12. GET /users - Get users filtered by gender and status")
    @Description("Verify that GET /users works correctly with filters like gender and status.")
    public void testGetAllUsersWithFilters() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("gender", "female");
        queryParams.put("status", "active");

        Response response = userClient.getUsers(queryParams);
        assertThat(response.statusCode()).isEqualTo(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemas/users-schema.json"));
        
        List<UserResponse> users = response.jsonPath().getList("$", UserResponse.class);
        assertThat(users)
            .as("Filtered users list should not be empty")
            .isNotEmpty();
            
        for (UserResponse u : users) {
            assertThat(u.getGender()).isEqualTo("female");
            assertThat(u.getStatus()).isEqualTo("active");
        }
    }
        @ParameterizedTest(name = "13. POST /users - Missing field: {0}")
    @org.junit.jupiter.params.provider.ValueSource(strings = {"name", "email", "gender", "status"})
    @Tag("Regression")
    @Tag("NegativePath")
    @DisplayName("13. POST /users - Create user with missing required field")
    @Description("Verify that creating a user without a required field returns 422 Unprocessable Entity.")
    public void testCreateUserMissingRequiredField(String missingField) {
        UserRequest request = new UserRequest();
        if (!"name".equals(missingField)) request.setName("Test User");
        if (!"email".equals(missingField)) request.setEmail(DataGenerator.generateRandomEmail());
        if (!"gender".equals(missingField)) request.setGender("male");
        if (!"status".equals(missingField)) request.setStatus("active");

        Response response = userClient.createUser(request);
        assertThat(response.statusCode()).isEqualTo(422);

        List<ValidationError> errors = response.jsonPath().getList("$", ValidationError.class);
        assertThat(errors)
            .as("Validation errors list should not be empty")
            .isNotEmpty();
        
        ValidationError fieldError = errors.stream()
                .filter(e -> missingField.equals(e.getField()))
                .findFirst()
                .orElse(null);
                
        assertThat(fieldError)
            .as("Should contain validation error for " + missingField)
            .isNotNull();
        assertThat(fieldError.getMessage())
            .as("Error message should indicate " + missingField + " is blank")
            .contains("can't be blank");
    }

    @Test
    @Tag("Regression")
    @Tag("Negative")
    @DisplayName("14. POST /users - Error on invalid authentication token")
    @Description("Verify that accessing protected endpoints with an invalid or malformed token returns 401")
    public void testCreateUserWithInvalidToken() {
        UserRequest randomUser = DataGenerator.generateRandomUser();
        String invalidToken = "invalid_token_" + System.currentTimeMillis();

        Response response = userClient.createUserWithCustomToken(randomUser, invalidToken);
        assertThat(response.statusCode()).isEqualTo(401);

        GenericError error = response.as(GenericError.class);
        assertThat(error.getMessage()).contains("Invalid token");
    }
}

