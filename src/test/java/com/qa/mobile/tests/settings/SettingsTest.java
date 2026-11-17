package com.qa.mobile.tests.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.mobile.base.BaseTest;
import com.qa.mobile.config.Config;
import com.qa.mobile.screens.SettingsScreen;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;
import org.testng.annotations.Test;

/**
 * SAMPLE tests for the Settings app of the phone (the default {@code app.id}). For your own app,
 * create your own module, for example {@code tests/login/LoginTest.java}, with its own screens.
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
      description =
          "TC_SETTINGS_03 - Search for \"Wi-Fi\" in Settings and check a result with \"Wi\" is shown")
  public void searchShowsWifiResult() {
    SettingsScreen settings = new SettingsScreen().search("Wi-Fi");

    assertThat(settings.hasResultContaining("Wi")).isTrue();
  }
}
