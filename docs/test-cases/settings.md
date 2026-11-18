# Settings Test Cases

**Automation:** `src/test/java/com/qa/mobile/tests/settings/SettingsTest.java`  |  **Report:** `reports/settings/Settings test <DD-MM-YYYY>_<HH-mm-ss>.html` (all reports: `reports/index.html`)
**Platforms:** Android (UiAutomator2), iOS (XCUITest)  |  **ID prefix:** `TC_SETTINGS_`  |  **Master CSV:** `docs/test-cases/settings.csv`

## Overview
Sample cases for the Settings app of the phone (the default `app.id`). They show how a module of tests, a screen object and a document fit together; for your own app write your own module.

## Scope and notes
- Locators can differ slightly between device vendors and OS versions. If a sample case fails on one device, adjust the locator in `SettingsScreen`.
- Run with the default `app.id` (the Settings app). Cases need a real device, emulator or simulator.

## A. Launch and search

| ID | Title | Priority | Type | Preconditions | Test data | Steps | Expected result | Automation |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TC_SETTINGS_01 | Launch the Settings app and check it is in the foreground | P1 - Critical | Smoke | A session for the Settings app is running | App id: `com.android.settings` (Android), `com.apple.Preferences` (iOS) | 1. T<br>2. h<br>3. e<br>4.  <br>5. a<br>6. p<br>7. p<br>8.  <br>9. s<br>10. t<br>11. a<br>12. t<br>13. e<br>14.  <br>15. i<br>16. s<br>17.  <br>18. "<br>19. r<br>20. u<br>21. n<br>22. n<br>23. i<br>24. n<br>25. g<br>26.  <br>27. i<br>28. n<br>29.  <br>30. f<br>31. o<br>32. r<br>33. e<br>34. g<br>35. r<br>36. o<br>37. u<br>38. n<br>39. d<br>40. "<br>41. . | ['Start the session.', 'Query the state of the Settings app.'] | Automated `src/test/java/com/qa/mobile/tests/settings/SettingsTest.java::SettingsTest.settingsAppIsInForeground` |
| TC_SETTINGS_02 | Open the Settings app and check the search is visible | P1 - Critical | UI | A session for the Settings app is running | - | 1. T<br>2. h<br>3. e<br>4.  <br>5. s<br>6. e<br>7. a<br>8. r<br>9. c<br>10. h<br>11.  <br>12. b<br>13. u<br>14. t<br>15. t<br>16. o<br>17. n<br>18.  <br>19. (<br>20. A<br>21. n<br>22. d<br>23. r<br>24. o<br>25. i<br>26. d<br>27. )<br>28.  <br>29. o<br>30. r<br>31.  <br>32. s<br>33. e<br>34. a<br>35. r<br>36. c<br>37. h<br>38.  <br>39. f<br>40. i<br>41. e<br>42. l<br>43. d<br>44.  <br>45. (<br>46. i<br>47. O<br>48. S<br>49. )<br>50.  <br>51. i<br>52. s<br>53.  <br>54. v<br>55. i<br>56. s<br>57. i<br>58. b<br>59. l<br>60. e<br>61. . | ['Start the session.', 'Check the search control.'] | Automated `src/test/java/com/qa/mobile/tests/settings/SettingsTest.java::SettingsTest.settingsSearchIsVisible` |
| TC_SETTINGS_03 | Search for "Wi-Fi" in Settings and check a result with "Wi" is shown | P3 - Medium | Functional | The Settings main screen is open | Search text: "Wi-Fi" | 1. A<br>2. t<br>3.  <br>4. l<br>5. e<br>6. a<br>7. s<br>8. t<br>9.  <br>10. o<br>11. n<br>12. e<br>13.  <br>14. r<br>15. e<br>16. s<br>17. u<br>18. l<br>19. t<br>20.  <br>21. t<br>22. h<br>23. a<br>24. t<br>25.  <br>26. c<br>27. o<br>28. n<br>29. t<br>30. a<br>31. i<br>32. n<br>33. s<br>34.  <br>35. "<br>36. W<br>37. i<br>38. "<br>39.  <br>40. i<br>41. s<br>42.  <br>43. s<br>44. h<br>45. o<br>46. w<br>47. n<br>48. . | ['Tap the search.', 'Type `Wi-Fi`.'] | Automated `src/test/java/com/qa/mobile/tests/settings/SettingsTest.java::SettingsTest.searchShowsWifiResult` |

## Summary

| Group | Cases | Automated |
| --- | --- | --- |
| A. Launch and search | 3 | 3 |
| **Total** | **3** | **3** |
