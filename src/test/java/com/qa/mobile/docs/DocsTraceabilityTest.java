package com.qa.mobile.docs;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.mobile.reporting.TestCaseCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.testng.annotations.Test;

/**
 * Traceability check (no device needed): the automated tests and the written test cases in {@code
 * docs/test-cases} must match 1:1 - same ID, title and test method. Run it alone with {@code ./mvnw
 * test -Dsuite=suites/docs.xml}.
 */
public class DocsTraceabilityTest {

  /** A test method: its description (the "TC_X_NN - title" text) and its name. */
  private static final Pattern TEST_METHOD =
      Pattern.compile(
          "description\\s*=\\s*((?:\"(?:\\\\.|[^\"\\\\])*\"\\s*\\+?\\s*)+)\\)\\s*public void (\\w+)\\(");

  private static final Pattern STRING_PART = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern ID_TITLE = Pattern.compile("^(TC_[A-Z0-9]+_\\d+) - (.+)$");
  private static final Path TESTS = Path.of("src/test/java/com/qa/mobile/tests");

  @Test(
      groups = "docs",
      description = "TC_DOCS_01 - Automated tests and documented test cases match")
  public void automatedTestsAndDocumentedCasesMatch() throws IOException {
    Map<String, Map<String, String>> docs = TestCaseCatalog.load(Path.of("docs/test-cases"));
    Map<String, String[]> code = new TreeMap<>(); // id -> {title, file, Class.method}
    List<String> problems = new ArrayList<>();

    try (Stream<Path> files = Files.walk(TESTS)) {
      for (Path file : files.filter(f -> f.toString().endsWith("Test.java")).toList()) {
        String source = Files.readString(file);
        String cls = file.getFileName().toString().replace(".java", "");
        Matcher m = TEST_METHOD.matcher(source);
        while (m.find()) {
          StringBuilder description = new StringBuilder();
          Matcher part = STRING_PART.matcher(m.group(1));
          while (part.find()) {
            description.append(part.group(1).replace("\\\"", "\""));
          }
          Matcher idTitle = ID_TITLE.matcher(description);
          if (!idTitle.matches()) {
            problems.add(
                cls + "." + m.group(2) + ": description must start with 'TC_<MODULE>_<NN> - '");
            continue;
          }
          String path = file.toString().replace('\\', '/');
          String[] previous =
              code.put(
                  idTitle.group(1), new String[] {idTitle.group(2), path, cls + "." + m.group(2)});
          if (previous != null) {
            problems.add(idTitle.group(1) + ": used by more than one test");
          }
        }
      }
    }

    code.keySet().stream()
        .filter(id -> !docs.containsKey(id))
        .forEach(
            id -> problems.add(id + ": automated in " + code.get(id)[2] + " but NOT documented"));
    docs.keySet().stream()
        .filter(id -> !code.containsKey(id))
        .forEach(
            id ->
                problems.add(
                    id + ": documented in " + docs.get(id).get("_file") + " but NOT automated"));
    code.forEach(
        (id, c) -> {
          Map<String, String> doc = docs.get(id);
          if (doc == null) {
            return;
          }
          if (!c[0].equals(doc.get("Title"))) {
            problems.add(
                id + ": title differs (code '" + c[0] + "' / doc '" + doc.get("Title") + "')");
          }
          if (!c[1].equals(doc.get("Test File")) || !c[2].equals(doc.get("Test Method"))) {
            problems.add(id + ": automation reference differs (code " + c[1] + "::" + c[2] + ")");
          }
        });

    System.out.println("tests in code: " + code.size() + " | cases in docs: " + docs.size());
    assertThat(problems).as("Traceability problems").isEmpty();
  }
}
