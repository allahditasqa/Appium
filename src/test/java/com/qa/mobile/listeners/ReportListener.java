package com.qa.mobile.listeners;

import com.qa.mobile.config.Config;
import com.qa.mobile.driver.DriverManager;
import com.qa.mobile.reporting.HtmlReport;
import com.qa.mobile.reporting.TestCaseCatalog;
import com.qa.mobile.utils.ScreenshotUtils;
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
 * Collects the outcome, the screenshot and the error of every test, and writes the module-wise HTML
 * reports when the suite finishes. The screenshot is taken after the test and before the session is
 * closed.
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
    List<Path> files =
        REPORT.write(
            Path.of(Config.get("reports.dir")),
            TestCaseCatalog.load(Path.of("docs", "test-cases")),
            Config.get("platform"),
            Config.get("app.id"));
    files.forEach(file -> LOG.info("Report: {}", file.toAbsolutePath()));
  }

  private void record(ITestResult result, String status) {
    String title =
        result.getMethod().getDescription() == null || result.getMethod().getDescription().isBlank()
            ? result.getMethod().getMethodName()
            : result.getMethod().getDescription();
    Matcher id = ID.matcher(title);
    String screenshot =
        DriverManager.hasDriver() ? ScreenshotUtils.base64(DriverManager.get()) : "";
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
            screenshot,
            result.getTestClass().getRealClass().getSimpleName()
                + "."
                + result.getMethod().getMethodName()));
    LOG.info("{} - {}", status.toUpperCase(), title);
  }

  /**
   * The module is the package below "tests": com.qa.mobile.tests.settings.SettingsTest gives
   * "settings".
   */
  private static String module(ITestResult result) {
    String pkg = result.getTestClass().getRealClass().getPackageName();
    int index = pkg.indexOf(".tests.");
    return index >= 0 ? pkg.substring(index + ".tests.".length()) : "general";
  }
}
