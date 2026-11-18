# Session Test Cases

**Automation:** `src/test/java/com/qa/mobile/tests/session/SessionTest.java`  |  **Report:** `reports/session/Session test <DD-MM-YYYY>_<HH-mm-ss>.html` (all reports: `reports/index.html`)
**Platforms:** Android (UiAutomator2), iOS (XCUITest)  |  **ID prefix:** `TC_SESSION_`  |  **Master CSV:** `docs/test-cases/session.csv`

## Overview
A health check of the framework and the device. It does not depend on the app under test: if these cases fail, nothing else can run.

## Scope and notes
- Runs on the platform chosen with `-Dplatform=android|ios`.
- These cases need a real device, emulator or simulator; they cannot run without one.

## A. Session

| ID | Title | Priority | Type | Preconditions | Test data | Steps | Expected result | Automation |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TC_SESSION_01 | Start a session and check the platform name matches the requested platform | P1 - Critical | Smoke | A device or emulator is connected and the Appium server can start | Platform: `android` or `ios` | 1. T<br>2. h<br>3. e<br>4.  <br>5. p<br>6. l<br>7. a<br>8. t<br>9. f<br>10. o<br>11. r<br>12. m<br>13.  <br>14. n<br>15. a<br>16. m<br>17. e<br>18.  <br>19. e<br>20. q<br>21. u<br>22. a<br>23. l<br>24. s<br>25.  <br>26. t<br>27. h<br>28. e<br>29.  <br>30. r<br>31. e<br>32. q<br>33. u<br>34. e<br>35. s<br>36. t<br>37. e<br>38. d<br>39.  <br>40. p<br>41. l<br>42. a<br>43. t<br>44. f<br>45. o<br>46. r<br>47. m<br>48.  <br>49. (<br>50. A<br>51. n<br>52. d<br>53. r<br>54. o<br>55. i<br>56. d<br>57.  <br>58. o<br>59. r<br>60.  <br>61. i<br>62. O<br>63. S<br>64. )<br>65. ,<br>66.  <br>67. i<br>68. g<br>69. n<br>70. o<br>71. r<br>72. i<br>73. n<br>74. g<br>75.  <br>76. c<br>77. a<br>78. s<br>79. e<br>80. . | ['Start a session for the chosen platform.', 'Read the platform name from the session.'] | Automated `src/test/java/com/qa/mobile/tests/session/SessionTest.java::SessionTest.sessionMatchesRequestedPlatform` |
| TC_SESSION_02 | Take a screenshot of the device screen and check it is not empty | P3 - Medium | Functional | A session is running | - | 1. T<br>2. h<br>3. e<br>4.  <br>5. s<br>6. c<br>7. r<br>8. e<br>9. e<br>10. n<br>11. s<br>12. h<br>13. o<br>14. t<br>15.  <br>16. c<br>17. o<br>18. n<br>19. t<br>20. a<br>21. i<br>22. n<br>23. s<br>24.  <br>25. i<br>26. m<br>27. a<br>28. g<br>29. e<br>30.  <br>31. d<br>32. a<br>33. t<br>34. a<br>35. . | ['Take a screenshot.'] | Automated `src/test/java/com/qa/mobile/tests/session/SessionTest.java::SessionTest.screenshotIsNotEmpty` |

## Summary

| Group | Cases | Automated |
| --- | --- | --- |
| A. Session | 2 | 2 |
| **Total** | **2** | **2** |
