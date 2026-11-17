package com.itzflow.mobile.listeners;

import com.itzflow.mobile.config.Config;
import com.itzflow.mobile.driver.DriverManager;
import com.itzflow.mobile.reporting.HtmlReport;
import com.itzflow.mobile.utils.ScreenshotUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Har test ka natija, screenshot aur error report mein jama karta hai; suite khatam hone par module-wise HTML report likhta
 * hai. Screenshot test ke baad aur session band hone se pehle liya jata hai.
 */
public class ReportListener implements ITestListener, ISuiteListener {
  private static final Logger LOG = LogManager.getLogger(ReportListener.class);
  private static final Pattern ID = Pattern.compile("TC_[A-Z0-9_.]+");
  private static final HtmlReport REPORT = new HtmlReport();

  @Override
  public void onTestSuccess(ITestResult result) {
    record(result, "passed");
  }

  @Override
  public void onTestFailure(ITestResult result) {
    record(result, "failed");
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    if (!result.wasRetried()) {
      record(result, "skipped");
    }
  }

  @Override
  public void onFinish(ISuite suite) {
    List<Path> files = REPORT.write(Path.of(Config.get("reports.dir")), Config.get("platform"));
    files.forEach(file -> LOG.info("Report: {}", file.toAbsolutePath()));
  }

  private void record(ITestResult result, String status) {
    String title =
        result.getMethod().getDescription() == null || result.getMethod().getDescription().isBlank()
            ? result.getMethod().getMethodName()
            : result.getMethod().getDescription();
    Matcher id = ID.matcher(title);
    String screenshot = DriverManager.hasDriver() ? ScreenshotUtils.base64(DriverManager.get()) : "";
    Throwable error = result.getThrowable();
    REPORT.add(
        new HtmlReport.Result(
            id.find() ? id.group() : result.getMethod().getMethodName(),
            title,
            module(result),
            result.getTestClass().getRealClass().getSimpleName(),
            status,
            result.getEndMillis() - result.getStartMillis(),
            error == null ? "" : String.valueOf(error.getMessage()),
            screenshot));
    LOG.info("{} - {}", status.toUpperCase(), title);
  }

  /** Package ke naam se module: ...tests.settings.SettingsTest → "settings". */
  private static String module(ITestResult result) {
    String pkg = result.getTestClass().getRealClass().getPackageName();
    int index = pkg.indexOf(".tests.");
    return index >= 0 ? pkg.substring(index + ".tests.".length()) : "general";
  }
}
