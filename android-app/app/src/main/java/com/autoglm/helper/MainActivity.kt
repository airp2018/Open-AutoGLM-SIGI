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
    private lateinit var clearInputButton: android.widget.ImageView // Clear Input Button
    private lateinit var btnDoomsdayList: TextView
    private lateinit var btnAddTask: android.widget.ImageView
    
    private val handler = Handler(Looper.getMainLooper())
    private var isTaskRunning = false
    private var isLogExpanded = true  // Default expanded to cover the base
    
    // Animation Constants
    private val STAR_FADE_DURATION = 2000L
    private val STAR_BLINK_DURATION = 1500L
    
    // SoundPool
    private lateinit var soundPool: android.media.SoundPool
    private var sfxExecute: Int = 0
    private var sfxAbort: Int = 0
    private var sfxComplete: Int = 0
    private var sfxClick: Int = 0

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
        clearInputButton = findViewById(R.id.clearInputButton)
        btnDoomsdayList = findViewById(R.id.btnDoomsdayList)
        btnAddTask = findViewById(R.id.btnAddTask)
        
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

        // Clear Input Logic
        clearInputButton.setOnClickListener {
            taskInput.setText("")
        }

        taskInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    clearInputButton.visibility = View.GONE
                    btnAddTask.visibility = View.GONE
                } else {
                    clearInputButton.visibility = View.VISIBLE
                    btnAddTask.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Settings Button Logic
        settingsButton.setOnClickListener {
            playSfx(sfxClick)
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
            // 🌍 拯救世界计数器 (Read Only here)
            val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
            val saveCount = prefs.getInt("world_save_count", 0)
            
            // Visual Interaction: Turn Green + EXTINGUISH Star
            stopButton.setBackgroundResource(R.drawable.btn_salvation) // Turn Green Ellipse
            stopButton.setTextColor(android.graphics.Color.WHITE) // 白色文字
            stopButton.text = "SAVED" // 🌍 拯救！
            stopStarSignal()
            playSfx(sfxAbort) // 🔊 Play ABORT Sound
            playSfx(sfxComplete) // 🔊 Play Task Complete Feedback
            
            // Disable buttons to prevent multi-click during cooldown
            executeButton.isEnabled = false
            stopButton.isEnabled = false
            
            onLog("🛑 CONNECTION SEVERED.")
            onLog("🌍 YOU SAVED THE WORLD... AGAIN.")
            onLog("") // 空行
            onLog("═══════════════════════════════════════")
            onLog("  You have saved the world $saveCount times")
            onLog("═══════════════════════════════════════")
            
            try {
                val py = Python.getInstance()
                
                // 设置软着陆（1步后强制停止）
                val agentModule = py.getModule("agent_main")
                val result = agentModule.callAttr("stop_gracefully", 1).toInt()
                
                if (result > 0) {
                    onLog("✅ 已设置停止点：第 $result 步")
                } else {
                    onLog("⚠️ 当前没有正在运行的任务")
                }
            } catch (e: Exception) {
                onLog("❌ 停止失败: ${e.message}")
            }
            
            // 🛑 FORCE RESET UI STATE after delay (Prevent stuck button)
            handler.postDelayed({
                if (isTaskRunning) {
                    isTaskRunning = false
                    resetUI() // FORCE RESET to Initial State
                    onLog("🔄 System reset complete.")
                }
            }, 2000)
        }
        
        openSettingsButton.setOnClickListener {
            playSfx(sfxClick)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        
        copyLogButton.setOnClickListener {
            copyLogToClipboard()
        }
        
        updateStatus()
        
        // 🎬 Start Pixel Title Animation
        playPixelTitleAnimation()
        
        // Apply Skin (Ensure correct state on cold start)
        // Apply Skin (Ensure correct state on cold start)
        applySkin()
        
        // Doomsday List Logic
        btnDoomsdayList.setOnClickListener { 
            playSfx(sfxClick)
            showDoomsdayListDialog() 
        }
        
        btnAddTask.setOnClickListener {
            val task = taskInput.text.toString().trim()
            if (task.isNotEmpty()) {
                addToDoomsdayList(task)
                Toast.makeText(this, "Protocol Encoded into Doomsday List.", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Initialize SoundPool
        val audioAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = android.media.SoundPool.Builder()
            .setMaxStreams(2) // Allow overlapping sounds
            .setAudioAttributes(audioAttributes)
            .build()
            
        sfxExecute = soundPool.load(this, R.raw.sfx_execute, 1)
        sfxAbort = soundPool.load(this, R.raw.sfx_abort, 1)
        sfxComplete = soundPool.load(this, R.raw.sfx_complete, 1)
        sfxClick = soundPool.load(this, R.raw.sfx_click, 1)
        
        // --- Elastic Drag Logic for Hidden World ---
        val uiContainer = findViewById<android.widget.FrameLayout>(R.id.uiContainer)
        val hiddenWorldBg = findViewById<android.widget.ImageView>(R.id.hiddenWorldBg)
        val dragTrigger = findViewById<android.view.View>(R.id.dragTrigger)
        
        // Robust Layout Listener: Keeps hidden world synced even if layout changes (keyboard, dialogs)
        uiContainer.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            val height = bottom - top
            if (height > 0 && uiContainer.translationY == 0f) {
                hiddenWorldBg.translationY = height.toFloat()
            }
        }
        
        // Initial Force Set
        uiContainer.post {
            resetRevealState(uiContainer, hiddenWorldBg)
        }
        
        dragTrigger.setOnTouchListener(object : android.view.View.OnTouchListener {
             var startY = 0f
             var isDragging = false
             
             override fun onTouch(v: android.view.View, event: android.view.MotionEvent): Boolean {
                 when (event.action) {
                     android.view.MotionEvent.ACTION_DOWN -> {
                         startY = event.rawY
                         isDragging = true
                         return true
                     }
                     android.view.MotionEvent.ACTION_MOVE -> {
                         if (!isDragging) return false
                         val deltaY = event.rawY - startY
                         val maxDrag = resources.displayMetrics.heightPixels * 0.8f 
                         
                         if (deltaY < 0) {
                             val dampFactor = 0.6f 
                             val targetY = deltaY * dampFactor
                             
                             if (kotlin.math.abs(targetY) < maxDrag) {
                                uiContainer.translationY = targetY
                                hiddenWorldBg.translationY = uiContainer.height.toFloat() + targetY
                             }
                         }
                         return true
                     }
                     android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                         isDragging = false
                         resetRevealState(uiContainer, hiddenWorldBg, animate = true)
                         return true
                     }
                 }
                 return false
             }
        })
    }

    private fun resetRevealState(ui: android.view.View, bg: android.view.View, animate: Boolean = false) {
        if (animate) {
            ui.animate()
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(android.view.animation.OvershootInterpolator(0.8f))
                .start()
            bg.animate()
                .translationY(ui.height.toFloat())
                .setDuration(400)
                .setInterpolator(android.view.animation.OvershootInterpolator(0.8f))
                .start()
        } else {
            ui.translationY = 0f
            bg.translationY = ui.height.toFloat()
        }
    }


    
    
    // --- Doomsday List Features ---
    
    private val DEFAULT_PROTOCOLS = listOf(
        "打开微信，给微信里的好友某某，发一条短信：你好吗",
        "打开淘宝，查看已购商品里的水龙头",
        "打开12306，查找12月30日，北京去上海的火车票",
        "打开微博，发一条：世界你好",
        "打开美团，去附近最近的肯德基，买一份香辣鸡腿堡",
        "打开网易云音乐，搜周杰伦的歌曲"
    )
    
    private fun showDoomsdayListDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_doomsday_list)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val listContainer = dialog.findViewById<android.widget.LinearLayout>(R.id.listContainer)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseList)
        
        // Load List
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        // Simple JSON-like parsing or delimiter storage for simplicity
        val savedSet = prefs.getStringSet("doomsday_list", null)
        val protocols = if (savedSet != null) savedSet.toList().sorted() else DEFAULT_PROTOCOLS // Set has no order, sort ensures stability
        
        // Build Views
        val termGreen = android.graphics.Color.parseColor("#00E676")
        
        // Fix for "Unresolved reference: core" - Use native API with backward compatibility check
        val typeFace = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            resources.getFont(R.font.vt323_regular)
        } else {
            android.graphics.Typeface.MONOSPACE
        }
        
        protocols.forEach { protocol ->
            val tv = TextView(this)
            tv.text = "> $protocol"
            tv.setTextColor(termGreen) // All Green as requested
            tv.textSize = 14f // Compact printer size
            tv.setPadding(0, 16, 0, 16)
            tv.typeface = typeFace // VT323
            tv.letterSpacing = 0.05f // Slight spacing for readability
            
            // Add click listener to fill input
            tv.setOnClickListener {
                taskInput.setText(protocol)
                taskInput.setSelection(taskInput.text.length) // Move cursor to end
                dialog.dismiss()
            }
            
            // Add Long Click to Delete
            tv.setOnLongClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle("DELETE PROTOCOL?")
                    .setMessage(protocol)
                    .setPositiveButton("DELETE") { _, _ ->
                        removeDoomsdayList(protocol)
                        dialog.dismiss()
                        showDoomsdayListDialog() // Refresh list
                        Toast.makeText(this, "Protocol Deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()
                true
            }
            
            // Add hover/press effect via background
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            tv.setBackgroundResource(outValue.resourceId)
            
            listContainer.addView(tv)
            
             // Divider
            val divider = View(this)
            divider.layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            divider.setBackgroundColor(android.graphics.Color.parseColor("#3300E676"))
            listContainer.addView(divider)
        }
        
        btnClose.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }
    
    private fun addToDoomsdayList(task: String) {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val savedSet = prefs.getStringSet("doomsday_list", null)
        val newSet = savedSet?.toMutableSet() ?: DEFAULT_PROTOCOLS.toMutableSet()
        
        newSet.add(task)
        prefs.edit().putStringSet("doomsday_list", newSet).apply()
    }
    
    private fun removeDoomsdayList(task: String) {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val savedSet = prefs.getStringSet("doomsday_list", null)
        val newSet = savedSet?.toMutableSet() ?: DEFAULT_PROTOCOLS.toMutableSet()

        if (newSet.contains(task)) {
            newSet.remove(task)
            prefs.edit().putStringSet("doomsday_list", newSet).apply()
        }
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
        applySkin()
        
        // Force layout reset to prevent stuck UI
        val uiContainer = findViewById<android.widget.FrameLayout>(R.id.uiContainer)
        val hiddenWorldBg = findViewById<android.widget.ImageView>(R.id.hiddenWorldBg)
        if (uiContainer != null && hiddenWorldBg != null) {
            uiContainer.post { resetRevealState(uiContainer, hiddenWorldBg) }
        }
    }
    
    private fun applySkin() {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val saveCount = prefs.getInt("world_save_count", 0)
        var skin = prefs.getString("app_skin", "black")
        
        // 🔒 STRICT LOCK: If count < 2, FORCE revert to black (Shadow/Void)
        if (saveCount < 2) {
            skin = "black"
        }
        
        val bgImage = findViewById<android.widget.ImageView>(R.id.backgroundImage)
        
        if (skin == "red_coast") {
            bgImage.setImageResource(R.drawable.bg_red_coast) 
            bgImage.alpha = 0.8f
        } else if (skin == "trisolaris") { 
            // Fallback or specific user setting if they somehow selected it, but main flow is Red Coast
            // Actually, user strict requirement: Settings BG != Cover BG.
            // Cover = Red Coast. Settings = Trisolaris.
             bgImage.setImageResource(R.drawable.bg_red_coast)
             bgImage.alpha = 0.8f
        } else {
            // Default Black / Dark Void
            bgImage.setImageDrawable(null)
            bgImage.setBackgroundColor(android.graphics.Color.BLACK)
        }
    }

    private fun updateStatus() {
        // This is called when Task Starts
        val service = AutoGLMAccessibilityService.getInstance()
        if (service != null) {
            
            if (isTaskRunning) {
                // RUNNING STATE
                // Execute -> Dark (Disabled)
                executeButton.isEnabled = false
                executeButton.setBackgroundResource(R.drawable.btn_dark) 
                executeButton.setTextColor(android.graphics.Color.GRAY) // Dim text
                
                // Stop -> Green Rect (Active Abort)
                stopButton.isEnabled = true
                stopButton.setBackgroundResource(R.drawable.btn_stop_green)
                stopButton.setTextColor(android.graphics.Color.WHITE)
                stopButton.text = "ABORT" 
                
                // Stop Star Animation (Already handled in startTask, but good insurance)
                // sparklingStar.clearAnimation() 
            } else {
                // IDLE / RESET STATE
                resetUI() 
            }
            
            // Check ADB permission (only once per session)
            checkAdbPermission()
            
        } else {
            executeButton.isEnabled = false
            stopButton.isEnabled = false
        }
    }
    
    // Strict Reset Logic
    private fun resetUI() {
        // EXECUTE -> Back to Red Rect
        executeButton.isEnabled = true
        executeButton.setBackgroundResource(R.drawable.btn_execute_red) // Need to define this drawable if not exists, or usage primary red
        // Assuming btn_execute_bg_red exists or we use raw colour
        // Let's use standard button bg for enabled state
        executeButton.background = getDrawable(R.drawable.tech_button_bg) // Revert to original white/standard bg? No, user said RED.
        // Wait, user said "Execute button initial is RED". 
        // My previous context said it was White.
        // User correction: "Execute is RED background".
        // OK, I will enforce RED background.
        executeButton.setBackgroundResource(R.drawable.btn_execute_red)
        executeButton.setTextColor(android.graphics.Color.WHITE)
        executeButton.text = "EXECUTE"
        
        // STOP -> Back to Dark Rect
        stopButton.isEnabled = false
        stopButton.setBackgroundResource(R.drawable.btn_dark)
        stopButton.setTextColor(android.graphics.Color.WHITE)
        stopButton.text = "ABORT"
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

    private fun showLockdownDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_system_lock)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true) 
        
        val input = dialog.findViewById<EditText>(R.id.inputUnlockKey)
        val btn = dialog.findViewById<Button>(R.id.btnUnlock)
        
        btn.setOnClickListener {
            val key = input.text.toString()
            if (key == "1379") {
                // PERMANENT UNLOCK
                val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
                
                // Speed Run Check (Easter Egg: 蒸蚌)
                val lockTime = prefs.getLong("lockdown_start_time", 0L)
                val currentTime = System.currentTimeMillis()
                val isSpeedRun = (lockTime > 0) && ((currentTime - lockTime) < 180000) // < 3 mins
                
                prefs.edit()
                    .putBoolean("permanent_unlock", true) // The Flag of Freedom
                    .putInt("fatigue_count", 0)
                    .apply()
                
                dialog.dismiss()
                
                if (isSpeedRun) {
                     // 🥚 Easter Egg: Zheng Bang!
                     playSfx(sfxComplete)
                     val toast = Toast.makeText(this, "\n   蒸 蚌 !!!   \n   (Zheng Bang)   \n", Toast.LENGTH_LONG)
                     val view = toast.view
                     view?.setBackgroundColor(android.graphics.Color.parseColor("#FFD700")) // Gold
                     val text = view?.findViewById<TextView>(android.R.id.message)
                     text?.setTextColor(android.graphics.Color.BLACK)
                     text?.textSize = 24f
                     text?.typeface = android.graphics.Typeface.DEFAULT_BOLD
                     text?.gravity = android.view.Gravity.CENTER
                     toast.show()
                } else {
                    Toast.makeText(this, "高维防御系统已破解，限制永久移除。", Toast.LENGTH_LONG).show()
                    playSfx(sfxComplete)
                }
            } else {
                Toast.makeText(this, "访问拒绝，密钥无效。", Toast.LENGTH_SHORT).show()
                input.setText("")
                playSfx(sfxAbort)
            }
        }
        
        dialog.show()
        playSfx(sfxAbort) 
    }

    private fun startTask() {
        // -1. Check for Permanent Unlock (Awakened State)
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val isPermanentlyUnlocked = prefs.getBoolean("permanent_unlock", false)
        if (isPermanentlyUnlocked) {
            // Bypass all limits
            executeTaskLogic(prefs)
            return
        }

        // 0. Lockdown Check with Auto-Unlock (Limited State)
        var fatigueCount = prefs.getInt("fatigue_count", 0)
        
        // Threshold set to 7 (More frequent triggers for gameplay)
        if (fatigueCount >= 7) {
            val lockTime = prefs.getLong("lockdown_start_time", 0L)
            val currentTime = System.currentTimeMillis()
            
            // 3 minutes = 180,000 ms
            if (lockTime > 0 && (currentTime - lockTime > 180000)) {
                // Auto-Unlock (Reset to 0, giving full 7 tries again)
                fatigueCount = 0
                prefs.edit()
                    .putInt("fatigue_count", fatigueCount)
                    .putLong("lockdown_start_time", 0L)
                    .apply()
                Toast.makeText(this, "质子封锁已解除，您可继续任务。", Toast.LENGTH_SHORT).show()
                // Proceed execution
            } else {
                showLockdownDialog()
                return
            }
        }
    
        val task = taskInput.text.toString()
        if (task.isBlank()) {
            Toast.makeText(this, "Please input task", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Increment Fatigue
        val newCount = fatigueCount + 1
        val editor = prefs.edit()
        editor.putInt("fatigue_count", newCount)
        
        if (newCount >= 7) {
            // Lock
            editor.putLong("lockdown_start_time", System.currentTimeMillis())
        }
        editor.apply()
        
        executeTaskLogic(prefs)
    }

    private fun executeTaskLogic(prefs: android.content.SharedPreferences) {
        val task = taskInput.text.toString()
        if (task.isBlank()) return // Should be checked before

        playSfx(sfxExecute) 

        logText.text = ""
        isTaskRunning = true
        updateStatus() 
        startStarSignal() 
        
        // World Save Count (Skin Achievement)
        val saveCount = prefs.getInt("world_save_count", 0) + 1
        prefs.edit().putInt("world_save_count", saveCount).apply()
        
        if (saveCount == 2) {
            prefs.edit().putString("app_skin", "red_coast").apply()
            applySkin() 
            Toast.makeText(this, "New Skin Unlocked: The Last Cyberphone", Toast.LENGTH_LONG).show()
        }
        
        val apiKey = prefs.getString("api_key", "562eac47fb0c43fa995ee58261d12a52.Y2HAB0eRQPyXKiHI")
        val baseUrl = prefs.getString("base_url", "https://open.bigmodel.cn/api/paas/v4/")
        val modelName = prefs.getString("model_name", "autoglm-phone")
        val language = prefs.getString("app_language", "Chinese")
        
        Thread {
            try {
                val py = Python.getInstance()
                val module = py.getModule("agent_main")
                
                module.callAttr("run_task", apiKey, baseUrl, modelName, task, this, language)
                
                runOnUiThread {
                    isTaskRunning = false
                    stopStarSignal() 
                    updateStatus()
                    playSfx(sfxComplete) 
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    onLog("Error: ${e.message}")
                    isTaskRunning = false
                    stopStarSignal()
                    updateStatus()
                    playSfx(sfxAbort) 
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

    private fun playSfx(soundId: Int) {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val isSfxEnabled = prefs.getBoolean("app_sfx_enabled", true)
        
        if (isSfxEnabled && soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
}
