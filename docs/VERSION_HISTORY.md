# Open-AutoGLM-Hybrid Version History

## v1.47 (Cyberpunk Audio & UI Refinement)
- **Date**: 2025-12-28
- **Audio System**:
  - Integrated a full **Cyberpunk SFX System** using `SoundPool`.
  - Added custom synthesized sound effects:
    - `EXECUTE`: Sci-fi engine start-up sound.
    - `ABORT`: Continuous signal interference/blocking sound (1.0s).
    - `COMPLETE`: Futuristic success chime (Arpeggio).
    - `CLICK`: Mechanical UI switch sound.
  - Added **Sound Effects Toggle** in Settings (ON/OFF).
- **UI State Logic**:
  - Implemented strict **UI State Machine** for buttons:
    - **Idle**: Execute (Red Rect), Abort (Dark Rect).
    - **Running**: Execute (Dark), Abort (Green Rect, "ABORT").
    - **Terminated**: Abort (Green Ellipse, "SAVED").
  - Added `resetUI()` to enforce visual reset after every task cycle.
- **Doomsday List**:
  - Added **Long-Press to Delete** functionality for protocols.
  - Added sound feedback to list interactions.
- **Fixes**:
  - Fixed `agent_main.py` stop logic to ensure immediate loop break (solved +1 step issue).

## v1.46 (Doomsday Protocols) (2025-12-27) - 当前版本

### 主要功能
1. **设置界面全面重构**
   - 将 RadioGroup 替换为 Spinner 下拉菜单
   - 实现服务提供商配置的严格 1:1 绑定（ZhipuAI/ModelScope）
   - 自动保存功能（移除显式保存按钮）
   - 添加返回按钮用于导航
   - 实现右上角闪烁星星动画
   - 更新世界拯救计数器为数字号牌样式
   - 背景更换为"三体星看地球"主题

2. **游戏化皮肤系统**
   - 默认皮肤：Dark Void（纯黑）
   - 可解锁皮肤：Red Coast Base（执行 2 次任务后解锁）
   - 达成成就时自动解锁并即时视觉升级
   - 严格锁定机制防止提前访问
   - 设置中的皮肤选择下拉菜单显示解锁状态

3. **末日协议（任务快速访问系统）**
   - 主界面新增"DOOMSDAY PROTOCOLS"按钮
   - 自定义对话框包含默认任务模板（微信、淘宝、12306等）
   - 点击填充功能实现即时任务输入
   - 通过"+"按钮将当前任务添加到列表（输入框左下角）
   - 使用 SharedPreferences 持久化存储
   - VT323 字体配合终端绿色样式，打字机美学

4. **主界面增强**
   - 为任务输入添加极简清除按钮（X 图标）
   - 将世界拯救计数器逻辑从 ABORT 移至 EXECUTE 按钮
   - 计数器现在在任务开始时递增（而非停止时）
   - 修复按钮状态管理（防止白色按钮卡死）
   - 改进皮肤应用逻辑，严格执行解锁强制

### 技术变更
- 更新 SettingsActivity.kt：Spinner 实现、自动保存、皮肤管理
- 更新 MainActivity.kt：清除按钮、末日列表对话框、皮肤解锁逻辑
- 修改 activity_settings.xml：紧凑布局、新 UI 组件
- 修改 activity_main.xml：输入增强、协议标签行
- 新增 16 个文件，3000+ 行代码

---

## v1.4.5 (2025-12-26) - 拯救世界计数器

### 核心功能
- 每次点击 ABORT 时记录拯救次数
- 显示统计信息：You have saved the world X times
- 修复 SAVED 按钮文字颜色（白色可见）

### 设计理念
- 灵感来源：刘慈欣《三体》红岸基地
- 用户 = 叶文洁，每次点击 ABORT = 拯救世界
- 游戏化体验：让用户感觉在玩科幻赛博朋克游戏

### 技术实现
- SharedPreferences 持久化计数器
- 装饰性统计信息显示
- 按钮状态：执行中（红色+白色ABORT）/ 已停止（绿色+白色SAVED）

### 新增文档
- DESIGN_PHILOSOPHY.md：详细阐述三体主题设计理念

---

## v1.4.4 (2025-12-26) - 优化 ABORT 停止机制

### 核心改进
- 点击 ABORT 后，干扰用户的动作（点击、滑动、输入、启动APP、返回、Home）立即被跳过
- 用户感知：点击停止后手机立即安静，像立即停止一样

### 技术细节
1. **agent_main.py (+29行)**
   - 在 launch/tap/swipe/input/back/home 动作执行前添加停止检查
   - 检测到停止信号时跳过动作并显示日志
   - 修复判断条件从 > 改为 >=

2. **android_helper.py (-78行)**
   - 删除 VISUAL_STOP_SIGNAL 变量
   - 删除 enable/disable_visual_stop_signal() 函数
   - 删除 _add_stop_banner() 函数（85行红色水印代码）
   - 简化 take_screenshot() 函数

3. **MainActivity.kt (-5行)**
   - 移除 enable_visual_stop_signal 调用
   - 简化 ABORT 按钮日志输出

### 保留机制
- buffer_steps=1 软着陆机制不变
- 截图和 AI 调用继续执行（用户感觉不到）

---

## v1.4.2 (2025-12-26) - 新增 10 个常用 APP 包名支持

### 新增 APP
- 拼多多: com.xunmeng.pinduoduo
- 抖音: com.ss.android.ugc.aweme
- B站: tv.danmaku.bili
- 网易云音乐: com.netease.cloudmusic
- QQ音乐: com.tencent.qqmusic
- 百度地图: com.baidu.BaiduMap
- 携程: ctrip.android.view
- 去哪儿: com.Qunar
- 知乎: com.zhihu.android

### 修改文件
- android_helper.py: APP_PACKAGES 字典新增 10 个 APP
- docs/APP_PACKAGES.md: 新增 APP 包名配置文档

**支持 APP 总数**: 27 个

---

## v1.4.1 (2025-12-26) - 优化美团购物流程

### 核心优化
- 移除死循环检测的自动 go_back() 操作
- 新增系统提示：明确购物流程和去结算按钮
- 优化警告消息：针对美团购物场景提供明确建议

### 修改文件
- agent_main.py: 系统提示、死循环检测、警告消息优化
- docs/RELEASE_v1.5_SHOPPING_LOOP_FIX.md: 版本文档

### 测试验证
- 小米15: 美团买咖啡任务完整流程成功
- 成功点击加入购物车和去结算按钮

---

## v1.4 (2025-12-26) - 修复弹窗点击穿透问题

### 核心问题
- AccessibilityService 的 dispatchGesture 在弹窗上失效
- 多节点重叠时选择了错误的（面积大的）节点

### 解决方案
- findClickableNodeAt: 收集所有候选节点，选最小面积
- 新增 collectClickableNodes 递归收集函数
- 添加详细的调试日志

### 修改文件
- AutoGLMAccessibilityService.kt: 核心点击逻辑重写
- agent_main.py: 死循环检测增强，修复 go_back 调用
- android_helper.py: 简化 tap 函数

### 测试验证
- 美团买咖啡任务：点击'加入购物车'按钮成功

---

## v1.2 (2025-12-19) - 三体终端主题完整版

### UI 主题
- 所有文本采用终端绿色等宽字体样式
- 设置页面用于 API 配置（智谱/魔搭）
- 可折叠日志区域，带圆角背景
- 星空红岸基地背景
- ABORT 时的拯救星星动画
- 粗体斜体标题样式

### 新增功能
- 完整的三体终端主题
- 设置界面（RadioGroup 选择提供商）
- 日志折叠/展开功能
- 星星动画效果

---

## v1.1-SIGI (2025-12-19) - 思想钢印主题改版

### 应用改名
- 原名：AutoGLM Helper
- 新名：SIGI (Sigillum Mentis - 思想钢印)
- 灵感来源：刘慈欣《三体》中的思想钢印概念
- 寓意：AI 驱动的自动化如同在手机上刻下思想的印记

### LOGO 图标更换
- 新设计：机械大脑图标
- 风格：白色齿轮和电路板图案组成的大脑形状
- 背景：深蓝色科幻主题 (#2C5F7F)
- 象征：AI 智能 + 机械自动化的完美结合

### UI 主题升级
- 采用深色科幻主题
- 主色调：深蓝黑 (#0A1929)
- 强调色：科技绿 (#00FF00)
- 文字：白色为主，灰色为辅
- 整体风格：未来科技感 + 赛博朋克

### 图标资源更新
- 生成全套 Android 图标尺寸（mdpi 到 xxxhdpi）
- 包含标准版和圆形版
- 文件大小优化（相比旧图标减少约 30%）

### 字符串资源更新
- app_name: AutoGLM Helper → SIGI
- service_running: 更新为 SIGI 服务运行中
- service_stopped: 更新为 SIGI 服务已停止
- accessibility_service_description: 更新为 SIGI 自动化助手

---

## v1.0-Hybrid (2025-12-19) - 初始版本

### 核心功能
1. **UI 改进与日志切换**
   - 实现日志可见性切换功能
   - 改进整体 UI 布局和样式
   - 添加终端风格的绿色配色方案

2. **标题动画**
   - 像素艺术标题的打字机动画效果
   - ClipDrawable 实现逐字显示
   - 启动时循环 2 次动画
   - 仅在冷启动时触发

3. **ADB 权限自动检测**
   - 自动检测 WRITE_SECURE_SETTINGS 权限
   - 提供清晰的权限授予指令
   - 仅在会话期间检查一次
   - 新增一键授权脚本：一键授予ADB权限.bat

4. **停止机制优化**
   - 实现视觉信号（星星动画）
   - 优雅停止功能（软着陆机制）
   - 双重停止机制确保可靠性

5. **AccessibilityService 增强**
   - 内置 ADB Keyboard 替代外部 ADB 命令
   - Base64 编码支持中文输入
   - 提升输入稳定性和可靠性
   - 动态焦点夺回机制绕过 clearFocus 防御
   - 记录点击位置，在输入法切换后自动重新点击恢复焦点

6. **Bug 修复**
   - 修复停止横幅导致的坐标偏移 Bug
   - 重置视觉停止信号，防止影响新任务
   - 修复文档文件编码问题（UTF-8）

7. **文档完善**
   - 创建 docs/ 目录组织项目文档
   - 创建 android-app/docs/ 目录存放应用文档
   - 新增 INDEX.md 作为文档索引
   - 新增 AI_README.md 作为 AI 助手快速指引
   - 新增 GIT_WORKFLOW.md 完整 Git 操作指南
   - 新增 STOP_MECHANISM.md 停止机制详细说明
   - 新增多个技术分析文档

### 技术实现
- 基于 AutoGLM 的混合架构
- Python 3.8 + Kotlin 集成
- Chaquopy 用于 Python-Android 桥接
- NanoHTTPD 用于本地服务器
- 自定义 AccessibilityService 实现

### 初始配置
- compileSdk: 34
- minSdk: 24
- targetSdk: 34
- versionCode: 1
- versionName: "1.0.0"
- 支持架构：armeabi-v7a, arm64-v8a, x86, x86_64

---

## 版本演进时间线

```
2025-12-19  v1.0-Hybrid          初始版本，基础功能
2025-12-19  v1.1-SIGI            思想钢印主题改版
2025-12-19  v1.2                 三体终端主题
2025-12-26  v1.4                 修复弹窗点击穿透
2025-12-26  v1.4.1               优化美团购物流程
2025-12-26  v1.4.2               新增 10 个 APP 支持
2025-12-26  v1.4.4               优化 ABORT 停止机制
2025-12-26  v1.4.5               拯救世界计数器
2025-12-27  v1.46                UI/UX 全面升级
```

## 版本对比总结

| 特性 | v1.0 | v1.4.5 | v1.46 |
|------|------|--------|-------|
| 设置界面 | 无 | RadioGroup | Spinner + 自动保存 |
| 皮肤系统 | 无 | 无 | 游戏化解锁（2个皮肤） |
| 快捷任务 | 无 | 无 | 末日协议列表 |
| 计数器 | 无 | 简单统计 | 数字号牌 + 解锁触发 |
| 输入增强 | 基础 | 基础 | 清除按钮 + 添加到列表 |
| 背景主题 | 红岸基地 | 红岸基地 | 动态切换（黑/红岸） |
| 支持 APP | 17 | 27 | 27 |
| 文件数量 | 48 | ~52 | 64 |
| 代码行数 | ~6000 | ~6500 | ~9000 |

---

## 技术债务与已知问题

### 已解决
- ✅ 停止横幅坐标偏移问题 (v1.0)
- ✅ 视觉停止信号污染问题 (v1.0)
- ✅ 弹窗点击穿透问题 (v1.4)
- ✅ 美团购物死循环问题 (v1.4.1)
- ✅ ABORT 停止延迟问题 (v1.4.4)
- ✅ 按钮状态卡死问题 (v1.46)
- ✅ XML 解析错误 (v1.46)
- ✅ AndroidX 依赖问题 (v1.46)

### 待优化
- [ ] 任务历史记录查看
- [ ] 末日协议分类管理
- [ ] 更多皮肤主题
- [ ] 导出/导入配置
- [ ] 性能优化和内存管理
- [ ] 多语言支持

---

## 开发统计

- **总提交数**: 17 次
- **开发周期**: 2025-12-19 至 2025-12-27 (9天)
- **主要贡献者**: User <user@example.com>
- **代码增长**: 6000 → 9000 行 (+50%)
- **功能模块**: 5 → 8 个 (+60%)
- **支持 APP**: 17 → 27 个 (+59%)

---

## 设计哲学

本项目深受刘慈欣《三体》系列影响，将科幻元素融入 AI 自动化工具：

1. **思想钢印 (Sigillum Mentis)**: AI 在手机上刻下自动化的印记
2. **红岸基地**: 用户如同叶文洁，通过 ABORT 按钮拯救世界
3. **三体星视角**: 从遥远的三体星回望地球（设置界面背景）
4. **末日协议**: 快捷任务列表，应对各种"末日"场景
5. **赛博朋克美学**: 终端绿、打字机字体、数字号牌

---

## 致谢

感谢所有为 AutoGLM 项目做出贡献的开发者，以及刘慈欣老师的《三体》系列为本项目提供的灵感。
