package com.itzflow.mobile.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * All settings in one place. Override order (the first one wins): per-thread override (TestNG parameter) →
 * {@code -Dkey=value} → environment variable ({@code device.name} becomes {@code DEVICE_NAME}) →
 * {@code config/local.properties} → {@code config/<platform>.properties} → {@code config/default.properties}.
 */
public final class Config {
  private static final Properties BASE = new Properties();
  private static final ThreadLocal<Map<String, String>> OVERRIDES =
      ThreadLocal.withInitial(HashMap::new);

  static {
    load("config/default.properties");
    String platform =
        firstNonBlank(
            System.getProperty("platform"),
            System.getenv("PLATFORM"),
            BASE.getProperty("platform"),
            "android");
    BASE.setProperty("platform", platform.toLowerCase(Locale.ROOT));
    load("config/" + BASE.getProperty("platform") + ".properties");

    Path local = Path.of("config", "local.properties"); // git-ignored, for this machine only
    if (Files.exists(local)) {
      try (InputStream in = Files.newInputStream(local)) {
        BASE.load(in);
      } catch (IOException e) {
        throw new UncheckedIOException("Cannot read " + local, e);
      }
    }
  }

  private Config() {}

  public static String get(String key) {
    String override = OVERRIDES.get().get(key);
    if (override != null) {
      return override;
    }
    String system = System.getProperty(key);
    if (system != null && !system.isBlank()) {
      return system.trim();
    }
    String env = System.getenv(key.toUpperCase(Locale.ROOT).replace('.', '_'));
    if (env != null && !env.isBlank()) {
      return env.trim();
    }
    return BASE.getProperty(key, "").trim();
  }

  public static int getInt(String key, int defaultValue) {
    String value = get(key);
    return value.isBlank() ? defaultValue : Integer.parseInt(value);
  }

  public static boolean getBool(String key) {
    return Boolean.parseBoolean(get(key));
  }

  public static MobilePlatform platform() {
    return MobilePlatform.from(get("platform"));
  }

  /** Overrides values for the current thread only (parallel runs give every device its own udid and port). */
  public static void override(Map<String, String> values) {
    OVERRIDES.get().putAll(values);
  }

  public static void clearOverrides() {
    OVERRIDES.remove();
  }

  private static void load(String resource) {
    try (InputStream in = Config.class.getClassLoader().getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("Config file not found on the classpath: " + resource);
      }
      BASE.load(in);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read " + resource, e);
    }
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "";
  }
}
