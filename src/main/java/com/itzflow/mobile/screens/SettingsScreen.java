package com.itzflow.mobile.screens;

import com.itzflow.mobile.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * SAMPLE screen: phone ki apni Settings app (har Android/iOS device par maujood, isliye framework bina kisi apk ke verify ho
 * jata hai). Apni app ke liye isi tarah ka naya screen class banao.
 */
public class SettingsScreen extends BaseScreen {
  // Android: search button ka content-description "Search..." hota hai (OEM ke hisab se thoda alag).
  // iOS: Settings ka search field.
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

  /** Search ke results mein diya hua text dikhta hai ya nahi. */
  public boolean hasResultContaining(String text) {
    By result =
        by(
            AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + text + "\")"),
            AppiumBy.iOSNsPredicateString("label CONTAINS[c] '" + text + "'"));
    return WaitUtils.isVisible(driver, result, 10);
  }
}
