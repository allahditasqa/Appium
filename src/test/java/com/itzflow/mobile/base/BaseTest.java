package com.itzflow.mobile.base;

import com.itzflow.mobile.config.Config;
import com.itzflow.mobile.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Har test se pehle session start, baad mein band. Parallel runs ke liye TestNG parameters (suites/parallel.xml) se har device
 * ko apni udid/port milti hai.
 */
public abstract class BaseTest {

  @Parameters({"udid", "deviceName", "systemPort", "wdaPort"})
  @BeforeMethod(alwaysRun = true)
  public void setUp(
      @Optional String udid,
      @Optional String deviceName,
      @Optional String systemPort,
      @Optional String wdaPort) {
    Map<String, String> overrides = new HashMap<>();
    putIfPresent(overrides, "udid", udid);
    putIfPresent(overrides, "device.name", deviceName);
    putIfPresent(overrides, "system.port", systemPort);
    putIfPresent(overrides, "wda.port", wdaPort);
    Config.override(overrides);

    DriverManager.start(Config.platform());
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    DriverManager.quit();
    Config.clearOverrides();
  }

  protected AppiumDriver driver() {
    return DriverManager.get();
  }

  private static void putIfPresent(Map<String, String> map, String key, String value) {
    if (value != null && !value.isBlank()) {
      map.put(key, value);
    }
  }
}
