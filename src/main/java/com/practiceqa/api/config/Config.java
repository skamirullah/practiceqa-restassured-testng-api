package com.practiceqa.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Settings for a run. Each value is looked up as a system property first
 * (-Dapi.port=4300), then an environment variable (API_PORT), then config.properties.
 */
public final class Config {
  private static final Properties FILE = load();

  private Config() {}

  private static Properties load() {
    Properties properties = new Properties();
    try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
      if (in != null) properties.load(in);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read config.properties", e);
    }
    return properties;
  }

  private static String get(String key) {
    String fromSystem = System.getProperty(key);
    if (fromSystem != null && !fromSystem.isBlank()) return fromSystem;
    String fromEnv = System.getenv(key.toUpperCase().replace('.', '_'));
    if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;
    return FILE.getProperty(key, "");
  }

  public static int port() {
    return Integer.parseInt(get("api.port"));
  }

  /** True when the suite should test an API that is already running instead of starting its own. */
  public static boolean usesExternalApi() {
    return !get("api.url").isBlank();
  }

  public static String baseUrl() {
    return usesExternalApi() ? get("api.url") : "http://127.0.0.1:" + port();
  }

  public static String downloadUrl() {
    return get("api.download");
  }
}
