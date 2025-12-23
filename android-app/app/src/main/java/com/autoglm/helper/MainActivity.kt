package com.autoglm.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.chaquo.python.Python

class MainActivity : Activity(), LogCallback {

    private lateinit var logText: TextView
    private lateinit var logScroll: ScrollView
    private lateinit var logToggle: TextView
    private lateinit var taskInput: EditText
    private lateinit var executeButton: Button
    private lateinit var stopButton: Button
    private lateinit var openSettingsButton: TextView
    private lateinit var copyLogButton: TextView
    private lateinit var settingsButton: android.widget.ImageButton // The Gear Icon
    private lateinit var sparklingStar: android.widget.ImageView // The Three Body Star
    private lateinit var titleImage: android.widget.ImageView // Pixel Art Title
    
    private val handler = Handler(Looper.getMainLooper())
    private var isTaskRunning = false
    private var isLogExpanded = true  // Default expanded to cover the base
    
    // Animation Constants
    private val STAR_FADE_DURATION = 2000L
    private val STAR_BLINK_DURATION = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        logText = findViewById(R.id.logText)
        logScroll = findViewById(R.id.logScroll)
        logToggle = findViewById(R.id.logToggle)
        taskInput = findViewById(R.id.taskInput)
        executeButton = findViewById(R.id.executeButton)
        stopButton = findViewById(R.id.stopButton)
        openSettingsButton = findViewById(R.id.openSettingsButton)
        copyLogButton = findViewById(R.id.copyLogButton)
        sparklingStar = findViewById(R.id.sparklingStar)
        settingsButton = findViewById(R.id.settingsButton)
        titleImage = findViewById(R.id.titleImage)
        
        val logLabel = findViewById<TextView>(R.id.logLabel)
        
        // --- FORCE TERMINAL STYLE (Fix for Black Text Issue) ---
        val terminalGreen = android.graphics.Color.parseColor("#00E676")
        val terminalGreenDark = android.graphics.Color.parseColor("#1B5E20")
        val monoTypeface = android.graphics.Typeface.MONOSPACE
        
        openSettingsButton.setTextColor(terminalGreen)
        logToggle.setTextColor(terminalGreen)
        logLabel.setTextColor(terminalGreen)
        copyLogButton.setTextColor(terminalGreen)
        logText.setTextColor(terminalGreen)
        
        taskInput.setTextColor(terminalGreen)
        taskInput.setHintTextColor(terminalGreenDark)
        
        // Removed manual typeface override to allow XML font (@font/vt323_regular) to work
        // -------------------------------------------------------
        
        // Settings Button Logic
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Log Toggle Logic
        logToggle.setOnClickListener {
            toggleLogVisibility()
        }
        
        executeButton.setOnClickListener {
            startTask()
        }
        
        stopButton.setOnClickListener {
            // Visual Interaction: Turn Green + EXTINGUISH Star
            stopButton.setBackgroundResource(R.drawable.btn_salvation) // Turn Green
            stopButton.setTextColor(android.graphics.Color.parseColor("#00E676"))
            stopButton.text = "SAVED" // 🌍 拯救！
            stopStarSignal()
            
            onLog("🛑 CONNECTION SEVERED.")
            onLog("🌍 YOU SAVED THE WORLD... AGAIN.")
            
            try {
                val py = Python.getInstance()
                
                // 1. 启用视觉停止信号（快速响应）
                val helperModule = py.getModule("android_helper")
                helperModule.callAttr("enable_visual_stop_signal")
                onLog("✅ 已在下一帧截图上添加红色停止横幅")
                
                // 2. 设置软着陆（保底机制，1步后强制停止）
                val agentModule = py.getModule("agent_main")
                val result = agentModule.callAttr("stop_gracefully", 1).toInt()
                
                if (result > 0) {
                    onLog("✅ 已设置保底停止点：第 $result 步")
                } else {
                    onLog("⚠️ 当前没有正在运行的任务")
                }
            } catch (e: Exception) {
                onLog("❌ 停止失败: ${e.message}")
            }
        }
        
        openSettingsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        
        copyLogButton.setOnClickListener {
            copyLogToClipboard()
        }
        
        updateStatus()
        
        // 🎬 Start Pixel Title Animation
        playPixelTitleAnimation()
    }

    // Only Animation and Color Change - NO TEXT CHANGE
    // Start "Three-Body Star" Blinking Animation (Signal Sent)
    private fun startStarSignal() {
        // Ensure star is on top
        sparklingStar.bringToFront()
        sparklingStar.visibility = View.VISIBLE
        
        // Randomize Position (Small distant dot appearing in different spots)
        val randomX = (Math.random() * 120 - 60).toFloat() 
        val randomY = (Math.random() * 80 - 40).toFloat()
        sparklingStar.translationX = randomX
        sparklingStar.translationY = randomY
        
        // Star Animation: Fade In -> Blink
        sparklingStar.alpha = 0f // Start invisible
        sparklingStar.animate()
            .alpha(1.0f)
            .setDuration(STAR_FADE_DURATION)
            .withEndAction {
                // Blink indefinitely
                val blinkAnim = android.view.animation.AlphaAnimation(1.0f, 0.2f)
                blinkAnim.duration = STAR_BLINK_DURATION
                blinkAnim.repeatMode = android.view.animation.Animation.REVERSE
                blinkAnim.repeatCount = android.view.animation.Animation.INFINITE
                sparklingStar.startAnimation(blinkAnim)
            }
            .start()
    }

    // Stop Star Signal (Connection Cut / Task End)
    private fun stopStarSignal() {
        sparklingStar.clearAnimation()
        sparklingStar.animate().alpha(0f).setDuration(500).withEndAction {
            sparklingStar.visibility = View.INVISIBLE
        }.start()
    }

    private fun copyLogToClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("AutoGLM Log", logText.text.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Log copied", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleLogVisibility() {
        isLogExpanded = !isLogExpanded
        if (isLogExpanded) {
            // 展开日志
            logScroll.visibility = View.VISIBLE
            logToggle.text = "▼"
        } else {
            // 折叠日志
            logScroll.visibility = View.GONE
            logToggle.text = "▶"
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val service = AutoGLMAccessibilityService.getInstance()
        if (service != null) {
            executeButton.isEnabled = !isTaskRunning
            
            // Only enable Stop button if task is running
            stopButton.isEnabled = isTaskRunning
            
            // Reset Stop Button style if task is running
            if (isTaskRunning) {
                stopButton.setBackgroundResource(R.drawable.tech_button_stop_bg) // Red background
                stopButton.setTextColor(android.graphics.Color.WHITE)
                stopButton.text = "ABORT" // Reset text
                
                // Stop Star Animation
                sparklingStar.clearAnimation()
                sparklingStar.alpha = 0f
            }
            
            // Check ADB permission (only once per session)
            checkAdbPermission()
            
        } else {
            executeButton.isEnabled = false
            stopButton.isEnabled = false
        }
    }
    
    private var adbPermissionChecked = false
    
    private fun checkAdbPermission() {
        if (adbPermissionChecked) return
        adbPermissionChecked = true
        
        try {
            // Try to read a secure setting - if it works, we might have the permission
            // But the real test is trying to WRITE, which we can't easily test without side effects
            // Instead, we'll try a harmless write and restore
            val testKey = "autoglm_permission_test"
            val originalValue = Settings.Secure.getString(contentResolver, testKey)
            
            // Try to write
            val canWrite = try {
                Settings.Secure.putString(contentResolver, testKey, "test")
                // Restore original
                if (originalValue != null) {
                    Settings.Secure.putString(contentResolver, testKey, originalValue)
                }
                true
            } catch (e: SecurityException) {
                false
            }
            
            if (!canWrite) {
                onLog("⚠️ ADB 权限未授权！请在 PC 上执行:")
                onLog("adb shell pm grant com.autoglm.helper android.permission.WRITE_SECURE_SETTINGS")
                Toast.makeText(this, "⚠️ 请先授权 ADB 权限（查看日志）", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    private fun startTask() {
        val task = taskInput.text.toString()
        if (task.isBlank()) {
            Toast.makeText(this, "Please input task", Toast.LENGTH_SHORT).show()
            return
        }

        logText.text = ""
        isTaskRunning = true
        updateStatus() // 更新按钮状态
        startStarSignal() // 🌟 发射信号：三体星开始闪烁
        
        // Read Config from SharedPreferences
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "562eac47fb0c43fa995ee58261d12a52.Y2HAB0eRQPyXKiHI")
        val baseUrl = prefs.getString("base_url", "https://open.bigmodel.cn/api/paas/v4/")
        val modelName = prefs.getString("model_name", "autoglm-phone")
        
        Thread {
            try {
                val py = Python.getInstance()
                val module = py.getModule("agent_main")
                
                // onLog("🚀 Executing with Model: $modelName") // Optional: Log model usage
                
                module.callAttr("run_task", apiKey, baseUrl, modelName, task, this)
                
                runOnUiThread {
                    isTaskRunning = false
                    stopStarSignal() // 🌟 任务完成：信号切断
                    updateStatus()
                    Toast.makeText(this, "Task completed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    onLog("❌ 运行出错: ${e.message}")
                    isTaskRunning = false
                    stopStarSignal()
                    updateStatus()
                }
            }
        }.start()
    }

    override fun onLog(message: String) {
        runOnUiThread {
            logText.append("$message\n")
            logScroll.post {
                logScroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun playPixelTitleAnimation() {
        // onLog("✨ Booting System UI...") 
        
        val originalDrawable = titleImage.drawable
        if (originalDrawable == null) {
            onLog("⚠️ Error: Title Image not loaded")
            return
        }
        
        // Create ClipDrawable
        val clipDrawable = android.graphics.drawable.ClipDrawable(
            originalDrawable, 
            android.view.Gravity.LEFT, 
            android.graphics.drawable.ClipDrawable.HORIZONTAL
        )
        
        // 🛑 Force invisible immediately
        clipDrawable.level = 0
        titleImage.setImageDrawable(clipDrawable)
        
        // Animation params
        val steps = 20
        val maxLevel = 10000
        val stepSize = maxLevel / steps
        val stepDelay = 80L // Faster speed for smoother look
        val totalLoops = 2
        var currentLoop = 0
        
        fun animateStep(currentLevel: Int) {
            if (currentLevel <= maxLevel) {
                clipDrawable.level = currentLevel
                titleImage.invalidate() // Force redraw
                handler.postDelayed({ animateStep(currentLevel + stepSize) }, stepDelay)
            } else {
                currentLoop++
                if (currentLoop < totalLoops) {
                    handler.postDelayed({
                        clipDrawable.level = 0
                        titleImage.invalidate()
                         handler.postDelayed({ animateStep(stepSize) }, 200)
                    }, 1000)
                } else {
                    clipDrawable.level = 10000
                    titleImage.invalidate()
                }
            }
        }
        
        // Start animation loop
        handler.postDelayed({ animateStep(stepSize) }, 500)
    }
}
