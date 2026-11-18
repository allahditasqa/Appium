package com.qa.mobile.utils;

import com.qa.mobile.config.Config;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Explicit waits. There is no Thread.sleep anywhere: every wait is for a condition. */
public final class WaitUtils {
  private WaitUtils() {}

  public static WebElement visible(AppiumDriver driver, By locator) {
    return visible(driver, locator, Config.getInt("explicit.wait.seconds", 20));
  }

  public static WebElement visible(AppiumDriver driver, By locator, int seconds) {
    return new WebDriverWait(driver, Duration.ofSeconds(seconds))
        .until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  public static WebElement clickable(AppiumDriver driver, By locator) {
    return new WebDriverWait(driver, Duration.ofSeconds(Config.getInt("explicit.wait.seconds", 20)))
        .until(ExpectedConditions.elementToBeClickable(locator));
  }

  /**
   * Waits until the app reaches the foreground and returns its last seen state. The app is started
   * asynchronously by the session, so the state is polled instead of read once.
   */
  public static ApplicationState waitForForeground(AppiumDriver driver, String appId, int seconds) {
    ApplicationState[] last = new ApplicationState[1];
    try {
      new WebDriverWait(driver, Duration.ofSeconds(seconds))
          .until(
              d -> {
                last[0] = ((InteractsWithApps) d).queryAppState(appId);
                return last[0] == ApplicationState.RUNNING_IN_FOREGROUND;
              });
    } catch (TimeoutException e) {
      // the last seen state is returned to the caller for the assertion message
    }
    return last[0];
  }

  /** True when the element becomes visible in time; false on timeout (no exception). */
  public static boolean isVisible(AppiumDriver driver, By locator, int seconds) {
    try {
      visible(driver, locator, seconds);
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }
}
