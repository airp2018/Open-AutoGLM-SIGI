
# ⚠️ READ ME FIRST / 必读

## 🇨🇳 中文说明
**这是什么?**
这是 SIGI (AutoGLM 智能体) 的一键部署包。它包含：
1. `SIGI2.1.apk`: 主程序
2. `ADBKeyboard.apk`: 自动输入法
3. `Install_Windows.bat`: 自动安装脚本 (Windows)

**如何使用?**
1. 用 USB 线连接手机到电脑。
2. 开启手机的 **USB 调试 (USB Debugging)** 模式。
3. 双击 `Install_Windows.bat`。
4. 手机上出现弹窗时，允许调试。
5. 等待脚本运行完毕，显示 "Success" 后即可拔线。

---

## 🇺🇸 English Instructions
**What is this?**
This is the Deployment Kit for SIGI (The AutoGLM Agent). It includes:
1. `SIGI2.1.apk`: The main application.
2. `ADBKeyboard.apk`: Helper for automated text input.
3. `Install_Windows.bat`: A script to automate the installation and permission granting.

**How to use?**
1. Connect your Android phone to your PC via USB.
2. Enable **"USB Debugging"** in your phone's Developer Options.
3. Double-click `Install_Windows.bat`.
4. Allow USB Debugging authorization on your phone screen if prompted.
5. Wait for the script to finish (displaying "Success"), then unplug.

**Note for Mac/Linux Users:**
Please check the `tools` folder or run the adb commands manually:
`./tools/adb install -r SIGI2.1.apk`
`./tools/adb install -r ADBKeyboard.apk`
`./tools/adb shell pm grant com.autoglm.helper android.permission.WRITE_SECURE_SETTINGS`
