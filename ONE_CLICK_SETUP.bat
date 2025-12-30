@echo off
chcp 65001 >nul
color 0B
echo ========================================================
echo       SIGILLUM MENTIS - Universal Setup Tool
echo ========================================================
echo.
echo [INFO] This tool can INSTALL the app (if APKs are present) 
echo        AND automatically GRANT necessary permissions.
echo.
echo [STEP 1] Connect phone via USB & Enable USB Debugging.
echo [STEP 2] If you have APK files, put them in this folder.
echo.
pause

echo.
echo [*] Checking connection...
adb devices
adb get-state >nul 2>&1
if errorlevel 1 (
    echo [!] Device connection failed.
    echo     Please check USB cable and drivers.
    pause
    exit /b
)

:: --- PART 1: AUTO INSTALLATION (If APKs exist) ---

:: 1. ADB Keyboard
if exist "ADBKeyboard.apk" (
    echo.
    echo [*] Found ADBKeyboard.apk. Installing...
    echo     (Please allow installation on your phone screen)
    adb install -r ADBKeyboard.apk
    if errorlevel 0 (
        echo [OK] ADB Keyboard installed.
        echo [*] Enabling ADB Keyboard...
        adb shell ime enable com.android.adbkeyboard/.AdbIME
        adb shell ime set com.android.adbkeyboard/.AdbIME
    )
) else (
    echo.
    echo [-] ADBKeyboard.apk not found. Skipping installation.
    echo     (If you already installed it, ignore this.)
)

:: 2. Main App (Detection: app-*.apk or sigi*.apk)
set APP_APK=""
if exist "app-release.apk" set APP_APK="app-release.apk"
if exist "app-debug.apk" set APP_APK="app-debug.apk"
if exist "sigi_app.apk" set APP_APK="sigi_app.apk"

if not %APP_APK% == "" (
    echo.
    echo [*] Found App APK: %APP_APK%. Installing...
    echo     (Please allow installation on your phone screen)
    adb install -r -t %APP_APK%
    if errorlevel 0 echo [OK] App installed.
) else (
    echo.
    echo [-] No App APK found. Skipping installation.
    echo     (Assuming you have already installed the App on your phone.)
)

:: --- PART 2: PERMISSION ACTIVATION (Always Run) ---
echo.
echo ========================================================
echo [*] ACTIVATING PERMISSIONS (The most important step)...
echo ========================================================

adb shell pm grant com.autoglm.helper android.permission.WRITE_SECURE_SETTINGS

if errorlevel 0 (
    echo.
    echo [OK] SUCCESS! Permission granted.
    echo      The "Cognitive Lock" protocols are now active.
) else (
    echo.
    echo [!] FAILED. 
    echo     Could not find package 'com.autoglm.helper'.
    echo     Please make sure the App is installed on your phone!
)

echo.
echo ========================================================
echo    SETUP FINISHED.
echo ========================================================
pause
