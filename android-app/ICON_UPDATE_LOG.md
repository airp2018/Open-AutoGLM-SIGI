# App Icon Update Log

## 更新日期
2025-12-19

## 新图标
- **设计**: 机械大脑图标
- **风格**: 白色齿轮和电路板图案在蓝色背景上
- **主题**: AI 自动化 / 智能机械

## 更新内容

### 源文件
- 新图标源文件: `app/src/main/res/brain_icon.jpg`
- 原图标备份: `app/src/main/res/sigillum_icon.jpg`

### 生成的图标尺寸
所有标准 Android 图标尺寸已生成：

| 密度 | 尺寸 | 文件 |
|------|------|------|
| mdpi | 48x48 | mipmap-mdpi/ic_launcher.png |
| hdpi | 72x72 | mipmap-hdpi/ic_launcher.png |
| xhdpi | 96x96 | mipmap-xhdpi/ic_launcher.png |
| xxhdpi | 144x144 | mipmap-xxhdpi/ic_launcher.png |
| xxxhdpi | 192x192 | mipmap-xxxhdpi/ic_launcher.png |

每个密度都包含：
- `ic_launcher.png` - 标准图标
- `ic_launcher_round.png` - 圆形图标

## 如何重新生成图标

如果需要更换图标，请按以下步骤操作：

1. 将新图标文件放置在 `app/src/main/res/` 目录
2. 编辑 `generate_icons.py`，修改 `source_image` 路径
3. 运行生成脚本：
   ```bash
   python generate_icons.py
   ```

## 构建说明

图标已自动集成到应用中，重新构建 APK 即可看到新图标：

```bash
cd android-app
./build-apk.bat
```

或使用 Android Studio 构建。

## 效果预览

新图标将在以下位置显示：
- 应用启动器（桌面图标）
- 应用列表
- 最近任务列表
- 通知栏（如果应用有通知）

---

**注意**: 如果在已安装的设备上更新图标，可能需要：
1. 卸载旧版本应用
2. 重启设备（某些启动器会缓存图标）
3. 安装新版本应用
