package com.qa.mobile.tests.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.mobile.base.BaseTest;
import com.qa.mobile.config.Config;
import com.qa.mobile.utils.ScreenshotUtils;
import org.testng.annotations.Test;

/** Health check of the framework and the device: whatever the app is, a session must start. */
public class SessionTest extends BaseTest {

  @Test(
      groups = {"smoke", "regression"},
      description =
          "TC_SESSION_01 - Start a session and check the platform name matches the requested platform")
  public void sessionMatchesRequestedPlatform() {
    String actual = driver().getCapabilities().getPlatformName().toString();

    assertThat(actual).isEqualToIgnoringCase(Config.platform().name());
  }

  @Test(
      groups = {"regression"},
      description =
          "TC_SESSION_02 - Take a screenshot of the device screen and check it is not empty")
  public void screenshotIsNotEmpty() {
    String screenshot = ScreenshotUtils.base64(driver());

    assertThat(screenshot).isNotBlank();
  }
}
