package com.qa.mobile.listeners;

import com.qa.mobile.config.Config;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Repeats a failing test up to {@code retry.count} times (default 0, so flaky tests stay visible). */
public class RetryAnalyzer implements IRetryAnalyzer {
  private int attempts = 0;

  @Override
  public boolean retry(ITestResult result) {
    return attempts++ < Config.getInt("retry.count", 0);
  }
}
