package com.itzflow.mobile.tests.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.itzflow.mobile.base.BaseTest;
import com.itzflow.mobile.config.Config;
import com.itzflow.mobile.screens.SettingsScreen;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.InteractsWithApps;
import org.testng.annotations.Test;

/**
 * SAMPLE tests (phone ki Settings app). Ye default app.id (Settings) ke liye hain; apni app test karte waqt apna module banao
 * (example: tests/login/LoginTest.java). Docs: docs/test-cases/settings.md
 */
public class SettingsTest extends BaseTest {

  @Test(
      groups = {"smoke", "regression"},
      description = "TC_SETTINGS_01 - Launch the Settings app and check it is in the foreground")
  public void settingsAppIsInForeground() {
    ApplicationState state = ((InteractsWithApps) driver()).queryAppState(Config.get("app.id"));

    assertThat(state).isEqualTo(ApplicationState.RUNNING_IN_FOREGROUND);
  }

  @Test(
      groups = {"smoke", "regression"},
      description = "TC_SETTINGS_02 - Open the Settings app and check the search is visible")
  public void settingsSearchIsVisible() {
    SettingsScreen settings = new SettingsScreen();

    assertThat(settings.isLoaded()).isTrue();
  }

  @Test(
      groups = {"regression"},
      description = "TC_SETTINGS_03 - Search for \"Wi-Fi\" in Settings and check a result with \"Wi-Fi\" is shown")
  public void searchShowsWifiResult() {
    SettingsScreen settings = new SettingsScreen().search("Wi-Fi");

    assertThat(settings.hasResultContaining("Wi")).isTrue();
  }
}
