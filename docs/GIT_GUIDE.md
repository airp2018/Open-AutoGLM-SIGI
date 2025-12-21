# Git 快速参考指南

## 日常使用命令

### 1. 保存当前修改
```bash
# 查看修改了哪些文件
git status

# 添加所有修改的文件
git add .

# 提交修改（附带说明）
git commit -m "修改说明，例如：修复日志颜色问题"
```

### 2. 查看历史记录
```bash
# 查看提交历史（简洁版）
git log --oneline

# 查看详细历史
git log

# 查看最近 5 次提交
git log -5 --oneline
```

### 3. 查看修改内容
```bash
# 查看未提交的修改
git diff

# 查看某个文件的修改
git diff android-app/app/src/main/python/agent_main.py

# 对比两个版本
git diff HEAD~1 HEAD
```

### 4. 回退版本
```bash
# 临时查看旧版本（不影响当前代码）
git checkout <commit-hash>

# 返回最新版本
git checkout master

# 永久回退到某个版本（谨慎使用！）
git reset --hard <commit-hash>
```

### 5. 创建分支（用于实验性修改）
```bash
# 创建新分支
git branch experimental

# 切换到新分支
git checkout experimental

# 在新分支上修改代码...
git add .
git commit -m "实验性功能"

# 切换回主分支
git checkout master

# 如果实验成功，合并分支
git merge experimental
```

## 推荐工作流

### 每次修改前
```bash
git status  # 确保工作区干净
```

### 修改代码后
```bash
git add .
git commit -m "清晰描述本次修改的内容"
```

### 定期检查
```bash
git log --oneline  # 查看版本历史
```

## 常见场景

### 场景1：想尝试新功能，但不确定是否有效
```bash
# 创建实验分支
git checkout -b test-new-feature

# 修改代码...
git add .
git commit -m "测试新功能"

# 如果成功
git checkout master
git merge test-new-feature

# 如果失败
git checkout master
git branch -D test-new-feature  # 删除实验分支
```

### 场景2：改坏了，想恢复到上一个版本
```bash
# 查看历史
git log --oneline

# 回退到上一个提交
git reset --hard HEAD~1
```

### 场景3：想看看某个旧版本的代码
```bash
# 查看历史，找到版本号
git log --oneline

# 临时切换到旧版本
git checkout <commit-hash>

# 查看完后返回
git checkout master
```

## 提示

- ✅ **经常提交**：每完成一个小功能就提交一次
- ✅ **清晰的提交信息**：让未来的你知道这次改了什么
- ✅ **使用分支**：实验性修改用分支，成功后再合并
- ⚠️ **谨慎使用 reset --hard**：这会永久删除修改

## 当前仓库状态
```bash
# 查看当前状态
git status

# 查看所有分支
git branch

# 查看最近提交
git log -3 --oneline
```
