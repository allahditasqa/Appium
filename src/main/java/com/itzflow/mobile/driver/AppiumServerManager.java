package com.itzflow.mobile.driver;

import com.itzflow.mobile.config.Config;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides the URL of the Appium server.
 *
 * <ul>
 *   <li>mode {@code local}: use the server that is already running, otherwise start one (stopped when the JVM exits)
 *   <li>mode {@code remote} or {@code cloud}: use {@code appium.server.url} (your own server or a cloud grid)
 * </ul>
 */
public final class AppiumServerManager {
  private static final Logger LOG = LogManager.getLogger(AppiumServerManager.class);
  private static AppiumDriverLocalService service;

  private AppiumServerManager() {}

  public static synchronized URL serverUrl() {
    String mode = Config.get("appium.server.mode");
    String configured = Config.get("appium.server.url");
    try {
      if (!"local".equalsIgnoreCase(mode)) {
        return URI.create(configured).toURL();
      }
      if (isUp(configured)) {
        LOG.info("Using the Appium server that is already running at {}", configured);
        return URI.create(configured).toURL();
      }
      if (service == null || !service.isRunning()) {
        startLocal();
      }
      return service.getUrl();
    } catch (IOException e) {
      throw new IllegalStateException("Bad Appium server URL: " + configured, e);
    }
  }

  private static void startLocal() {
    AppiumServiceBuilder builder =
        new AppiumServiceBuilder()
            .withIPAddress("127.0.0.1")
            .usingPort(Config.getInt("appium.local.port", 4723))
            .withArgument(GeneralServerFlag.LOG_LEVEL, "error");
    String mainJs = Config.get("appium.main.js");
    if (!mainJs.isBlank()) {
      builder.withAppiumJS(new File(mainJs));
    }
    service = AppiumDriverLocalService.buildService(builder);
    service.start();
    LOG.info("Started a local Appium server at {}", service.getUrl());
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  if (service != null && service.isRunning()) {
                    service.stop();
                  }
                }));
  }

  private static boolean isUp(String baseUrl) {
    try {
      HttpResponse<Void> response =
          HttpClient.newBuilder()
              .connectTimeout(Duration.ofSeconds(2))
              .build()
              .send(
                  HttpRequest.newBuilder(URI.create(baseUrl + "/status"))
                      .timeout(Duration.ofSeconds(3))
                      .GET()
                      .build(),
                  HttpResponse.BodyHandlers.discarding());
      return response.statusCode() == 200;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
