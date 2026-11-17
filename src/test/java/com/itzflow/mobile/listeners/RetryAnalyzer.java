package com.itzflow.mobile.listeners;

import com.itzflow.mobile.config.Config;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** retry.count (default 0) tak fail hone wale test ko dobara chalata hai. Default par retry nahi: flaky tests chhupte nahi. */
public class RetryAnalyzer implements IRetryAnalyzer {
  private int attempts = 0;

  @Override
  public boolean retry(ITestResult result) {
    return attempts++ < Config.getInt("retry.count", 0);
  }
}
