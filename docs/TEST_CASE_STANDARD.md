# Test Case Standard (the same format in every module)

Every test case of this project is written in this format, so anyone can read, review and run a case without help.
Language: simple English, short sentences, exact UI text in quotes.

## 1. Folder structure

| Item | Location | Example |
| --- | --- | --- |
| Test case document (source of truth) | `docs/test-cases/<module-slug>.md` | `docs/test-cases/settings.md` |
| **Master CSV** (regression list, one fixed file per module) | `docs/test-cases/<module-slug>.csv` | `docs/test-cases/settings.csv` |
| Automation code | `src/test/java/com/qa/mobile/tests/<module>/<Name>Test.java` | `SettingsTest.java` |
| Page objects | `src/main/java/com/qa/mobile/screens/` | `SettingsScreen.java` |
| Run report (new file every run) | `reports/<module-slug>/<Module> test <DD-MM-YYYY>_<HH-mm-ss>.html` | `reports/settings/Settings test 18-11-2026_12-16-28.html` |
| Run CSV (cases that ran) | `reports/<module-slug>/<Module> test cases <DD-MM-YYYY>_<HH-mm-ss>.csv` | |
| History of runs | `reports/index.html`, `reports/history.json` | |

## 2. Test case ID

`TC_<MODULE>_<NN>` (NN = 01, 02 ...), for example `TC_SETTINGS_03`. An ID is never used twice or reused.

The ID is the start of the TestNG `description` of the test (the name shown in the report). The text after the ID is the title and must be exactly the Title column of the document:

```java
@Test(groups = {"smoke", "regression"},
      description = "TC_SETTINGS_01 - Launch the Settings app and check it is in the foreground")
public void settingsAppIsInForeground() { ... }
```

| Module | Prefix | Test class |
| --- | --- | --- |
| Session | `TC_SESSION_` | `tests/session/SessionTest.java` |
| Settings (sample app) | `TC_SETTINGS_` | `tests/settings/SettingsTest.java` |

## 3. Document format

1. `# <Module> Test Cases`
2. Header lines: automation files, report location, application URL, ID prefixes, master CSV
3. **Overview**, **Scope and notes**
4. One table per group (`## A. ...`) with the columns below
5. **Summary** (cases and automated per group, total)

| Column | Meaning |
| --- | --- |
| **ID** | Rule above |
| **Title** | One sentence starting with "" |
| **Priority** | `P1 - Critical`, `P2 - High`, `P3 - Medium`, `P4 - Low` |
| **Type** | Smoke, Functional, Negative, Validation, UI |
| **Preconditions** | State before step 1 |
| **Test data** | Exact data, `-` if none |
| **Steps** | Numbered, action based |
| **Expected result** | Clear and checkable |
| **Automation** | `Automated` + `<path of the .java file>::<Class>.<method>` |

Master CSV columns: `ID, Module, Sub-module, Title, Priority, Type, Preconditions, Test Data, Steps, Expected Result, Automation Status, Test File, Test Method, Last Updated`.
The run CSV in `reports/` has the 17 execution columns (status, actual result, duration, platform, app under test ...).

## 4. Rules
1. One case checks one thing.
2. Only automated cases are documented, and every automated case is documented.
3. New case = new ID; never reuse an ID. Removing a case removes test and row together.
4. Known app defects are marked with `@Test(enabled = false)` or an expected-failure note and a reason, and listed in `docs/DEFECTS.md`.

## 5. Keeping documents, CSV and code in sync

| Command | Purpose |
| --- | --- |
| `powershell scripts/Build-CaseCsv.ps1` | Rebuild the master CSV from the `.md` files (unchanged rows keep `Last Updated`) |
| `powershell scripts/Build-CaseCsv.ps1 -Check` | Fail if a CSV is older than its document |
| `./mvnw test -Dsuite=suites/docs.xml` | `DocsTraceabilityTest` (no device needed) compares the test descriptions in the code with the master CSV: ID, title, test class and method |

CI runs the format check, the compile and the traceability suite on every push. The report reads priority, steps, data and expected result from the master CSV by ID.

## 6. Checklist for a new case
- [ ] Row (new ID) added to `docs/test-cases/<module>.md`
- [ ] Test method with `description = "TC_<MODULE>_<NN> - <title>"` identical to the row
- [ ] `Build-CaseCsv.ps1` run, `./mvnw test -Dsuite=suites/docs.xml` green
- [ ] Full run done, report checked, README counts updated
