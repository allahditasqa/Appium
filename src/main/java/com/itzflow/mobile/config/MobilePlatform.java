package com.itzflow.mobile.config;

import java.util.Locale;

/** The platforms the framework supports. */
public enum MobilePlatform {
  ANDROID,
  IOS;

  public static MobilePlatform from(String value) {
    return switch (value.trim().toLowerCase(Locale.ROOT)) {
      case "android" -> ANDROID;
      case "ios" -> IOS;
      default ->
          throw new IllegalArgumentException(
              "Unknown platform '" + value + "'. Use -Dplatform=android or -Dplatform=ios");
    };
  }

  public boolean isAndroid() {
    return this == ANDROID;
  }

  public boolean isIos() {
    return this == IOS;
  }
}
