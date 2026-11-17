#!/usr/bin/env bash
# Environment check for mobile test automation (macOS / Linux / Git Bash).
# Prints what is installed and what is missing, so a setup problem is found in seconds. Usage: ./scripts/doctor.sh
ok()   { printf '  [ok]      %s\n' "$1"; }
warn() { printf '  [missing] %s\n' "$1"; }

echo "Java and build tools"
if command -v java >/dev/null 2>&1; then ok "java: $(java -version 2>&1 | head -1)"; else warn "java (JDK 17 or newer)"; fi
if command -v mvn >/dev/null 2>&1; then ok "maven: $(mvn -v 2>&1 | head -1)"; else warn "maven"; fi

echo "Appium"
if command -v appium >/dev/null 2>&1; then
  ok "appium: $(appium -v 2>&1 | head -1)"
  appium driver list --installed 2>&1 | grep -E "uiautomator2|xcuitest" | sed 's/^/            /' || warn "no Appium driver installed (appium driver install uiautomator2 / xcuitest)"
else
  warn "appium (npm install -g appium, then appium driver install uiautomator2)"
fi

echo "Android"
if [ -n "${ANDROID_HOME:-}${ANDROID_SDK_ROOT:-}" ]; then ok "ANDROID_HOME: ${ANDROID_HOME:-$ANDROID_SDK_ROOT}"; else warn "ANDROID_HOME (path of the Android SDK)"; fi
if command -v adb >/dev/null 2>&1; then
  ok "adb: $(adb version 2>&1 | head -1)"
  devices=$(adb devices | tail -n +2 | grep -w device || true)
  if [ -n "$devices" ]; then ok "connected devices:"; echo "$devices" | sed 's/^/            /'; else warn "no device or emulator connected (adb devices)"; fi
else
  warn "adb (Android platform-tools)"
fi

if [ "$(uname)" = "Darwin" ]; then
  echo "iOS"
  if command -v xcodebuild >/dev/null 2>&1; then ok "xcode: $(xcodebuild -version 2>&1 | head -1)"; else warn "Xcode"; fi
  if command -v xcrun >/dev/null 2>&1; then xcrun simctl list devices booted 2>/dev/null | sed 's/^/            /'; fi
fi
