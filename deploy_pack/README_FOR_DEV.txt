【AutoGLM-SIGI 分发包制作指南】

这是留给您（开发者）看的。为了制作给用户的“一键安装包”，请往这个文件夹里放入以下文件：

1. 必需文件：
-------------------------------------------------------
[ ] SIGI.apk 
    -> 请将您编译好的 android-app/app/build/outputs/apk/debug/app-debug.apk 
       改名为 SIGI.apk 并放入此处。

[ ] ADBKeyboard.apk
    -> 这是安卓输入法APK，请网上下载或从旧项目复制，放入此处。

2. ADB 工具文件（必需，否则脚本无法运行）：
-------------------------------------------------------
[ ] adb.exe
[ ] AdbWinApi.dll
[ ] AdbWinUsbApi.dll
    -> 请从您的 Android SDK 目录 (platform-tools) 复制这三个文件到此处。
    -> 通常在 C:\Users\YANQIAO\AppData\Local\Android\Sdk\platform-tools

3. 打包发布：
-------------------------------------------------------
集齐上述 5 个文件（SIGI.apk, ADBKeyboard.apk, 3个adb文件）后，
将整个 deploy_pack 文件夹压缩为 zip，改名为“AutoGLM-SIGI-内测版.zip”，即可发给用户。

用户解压后，双击“一键安装.bat”即可。
