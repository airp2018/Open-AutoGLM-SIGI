# AutoGLM Hybrid - Android 自动化助手

基于 Chaquopy 的一体化 Android 自动化解决方案。

## 当前版本特性

### 核心功能
- ✅ AI 驱动的手机自动化（基于 AutoGLM-Phone 模型）
- ✅ 无需 Termux，单 APK 部署
- ✅ 支持多种操作：Launch、Tap、Type、Swipe、Back、Home、Wait
- ✅ 对话历史记忆功能
- ✅ 动态坐标缩放

### UI 特性
- ✅ 未来科技风格界面（Tech Lab 主题）
- ✅ 中英文混合显示
- ✅ 可折叠日志区域（节省屏幕空间）
- ✅ 白底黑字日志（清晰易读）
- ✅ 实时状态显示

### 停止机制
- ✅ 视觉停止信号（红色横幅 + 白色 X 标记）
- ✅ 软着陆机制（动态步数限制）
- ✅ 双重保险停止系统

### 智能引导
- ✅ 第一步强制 Launch 指令（防止 AI 在控制台界面发呆）
- ✅ 简化的 System Prompt（提高 AI 响应准确性）

## 版本历史

### v1.0-Hybrid (2025-12-19) 🏷️
**主要改进：**
- ✅ 日志显示优化：从绿底改为白底黑字，提高可读性
- ✅ 日志折叠功能：点击小三角（▼/▶）可折叠/展开日志区域
- ✅ emoji 替换：将所有 emoji 图标替换为文本符号（如 `[*]`, `[OK]`, `[X]`），确保颜色一致
- ✅ Git 版本控制：初始化仓库，添加完整文档

**技术细节：**
- 日志背景色：`#FFFFFF`（白色）
- 日志文字色：`#212121`（深灰黑）
- 日志字体：`monospace`（等宽字体）
- 折叠按钮：绿色三角图标，支持点击切换

### v1.0 (2025-12-19)
- 初始版本提交
- 完整的 UI 重构
- 停止机制优化
- 第一步强制 Launch 引导

## 使用方法

1. 构建 APK：运行 `android-app/build-apk.bat`
2. 安装到 Android 设备
3. 开启无障碍权限
4. 输入任务并点击"开始执行"

## 技术栈

- **Android**: Kotlin, Accessibility Service
- **Python**: Chaquopy, OpenAI SDK, Pillow
- **AI Model**: AutoGLM-Phone (智谱 AI)

## 文档

📚 **[完整文档索引](docs/INDEX.md)** - 查看所有文档

**快速链接**：
- [AI 助手指引](docs/AI_README.md) - AI 操作仓库必读
- [Git 工作流程](docs/GIT_WORKFLOW.md) - Git 命令和规范
- [架构设计](ARCHITECTURE.md) - 技术架构说明
- [停止机制说明](android-app/docs/STOP_MECHANISM.md) - 了解停止按钮工作原理
- [构建说明](android-app/docs/BUILD_INSTRUCTIONS.md) - 如何构建 APK

## Git 使用建议

### 创建新版本
```bash
# 修改代码后
git add .
git commit -m "描述你的修改"
```

### 查看历史
```bash
git log --oneline
```

### 回退到之前版本
```bash
git log  # 查看提交历史，找到想回退的版本号
git checkout <commit-hash>  # 临时查看
git reset --hard <commit-hash>  # 永久回退（谨慎使用）
```

### 对比版本差异
```bash
git diff  # 查看未提交的修改
git diff HEAD~1  # 对比上一个版本
```

## 开发者

AutoGLM 开发团队
