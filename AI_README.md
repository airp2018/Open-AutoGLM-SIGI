# 📌 给 AI 助手的快速指引

> **重要**: 在操作此仓库前，请先阅读 `GIT_WORKFLOW.md`

## 🎯 关键信息

**仓库位置**: `C:\Users\YANQIAO\Documents\Augment\Open-AutoGLM-Hybrid-main`

**项目名称**: SIGI (思想钢印 / Sigillum Mentis)

**当前版本**: v1.1-SIGI

**分支**: master

## ⚠️ 必读文档

1. **`GIT_WORKFLOW.md`** ← 最重要！包含：
   - 仓库完整信息
   - PowerShell UTF-8 编码设置（必须！）
   - 所有常用 Git 命令
   - 工作流程和示例
   - 故障排除

2. **`README.md`** - 项目说明

3. **`ARCHITECTURE.md`** - 架构设计

4. **`UPDATE_SUMMARY_v1.1-SIGI.md`** - 最新版本更新摘要

## 🔧 快速开始

### 执行任何 Git 命令前，必须设置编码：

```powershell
cd C:\Users\YANQIAO\Documents\Augment\Open-AutoGLM-Hybrid-main
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### 查看当前状态：

```powershell
git status
git log --oneline -5
```

## 📝 提交模板

```powershell
git add .
git commit -m "类型: 简短描述"
```

**类型**: feat | fix | docs | style | refactor | ui | release

---

💡 **详细说明请查看**: `GIT_WORKFLOW.md`
