package com.qa.mobile.driver;

import com.qa.mobile.config.Config;
import com.qa.mobile.config.MobilePlatform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;
import java.time.Duration;

/** Creates the right driver (AndroidDriver or IOSDriver) for a platform. */
public final class DriverFactory {
  private DriverFactory() {}

  public static AppiumDriver create(MobilePlatform platform) {
    URL server = AppiumServerManager.serverUrl();
    AppiumDriver driver =
        switch (platform) {
          case ANDROID -> new AndroidDriver(server, CapabilitiesFactory.android());
          case IOS -> new IOSDriver(server, CapabilitiesFactory.ios());
        };
    driver
        .manage()
        .timeouts()
        .implicitlyWait(Duration.ofSeconds(Config.getInt("implicit.wait.seconds", 0)));
    return driver;
  }
}
