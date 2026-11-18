package com.qa.mobile.reporting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.openqa.selenium.json.Json;

/**
 * Module-wise reports in the same layout as the other automation repositories:
 *
 * <pre>
 * reports/index.html                                            history of runs, newest first
 * reports/history.json                                          machine readable history (append only)
 * reports/&lt;module-slug&gt;/&lt;Module&gt; test &lt;DD-MM-YYYY&gt;_&lt;HH-mm-ss&gt;.html
 * reports/&lt;module-slug&gt;/&lt;Module&gt; test cases &lt;DD-MM-YYYY&gt;_&lt;HH-mm-ss&gt;.csv
 * </pre>
 *
 * Every report is one self-contained file (screenshots embedded). A name that already exists gets a
 * " (2)" suffix, so an old report is never overwritten.
 */
public final class HtmlReport {

  private static final String[] CSV_COLUMNS = {
    "Test Case ID",
    "Module",
    "Test Case Title",
    "Priority",
    "Test Type",
    "Preconditions",
    "Test Steps",
    "Test Data",
    "Expected Result",
    "Actual Result",
    "Execution Status",
    "Execution Type",
    "Automation Method",
    "Duration (s)",
    "Executed On",
    "Platform",
    "App Under Test"
  };

  /** The outcome of one test. {@code method} is "Class.method". */
  public record Result(
      String id,
      String title,
      String module,
      String group,
      String status,
      long millis,
      String error,
      String screenshotBase64,
      String method) {}

  private final List<Result> results = Collections.synchronizedList(new ArrayList<>());
  private final LocalDateTime started = LocalDateTime.now();

  public void add(Result result) {
    results.add(result);
  }

  /**
   * Writes one HTML report and one CSV per module, appends the run to history.json and rebuilds
   * index.html. Returns the written files.
   */
  public List<Path> write(
      Path reportsDir, Map<String, Map<String, String>> cases, String platform, String app) {
    String stamp = started.format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
    Map<String, List<Result>> byModule = new LinkedHashMap<>();
    synchronized (results) {
      for (Result r : results) {
        byModule.computeIfAbsent(r.module(), k -> new ArrayList<>()).add(r);
      }
    }
    List<Path> written = new ArrayList<>();
    List<Map<String, Object>> modules = new ArrayList<>();
    try {
      for (Map.Entry<String, List<Result>> entry : byModule.entrySet()) {
        String module = pretty(entry.getKey());
        List<Result> list = new ArrayList<>(entry.getValue());
        list.sort(Comparator.comparing(Result::id, String.CASE_INSENSITIVE_ORDER));
        Path dir = reportsDir.resolve(entry.getKey());
        Files.createDirectories(dir);
        Path html = unique(dir, module + " test " + stamp, ".html");
        Path csv = unique(dir, module + " test cases " + stamp, ".csv");
        Files.writeString(
            html, render(module, platform, started, list, cases), StandardCharsets.UTF_8);
        Files.writeString(csv, csv(module, platform, app, list, cases), StandardCharsets.UTF_8);
        written.add(html);
        written.add(csv);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Module", module);
        m.put("ReportPath", entry.getKey() + "/" + html.getFileName());
        m.put("CsvPath", entry.getKey() + "/" + csv.getFileName());
        m.put("Total", list.size());
        m.put("Passed", count(list, "passed"));
        m.put("Failed", count(list, "failed"));
        m.put("Skipped", count(list, "skipped"));
        m.put("DurationSeconds", list.stream().mapToLong(Result::millis).sum() / 1000.0);
        modules.add(m);
      }
      Map<String, Object> run = new LinkedHashMap<>();
      run.put("RunId", started.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
      run.put("StartedAt", started.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      run.put(
          "DurationSeconds", Duration.between(started, LocalDateTime.now()).toMillis() / 1000.0);
      run.put("Platform", platform);
      run.put("Modules", modules);
      written.addAll(appendHistoryAndIndex(reportsDir, run));
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot write the reports", e);
    }
    return written;
  }

  private static List<Path> appendHistoryAndIndex(Path reportsDir, Map<String, Object> run)
      throws IOException {
    Files.createDirectories(reportsDir);
    Path historyFile = reportsDir.resolve("history.json");
    List<Map<String, Object>> history = new ArrayList<>();
    if (Files.exists(historyFile)) {
      List<Map<String, Object>> old =
          new Json().toType(Files.readString(historyFile), Json.LIST_OF_MAPS_TYPE);
      history.addAll(old);
    }
    history.add(run);
    Files.writeString(historyFile, new Json().toJson(history), StandardCharsets.UTF_8);
    Path index = reportsDir.resolve("index.html");
    Files.writeString(index, renderIndex(history), StandardCharsets.UTF_8);
    return List.of(historyFile, index);
  }

  @SuppressWarnings("unchecked")
  private static String renderIndex(List<Map<String, Object>> history) {
    StringBuilder sb = new StringBuilder();
    sb.append(
            "<h1>Appium Mobile - Test Reports</h1><div class=\"meta\">History: newest run on top. ")
        .append("Under each date-time you see the module-wise reports. ")
        .append("Old runs are never overwritten or deleted.</div>");
    List<Map<String, Object>> sorted = new ArrayList<>(history);
    sorted.sort(
        (a, b) -> String.valueOf(b.get("StartedAt")).compareTo(String.valueOf(a.get("StartedAt"))));
    for (Map<String, Object> run : sorted) {
      List<Map<String, Object>> modules = (List<Map<String, Object>>) run.get("Modules");
      long failed = sum(modules, "Failed");
      LocalDateTime when = LocalDateTime.parse(String.valueOf(run.get("StartedAt")));
      sb.append("<h2>")
          .append(when.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
          .append(" &bull; ")
          .append(when.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
          .append(" <span class=\"meta\">&nbsp;")
          .append(esc(String.valueOf(run.getOrDefault("Platform", ""))))
          .append(" &middot; total ")
          .append(sum(modules, "Total"))
          .append(" &middot; <span class=\"pass-t\">")
          .append(sum(modules, "Passed"))
          .append(" pass</span> &middot; <span class=\"")
          .append(failed > 0 ? "fail-t" : "")
          .append("\">")
          .append(failed)
          .append(" fail</span> &middot; <span class=\"skip-t\">")
          .append(sum(modules, "Skipped"))
          .append(" skip</span></span></h2>")
          .append("<table><thead><tr><th>Module</th><th>Total</th><th>Pass</th><th>Fail</th>")
          .append("<th>Skip</th><th>Result</th><th>Report</th><th>Test cases</th></tr></thead>")
          .append("<tbody>");
      for (Map<String, Object> m : modules) {
        boolean ok = ((Number) m.get("Failed")).longValue() == 0;
        sb.append("<tr><td>")
            .append(esc(String.valueOf(m.get("Module"))))
            .append("</td><td>")
            .append(m.get("Total"))
            .append("</td><td class=\"pass-t\">")
            .append(m.get("Passed"))
            .append("</td><td class=\"")
            .append(ok ? "" : "fail-t")
            .append("\">")
            .append(m.get("Failed"))
            .append("</td><td class=\"skip-t\">")
            .append(m.get("Skipped"))
            .append("</td><td class=\"")
            .append(ok ? "pass-t" : "fail-t")
            .append("\">")
            .append(ok ? "Passed" : "Failed")
            .append("</td><td><a href=\"")
            .append(href(String.valueOf(m.get("ReportPath"))))
            .append("\">HTML</a></td><td><a href=\"")
            .append(href(String.valueOf(m.get("CsvPath"))))
            .append("\">CSV</a></td></tr>");
      }
      sb.append("</tbody></table>");
    }
    return page("Appium Mobile - Test Reports", sb.toString());
  }

  private static String csv(
      String module,
      String platform,
      String app,
      List<Result> list,
      Map<String, Map<String, String>> cases) {
    StringBuilder sb = new StringBuilder("﻿");
    sb.append(String.join(",", java.util.Arrays.stream(CSV_COLUMNS).map(HtmlReport::q).toList()))
        .append("\r\n");
    String executed =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    for (Result r : list) {
      Map<String, String> c = cases.getOrDefault(r.id(), Map.of());
      String actual =
          r.status().equals("passed")
              ? "As expected (all assertions passed)"
              : r.status().equals("failed") ? r.error() : "Test was not executed: " + r.error();
      String status =
          r.status().equals("passed") ? "Pass" : r.status().equals("failed") ? "Fail" : "Skipped";
      String[] values = {
        r.id(),
        module,
        r.title(),
        c.getOrDefault("Priority", ""),
        c.getOrDefault("Type", ""),
        c.getOrDefault("Preconditions", ""),
        c.getOrDefault("Steps", ""),
        c.getOrDefault("Test Data", ""),
        c.getOrDefault("Expected Result", ""),
        actual,
        status,
        "Automated",
        r.method(),
        String.format(Locale.ROOT, "%.1f", r.millis() / 1000.0),
        executed,
        platform,
        app
      };
      sb.append(String.join(",", java.util.Arrays.stream(values).map(HtmlReport::q).toList()))
          .append("\r\n");
    }
    return sb.toString();
  }

  private static String q(String v) {
    String text = v == null ? "" : v.replace("\"", "\"\"").replaceAll("[\\r\\n]+", " ");
    return "\"" + text + "\"";
  }

  private static Path unique(Path dir, String base, String ext) {
    Path path = dir.resolve(base + ext);
    for (int i = 2; Files.exists(path); i++) {
      path = dir.resolve(base + " (" + i + ")" + ext);
    }
    return path;
  }

  private static long count(List<Result> list, String status) {
    return list.stream().filter(r -> r.status().equals(status)).count();
  }

  private static long sum(List<Map<String, Object>> modules, String key) {
    return modules.stream().mapToLong(m -> ((Number) m.get(key)).longValue()).sum();
  }

  private static String href(String path) {
    return URLEncoder.encode(path, StandardCharsets.UTF_8).replace("%2F", "/").replace("+", "%20");
  }

  private static String page(String title, String body) {
    return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
        + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>"
        + esc(title)
        + "</title><style>"
        + STYLE
        + "</style></head><body><main>"
        + body
        + "</main></body></html>";
  }

  private static String render(
      String module,
      String platform,
      LocalDateTime now,
      List<Result> list,
      Map<String, Map<String, String>> cases) {
    String cards =
        list.stream().map(r -> card(r, cases.get(r.id()))).collect(Collectors.joining("\n"));
    double seconds = list.stream().mapToLong(Result::millis).sum() / 1000.0;
    String overall = count(list, "failed") > 0 ? "failed" : "passed";
    String stamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    String body =
        "<p class=\"meta\"><a href=\"../index.html\">&larr; All reports</a></p><h1>"
            + esc(module)
            + "</h1><div class=\"meta\">Run: "
            + stamp
            + " &middot; platform "
            + esc(platform)
            + " &middot; duration "
            + String.format(Locale.ROOT, "%.0f", seconds)
            + "s &middot; overall: "
            + overall
            + " &middot; click a screenshot to enlarge</div>"
            + "<div class=\"sum\"><div><b>"
            + list.size()
            + "</b>total</div><div><b class=\"pass-t\">"
            + count(list, "passed")
            + "</b>passed</div><div><b class=\"fail-t\">"
            + count(list, "failed")
            + "</b>failed</div><div><b class=\"skip-t\">"
            + count(list, "skipped")
            + "</b>skipped</div></div>"
            + cards;
    return page(module + " Test Report " + stamp, body);
  }

  private static String card(Result r, Map<String, String> c) {
    String cls =
        r.status().equals("passed") ? "pass" : r.status().equals("skipped") ? "skip" : "fail";
    String shot =
        r.screenshotBase64().isBlank()
            ? "<p class=\"meta\">No screenshot</p>"
            : "<div class=\"shots\"><img src=\"data:image/png;base64,"
                + r.screenshotBase64()
                + "\" alt=\"screenshot\" onclick=\"this.classList.toggle('big')\"></div>";
    String error = r.error().isBlank() ? "" : "<pre>" + esc(r.error()) + "</pre>";
    String details = "";
    if (c != null) {
      details =
          "<div class=\"meta\">"
              + esc(c.get("Priority"))
              + " &middot; "
              + esc(c.get("Type"))
              + "</div><details><summary class=\"meta\">Test case details</summary><table>"
              + "<tr><th>Preconditions</th><td>"
              + esc(c.get("Preconditions"))
              + "</td></tr><tr><th>Test steps</th><td>"
              + esc(c.get("Steps"))
              + "</td></tr><tr><th>Test data</th><td>"
              + esc(c.get("Test Data"))
              + "</td></tr><tr><th>Expected result</th><td>"
              + esc(c.get("Expected Result"))
              + "</td></tr></table></details>";
    }
    return "<section class=\"card "
        + cls
        + "\"><div class=\"head\"><span class=\"badge "
        + cls
        + "\">"
        + esc(r.status())
        + "</span><b>"
        + esc(r.title())
        + "</b><span class=\"meta\">"
        + esc(r.method())
        + " &middot; "
        + String.format(Locale.ROOT, "%.1f", r.millis() / 1000.0)
        + "s</span></div>"
        + details
        + error
        + shot
        + "</section>";
  }

  private static String pretty(String s) {
    String spaced = s.replace('-', ' ').replace('_', ' ');
    return spaced.isEmpty()
        ? spaced
        : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
  }

  private static String esc(String s) {
    return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static final String STYLE =
      """
      :root{--bg:#f6f7f9;--fg:#1c1f24;--card:#fff;--line:#dde1e6;--pass:#1a7f37;--fail:#cf222e;--skip:#8a6d00;--mute:#6b7380}
      @media(prefers-color-scheme:dark){:root{--bg:#14161a;--fg:#e6e8eb;--card:#1d2026;--line:#2e333b;--pass:#3fb950;--fail:#ff7b72;--skip:#d4a72c;--mute:#9aa3b0}}
      body{margin:0;padding:24px 16px;background:var(--bg);color:var(--fg);font:15px/1.5 system-ui,Segoe UI,sans-serif}
      main{max-width:1000px;margin:auto}h1{margin:0 0 4px}.meta{color:var(--mute);font-size:13px}
      .sum{display:flex;gap:12px;flex-wrap:wrap;margin:16px 0}.sum div{background:var(--card);border:1px solid var(--line);border-radius:8px;padding:10px 16px}
      .sum b{font-size:22px;display:block}.card{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--line);border-radius:8px;padding:12px 16px;margin:12px 0}
      .card.pass{border-left-color:var(--pass)}.card.fail{border-left-color:var(--fail)}.card.skip{border-left-color:var(--skip)}
      .head{display:flex;gap:10px;align-items:baseline;flex-wrap:wrap}.badge{font-size:12px;text-transform:uppercase;padding:2px 8px;border-radius:99px;border:1px solid currentColor}
      .badge.pass{color:var(--pass)}.badge.fail{color:var(--fail)}.badge.skip{color:var(--skip)}
      pre{background:var(--bg);padding:10px;border-radius:6px;overflow:auto;white-space:pre-wrap;color:var(--fail)}
      .shots{margin-top:10px}img{max-width:220px;border:1px solid var(--line);border-radius:6px;cursor:zoom-in}img.big{max-width:100%;cursor:zoom-out}
      table{width:100%;border-collapse:collapse;background:var(--card);border:1px solid var(--line)}th,td{text-align:left;padding:8px 12px;border-bottom:1px solid var(--line)}
      h2{margin:28px 0 8px}a{color:inherit}
      .pass-t{color:var(--pass)}.fail-t{color:var(--fail)}.skip-t{color:var(--skip)}
      """;
}
