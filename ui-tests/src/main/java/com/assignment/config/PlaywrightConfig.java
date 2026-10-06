package com.assignment.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Browser / environment settings loaded from {@code src/test/resources/env/{env}.json}
 * or fallback {@code src/test/resources/playwright.json}.
 * <p>
 * Supports dynamic environment selection via {@code -Denv=sit} or {@code ENV=staging}.
 * Precedence: JVM system property &gt; environment variable &gt; config file &gt; built-in default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PlaywrightConfig {

    private static final String DEFAULT_FILE = "playwright.json";
    private static PlaywrightConfig instance;

    private String environment = "local";
    private String browser = "chromium";
    private boolean headless = true;
    private int slowMo = 0;
    private String baseUrl = "https://www.saucedemo.com";
    private double defaultTimeoutMs = 10_000;
    private double performanceTimeoutMs = 10_000;
    private String targetUser = "standard_user";
    private Map<String, UserCredentials> users = new HashMap<>();

    public static synchronized PlaywrightConfig get() {
        if (instance == null) {
            instance = loadConfig();
            instance.applyOverrides();
        }
        return instance;
    }

    private static PlaywrightConfig loadConfig() {
        String env = resolveActiveEnv();
        String envFile = "env/" + env.toLowerCase() + ".json";

        // 1. Try loading environment-specific configuration
        try (InputStream is = PlaywrightConfig.class.getClassLoader().getResourceAsStream(envFile)) {
            if (is != null) {
                PlaywrightConfig config = new ObjectMapper().readValue(is, PlaywrightConfig.class);
                config.setEnvironment(env.toLowerCase());
                return config;
            }
        } catch (Exception ignored) {
        }

        // 1.1 If local.json is not found (e.g. fresh clone before copying template), try local.json.template
        if ("local".equalsIgnoreCase(env)) {
            try (InputStream is = PlaywrightConfig.class.getClassLoader().getResourceAsStream("env/local.json.template")) {
                if (is != null) {
                    PlaywrightConfig config = new ObjectMapper().readValue(is, PlaywrightConfig.class);
                    config.setEnvironment("local");
                    return config;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Fallback to default playwright.json
        try (InputStream is = PlaywrightConfig.class.getClassLoader().getResourceAsStream(DEFAULT_FILE)) {
            if (is != null) {
                PlaywrightConfig config = new ObjectMapper().readValue(is, PlaywrightConfig.class);
                config.setEnvironment(env.toLowerCase());
                return config;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load environment config " + envFile + " or " + DEFAULT_FILE, e);
        }

        return new PlaywrightConfig();
    }

    private static String resolveActiveEnv() {
        String sysEnv = System.getProperty("env");
        if (sysEnv != null && !sysEnv.isBlank()) {
            return sysEnv.trim();
        }
        String osEnv = System.getenv("ENV");
        if (osEnv != null && !osEnv.isBlank()) {
            return osEnv.trim();
        }
        return "local";
    }

    private void applyOverrides() {
        environment = override("env", environment);
        browser = override("browser", browser);
        headless = Boolean.parseBoolean(override("headless", String.valueOf(headless)));
        slowMo = Integer.parseInt(override("slowMo", String.valueOf(slowMo)));
        baseUrl = override("baseUrl", baseUrl);
        targetUser = override("targetUser", targetUser);
    }

    private static String override(String key, String current) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv(key.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase());
        if (env != null && !env.isBlank()) return env;
        return current;
    }

    public String getEnvironment() { return environment; }
    public String getBrowser() { return browser; }
    public boolean isHeadless() { return headless; }
    public int getSlowMo() { return slowMo; }
    public String getBaseUrl() { return baseUrl.replaceAll("/+$", ""); }
    public double getDefaultTimeoutMs() { return defaultTimeoutMs; }
    public double getPerformanceTimeoutMs() { return performanceTimeoutMs; }
    public String getTargetUsername() { return targetUser; }
    public com.assignment.models.User getTargetUser() { return com.assignment.models.User.fromUsername(targetUser); }
    public Map<String, UserCredentials> getUsers() { return users; }

    public UserCredentials getUserCredentials(String usernameKey) {
        if (users != null && users.containsKey(usernameKey)) {
            UserCredentials creds = users.get(usernameKey);
            String sanitizedUser = usernameKey.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
            String envUpper = environment.toUpperCase();

            // 1. Most specific: Environment + User specific (e.g. SIT_PROBLEM_USER_PASSWORD, DEV_STANDARD_USER_PASSWORD)
            String envUserPass = getPropertyOrEnv(envUpper + "_" + sanitizedUser + "_PASSWORD");
            if (envUserPass != null && !envUserPass.isBlank()) {
                return new UserCredentials(creds.getUsername(), envUserPass);
            }

            // 2. User specific cross-environment (e.g. PROBLEM_USER_PASSWORD, STANDARD_USER_PASSWORD)
            String userPass = getPropertyOrEnv(sanitizedUser + "_PASSWORD");
            if (userPass != null && !userPass.isBlank()) {
                return new UserCredentials(creds.getUsername(), userPass);
            }

            // 3. Environment default password (e.g. SIT_SAUCE_PASSWORD, DEV_SAUCE_PASSWORD)
            String envSpecificPass = getPropertyOrEnv(envUpper + "_SAUCE_PASSWORD");
            if (envSpecificPass != null && !envSpecificPass.isBlank()) {
                return new UserCredentials(creds.getUsername(), envSpecificPass);
            }

            // 4. Global default password (SAUCE_PASSWORD)
            String globalPass = getPropertyOrEnv("SAUCE_PASSWORD");
            if (globalPass != null && !globalPass.isBlank()) {
                return new UserCredentials(creds.getUsername(), globalPass);
            }

            // 5. Config file value (e.g. from local.json) or public demo default
            if (creds.getPassword() != null && !creds.getPassword().isBlank()) {
                return creds;
            }
            return new UserCredentials(creds.getUsername(), "secret_sauce");
        }
        return null;
    }

    private static String getPropertyOrEnv(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp;
        String envVal = System.getenv(key);
        if (envVal != null && !envVal.isBlank()) return envVal;
        return null;
    }

    // Setters for Jackson deserialization
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setHeadless(boolean headless) { this.headless = headless; }
    public void setSlowMo(int slowMo) { this.slowMo = slowMo; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setDefaultTimeoutMs(double v) { this.defaultTimeoutMs = v; }
    public void setPerformanceTimeoutMs(double v) { this.performanceTimeoutMs = v; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
    public void setUsers(Map<String, UserCredentials> users) { this.users = users; }
}
