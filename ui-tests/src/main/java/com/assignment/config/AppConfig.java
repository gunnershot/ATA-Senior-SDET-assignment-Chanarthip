package com.assignment.config;

/**
 * Backward compatibility facade delegating to {@link PlaywrightConfig} and {@link Credentials}.
 */
public final class AppConfig {

    private AppConfig() {}

    public static final String BASE_URL = PlaywrightConfig.get().getBaseUrl();

    public static String getPassword() {
        return Credentials.password();
    }
}

