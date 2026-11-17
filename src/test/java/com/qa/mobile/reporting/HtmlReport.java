package com.qa.mobile.reporting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * One self-contained HTML report per module (screenshots embedded, opens without a server). The file name contains
 * the date and time: {@code reports/<module>/<module> <platform> test DD-MM-YYYY_HH-mm.html}, so an old report is
 * never overwritten.
 */
public final class HtmlReport {

  /** The outcome of one test. */
  public record Result(
      String id,
      String title,
      String module,
      String group,
      String status,
      long millis,
      String error,
      String screenshotBase64) {}

  private final List<Result> results = Collections.synchronizedList(new ArrayList<>());

  public void add(Result result) {
    results.add(result);
  }

  /** Writes one report per module and returns the files. */
  public List<Path> write(Path reportsDir, String platform) {
    LocalDateTime now = LocalDateTime.now();
    String stamp = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));
    Map<String, List<Result>> byModule = new LinkedHashMap<>();
    synchronized (results) {
      for (Result r : results) {
        byModule.computeIfAbsent(r.module(), k -> new ArrayList<>()).add(r);
      }
    }
    List<Path> written = new ArrayList<>();
    for (Map.Entry<String, List<Result>> entry : byModule.entrySet()) {
      String module = entry.getKey();
      Path dir = reportsDir.resolve(module);
      try {
        Files.createDirectories(dir);
        String base = module + " " + platform + " test " + stamp;
        Path file = dir.resolve(base + ".html");
        if (Files.exists(file)) {
          file = dir.resolve(base + "-" + now.format(DateTimeFormatter.ofPattern("ss")) + ".html");
        }
        Files.writeString(
            file, render(module, platform, now, entry.getValue()), StandardCharsets.UTF_8);
        written.add(file);
      } catch (IOException e) {
        throw new UncheckedIOException("Cannot write the report for " + module, e);
      }
    }
    return written;
  }

  private static String render(
      String module, String platform, LocalDateTime now, List<Result> list) {
    long passed = list.stream().filter(r -> r.status().equals("passed")).count();
    long failed = list.stream().filter(r -> r.status().equals("failed")).count();
    long skipped = list.stream().filter(r -> r.status().equals("skipped")).count();
    String cards =
        list.stream()
            .sorted((a, b) -> a.id().compareToIgnoreCase(b.id()))
            .map(HtmlReport::card)
            .collect(Collectors.joining("\n"));
    return """
        <!doctype html><html lang="en"><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>%1$s %2$s Test Report</title><style>%3$s</style></head><body><main>
        <h1>%1$s &rsaquo; %2$s</h1>
        <div class="meta">Run: %4$s &middot; click a screenshot to enlarge</div>
        <div class="sum"><div><b>%5$d</b>total</div><div><b class="pass-t">%6$d</b>passed</div>
        <div><b class="fail-t">%7$d</b>failed</div><div><b class="skip-t">%8$d</b>skipped</div></div>
        %9$s
        </main></body></html>
        """
        .formatted(
            esc(pretty(module)),
            esc(platform),
            STYLE,
            now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
            list.size(),
            passed,
            failed,
            skipped,
            cards);
  }

  private static String card(Result r) {
    String cls =
        r.status().equals("passed") ? "pass" : r.status().equals("skipped") ? "skip" : "fail";
    String shot =
        r.screenshotBase64().isBlank()
            ? "<p class=\"meta\">No screenshot</p>"
            : "<div class=\"shots\"><img src=\"data:image/png;base64,"
                + r.screenshotBase64()
                + "\" alt=\"screenshot\" onclick=\"this.classList.toggle('big')\"></div>";
    String error = r.error().isBlank() ? "" : "<pre>" + esc(r.error()) + "</pre>";
    return "<section class=\"card "
        + cls
        + "\"><div class=\"head\"><span class=\"badge "
        + cls
        + "\">"
        + esc(r.status())
        + "</span><b>"
        + esc(r.title())
        + "</b><span class=\"meta\">"
        + esc(r.group())
        + " &middot; "
        + String.format("%.1f", r.millis() / 1000.0)
        + "s</span></div>"
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
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
      .pass-t{color:var(--pass)}.fail-t{color:var(--fail)}.skip-t{color:var(--skip)}
      """;
}
