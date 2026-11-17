# Settings (sample app) - Test Cases

**Automation:** `src/test/java/com/qa/mobile/tests/settings/SettingsTest.java`  |  **Report:** `reports/settings/settings <platform> test <DD-MM-YYYY>_<HH-mm>.html`

## Overview
Sample cases for the Settings app that exists on every Android and iOS device. They prove that the framework (server, driver, screens, waits, reports) works end to end without any app binary. Replace them with the cases of your own app.

## Scope and notes
- Locators can differ slightly between device vendors and OS versions. If a sample case fails on one device, adjust the locator in `SettingsScreen`.
- Run with the default `app.id` (the Settings app). For your own app, write your own module.

## A. Launch and search

| ID | Title | Priority | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- | --- |
| TC_SETTINGS_01 | Launch the Settings app and check it is in the foreground | High | A session for the Settings app is running | 1. Start the session. | The app state is "running in foreground". |
| TC_SETTINGS_02 | Open the Settings app and check the search is visible | High | A session for the Settings app is running | 1. Start the session. | The search button (Android) or search field (iOS) is visible. |
| TC_SETTINGS_03 | Search for "Wi-Fi" in Settings and check a result with "Wi" is shown | Medium | The Settings main screen is open | 1. Tap the search. 2. Type `Wi-Fi`. | At least one result that contains "Wi" is shown. |
