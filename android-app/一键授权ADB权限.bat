@echo off
chcp 65001 >nul
echo ============================================
echo    AutoGLM 一键授权脚本
echo ============================================
echo.

echo [1/3] 检查 ADB 连接...
adb devices
echo.

echo [2/3] 授权 WRITE_SECURE_SETTINGS 权限...
adb shell pm grant com.autoglm.helper android.permission.WRITE_SECURE_SETTINGS
if %errorlevel% equ 0 (
    echo ✓ 授权成功！
) else (
    echo ✗ 授权失败，请确保：
    echo   - 手机已连接并开启 USB 调试
    echo   - 已安装 AutoGLM App
)
echo.

echo [3/3] 验证权限...
adb shell dumpsys package com.autoglm.helper | findstr "WRITE_SECURE_SETTINGS"
echo.

echo ============================================
echo 完成！现在可以使用 ADB Keyboard 输入了
echo ============================================
pause
