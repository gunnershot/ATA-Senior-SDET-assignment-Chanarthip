package com.assignment.api.utils;

import com.assignment.api.models.UserRequest;
import net.datafaker.Faker;

public class DataGenerator {
    private static final Faker faker = new Faker();

    public static UserRequest generateRandomUser() {
        return new UserRequest(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                faker.options().option("male", "female"),
                faker.options().option("active", "inactive")
        );
    }

    public static String generateRandomName() {
        return faker.name().fullName();
    }

    public static String generateRandomEmail() {
        return faker.internet().emailAddress();
    }
}
