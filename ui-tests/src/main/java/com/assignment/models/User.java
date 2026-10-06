package com.assignment.models;

import com.assignment.config.PlaywrightConfig;
import com.assignment.config.UserCredentials;
import com.assignment.config.Credentials;

/** SauceDemo accounts. Dynamic credentials loaded per active environment. */
public enum User {
    STANDARD("standard_user"),
    LOCKED_OUT("locked_out_user"),
    PROBLEM("problem_user"),
    PERFORMANCE_GLITCH("performance_glitch_user");

    private final String defaultKey;

    User(String defaultKey) {
        this.defaultKey = defaultKey;
    }

    public String username() {
        UserCredentials creds = PlaywrightConfig.get().getUserCredentials(defaultKey);
        if (creds != null && creds.getUsername() != null && !creds.getUsername().isBlank()) {
            return creds.getUsername();
        }
        return defaultKey;
    }

    public String password() {
        UserCredentials creds = PlaywrightConfig.get().getUserCredentials(defaultKey);
        if (creds != null && creds.getPassword() != null && !creds.getPassword().isBlank()) {
            return creds.getPassword();
        }
        return Credentials.password();
    }

    public static User fromUsername(String username) {
        if (username == null || username.isBlank()) {
            return STANDARD;
        }
        for (User u : values()) {
            if (u.defaultKey.equalsIgnoreCase(username) || 
                u.name().equalsIgnoreCase(username) || 
                u.username().equalsIgnoreCase(username)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Unknown user: " + username + ". Supported accounts: standard_user, locked_out_user, problem_user, performance_glitch_user");
    }
}
