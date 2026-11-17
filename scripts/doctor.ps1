# Environment check for mobile test automation (Windows PowerShell).
# Prints what is installed and what is missing, so a setup problem is found in seconds.  Usage: ./scripts/doctor.ps1
function Ok($text)   { Write-Host "  [ok]      $text" -ForegroundColor Green }
function Warn($text) { Write-Host "  [missing] $text" -ForegroundColor Yellow }

Write-Host "Java and build tools"
if (Get-Command java -ErrorAction SilentlyContinue) { Ok ("java: " + (& java -version 2>&1 | Select-Object -First 1)) } else { Warn "java (JDK 17 or newer)" }
if (Get-Command mvn -ErrorAction SilentlyContinue) { Ok ("maven: " + (& mvn -v 2>&1 | Select-Object -First 1)) } else { Warn "maven" }

Write-Host "Appium"
if (Get-Command appium -ErrorAction SilentlyContinue) {
  Ok ("appium: " + (& appium -v 2>&1 | Select-Object -First 1))
  $drivers = & appium driver list --installed 2>&1 | Select-String "uiautomator2|xcuitest"
  if ($drivers) { $drivers | ForEach-Object { "            " + $_.Line } } else { Warn "no Appium driver installed (appium driver install uiautomator2)" }
} else {
  Warn "appium (npm install -g appium, then appium driver install uiautomator2)"
}

Write-Host "Android"
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { $env:ANDROID_SDK_ROOT }
if ($sdk) { Ok "ANDROID_HOME: $sdk" } else { Warn "ANDROID_HOME (path of the Android SDK)" }
if (Get-Command adb -ErrorAction SilentlyContinue) {
  Ok ("adb: " + (& adb version 2>&1 | Select-Object -First 1))
  $devices = & adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\sdevice$" }
  if ($devices) { Ok "connected devices:"; $devices | ForEach-Object { "            $_" } } else { Warn "no device or emulator connected (adb devices)" }
} else {
  Warn "adb (Android platform-tools)"
}
