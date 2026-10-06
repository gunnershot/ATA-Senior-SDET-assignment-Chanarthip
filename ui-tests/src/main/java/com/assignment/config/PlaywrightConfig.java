package com.assignment.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

/**
 * Browser / environment settings loaded once from {@code src/test/resources/playwright.json}.
 * <p>
 * Any value can be overridden without editing the file, which is what CI uses:
 * <pre>
 *   mvn test -Dheadless=true -Dbrowser=firefox
 *   HEADLESS=true mvn test
 * </pre>
 * Precedence: JVM system property &gt; environment variable &gt; playwright.json &gt; built-in default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PlaywrightConfig {

    private static final String FILE = "playwright.json";
    private static PlaywrightConfig instance;

    private String browser = "chromium";
    private boolean headless = true;
    private int slowMo = 0;
    private String baseUrl = "https://www.saucedemo.com";
    private double defaultTimeoutMs = 10_000;
    private double performanceTimeoutMs = 10_000;
    private String targetUser = "standard_user";

    public static synchronized PlaywrightConfig get() {
        if (instance == null) {
            instance = loadFromFile();
            instance.applyOverrides();
        }
        return instance;
    }

    private static PlaywrightConfig loadFromFile() {
        try (InputStream is = PlaywrightConfig.class.getClassLoader().getResourceAsStream(FILE)) {
            return is == null ? new PlaywrightConfig() : new ObjectMapper().readValue(is, PlaywrightConfig.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + FILE, e);
        }
    }

    private void applyOverrides() {
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

    public String getBrowser() { return browser; }
    public boolean isHeadless() { return headless; }
    public int getSlowMo() { return slowMo; }
    public String getBaseUrl() { return baseUrl.replaceAll("/+$", ""); }
    public double getDefaultTimeoutMs() { return defaultTimeoutMs; }
    public double getPerformanceTimeoutMs() { return performanceTimeoutMs; }
    public String getTargetUsername() { return targetUser; }
    public com.assignment.models.User getTargetUser() { return com.assignment.models.User.fromUsername(targetUser); }

    // Setters are used by Jackson only.
    public void setBrowser(String browser) { this.browser = browser; }
    public void setHeadless(boolean headless) { this.headless = headless; }
    public void setSlowMo(int slowMo) { this.slowMo = slowMo; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setDefaultTimeoutMs(double v) { this.defaultTimeoutMs = v; }
    public void setPerformanceTimeoutMs(double v) { this.performanceTimeoutMs = v; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
}

