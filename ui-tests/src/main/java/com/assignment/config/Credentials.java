package com.assignment.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Credentials. Read from the {@code SAUCE_PASSWORD} environment variable, or from
 * {@code ui-tests/.env} for local runs (the file is git-ignored).
 */
public final class Credentials {

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    /** SauceDemo publishes this password on its login page, so it's a safe fallback for local runs. */
    private static final String PUBLIC_DEMO_PASSWORD = "secret_sauce";

    private Credentials() {}

    public static String password() {
        String value = DOTENV.get("SAUCE_PASSWORD"); // dotenv also checks real environment variables
        return (value == null || value.isBlank()) ? PUBLIC_DEMO_PASSWORD : value;
    }
}

