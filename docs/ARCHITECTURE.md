# Open-AutoGLM 混合方案架构设计

## 项目结构

```
Open-AutoGLM-Hybrid/
├── android-app/                    # Android APP (无障碍服务)
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/autoglm/helper/
│   │   │       │   ├── AutoGLMAccessibilityService.kt    # 无障碍服务
│   │   │       │   ├── HttpServer.kt                      # HTTP 服务器
│   │   │       │   ├── ScreenshotHelper.kt                # 截图助手
│   │   │       │   ├── GestureHelper.kt                   # 手势助手
│   │   │       │   └── MainActivity.kt                    # 主界面
│   │   │       ├── res/                                   # 资源文件
│   │   │       └── AndroidManifest.xml                    # 清单文件
│   │   └── build.gradle.kts                               # 构建配置
│   ├── gradle/                                            # Gradle 配置
│   ├── build.gradle.kts                                   # 项目构建配置
│   └── settings.gradle.kts                                # 项目设置
│
├── termux-scripts/                 # Termux 脚本
│   ├── deploy.sh                   # 一键部署脚本
│   ├── autoglm                     # 启动脚本
│   ├── phone_controller.py         # 手机控制器（自动降级）
│   └── requirements.txt            # Python 依赖
│
├── docs/                           # 文档
│   ├── DEPLOYMENT_GUIDE.md         # 部署指南
│   ├── USER_MANUAL.md              # 使用手册
│   ├── TROUBLESHOOTING.md          # 故障排除
│   └── FAQ.md                      # 常见问题
│
├── ARCHITECTURE.md                 # 本文件
└── README.md                       # 项目说明
```

## 技术架构

### 1. Android APP (AutoGLM Helper)

**技术栈**:
- Kotlin
- Android SDK 24+ (Android 7.0+)
- AccessibilityService API
- NanoHTTPD (轻量级 HTTP 服务器)

**核心功能**:
1. 无障碍服务 - 执行自动化操作
2. HTTP 服务器 - 接收来自 Termux 的命令
3. 截图功能 - 捕获屏幕内容
4. 手势模拟 - 点击、滑动、输入

**API 接口**:
```
POST /tap          - 点击操作
POST /swipe        - 滑动操作
POST /input        - 输入文字
GET  /screenshot   - 获取截图
GET  /status       - 检查服务状态
```

### 2. Termux 集成

**技术栈**:
- Python 3.11
- requests (HTTP 客户端)
- Pillow (图像处理)
- openai (GRS AI 客户端)

**核心组件**:
1. `phone_controller.py` - 手机控制器（自动降级逻辑）
2. `deploy.sh` - 一键部署脚本
3. `autoglm` - 启动脚本

**自动降级逻辑**:
```python
class PhoneController:
    def __init__(self):
        # 1. 尝试无障碍服务
        if self.try_accessibility_service():
            self.mode = "accessibility"
        # 2. 降级到 LADB
        elif self.try_ladb():
            self.mode = "ladb"
        # 3. 都不可用
        else:
            raise Exception("无可用控制方式")
```

### 3. 通信协议

**无障碍服务模式**:
```
Termux (Python)
    ↓ HTTP Request (localhost:8080)
AutoGLM Helper (HTTP Server)
    ↓ AccessibilityService API
Android System
    ↓ 执行操作
手机屏幕
```

**LADB 模式**:
```
Termux (Python)
    ↓ ADB Command (localhost:5555)
LADB (ADB Server)
    ↓ ADB Protocol
Android System
    ↓ 执行操作
手机屏幕
```

## 数据流

### 截图流程

**无障碍服务模式**:
```
1. Termux 发送 GET /screenshot
2. AutoGLM Helper 调用 takeScreenshot()
3. 返回 Base64 编码的图片
4. Termux 解码并保存
5. 发送给 GRS AI 分析
```

**LADB 模式**:
```
1. Termux 执行 adb shell screencap
2. ADB 保存到 /sdcard/tmp.png
3. Termux 执行 adb pull
4. 获取图片文件
5. 发送给 GRS AI 分析
```

### 操作流程

**点击操作**:
```
1. GRS AI 返回坐标 (x, y)
2. Termux 发送 POST /tap {x, y}
3. AutoGLM Helper 执行 dispatchGesture()
4. 系统执行点击
5. 返回执行结果
```

## 安全性设计

### 1. 本地通信
- HTTP 服务器只监听 localhost (127.0.0.1)
- 不暴露到外网
- 无需身份验证（本地通信）

### 2. 权限控制
- 无障碍权限仅用于自动化
- 不收集用户数据
- 不上传任何信息

### 3. 开源透明
- 所有代码开源
- 用户可审查
- 无隐藏功能

## 性能优化

### 1. 截图优化
- 压缩图片质量（JPEG 80%）
- 限制图片尺寸（最大 1080p）
- 缓存机制

### 2. 网络优化
- HTTP Keep-Alive
- 连接池复用
- 超时控制

### 3. 内存优化
- 及时释放 Bitmap
- 避免内存泄漏
- GC 优化

## 兼容性设计

### Android 版本适配

**Android 7-8**:
- 使用基础无障碍 API
- 不使用 takeScreenshot() (需要 Android 9+)
- 降级到 ADB screencap

**Android 9+**:
- 使用 takeScreenshot() API
- 完整无障碍功能

**Android 11+**:
- 支持 LADB 备份方案
- 无线调试功能

### 厂商适配

**MIUI**:
- 提示开启后台运行
- 提示关闭电池优化

**ColorOS**:
- 提示关闭权限监控

**EMUI**:
- 提示开启 ADB 相关选项

## 部署流程

### 1. 用户准备
- 安装 Termux
- 安装 AutoGLM Helper APK
- 开启无障碍权限

### 2. 自动部署
```bash
curl -L https://链接/deploy.sh | bash
```

### 3. 配置 GRS AI
```bash
export PHONE_AGENT_API_KEY="your_key"
```

### 4. 启动使用
```bash
autoglm
```

## 错误处理

### 1. 连接失败
- 检查 AutoGLM Helper 是否运行
- 检查无障碍权限是否开启
- 自动降级到 LADB

### 2. 操作失败
- 重试机制（最多 3 次）
- 超时控制（10 秒）
- 错误日志记录

### 3. 服务崩溃
- 自动重启机制
- 状态检查
- 用户提示

## 未来扩展

### 1. 语音控制
- 集成语音识别
- 语音输入任务

### 2. Web 界面
- 浏览器控制
- 远程访问（安全认证）

### 3. 任务录制
- 录制操作序列
- 回放功能

### 4. 多设备支持
- 同时控制多台手机
- 设备管理

## 技术选型理由

### 为什么用 Kotlin？
- Android 官方推荐
- 代码简洁
- 空安全

### 为什么用 NanoHTTPD？
- 轻量级（< 100KB）
- 无依赖
- 易于集成

### 为什么用 AccessibilityService？
- 无需 root
- 系统级 API
- 稳定可靠

### 为什么用 Python？
- Open-AutoGLM 原生支持
- 生态丰富
- 易于修改

## 开发时间估算

| 阶段 | 时间 | 说明 |
|------|------|------|
| Android APP 开发 | 1 天 | 核心功能 |
| Termux 脚本开发 | 0.5 天 | 集成和降级逻辑 |
| 测试和调试 | 0.5 天 | 各种场景测试 |
| 文档编写 | 0.5 天 | 完整文档 |
| 打包和发布 | 0.5 天 | APK 签名等 |
| **总计** | **3 天** | **完整方案** |

## 交付物清单

1. ✅ AutoGLM Helper APK
2. ✅ Termux 部署脚本
3. ✅ 完整源代码
4. ✅ 部署指南（图文）
5. ✅ 使用手册
6. ✅ 故障排除文档
7. ✅ FAQ
8. ✅ 架构文档（本文件）
