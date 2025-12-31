@echo off
chcp 65001 >nul
title SIGI Installer

:: Smart APK Detection
set "APK_SIGI="
for %%f in ("%~dp0SIGI*.apk") do set "APK_SIGI=%%f"
if "%APK_SIGI%"=="" (
    echo [Error] No SIGI APK found!
    pause
    exit /b
)
set "APK_KBD=%~dp0ADBKeyboard.apk"

echo ==========================================================
echo  SIGILLUM MENTIS - Installer
echo ==========================================================
echo.
echo [Info] Detected APK: %APK_SIGI%
echo.

:: ADB Detection - Simple and Robust
set "LOCAL_ADB=%~dp0tools\adb.exe"
if exist "%LOCAL_ADB%" (
    set "ADB_CMD=%LOCAL_ADB%"
    echo [Info] Using LOCAL ADB from package
    goto :adb_ready
)

adb version >nul 2>&1
if %errorlevel% equ 0 (
    set "ADB_CMD=adb"
    echo [Info] Using SYSTEM ADB
    goto :adb_ready
)

echo [Error] ADB not found!
pause
exit /b

:adb_ready
echo.
echo [Step 1] Detecting devices...
"%ADB_CMD%" devices
echo.

echo [Step 2] Installing SIGI...
"%ADB_CMD%" install -r "%APK_SIGI%"

echo.
echo [Step 3] Installing ADB Keyboard...
"%ADB_CMD%" install -r "%APK_KBD%"

echo.
echo [Step 4] Granting permissions...
"%ADB_CMD%" shell pm grant com.autoglm.helper android.permission.WRITE_SECURE_SETTINGS

echo.
echo [Step 5] Enabling ADB Keyboard...
"%ADB_CMD%" shell ime enable com.android.adbkeyboard/.AdbIME

echo.
echo ==========================================================
echo  DONE!
echo.
echo  MANUAL STEP: Settings > Accessibility > Downloaded Apps > SIGI > Enable
echo ==========================================================
pause
