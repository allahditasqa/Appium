package com.qa.mobile.utils;

import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public final class ScreenshotUtils {
  private static final Logger LOG = LogManager.getLogger(ScreenshotUtils.class);

  private ScreenshotUtils() {}

  /** The screen as a base64 PNG (to embed in a report). An empty string if the screenshot fails. */
  public static String base64(AppiumDriver driver) {
    try {
      return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    } catch (RuntimeException e) {
      LOG.warn("Could not take a screenshot: {}", e.getMessage());
      return "";
    }
  }
}
