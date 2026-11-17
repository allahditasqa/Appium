package com.qa.mobile.screens;

import com.qa.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * SAMPLE screen: the Settings app of the phone. Every Android and iOS device has it, so the framework can be verified
 * without any apk or ipa. For your own app, create screens like this one.
 */
public class SettingsScreen extends BaseScreen {
  // Android: the search button has a content description that starts with "Search" (it differs a little per vendor).
  // iOS: the search field of the Settings app.
  private final By searchButton =
      by(
          AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Search\")"),
          AppiumBy.iOSClassChain("**/XCUIElementTypeSearchField"));

  private final By searchInput =
      by(
          AppiumBy.className("android.widget.EditText"),
          AppiumBy.iOSClassChain("**/XCUIElementTypeSearchField"));

  @Override
  protected By loadedMarker() {
    return searchButton;
  }

  public SettingsScreen search(String term) {
    tap(searchButton);
    type(searchInput, term);
    return this;
  }

  /** True when a search result contains the given text. */
  public boolean hasResultContaining(String text) {
    By result =
        by(
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + text + "\")"),
            AppiumBy.iOSNsPredicateString("label CONTAINS[c] '" + text + "'"));
    return WaitUtils.isVisible(driver, result, 10);
  }
}
