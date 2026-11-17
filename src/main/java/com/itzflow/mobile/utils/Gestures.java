package com.itzflow.mobile.utils;

import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

/** Touch gestures (W3C Actions): android aur iOS dono par ek jaise chalte hain. */
public final class Gestures {
  /** Ungli ki direction (UP = ungli neeche se upar, yani content upar scroll hota hai). */
  public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
  }

  private static final PointerInput FINGER = new PointerInput(PointerInput.Kind.TOUCH, "finger");

  private Gestures() {}

  /** Screen ke beech se swipe. percent: 0.1 se 0.9 (screen ka kitna hissa). */
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

  /** Content neeche ki taraf scroll (ungli upar). */
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

  private static org.openqa.selenium.interactions.Interaction move(
      Duration duration, int x, int y) {
    return FINGER.createPointerMove(duration, PointerInput.Origin.viewport(), x, y);
  }
}
