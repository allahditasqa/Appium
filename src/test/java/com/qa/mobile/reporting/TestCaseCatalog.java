package com.qa.mobile.reporting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The written test cases: the master CSV files in {@code docs/test-cases}, by test case ID. Used by
 * the report (steps, expected result ...) and by the traceability check.
 */
public final class TestCaseCatalog {

  private TestCaseCatalog() {}

  /** Reads every master CSV; the key is the test case ID, the value maps column name to text. */
  public static Map<String, Map<String, String>> load(Path casesDir) {
    Map<String, Map<String, String>> cases = new LinkedHashMap<>();
    if (!Files.isDirectory(casesDir)) {
      return cases;
    }
    try (Stream<Path> files = Files.list(casesDir)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".csv")).sorted().toList()) {
        List<List<String>> rows = parse(Files.readString(file, StandardCharsets.UTF_8));
        List<String> header = rows.get(0);
        for (List<String> row : rows.subList(1, rows.size())) {
          if (row.size() != header.size()) {
            continue;
          }
          Map<String, String> map = new LinkedHashMap<>();
          for (int i = 0; i < header.size(); i++) {
            map.put(header.get(i), row.get(i));
          }
          map.put("_file", file.getFileName().toString());
          cases.put(map.get("ID"), map);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot read the test case CSV files in " + casesDir, e);
    }
    return cases;
  }

  /** Minimal CSV parser: quoted fields, doubled quotes, CR LF or LF line ends. */
  static List<List<String>> parse(String text) {
    List<List<String>> rows = new ArrayList<>();
    List<String> row = new ArrayList<>();
    StringBuilder cell = new StringBuilder();
    boolean quoted = false;
    String s = text.startsWith("\uFEFF") ? text.substring(1) : text;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (quoted) {
        if (c == '"' && i + 1 < s.length() && s.charAt(i + 1) == '"') {
          cell.append('"');
          i++;
        } else if (c == '"') {
          quoted = false;
        } else {
          cell.append(c);
        }
      } else if (c == '"') {
        quoted = true;
      } else if (c == ',') {
        row.add(cell.toString());
        cell.setLength(0);
      } else if (c == '\n') {
        row.add(cell.toString());
        cell.setLength(0);
        rows.add(row);
        row = new ArrayList<>();
      } else if (c != '\r') {
        cell.append(c);
      }
    }
    if (cell.length() > 0 || !row.isEmpty()) {
      row.add(cell.toString());
      rows.add(row);
    }
    return rows;
  }
}
