package com.assignment.models;

import com.assignment.config.Credentials;

/** SauceDemo accounts. All share one password, read from the environment via {@link Credentials}. */
public enum User {
    STANDARD("standard_user"),
    LOCKED_OUT("locked_out_user"),
    PROBLEM("problem_user"),
    PERFORMANCE_GLITCH("performance_glitch_user");

    private final String username;

    User(String username) {
        this.username = username;
    }

    public String username() {
        return username;
    }

    public String password() {
        return Credentials.password();
    }

    public static User fromUsername(String username) {
        if (username == null || username.isBlank()) {
            return STANDARD;
        }
        for (User u : values()) {
            if (u.username.equalsIgnoreCase(username) || u.name().equalsIgnoreCase(username)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Unknown user: " + username + ". Supported accounts: standard_user, locked_out_user, problem_user, performance_glitch_user");
    }
}

