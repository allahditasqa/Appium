package com.qa.mobile.base;

import com.qa.mobile.config.Config;
import com.qa.mobile.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Starts a session before every test and closes it afterwards. For parallel runs, the TestNG
 * parameters in suites/parallel.xml give every device its own udid and ports.
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
