package com.assignment.api.providers;

import com.assignment.api.models.UserRequest;
import com.assignment.api.utils.DataGenerator;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;

public class UserDataProvider {

    public static Stream<Arguments> invalidUserDataProvider() {
        return Stream.of(
            Arguments.of(
                new UserRequest("", DataGenerator.generateRandomEmail(), "male", "active"),
                "name", "can't be blank"
            ),
            Arguments.of(
                new UserRequest(DataGenerator.generateRandomName(), "invalid-email", "male", "active"),
                "email", "is invalid"
            ),
            Arguments.of(
                new UserRequest(DataGenerator.generateRandomName(), DataGenerator.generateRandomEmail(), "alien", "active"),
                "gender", "can't be blank, can be male of female"
            ),
            Arguments.of(
                new UserRequest(DataGenerator.generateRandomName(), DataGenerator.generateRandomEmail(), "male", "offline"),
                "status", "can't be blank"
            )
        );
    }
}
