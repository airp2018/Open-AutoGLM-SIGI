# 🚀 快速开始指南

## 3 步获取 APK 并部署

---

## 步骤 1: 上传到 GitHub (5 分钟)

### 1.1 创建 GitHub 仓库

1. 访问 https://github.com/new
2. 仓库名: `Open-AutoGLM-Hybrid`
3. 可见性: Public 或 Private
4. 点击 "Create repository"

### 1.2 上传代码

**最简单方式 - 网页上传**:

1. 解压 `Open-AutoGLM-Hybrid-v1.0.0-GitHub.zip`
2. 在 GitHub 仓库页面点击 "uploading an existing file"
3. 将所有文件拖拽到页面
4. 点击 "Commit changes"

**或使用 Git 命令**:

```bash
cd Open-AutoGLM-Hybrid
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/Open-AutoGLM-Hybrid.git
git push -u origin main
```

---

## 步骤 2: 等待自动构建 (5-10 分钟)

### 2.1 查看构建状态

1. 在 GitHub 仓库页面，点击 "Actions" 标签
2. 看到 "Build Android APK" 正在运行
3. 等待变成绿色勾号 ✅

### 2.2 下载 APK

1. 点击构建任务名称
2. 滚动到底部 "Artifacts" 部分
3. 点击 "AutoGLM-Helper-Debug" 下载
4. 解压 ZIP，得到 `app-debug.apk`

---

## 步骤 3: 部署到手机 (30 分钟)

### 3.1 安装 APP

**安装 Termux**:
1. 从 F-Droid 下载: https://f-droid.org/packages/com.termux/
2. 安装 Termux.apk

**安装 AutoGLM Helper**:
1. 将下载的 `app-debug.apk` 传到手机
2. 点击安装
3. 允许安装未知来源应用

### 3.2 开启无障碍权限

1. 打开 AutoGLM Helper
2. 点击 "打开设置"
3. 找到 "AutoGLM Helper"
4. 打开开关
5. 点击 "允许"

### 3.3 运行部署脚本

1. 打开 Termux
2. 复制粘贴以下命令:

```bash
# 下载部署脚本 (需要替换为实际链接)
curl -O https://your-link/deploy.sh

# 运行
chmod +x deploy.sh
./deploy.sh
```

3. 输入您的 GRS AI API Key
4. 等待自动完成

---

## 🎉 开始使用

```bash
# 启动
autoglm

# 输入任务
请输入任务: 打开淘宝搜索蓝牙耳机

# 手机自动执行 ✅
```

---

## 📚 详细文档

- [GitHub 构建指南](GITHUB_BUILD_GUIDE.md) - 详细的 GitHub Actions 说明
- [部署指南](docs/DEPLOYMENT_GUIDE.md) - 完整的部署步骤
- [用户手册](docs/USER_MANUAL.md) - 日常使用指南

---

## ❓ 遇到问题？

### Q: GitHub Actions 构建失败？

**A**: 查看 Actions 日志，通常是:
- Gradle 版本问题 → 检查 `gradle-wrapper.properties`
- 网络问题 → 重新触发构建

### Q: 无法下载 Artifacts？

**A**: 
- 确保已登录 GitHub
- 或使用 GitHub CLI: `gh run download`

### Q: 部署脚本失败？

**A**: 
- 检查网络连接
- 查看 `docs/DEPLOYMENT_GUIDE.md` 故障排除部分

---

## ⏱️ 时间估算

| 步骤 | 时间 |
|------|------|
| 上传到 GitHub | 5 分钟 |
| 自动构建 APK | 5-10 分钟 |
| 部署到手机 | 30 分钟 |
| **总计** | **40-45 分钟** |

---

**就这么简单！**
