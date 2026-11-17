# Session - Test Cases

**Automation:** `src/test/java/com/qa/mobile/tests/session/SessionTest.java`  |  **Report:** `reports/session/session <platform> test <DD-MM-YYYY>_<HH-mm>.html`

## Overview
A health check of the framework and the device. It does not depend on the app under test: if these cases fail, nothing else can run.

## A. Session

| ID | Title | Priority | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- | --- |
| TC_SESSION_01 | Start a session and check the platform name matches the requested platform | High | A device or emulator is connected and the Appium server can start | 1. Start a session for the chosen platform. 2. Read the platform name from the session. | The platform name equals the requested platform (Android or iOS). |
| TC_SESSION_02 | Take a screenshot of the device screen and check it is not empty | Medium | A session is running | 1. Take a screenshot. | The screenshot contains image data. |
