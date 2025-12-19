# Sigillum Mentis 图标设计方案（基于参考图）

## 设计参考
基于用户提供的参考图：深蓝背景 + 白色方形边框 + 大脑齿轮图案

## 最终设计方案：极简扁平化

### 配色
- 背景：深蓝 `#0A1929`
- 图标边框：白色 `#FFFFFF`
- 图标内部：浅蓝 `#90CAF9` 或白色
- 发光效果：科技蓝 `#1976D2`

### 图标元素（1024x1024）

```
┌────────────────────────────────┐
│                                │
│    ┏━━━━━━━━━━━━━━━━━━━━┓      │  深蓝背景 #0A1929
│    ┃                    ┃      │
│    ┃      ╱◯╲           ┃      │  白色方形边框
│    ┃     ◯──◯──◯        ┃      │  
│    ┃      ╲◯╱           ┃      │  中央：大脑形状 + 齿轮
│    ┃    ⚙ ⚙ ⚙          ┃      │  电路纹路
│    ┃                    ┃      │  
│    ┗━━━━━━━━━━━━━━━━━━━━┛      │
│                                │
└────────────────────────────────┘
```

## SVG 代码实现

创建文件：`app/src/main/res/drawable/ic_launcher_foreground.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- 深蓝背景 -->
    <path
        android:fillColor="#0A1929"
        android:pathData="M0,0h108v108h-108z"/>
    
    <!-- 白色方形边框（圆角）-->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="4"
        android:fillColor="#00000000"
        android:pathData="M24,24 h60 a4,4 0 0,1 4,4 v52 a4,4 0 0,1 -4,4 h-60 a4,4 0 0,1 -4,-4 v-52 a4,4 0 0,1 4,-4 z"/>
    
    <!-- 内部白色边框（双层效果）-->
    <path
        android:strokeColor="#90CAF9"
        android:strokeWidth="2"
        android:fillColor="#00000000"
        android:pathData="M28,28 h52 a2,2 0 0,1 2,2 v48 a2,2 0 0,1 -2,2 h-52 a2,2 0 0,1 -2,-2 v-48 a2,2 0 0,1 2,-2 z"/>
    
    <!-- 大脑轮廓（简化版）-->
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="2.5"
        android:fillColor="#00000000"
        android:pathData="M42,40 Q38,38 36,42 Q34,46 36,50 Q38,54 42,56 Q46,58 50,58 Q54,58 58,56 Q62,54 64,50 Q66,46 64,42 Q62,38 58,40 Q54,42 50,40 Q46,38 42,40 Z"/>
    
    <!-- 齿轮 1（左）-->
    <path
        android:fillColor="#1976D2"
        android:pathData="M38,54 l2,-1 l1,2 l2,-1 l-1,2 l2,1 l-2,1 l1,2 l-2,-1 l-1,2 l-1,-2 l-2,1 l1,-2 l-2,-1 l2,-1 l-1,-2 l2,1 Z"/>
    
    <!-- 齿轮 2（中）-->
    <path
        android:fillColor="#42A5F5"
        android:pathData="M50,58 l2,-1 l1,2 l2,-1 l-1,2 l2,1 l-2,1 l1,2 l-2,-1 l-1,2 l-1,-2 l-2,1 l1,-2 l-2,-1 l2,-1 l-1,-2 l2,1 Z"/>
    
    <!-- 齿轮 3（右）-->
    <path
        android:fillColor="#1976D2"
        android:pathData="M62,54 l2,-1 l1,2 l2,-1 l-1,2 l2,1 l-2,1 l1,2 l-2,-1 l-1,2 l-1,-2 l-2,1 l1,-2 l-2,-1 l2,-1 l-1,-2 l2,1 Z"/>
    
    <!-- 电路连接线 -->
    <path
        android:strokeColor="#90CAF9"
        android:strokeWidth="1.5"
        android:pathData="M40,52 L38,54 M50,56 L50,58 M60,52 L62,54"/>
    
    <!-- 神经网络节点 -->
    <circle android:fillColor="#FFFFFF" android:cx="40" android:cy="45" android:r="1.5"/>
    <circle android:fillColor="#FFFFFF" android:cx="50" android:cy="42" android:r="1.5"/>
    <circle android:fillColor="#FFFFFF" android:cx="60" android:cy="45" android:r="1.5"/>
    
    <!-- 连接线 -->
    <path
        android:strokeColor="#90CAF9"
        android:strokeWidth="1"
        android:pathData="M40,45 L50,42 L60,45"/>
</vector>
```

## 简化版（更容易实现）

如果上面的 SVG 太复杂，可以用这个极简版本：

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- 深蓝背景 -->
    <path
        android:fillColor="#0A1929"
        android:pathData="M0,0h108v108h-108z"/>
    
    <!-- 白色方形（圆角）-->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M24,24 h60 a4,4 0 0,1 4,4 v52 a4,4 0 0,1 -4,4 h-60 a4,4 0 0,1 -4,-4 v-52 a4,4 0 0,1 4,-4 z"/>
    
    <!-- 深蓝内部 -->
    <path
        android:fillColor="#0A1929"
        android:pathData="M28,28 h52 a2,2 0 0,1 2,2 v48 a2,2 0 0,1 -2,2 h-52 a2,2 0 0,1 -2,-2 v-48 a2,2 0 0,1 2,-2 z"/>
    
    <!-- 大脑图标（使用简单的 S 形状代表 Sigillum）-->
    <path
        android:fillColor="#90CAF9"
        android:pathData="M45,40 Q40,40 40,45 Q40,48 43,48 L57,48 Q60,48 60,53 Q60,58 55,58 L48,58 Q45,58 45,55"/>
    
    <!-- 下方 M 形状代表 Mentis -->
    <path
        android:fillColor="#1976D2"
        android:pathData="M42,62 L42,70 L46,65 L50,70 L54,65 L58,70 L58,62"/>
</vector>
```

## 使用 PNG 图片（推荐）

如果 Vector Drawable 太复杂，建议使用在线工具生成 PNG：

### 在线工具推荐
1. **Figma** (https://figma.com) - 免费，专业
2. **Canva** (https://canva.com) - 简单易用
3. **Photopea** (https://photopea.com) - 在线 Photoshop

### 设计步骤
1. 创建 1024x1024 画布
2. 填充深蓝背景 `#0A1929`
3. 添加白色方形（圆角 32px），居中，800x800
4. 在方形内部添加：
   - 大脑轮廓（可以用齿轮 + 脑形状组合）
   - 或者简化为字母 "S M"（Sigillum Mentis）
   - 或者使用印章图案
5. 添加浅蓝色 `#90CAF9` 装饰线条
6. 导出为 PNG

### 文件命名和位置
- `ic_launcher.png` (主图标)
- 放置在各个 mipmap 文件夹：
  - `mipmap-xxxhdpi/` (192x192)
  - `mipmap-xxhdpi/` (144x144)
  - `mipmap-xhdpi/` (96x96)
  - `mipmap-hdpi/` (72x72)
  - `mipmap-mdpi/` (48x48)

## 快速测试方案

如果需要立即看到效果，可以暂时使用纯色方块：

在 `res/values/colors.xml` 添加：
```xml
<color name="ic_launcher_background">#0A1929</color>
```

然后在 `AndroidManifest.xml` 中：
```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:theme="@style/AppTheme">
```

## 参考图特点总结
- ✅ 深蓝背景（#0A1929）
- ✅ 白色方形边框（圆角）
- ✅ 中央大脑 + 齿轮图案
- ✅ 电路纹路装饰
- ✅ 极简扁平化风格
- ✅ 科技感强烈

这个设计完美体现了"思想钢印"（Sigillum Mentis）的概念！
