package com.assignment.api.client;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import java.io.PrintStream;

import com.assignment.api.config.Config;
import com.assignment.api.models.UserRequest;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import com.assignment.api.utils.MaskedAllureRestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class UserClient {
    private static final String USERS_ENDPOINT = Config.getUsersEndpoint();

    private static final Logger log = LogManager.getLogger("API-LOG");

    private RequestSpecification getRequestSpec() {
        PrintStream logStream = IoBuilder.forLogger(log).setLevel(Level.INFO).buildPrintStream();
        
        return RestAssured.given()
                .config(RestAssuredConfig.config().logConfig(LogConfig.logConfig().blacklistHeader("Authorization")))
                .baseUri(Config.getBaseUrl())
                .header("Authorization", "Bearer " + Config.getApiToken())
                .contentType(ContentType.JSON)
                .filters(new RequestLoggingFilter(logStream), new ResponseLoggingFilter(logStream), new MaskedAllureRestAssured());
    }
    
    private RequestSpecification getRequestSpecWithoutToken() {
        PrintStream logStream = IoBuilder.forLogger(log).setLevel(Level.INFO).buildPrintStream();
        return RestAssured.given()
                .baseUri(Config.getBaseUrl())
                .contentType(ContentType.JSON)
                .filters(new RequestLoggingFilter(logStream), new ResponseLoggingFilter(logStream), new MaskedAllureRestAssured());
    }

    private RequestSpecification getRequestSpecWithToken(String token) {
        PrintStream logStream = IoBuilder.forLogger(log).setLevel(Level.INFO).buildPrintStream();
        return RestAssured.given()
                .baseUri(Config.getBaseUrl())
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .filters(new RequestLoggingFilter(logStream), new ResponseLoggingFilter(logStream), new MaskedAllureRestAssured());
    }

    @Step("Create a new user")
    public Response createUser(UserRequest user) {
        return getRequestSpec()
                .body(user)
                .when()
                .post(USERS_ENDPOINT);
    }
    
    @Step("Create user without token")
    public Response createUserWithoutToken(UserRequest user) {
        return getRequestSpecWithoutToken()
                .body(user)
                .when()
                .post(USERS_ENDPOINT);
    }

    @Step("Create user with custom token: {token}")
    public Response createUserWithCustomToken(UserRequest user, String token) {
        return getRequestSpecWithToken(token)
                .body(user)
                .when()
                .post(USERS_ENDPOINT);
    }

    @Step("Get list of users with query params")
    public Response getUsers(java.util.Map<String, Object> queryParams) {
        return getRequestSpec()
                .queryParams(queryParams != null ? queryParams : java.util.Collections.emptyMap())
                .when()
                .get(USERS_ENDPOINT);
    }

    @Step("Get list of users without token")
    public Response getUsersWithoutToken(java.util.Map<String, Object> queryParams) {
        return getRequestSpecWithoutToken()
                .queryParams(queryParams != null ? queryParams : java.util.Collections.emptyMap())
                .when()
                .get(USERS_ENDPOINT);
    }

    @Step("Get user by ID: {id}")
    public Response getUser(Long id) {
        return getRequestSpec()
                .pathParam("id", id)
                .when()
                .get(USERS_ENDPOINT + "/{id}");
    }

    @Step("Get user by ID without token: {id}")
    public Response getUserWithoutToken(Long id) {
        return getRequestSpecWithoutToken()
                .pathParam("id", id)
                .when()
                .get(USERS_ENDPOINT + "/{id}");
    }

    @Step("Update user with ID: {id}")
    public Response updateUser(Long id, UserRequest user) {
        return getRequestSpec()
                .pathParam("id", id)
                .body(user)
                .when()
                .put(USERS_ENDPOINT + "/{id}");
    }

    @Step("Delete user with ID: {id}")
    public Response deleteUser(Long id) {
        return getRequestSpec()
                .pathParam("id", id)
                .when()
                .delete(USERS_ENDPOINT + "/{id}");
    }
}
