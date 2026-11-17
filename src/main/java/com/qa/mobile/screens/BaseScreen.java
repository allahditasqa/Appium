package com.qa.mobile.screens;

import com.qa.mobile.config.MobilePlatform;
import com.qa.mobile.driver.DriverManager;
import com.qa.mobile.utils.Gestures;
import com.qa.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Screen Object Model, the mobile version of the Page Object Model: one class per screen. Locators and actions live in
 * screens, never in tests. Android and iOS locators sit in the same class, chosen by {@link #by(By, By)}.
 */
public abstract class BaseScreen {
  protected final AppiumDriver driver = DriverManager.get();
  protected final MobilePlatform platform = DriverManager.platform();
  protected final Logger log = LogManager.getLogger(getClass());

  /** The locator that fits the current platform. */
  protected By by(By android, By ios) {
    return platform.isAndroid() ? android : ios;
  }

  /** An element that is always visible once the screen has loaded. */
  protected abstract By loadedMarker();

  public boolean isLoaded() {
    return WaitUtils.isVisible(driver, loadedMarker(), 15);
  }

  protected WebElement waitVisible(By locator) {
    return WaitUtils.visible(driver, locator);
  }

  protected void tap(By locator) {
    log.info("Tap {}", locator);
    WaitUtils.clickable(driver, locator).click();
  }

  protected void type(By locator, String text) {
    log.info("Type '{}' into {}", text, locator);
    WebElement element = waitVisible(locator);
    element.click();
    element.clear();
    element.sendKeys(text);
  }

  protected String textOf(By locator) {
    return waitVisible(locator).getText();
  }

  protected boolean isDisplayed(By locator) {
    try {
      return driver.findElement(locator).isDisplayed();
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  protected void scrollDown() {
    Gestures.scrollDown(driver);
  }
}
