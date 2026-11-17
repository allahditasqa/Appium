package com.qa.mobile.utils;

import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Interaction;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

/** Touch gestures built with W3C Actions, so they behave the same on Android and iOS. */
public final class Gestures {
  /** Direction of the finger (UP = the finger moves from the bottom to the top, so the content scrolls up). */
  public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
  }

  private static final PointerInput FINGER = new PointerInput(PointerInput.Kind.TOUCH, "finger");

  private Gestures() {}

  /** Swipes from the centre of the screen. {@code percent} is the part of the screen to cross (0.1 to 0.9). */
  public static void swipe(AppiumDriver driver, Direction direction, double percent) {
    Dimension size = driver.manage().window().getSize();
    int centerX = size.width / 2;
    int centerY = size.height / 2;
    int distanceX = (int) (size.width * percent / 2);
    int distanceY = (int) (size.height * percent / 2);

    int startX = centerX;
    int startY = centerY;
    int endX = centerX;
    int endY = centerY;
    switch (direction) {
      case UP -> {
        startY = centerY + distanceY;
        endY = centerY - distanceY;
      }
      case DOWN -> {
        startY = centerY - distanceY;
        endY = centerY + distanceY;
      }
      case LEFT -> {
        startX = centerX + distanceX;
        endX = centerX - distanceX;
      }
      case RIGHT -> {
        startX = centerX - distanceX;
        endX = centerX + distanceX;
      }
    }
    drag(driver, startX, startY, endX, endY, Duration.ofMillis(600));
  }

  /** Scrolls the content down (the finger moves up). */
  public static void scrollDown(AppiumDriver driver) {
    swipe(driver, Direction.UP, 0.6);
  }

  public static void tap(AppiumDriver driver, int x, int y) {
    Sequence tap = new Sequence(FINGER, 1);
    tap.addAction(move(Duration.ZERO, x, y));
    tap.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    tap.addAction(new Pause(FINGER, Duration.ofMillis(80)));
    tap.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    driver.perform(List.of(tap));
  }

  public static void drag(
      AppiumDriver driver, int startX, int startY, int endX, int endY, Duration duration) {
    Sequence drag = new Sequence(FINGER, 1);
    drag.addAction(move(Duration.ZERO, startX, startY));
    drag.addAction(FINGER.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    drag.addAction(move(duration, endX, endY));
    drag.addAction(FINGER.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    driver.perform(List.of(drag));
  }

  private static Interaction move(Duration duration, int x, int y) {
    return FINGER.createPointerMove(duration, PointerInput.Origin.viewport(), x, y);
  }
}
