# Test Strategy: Mobile Test Automation (Appium + Java + TestNG)

**Version:** 1.0  |  **Prepared by:** QA (SDET)

## 1. Objective
A reusable framework that runs the same style of tests on Android (UiAutomator2) and iOS (XCUITest), on a real device, an emulator/simulator or a cloud grid, with written test cases and module-wise reports linked by the test case ID.

## 2. Application under test
The framework ships with sample tests for the **Settings app** of the phone (it exists on every device, so the framework can be checked without an app file). Your own app is added with `-Dapp.path=...` or `-Dapp.id=...` and its own module of tests.

## 3. Scope

| Module | Test class | Cases | Type |
| --- | --- | --- | --- |
| Session | `SessionTest` | 2 | Smoke, Functional |
| Settings (sample) | `SettingsTest` | 3 | Smoke, UI, Functional |
| **Total** | | **5** | |

Out of scope for the sample: business flows of a real app, performance, push notifications, biometrics, deep links.

## 4. Approach
Screen Object Model (`screens/`), a fresh session per test (`BaseTest`), explicit waits only (`WaitUtils`), configuration by layers (command line, environment, `local.properties`, platform file, defaults), TestNG groups `smoke` (P1) and `regression` (all), suites in `src/test/resources/suites`. Every test has a screenshot in its report. Retries are off by default (`retry.count=0`).

## 5. Environment
JDK 17+, Node.js 20+, Appium 2 with the UiAutomator2 (Android) and XCUITest (iOS, Mac only) drivers, Android SDK / adb or Xcode. Check with `scripts/doctor.sh` or `scripts/doctor.ps1`.

## 6. Entry and exit criteria
Entry: `./mvnw spotless:check test-compile` is green, `./mvnw test -Dsuite=suites/docs.xml` is green, a device or emulator is listed by `adb devices`. Exit: all P1 cases pass, every failure is fixed or logged in `docs/DEFECTS.md`, final run with no retries.

## 7. Risks

| Risk | Mitigation |
| --- | --- |
| Vendor-specific Settings UI (locators differ) | Locators only in `SettingsScreen`; accessibility id and id first |
| Device state (language, popups, locked screen) | Preconditions in each case; fresh session per test |
| No device in CI | CI only compiles, checks format and documents; the emulator smoke job is started by hand |

## 8. Deliverables
Test case documents and master CSV, automation code, module-wise reports with history (`reports/`, from real device runs only), traceability suite, CI pipeline.
