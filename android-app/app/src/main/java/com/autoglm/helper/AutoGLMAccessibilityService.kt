package com.autoglm.helper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.ByteArrayOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AutoGLMAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoGLM-Service"
        const val PORT = 8080
        
        @Volatile
        private var instance: AutoGLMAccessibilityService? = null
        
        fun getInstance(): AutoGLMAccessibilityService? = instance
    }

    private var httpServer: HttpServer? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        
        Log.i(TAG, "Service connected")
        
        // 启动 HTTP 服务器
        startHttpServer()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 不需要处理事件
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    fun performBack(): Boolean {
        Log.d(TAG, "Performing BACK action")
        val success = performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        Log.d(TAG, "BACK action success: $success")
        return success
    }
    
    fun performHome(): Boolean {
        Log.d(TAG, "Performing HOME action")
        val success = performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        Log.d(TAG, "HOME action success: $success")
        return success
    }

    fun findNodeByText(text: String): android.graphics.Rect? {
        val rootNode = rootInActiveWindow ?: return null
        
        // 广度优先搜索
        val queue = java.util.LinkedList<AccessibilityNodeInfo>()
        queue.add(rootNode)
        
        while (!queue.isEmpty()) {
            val node = queue.poll() ?: continue
            
            // 检查文本匹配 (包含关系)
            if (node.text != null && node.text.toString().contains(text)) {
                val rect = android.graphics.Rect()
                node.getBoundsInScreen(rect)
                node.recycle()
                return rect
            }
            
            // 检查 ContentDescription
            if (node.contentDescription != null && node.contentDescription.toString().contains(text)) {
                 val rect = android.graphics.Rect()
                node.getBoundsInScreen(rect)
                node.recycle()
                return rect
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
            node.recycle()
        }
        return null
    }    

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        
        // 停止 HTTP 服务器
        stopHttpServer()
        
        Log.i(TAG, "Service destroyed")
    }

    private fun startHttpServer() {
        try {
            httpServer = HttpServer(this, PORT)
            httpServer?.start()
            Log.i(TAG, "HTTP server started on port $PORT")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start HTTP server", e)
        }
    }

    private fun stopHttpServer() {
        httpServer?.stop()
        httpServer = null
        Log.i(TAG, "HTTP server stopped")
    }

    fun isAccessibilityEnabled(): Boolean {
        return instance != null
    }

    /**
     * 执行点击操作
     */
    /**
     * 执行点击操作 - 优先尝试查找节点并点击，失败则使用手势
     */
    fun performTap(x: Int, y: Int): Boolean {
        // 1. 尝试查找该位置的可点击节点
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            // 调试：打印该坐标下的所有节点（包括不可点击的）
            Log.d(TAG, "Inspecting nodes at ($x, $y):")
            inspectNodesAt(rootNode, x, y)
            
            val clickableNode = findClickableNodeAt(rootNode, x, y)
            if (clickableNode != null) {
                val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (success) {
                    val nodeInfo = "${clickableNode.className} ('${clickableNode.text ?: ""}') bounds=${getBounds(clickableNode)}"
                    Log.d(TAG, "Tap at ($x, $y): Performed ACTION_CLICK on -> $nodeInfo")
                    clickableNode.recycle()
                    return true
                }
                Log.w(TAG, "Tap at ($x, $y): Found clickable node but ACTION_CLICK failed")
                clickableNode.recycle()
            } else {
                Log.d(TAG, "Tap at ($x, $y): No clickable node found, falling back to gesture")
            }
        } else {
             Log.w(TAG, "Tap at ($x, $y): rootInActiveWindow is null")
        }

        // 2. 如果找不到节点或点击失败，使用手势模拟
        return try {
            val path = Path()
            path.moveTo(x.toFloat(), y.toFloat())
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            
            val latch = CountDownLatch(1)
            var success = false
            
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    success = true
                    latch.countDown()
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    success = false
                    latch.countDown()
                }
            }, null)
            
            latch.await(5, TimeUnit.SECONDS)
            Log.d(TAG, "Tap at ($x, $y): Gesture success=$success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform tap", e)
            false
        }
    }
    
    // 调试辅助：打印坐标下的节点信息
    private fun inspectNodesAt(node: AccessibilityNodeInfo, x: Int, y: Int) {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        
        if (rect.contains(x, y)) {
            // 是叶子节点或者关键节点？打印出来
            if (node.childCount == 0 || node.isClickable || node.isScrollable || node.isEditable) {
                Log.d(TAG, "  Found: ${node.className}, clickable=${node.isClickable}, bounds=$rect, text='${node.text}'")
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                inspectNodesAt(child, x, y)
                child.recycle()
            }
        }
    }
    
    private fun getBounds(node: AccessibilityNodeInfo): String {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        return rect.toString()
    }

    private fun findClickableNodeAt(node: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        
        // 只有当点在区域内才继续
        if (!rect.contains(x, y)) {
            return null
        }

        // 优先检查子节点（因为子节点在上面）
        // 从后往前遍历（Z-order），不过 AccessibilityNodeInfo children 顺序不一定对应 Z-order，但一般是可以的
        for (i in node.childCount - 1 downTo 0) {
            val child = node.getChild(i) ?: continue
            val result = findClickableNodeAt(child, x, y)
            if (result != null) {
                // 找到了子节点，返回它
                // 注意：child 本身在递归中如果没有被返回，已经被 recycle 了（由调用者负责）
                // 但在这里，findClickableNodeAt 返回了一个有效的 node，所以不需要在这里 recycle child
                // 等等，child 是父节点生成的。递归调用后，如果返回非空，说明找到了。
                // 如果返回空，说明没找到，我们需要 recycle child。
                // 这里的逻辑有点绕，让我们简化：
                // 如果递归返回了 result，那么 result 是一个有效的 node（可能是 child 或者 child 的后代）
                // child 引用本身可能已经失效（如果 result 是 child 的后代），或者 result 就是 child。
                // 无论如何，我们只关心 result。
                // 但是！我们需要确保 child 被 recycle，如果 result 不是 child。
                // 实际上，AccessibilityNodeInfo 的引用管理很麻烦。
                
                // 简单点：每个层级只负责 recycle 它自己产生的 child 引用。
                // 如果返回了 result，那么 result 必须是 caller 负责 recycle 的。
                
                return result
            }
            child.recycle()
        }

        // 检查当前节点是否可点击
        if (node.isClickable) {
            // 返回这一节点。但在 AccessibilityService 中，可以通过 obtain 来复制节点？
            // 不，直接返回。调用者（performTap）会负责 recycle 它。
            // 但是 node 是从外面传进来的，或者是 getChild 得到的。
            // 为了安全，我们可以返回 node 的一个副本，或者约定调用者负责。
            // 在这个递归结构中，如果返回 node，上层循环中的 child.recycle() 就不应该执行。
            
            // 为了简单，我们使用一种更安全的策略：只在最顶层调用处 recycle。
            // 但这样会导致中间节点无法及时回收。
            
            // 妥协方案：如果当前节点可点击，返回 AccessibilityNodeInfo.obtain(node)
            return AccessibilityNodeInfo.obtain(node)
        }

        return null
    }

    /**
     * 执行滑动操作
     */
    fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int): Boolean {
        return try {
            val path = Path()
            path.moveTo(x1.toFloat(), y1.toFloat())
            path.lineTo(x2.toFloat(), y2.toFloat())
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration.toLong()))
                .build()
            
            val latch = CountDownLatch(1)
            var success = false
            
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    success = true
                    latch.countDown()
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    success = false
                    latch.countDown()
                }
            }, null)
            
            latch.await(10, TimeUnit.SECONDS)
            Log.d(TAG, "Swipe from ($x1, $y1) to ($x2, $y2): $success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform swipe", e)
            false
        }
    }

    /**
     * 执行输入操作
     * 改进版：优先使用 ACTION_SET_TEXT，失败时使用剪贴板 + ACTION_PASTE
     * 增强查找：增加通过 hint 和任意 editable 节点的查找
     */
    fun performInput(text: String): Boolean {
        Log.d(TAG, "🔥🔥🔥 [NEW CODE V2] performInput called with text: '$text' 🔥🔥🔥")
        return try {
            val rootNode = rootInActiveWindow ?: return false
            var editNode: AccessibilityNodeInfo? = null
            
            // 1️⃣ 优先查找有焦点的可编辑框
            editNode = findFocusedEditText(rootNode)
            
            // 2️⃣ 如果没找到，查找第一个可见的可编辑框 (标准 EditText)
            if (editNode == null) {
                Log.d(TAG, "No focused EditText, searching for visible EditText")
                editNode = findFirstVisibleEditText(rootNode)
            }
            
            // 3️⃣ 🔥 如果还没找到，查找匹配提示语的节点 (针对美团等自定义 View)
            if (editNode == null) {
                Log.d(TAG, "No visible EditText, searching by hint text...")
                editNode = findInputNodeByHint(rootNode)
            }
            
            // 4️⃣ 🔥 最后尝试：任何宣称自己是 Editable 的节点
            if (editNode == null) {
                Log.d(TAG, "Still not found, searching for ANY editable node...")
                editNode = findAnyEditableNode(rootNode)
            }
            
            if (editNode != null) {
                Log.d(TAG, "🎯 Target node found: class=${editNode.className}, editable=${editNode.isEditable}")
                
                // 尝试聚焦
                if (!editNode.isFocused) {
                    editNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                    // 如果在店内搜索，可能需要点击才能激活
                    val rect = android.graphics.Rect()
                    editNode.getBoundsInScreen(rect)
                    performTap((rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2)
                    Thread.sleep(500)
                }

                // 3️⃣ 尝试 ACTION_SET_TEXT
                val arguments = android.os.Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                val success = editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                
                if (success) {
                    Log.d(TAG, "✅ Input text '$text' via ACTION_SET_TEXT: success")
                    editNode.recycle()
                    return true
                } else {
                    Log.w(TAG, "⚠️ ACTION_SET_TEXT failed, trying clipboard paste...")
                    // 4️⃣ 备选方案：使用剪贴板 + ACTION_PASTE
                    val pasteSuccess = performInputViaClipboard(text, editNode)
                    editNode.recycle()
                    return pasteSuccess
                }
            } else {
                Log.w(TAG, "❌ No input node found via traversal. Trying logic fallback: SYSTEM FOCUS...")
                
                // 🔥🔥🔥 终极兜底：直接问系统谁有焦点 🔥🔥🔥
                // 这能解决遍历树找不到节点，但键盘其实已经弹出的情况
                val systemFocus = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                if (systemFocus != null) {
                    Log.d(TAG, "⚡ Found SYSTEM FOCUS node: ${systemFocus.className}")
                    val pasteSuccess = performInputViaClipboard(text, systemFocus)
                    systemFocus.recycle()
                    if (pasteSuccess) return true
                }
                
                Log.e(TAG, "💀 Absolute failure: No node found even via system focus.")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to perform input", e)
            false
        }
    }
    
    /**
     * 使用剪贴板粘贴输入文本（备选方案）
     */
    private fun performInputViaClipboard(text: String, editNode: AccessibilityNodeInfo): Boolean {
        return try {
            Log.d(TAG, "📋 Using clipboard paste for: '$text'")
            
            // 1. 将文本复制到剪贴板
            val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("autoglm_input", text)
            clipboard.setPrimaryClip(clip)
            Log.d(TAG, "✅ Text copied to clipboard")
            Thread.sleep(200)
            
            // 2. 执行粘贴操作
            val pasteSuccess = editNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            Log.d(TAG, "📋 Clipboard paste result: $pasteSuccess")
            
            pasteSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Clipboard paste failed", e)
            false
        }
    }

    private fun findFocusedEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 1. 优先：既有焦点又是 EditText
        if (node.isFocused && (node.isEditable || node.className.contains("EditText", ignoreCase = true))) {
            return node
        }
        
        // 递归查找
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFocusedEditText(child)
            if (result != null) return result
            child.recycle()
        }
        
        return null
    }

    private fun findFirstVisibleEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 2. 次选：可见的 EditText
        if (node.isVisibleToUser && (node.isEditable || node.className.contains("EditText", ignoreCase = true))) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFirstVisibleEditText(child)
            if (result != null) return result
            child.recycle()
        }
        
        return null
    }
    
    /**
     * 🔥 新增：通过常见提示语查找输入框（针对美团等自定义控件）
     */
    private fun findInputNodeByHint(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val text = node.text?.toString() ?: ""
        val hintText = node.hintText?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        
        // 常见的搜索框提示语
        val keywords = listOf("请输入", "搜索", "Search", "输入")
        val allText = "$text $hintText $contentDesc"
        
        val isMatch = keywords.any { allText.contains(it) }
        
        if (node.isVisibleToUser && isMatch && (node.isClickable || node.isFocusable)) {
            Log.d(TAG, "🎯 Found potential input by hint: class=${node.className}, text=$allText")
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findInputNodeByHint(child)
            if (result != null) return result
            child.recycle()
        }
        
        return null
    }

    /**
     * 🔥 新增：查找任何宣称自己是 Editable 的节点
     */
    private fun findAnyEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isVisibleToUser && node.isEditable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findAnyEditableNode(child)
            if (result != null) return result
            child.recycle()
        }
        
        return null
    }

    /**
     * 截取屏幕并返回 Base64 编码
     */
    fun takeScreenshotBase64(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 使用 takeScreenshot API
                val latch = CountDownLatch(1)
                var bitmap: Bitmap? = null
                
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshotResult: ScreenshotResult) {
                            bitmap = Bitmap.wrapHardwareBuffer(
                                screenshotResult.hardwareBuffer,
                                screenshotResult.colorSpace
                            )
                            latch.countDown()
                        }
                        
                        override fun onFailure(errorCode: Int) {
                            Log.e(TAG, "Screenshot failed with error code: $errorCode")
                            latch.countDown()
                        }
                    }
                )
                
                latch.await(5, TimeUnit.SECONDS)
                
                if (bitmap != null) {
                    val outputStream = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val bytes = outputStream.toByteArray()
                    bitmap!!.recycle()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                } else {
                    null
                }
            } else {
                // Android 7-10 不支持 takeScreenshot，返回 null
                // 调用方应降级到 ADB screencap
                Log.w(TAG, "takeScreenshot not supported on Android < 11")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to take screenshot", e)
            null
        }
    }

    /**
     * 启动应用 - 使用多种方法尝试
     */
    fun launchApp(packageName: String): Boolean {
        Log.d(TAG, "Attempting to launch app: $packageName")
        
        // 方法 1: 使用 PackageManager.getLaunchIntentForPackage
        try {
            val pm = packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                Log.i(TAG, "Successfully launched app via PackageManager: $packageName")
                return true
            } else {
                Log.w(TAG, "getLaunchIntentForPackage returned null for: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Method 1 failed: ${e.message}")
        }
        
        // 方法 2: 使用显式 Intent（针对常见应用）
        try {
            Log.d(TAG, "Trying explicit intent for: $packageName")
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setPackage(packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            if (resolveInfo.isNotEmpty()) {
                val activityInfo = resolveInfo[0].activityInfo
                intent.setClassName(activityInfo.packageName, activityInfo.name)
                startActivity(intent)
                Log.i(TAG, "Successfully launched app via explicit intent: $packageName")
                return true
            } else {
                Log.w(TAG, "No launcher activity found for: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Method 2 failed: ${e.message}")
        }
        
        // 方法 3: 使用 am start 命令
        try {
            Log.d(TAG, "Trying am start command for: $packageName")
            val process = Runtime.getRuntime().exec(
                arrayOf("sh", "-c", "am start -n $packageName/.MainActivity 2>&1 || am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p $packageName")
            )
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.i(TAG, "Successfully launched app via am start: $packageName")
                return true
            } else {
                Log.w(TAG, "am start command failed with exit code: $exitCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Method 3 failed: ${e.message}")
        }
        
        Log.e(TAG, "All methods failed to launch: $packageName")
        return false
    }

    // ============ ADB Keyboard 集成 ============
    
    private var originalIme: String? = null
    
    fun switchInputMethod(ime: String): Boolean {
        return try {
            // 保存当前输入法
            originalIme = android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.DEFAULT_INPUT_METHOD
            )
            
            Log.d(TAG, "Current IME: $originalIme, switching to: $ime")
            
            // 切换到指定输入法
            android.provider.Settings.Secure.putString(
                contentResolver,
                android.provider.Settings.Secure.DEFAULT_INPUT_METHOD,
                ime
            )
            
            Log.i(TAG, "Switched to IME: $ime")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch IME: ${e.message}")
            false
        }
    }
    
    fun restoreInputMethod(): Boolean {
        return try {
            if (originalIme != null) {
                android.provider.Settings.Secure.putString(
                    contentResolver,
                    android.provider.Settings.Secure.DEFAULT_INPUT_METHOD,
                    originalIme
                )
                Log.i(TAG, "Restored IME: $originalIme")
                originalIme = null
                true
            } else {
                Log.w(TAG, "No original IME to restore")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore IME: ${e.message}")
            false
        }
    }
    
    fun sendAdbBroadcast(action: String, extras: org.json.JSONObject?): Boolean {
        return try {
            val intent = Intent(action)
            
            // 添加 extras
            extras?.let {
                val keys = it.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = it.getString(key)
                    intent.putExtra(key, value)
                }
            }
            
            sendBroadcast(intent)
            Log.i(TAG, "Sent broadcast: $action")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send broadcast: ${e.message}")
            false
        }
    }
}
