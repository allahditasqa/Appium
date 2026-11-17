package com.itzflow.mobile.screens;

import com.itzflow.mobile.config.MobilePlatform;
import com.itzflow.mobile.driver.DriverManager;
import com.itzflow.mobile.utils.Gestures;
import com.itzflow.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Screen Object Model (POM ka mobile version): har screen ka ek class. Locators aur actions sirf screens mein, tests mein
 * nahi. Android aur iOS ke locators {@link #by(By, By)} se ek hi class mein.
 */
public abstract class BaseScreen {
  protected final AppiumDriver driver = DriverManager.get();
  protected final MobilePlatform platform = DriverManager.platform();
  protected final Logger log = LogManager.getLogger(getClass());

  /** Platform ke hisab se sahi locator. */
  protected By by(By android, By ios) {
    return platform.isAndroid() ? android : ios;
  }

  /** Is screen ke load hone ki nishani (koi ek element jo hamesha dikhta hai). */
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
