@echo off
setlocal enabledelayedexpansion

:: 设置控制台编码为 UTF-8 (避免乱码)
chcp 65001 >nul
title AutoGLM-SIGI 一键安装助手 v1.0
color 0A

echo ========================================================
echo        欢迎使用 AutoGLM-SIGI 一键安装助手
echo ========================================================
echo.
echo [说明]
echo 本程序将自动为您安装以下组件：
echo 1. SIGI 主程序 (AI控制中心)
echo 2. ADB Keyboard (AI专用输入法)
echo 3. 自动授予系统底层权限 (Write Secure Settings)
echo.
echo [准备工作]
echo 请确保：
echo 1. 手机已开启【开发者选项】 -> 【USB调试】
echo 2. 小米手机需额外开启【USB安装】和【USB调试(安全设置)】
echo 3. 手机已通过数据线连接电脑
echo.
echo 按任意键开始检测设备...
pause >nul

:CHECK_ADB
cls
echo [1/5] 正在检测 ADB 环境...
if not exist "adb.exe" (
    color 0C
    echo [错误] 找不到 adb.exe！
    echo 请联系管理员索要完整的安装包。
    echo 缺少文件：adb.exe, AdbWinApi.dll, AdbWinUsbApi.dll
    pause
    exit
)

echo ADB 环境正常。
echo.
echo [2/5] 正在寻找手机...
echo 请留意手机屏幕，如有弹窗请允许【USB调试授权】！
echo.

adb start-server >nul 2>&1

:WAIT_DEVICE
adb devices > devices.txt
findstr /C:"device" devices.txt | findstr /V /C:"List" >nul
if %errorlevel% neq 0 (
    echo [等待中] 未检测到手机，或未授权...
    echo - 请检查是否插好数据线
    echo - 请检查手机上是否允许了USB调试弹窗
    timeout /t 2 >nul
    goto WAIT_DEVICE
)

echo [成功] 已连接设备！
del devices.txt
echo.

:INSTALL_MAIN
echo [3/5] 正在安装 SIGI 主程序...
if not exist "SIGI.apk" (
    color 0E
    echo [警告] 找不到 SIGI.apk，跳过主程序安装。
) else (
    adb install -r -g SIGI.apk
    if !errorlevel! equ 0 (
        echo [成功] SIGI 安装完成。
    ) else (
        color 0C
        echo [失败] 安装失败！请检查手机上是否拦截了安装请求（尤其是小米手机）。
        echo 按任意键重试安装，或按 Ctrl+C 退出...
        pause
        goto INSTALL_MAIN
    )
)
echo.

:INSTALL_IME
echo [4/5] 正在安装 ADB 输入法...
if not exist "ADBKeyboard.apk" (
    echo [提示] 找不到 ADBKeyboard.apk，跳过。
) else (
    adb install -r ADBKeyboard.apk >nul 2>&1
    echo [成功] 输入法安装/更新完成。
)
echo.

:GRANT_PERM
echo [5/5] 正在授予核心权限...
adb shell pm grant com.autoglm.helper android.permission.WRITE_SECURE_SETTINGS
if %errorlevel% equ 0 (
    echo [成功] 权限授予成功！
) else (
    echo [警告] 权限授予可能失败，请进App手动检查。
)

echo.
echo 正在激活 ADB 输入法...
adb shell ime enable com.android.adbkeyboard/.AdbIME >nul 2>&1
adb shell ime set com.android.adbkeyboard/.AdbIME >nul 2>&1
echo [成功] 输入法已激活。

echo.
echo ========================================================
echo               全部安装步骤执行完毕！
echo ========================================================
echo.
echo 现在您可以拔掉数据线，在手机上打开 SIGI 体验了。
echo.
pause
exit
