package com.assignment.api.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.aeonbits.owner.ConfigFactory;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    private static Dotenv dotenv;
    private static final EnvironmentConfig envConfig;

    static {
        // Load dotenv for secrets
        try {
            if (Files.exists(Paths.get("api-tests/.env"))) {
                dotenv = Dotenv.configure().directory("api-tests").ignoreIfMissing().load();
            } else {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            }
        } catch (Exception e) {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }

        // Setup Owner configuration
        String environment = System.getProperty("env", "default");
        ConfigFactory.setProperty("env", environment);
        envConfig = ConfigFactory.create(EnvironmentConfig.class, System.getProperties(), System.getenv());
    }

    public static String getBaseUrl() {
        return envConfig.baseUrl();
    }
    
    public static String getUsersEndpoint() {
        return envConfig.usersEndpoint();
    }

    public static String getApiToken() {
        String token = dotenv.get("GOREST_API_TOKEN");
        if (token == null || token.trim().isEmpty()) {
            token = System.getenv("GOREST_API_TOKEN");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("GOREST_API_TOKEN is missing. Please provide it via .env file or system environment variables.");
        }
        return token.replace("\"", "").replace("'", "").trim();
    }
}



