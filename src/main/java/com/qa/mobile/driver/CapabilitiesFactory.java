package com.qa.mobile.driver;

import com.qa.mobile.config.Config;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.openqa.selenium.MutableCapabilities;

/**
 * Builds the Appium capabilities from the configuration (Android UiAutomator2, iOS XCUITest, cloud
 * extras).
 */
public final class CapabilitiesFactory {
  private CapabilitiesFactory() {}

  public static UiAutomator2Options android() {
    UiAutomator2Options options =
        new UiAutomator2Options()
            .setDeviceName(Config.get("device.name"))
            .setNewCommandTimeout(
                Duration.ofSeconds(Config.getInt("new.command.timeout.seconds", 300)))
            .setAutoGrantPermissions(true)
            .setNoReset(Config.getBool("no.reset"));
    ifPresent(Config.get("platform.version"), options::setPlatformVersion);
    ifPresent(Config.get("udid"), options::setUdid);

    String appPath = Config.get("app.path");
    if (!appPath.isBlank()) {
      options.setApp(
          Path.of(appPath).toAbsolutePath().toString()); // the apk is installed and started
    } else {
      options.setAppPackage(Config.get("app.id")); // an app that is already installed on the device
      ifPresent(Config.get("app.activity"), options::setAppActivity);
      // Every test must start on the app's home screen. With noReset the app keeps the screen the
      // previous test left, so restart it for each session (its data is NOT cleared).
      options.setCapability("appium:forceAppLaunch", true);
      options.setCapability("appium:shouldTerminateApp", true);
    }
    int systemPort = Config.getInt("system.port", 0);
    if (systemPort > 0) {
      options.setSystemPort(systemPort); // parallel runs: every device has its own port
    }
    applyCloud(options);
    return options;
  }

  public static XCUITestOptions ios() {
    XCUITestOptions options =
        new XCUITestOptions()
            .setDeviceName(Config.get("device.name"))
            .setNewCommandTimeout(
                Duration.ofSeconds(Config.getInt("new.command.timeout.seconds", 300)))
            .setNoReset(Config.getBool("no.reset"));
    ifPresent(Config.get("platform.version"), options::setPlatformVersion);
    ifPresent(Config.get("udid"), options::setUdid);
    // Signing for WebDriverAgent on a real device (set as raw capabilities)
    ifPresent(Config.get("xcode.org.id"), id -> options.setCapability("appium:xcodeOrgId", id));
    ifPresent(
        Config.get("xcode.signing.id"), id -> options.setCapability("appium:xcodeSigningId", id));

    String appPath = Config.get("app.path");
    if (!appPath.isBlank()) {
      options.setApp(Path.of(appPath).toAbsolutePath().toString());
    } else {
      options.setBundleId(Config.get("app.id"));
    }
    int wdaPort = Config.getInt("wda.port", 0);
    if (wdaPort > 0) {
      options.setWdaLocalPort(wdaPort); // parallel runs: every device has its own port
    }
    applyCloud(options);
    return options;
  }

  /**
   * BrowserStack "bstack:options". Credentials come only from the environment, never from a file.
   */
  private static void applyCloud(MutableCapabilities capabilities) {
    if (!"browserstack".equalsIgnoreCase(Config.get("cloud.provider"))) {
      return;
    }
    Map<String, Object> bstack = new HashMap<>();
    bstack.put("userName", required("browserstack.username"));
    bstack.put("accessKey", required("browserstack.access.key"));
    bstack.put("projectName", Config.get("cloud.project"));
    bstack.put("buildName", Config.get("cloud.build"));
    capabilities.setCapability("bstack:options", bstack);
  }

  private static String required(String key) {
    String value = Config.get(key);
    if (value.isBlank()) {
      throw new IllegalStateException(
          "Missing setting '"
              + key
              + "' (set the environment variable "
              + key.toUpperCase().replace('.', '_')
              + ")");
    }
    return value;
  }

  private static void ifPresent(String value, Consumer<String> setter) {
    if (!value.isBlank()) {
      setter.accept(value);
    }
  }
}
