package com.assignment.api.base;

import com.assignment.api.client.UserClient;
import com.assignment.api.models.UserRequest;
import com.assignment.api.models.UserResponse;
import com.assignment.api.utils.DataGenerator;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;

import java.util.ArrayList;
import java.util.List;

public class BaseApiTest {
    protected UserClient userClient = new UserClient();
    protected List<Long> createdUserIds = new ArrayList<>();

    @BeforeEach
    public void setUpBase() {
        io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        // initialized at field level to prevent NPE
    }

    @AfterEach
    public void tearDownBase() {
        for (Long id : createdUserIds) {
            Response response = userClient.deleteUser(id);
            if (response.statusCode() != 204 && response.statusCode() != 404) {
                System.err.println("Failed to clean up user with ID: " + id);
            }
        }
    }

    protected UserResponse setupTestUser() {
        UserRequest randomUser = DataGenerator.generateRandomUser();
        Response response = userClient.createUser(randomUser);
        
        Assumptions.assumeTrue(response.statusCode() == 201, "Setup skipped: Unable to create test user for prerequisite.");
        
        UserResponse createdUser = response.as(UserResponse.class);
        createdUserIds.add(createdUser.getId());
        return createdUser;
    }
}


