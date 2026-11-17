package com.qa.mobile.driver;

import com.qa.mobile.config.MobilePlatform;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Every thread gets its own driver (ThreadLocal), so parallel runs on several devices never
 * interfere.
 */
public final class DriverManager {
  private static final Logger LOG = LogManager.getLogger(DriverManager.class);
  private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();
  private static final ThreadLocal<MobilePlatform> PLATFORM = new ThreadLocal<>();

  private DriverManager() {}

  public static AppiumDriver start(MobilePlatform platform) {
    quit(); // close a session that was left open
    AppiumDriver driver = DriverFactory.create(platform);
    DRIVER.set(driver);
    PLATFORM.set(platform);
    LOG.info("Started {} session {}", platform, driver.getSessionId());
    return driver;
  }

  public static AppiumDriver get() {
    AppiumDriver driver = DRIVER.get();
    if (driver == null) {
      throw new IllegalStateException(
          "No driver on this thread. Start one with DriverManager.start() (BaseTest does this).");
    }
    return driver;
  }

  public static boolean hasDriver() {
    return DRIVER.get() != null;
  }

  public static MobilePlatform platform() {
    MobilePlatform platform = PLATFORM.get();
    if (platform == null) {
      throw new IllegalStateException("No platform on this thread. Start a driver first.");
    }
    return platform;
  }

  public static void quit() {
    AppiumDriver driver = DRIVER.get();
    if (driver != null) {
      try {
        driver.quit();
      } catch (RuntimeException e) {
        LOG.warn("Could not close the session cleanly: {}", e.getMessage());
      }
    }
    DRIVER.remove();
    PLATFORM.remove();
  }
}
