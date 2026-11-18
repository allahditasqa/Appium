# Mobile Test Automation Framework: Appium + Java + TestNG

[![CI](https://github.com/allahditasqa/Appium/actions/workflows/ci.yml/badge.svg)](https://github.com/allahditasqa/Appium/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Appium](https://img.shields.io/badge/Appium-2%20%2F%203-662D91)
![TestNG](https://img.shields.io/badge/TestNG-7.12-FF6F00)
![Platforms](https://img.shields.io/badge/platforms-Android%20%2B%20iOS-brightgreen)

A complete mobile test automation framework for **Android and iOS apps**, written in **Java** with **Appium**, **TestNG** and
Maven. One code base, one set of tests, two platforms: the platform is chosen with a single flag. It runs on real devices,
emulators and simulators, and on a cloud grid.

> **Sibling frameworks:** [Playwright](https://github.com/allahditasqa/Playwright) (web) and [Cypress](https://github.com/allahditasqa/Cypress) (web).

## What this repository demonstrates

- **Screen Object Model:** one class per screen; Android and iOS locators live side by side in the same class (`by(android, ios)`), so a test is written once.
- **Thread-safe parallel execution:** a `ThreadLocal` driver per thread, and a TestNG suite that gives every device its own `udid`, `systemPort` and `wdaPort`.
- **Layered configuration:** defaults → platform file → `local.properties` → environment variable → `-D` flag → TestNG parameter. No value is hard-coded in a test.
- **Appium server management:** uses a server that is already running, or starts one itself and stops it when the run ends.
- **Cloud ready:** BrowserStack capabilities are added from environment variables (credentials never live in a file).
- **Reports a team can read:** a self-contained HTML report **per module** with a screenshot for every test, named with the run date and time.
- **Quality gates:** Google Java Format enforced by Spotless, and a GitHub Actions pipeline that can run the smoke suite on an Android emulator.

## Tech stack

| Area | Tool |
| --- | --- |
| Language and build | Java 17, Maven (with wrapper) |
| Mobile automation | Appium 2 / 3, Appium Java client 11, UiAutomator2 (Android), XCUITest (iOS) |
| Test runner | TestNG 7.12 with suite files |
| Assertions | AssertJ |
| Logging | Log4j 2 |
| Format | Spotless + Google Java Format |
| CI | GitHub Actions |

## Architecture

```
src/test/java/.../tests/<module>/    WHAT to check: test classes (steps and assertions only)
        │ extends
src/test/java/.../base/BaseTest      starts a session before each test and closes it after
        │ uses
src/main/java/.../screens/           HOW to talk to the app: screen objects with Android + iOS locators
src/main/java/.../utils/             waits, gestures (W3C Actions), screenshots
src/main/java/.../driver/            DriverFactory, DriverManager (ThreadLocal), AppiumServerManager, CapabilitiesFactory
src/main/java/.../config/            Config (layered settings), MobilePlatform
src/main/resources/config/           default.properties, android.properties, ios.properties
src/test/resources/suites/           smoke.xml, regression.xml, parallel.xml
src/test/java/.../listeners/         ReportListener (screenshots + HTML), RetryAnalyzer
```

### Design decisions (and why)

| Decision | Why |
| --- | --- |
| Locators only in screen classes | An app change is fixed in one file, never in every test |
| One class for both platforms | Shared behaviour is written once; only the locator differs |
| Explicit waits only, no `Thread.sleep` | Waits end as soon as the screen is ready, so tests are fast and stable |
| W3C Actions for gestures | Works on Android and iOS without platform-specific scripts |
| Retries off by default (`retry.count=0`) | A flaky test must be visible, not hidden |
| Settings in `.properties` plus overrides | Switching device, app or server needs no code change |
| Reports and logs are git-ignored | Device screenshots can contain private data |

## Project structure

```
.
├── apps/                  put your .apk / .app / .ipa here (git-ignored)
├── docs/test-cases/       test case documents (standard format)
├── scripts/               doctor.ps1 / doctor.sh: checks Java, Maven, Appium, adb, devices
├── src/
│   ├── main/java/com/qa/mobile/{config,driver,screens,utils}
│   ├── main/resources/config/
│   └── test/java/com/qa/mobile/{base,listeners,reporting,tests}
├── .github/workflows/ci.yml
├── mvnw, mvnw.cmd         Maven wrapper (no Maven install needed)
└── pom.xml
```

## Getting started

**Prerequisites:** JDK 17+, Node.js 20+, and for Android the Android SDK (`adb`); for iOS a Mac with Xcode.

```bash
npm install -g appium
appium driver install uiautomator2      # Android
appium driver install xcuitest          # iOS (Mac only)

./scripts/doctor.sh                     # Windows: ./scripts/doctor.ps1   -> shows what is missing
./mvnw test -Dplatform=android          # smoke suite on the connected device or emulator
```

By default the sample tests run against the **Settings app** of the phone, because it exists on every Android and iOS
device: the framework can be verified without any app file. To test your own app:

```bash
./mvnw test -Dplatform=android -Dapp.path=apps/my-app.apk
./mvnw test -Dplatform=ios     -Dapp.path=apps/MyApp.app -Ddevice.name="iPhone 16"
```

| Goal | Command |
| --- | --- |
| Smoke suite (High priority) | `./mvnw test -Dsuite=suites/smoke.xml` |
| Regression suite | `./mvnw test -Dsuite=suites/regression.xml` |
| iOS instead of Android | add `-Dplatform=ios` |
| A specific device | `-Dudid=<udid>` (see `adb devices`) |
| Several devices at once | `./mvnw test -Dsuite=suites/parallel.xml` (edit the udids in the suite file) |
| Cloud grid | `-Dappium.server.mode=cloud -Dcloud.provider=browserstack -Dappium.server.url=https://hub-cloud.browserstack.com/wd/hub` plus `BROWSERSTACK_USERNAME` and `BROWSERSTACK_ACCESS_KEY` |
| Fix code format | `./mvnw spotless:apply` |

## Configuration

Every setting has a default and can be overridden. The first match wins:

`TestNG parameter` → `-Dkey=value` → `environment variable (DEVICE_NAME)` → `config/local.properties` → `<platform>.properties` → `default.properties`

| Key | Meaning | Default |
| --- | --- | --- |
| `platform` | `android` or `ios` | `android` |
| `app.path` / `app.id` | app file to install, or package / bundle id of an installed app | Settings app |
| `device.name`, `udid`, `platform.version` | which device | the connected one |
| `appium.server.mode` | `local`, `remote` or `cloud` | `local` |
| `explicit.wait.seconds` | default wait | `20` |
| `retry.count` | repeat a failing test | `0` |
| `no.reset` | keep app data between tests | `true` |

## Writing a test for your own app

```java
// 1. A screen object: locators for both platforms, and actions
public class LoginScreen extends BaseScreen {
  private final By username = by(AppiumBy.id("com.example:id/username"), AppiumBy.accessibilityId("username"));
  private final By signIn   = by(AppiumBy.id("com.example:id/sign_in"),  AppiumBy.accessibilityId("Sign in"));

  @Override protected By loadedMarker() { return signIn; }

  public void login(String user, String password) {
    type(username, user);
    // ...
    tap(signIn);
  }
}

// 2. A test: only steps and assertions
public class LoginTest extends BaseTest {
  @Test(groups = {"smoke", "regression"},
        description = "TC_LOGIN_01 - Login with a valid username and password and check the home screen opens")
  public void validLogin() {
    new LoginScreen().login("demo", "secret");
    assertThat(new HomeScreen().isLoaded()).isTrue();
  }
}
```

## Reporting

Every run writes module-wise reports and a history page. A report name that already exists gets a ` (2)` suffix, so old reports are never overwritten.

```
reports/
├── index.html                 # history of runs, newest first, links to every module report and CSV
├── history.json               # machine readable history (append only)
└── <module>/                  # session, settings, ...
    ├── <Module> test <DD-MM-YYYY>_<HH-mm-ss>.html        # status, written steps, error, screenshot per test
    └── <Module> test cases <DD-MM-YYYY>_<HH-mm-ss>.csv   # 17 columns, same as the Selenium repos
```

* **How to generate:** `./mvnw test -Dplatform=android` (the `ReportListener` writes everything at the end of the suite). Logs: `logs/test.log`.
* **Status of committed reports:** none yet. The reporting code was compile-checked and its documents were verified, but a run against a device could not be completed in the environment where this layout was added (the session setup failed with `Unable to establish loopback connection` before any test started), so **no report is committed**. Only reports from real device runs are committed to `reports/`; they may contain screenshots of the device screen, so check them first.

## Test cases

Every automated test has a written test case; documents, master CSV and code share the same ID.

| What | Where |
| --- | --- |
| Standard | [`docs/TEST_CASE_STANDARD.md`](docs/TEST_CASE_STANDARD.md) |
| Strategy | [`docs/TEST_STRATEGY.md`](docs/TEST_STRATEGY.md) |
| Documents (2 modules, **5 cases**, 5 automated) | [Session](docs/test-cases/session.md), [Settings (sample app)](docs/test-cases/settings.md) |
| Master CSV per module | `docs/test-cases/*.csv` |

* ID `TC_<MODULE>_<NN>` is the start of the TestNG `description`; the title after it equals the document title.
* `powershell scripts/Build-CaseCsv.ps1` builds the CSV from the documents (`-Check` only verifies).
* `./mvnw test -Dsuite=suites/docs.xml` runs `DocsTraceabilityTest` (no device needed) and fails if code and documents differ.

## CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml):

1. **Format check, compile and document traceability** on every push and pull request (`spotless:check test-compile`, then `test -Dsuite=suites/docs.xml`).
2. **Android smoke on an emulator**, started manually (`Run workflow`): installs Appium and the UiAutomator2 driver,
   boots an emulator with hardware acceleration, runs the smoke suite and uploads the reports.

## Roadmap

- Page-level test data builders and a second sample app module
- Allure reporting on top of the HTML report
- Visual checks of key screens
