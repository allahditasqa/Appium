package com.qa.mobile.utils;

import com.qa.mobile.config.Config;
import io.appium.java_client.AppiumDriver;
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
    return new WebDriverWait(
            driver, Duration.ofSeconds(Config.getInt("explicit.wait.seconds", 20)))
        .until(ExpectedConditions.elementToBeClickable(locator));
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
