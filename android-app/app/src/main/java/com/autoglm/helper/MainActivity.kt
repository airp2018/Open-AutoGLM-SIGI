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
    private lateinit var btnModeSwitch: TextView
    
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

    // --- 🏳️ Apology Stamp Logic ---
    private var taskStartTime: Long = 0
    private val apologyStamps = mapOf(
        "JP" to R.drawable.stamp_apology_jp,
        "CN" to R.drawable.stamp_apology_cn,
        "US" to R.drawable.stamp_apology_us,
        "UK" to R.drawable.stamp_apology_uk,
        "FR" to R.drawable.stamp_apology_fr,
        "DE" to R.drawable.stamp_apology_de,
        "IT" to R.drawable.stamp_apology_it,
        "IN" to R.drawable.stamp_apology_in
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        logText = findViewById(R.id.logText)

        // --- 🏳️ Auto-Detect Task Completion for Latency Check ---
        logText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString() ?: ""
                // Check for completion signal (and ensure timer is active)
                if (taskStartTime > 0 && (text.contains("[OK] 任务完成") || text.contains("MISSION ACCOMPLISHED"))) {
                     checkLatencyAndCompensate()
                }
            }
        })
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
        btnModeSwitch = findViewById(R.id.btnModeSwitch) // Initialize
        
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
            


            // --- 🏳️ Check Latency Compensation BEFORE Reset ---
            checkLatencyAndCompensate()
            
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
                val result = agentModule.callAttr("stop_gracefully", 0).toInt()
                
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
        
        // --- 💰 Agent Adventure Logic ---
        val btnAgentAdventure = findViewById<TextView>(R.id.btnAgentAdventure)
        btnAgentAdventure.setOnClickListener {
            playSfx(sfxClick)
            showAgentAdventureDialog()
        }
        
        // --- 🪐 Cyber Market Logic ---
        val btnCyberMarket = findViewById<TextView>(R.id.btnCyberMarket)
        btnCyberMarket.setOnClickListener {
            playSfx(sfxClick)
            showCyberMarketDialog()
        }
        
        btnAddTask.setOnClickListener {
            val task = taskInput.text.toString().trim()
            if (task.isNotEmpty()) {
                addToDoomsdayList(task)
                Toast.makeText(this, "Protocol Encoded into Doomsday List.", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnModeSwitch.setOnClickListener {
            playSfx(sfxClick)
            showModeSelectionDialog()
        }

        // --- 📜 RULES BUTTON LOGIC ---
        val btnRules = findViewById<TextView>(R.id.btnRules)
        btnRules.setOnClickListener {
            playSfx(sfxClick)
            showRulesDialog()
        }

        // Auto-Show Rules on First Run
        val rulesPref = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        if (rulesPref.getBoolean("has_seen_rules", false)) {
            // Already seen -> Ghost Mode (Invisible but clickable)
            btnRules.alpha = 0f
        } else {
            // First time -> Show immediately
            btnRules.alpha = 1f
            // Delay slightly to let layout settle
            handler.postDelayed({ showRulesDialog() }, 1000)
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
        
        val revealArrow = findViewById<android.view.View>(R.id.revealArrow)
        
        dragTrigger.setOnTouchListener(object : android.view.View.OnTouchListener {
             var startY = 0f
             var isLockedIn = false
             
             override fun onTouch(v: android.view.View, event: android.view.MotionEvent): Boolean {
                 val screenWidth = resources.displayMetrics.widthPixels
                 val centerX = screenWidth / 2f
                 val touchX = event.rawX
                 
                 // "Dark Door" Logic: Only active within a small center zone
                 // Threshold: ~12% of screen width (narrow strip)
                 val threshold = screenWidth * 0.12f 
                 val isInZone = kotlin.math.abs(touchX - centerX) < threshold
                 
                 when (event.action) {
                     android.view.MotionEvent.ACTION_DOWN -> {
                         if (isInZone) {
                             isLockedIn = true
                             startY = event.rawY
                             // Reveal: Low Brightness (Subtle)
                             revealArrow.animate().alpha(0.3f).setDuration(300).start()
                             v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                         } else {
                             isLockedIn = false
                             revealArrow.animate().alpha(0f).setDuration(200).start()
                         }
                         return true
                     }
                     android.view.MotionEvent.ACTION_MOVE -> {
                         // Exploration Mode: If started outside, can slide into the zone?
                         if (!isLockedIn) {
                             if (isInZone) {
                                 // Found it!
                                 isLockedIn = true
                                 startY = event.rawY // Reset Y to prevent jump
                                 revealArrow.animate().alpha(0.3f).setDuration(300).start()
                                 v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                             }
                             return true
                         }
                         
                         // Logic: If user wanders OUT of zone while dragging?
                         // "Tightrope": If they deviate too far X, maybe drop? 
                         // For now, let's be generous: once locked, you can drag loosely, 
                         // but if you intentionally slide X way out, maybe lose it?
                         // Keeping it simple: Once locked in session, stay locked until UP, 
                         // unless we want "High Skill" requirement. 
                         // User said "In other positions... cannot drag". 
                         // Let's implement strict X checking or loose? 
                         // "Exploration" implies finding the door. Once found, usually you can open it.
                         
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
                         isLockedIn = false
                         // Hide visual cue
                         revealArrow.animate().alpha(0f).setDuration(400).start()
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
        "Oh, darling，打开微博，发一条：再见爱人",
        "忍冬将至，打开美团，去附近的肯德基，买2份香辣鸡翅",
        "打开12306，查询明天北京去上海的高铁票，还有木有呀",
        "打开淘宝，查看她妈的已购商品里的西洋参，该补补了",
        "打开网易云音乐，搜索：末日狂奔（黄子弘凡）",
        "打开携程，北京百子湾附近，低于300的快捷酒店，我不想死家里呀",
        "打开豆瓣，搜索书籍《集异壁》，找到后点击查看评分详情"
    )
    
    private fun showDoomsdayListDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_doomsday_list)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val listContainer = dialog.findViewById<android.widget.LinearLayout>(R.id.listContainer)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseList)
        val tvBalance = dialog.findViewById<TextView>(R.id.tvDoomsdayBalance)
        
        // Show Balance
        tvBalance.text = "${getCoins()} 💰"
        
        // Load List
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        // Simple JSON-like parsing or delimiter storage for simplicity
        val savedSet = prefs.getStringSet("doomsday_list", null)
        val protocols = if (savedSet != null) savedSet.toList().sorted() else DEFAULT_PROTOCOLS // Set has no order, sort ensures stability
        
        // Build Views
        val termGreen = android.graphics.Color.parseColor("#00E676")
        
        // Fix for "Unresolved reference: core" - Use native API with backward compatibility check
        val typeFace = android.graphics.Typeface.MONOSPACE
        
        protocols.forEach { protocol ->
            val tv = TextView(this)
            tv.text = "> $protocol"
            tv.setTextColor(termGreen) // All Green as requested
            tv.textSize = 14f // Compact printer size
            tv.setPadding(0, 16, 0, 16)
            tv.typeface = typeFace // VT323
            tv.letterSpacing = 0.05f // Slight spacing for readability
            
            // Reward Indicator (Separate TextView on right)
            val rowLayout = android.widget.LinearLayout(this)
            rowLayout.orientation = android.widget.LinearLayout.HORIZONTAL
            
            val tvReward = TextView(this)
            tvReward.text = "+3 💰"
            tvReward.setTextColor(android.graphics.Color.parseColor("#FFD700"))
            tvReward.textSize = 12f
            tvReward.typeface = typeFace
            
            tv.layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            rowLayout.addView(tv)
            rowLayout.addView(tvReward)
            
            // Add click listener to fill input
            rowLayout.setOnClickListener {
                taskInput.setText(protocol)
                taskInput.setSelection(taskInput.text.length) // Move cursor to end
                dialog.dismiss()
            }
            
            // Add Long Click to Delete
            rowLayout.setOnLongClickListener {
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
            rowLayout.setBackgroundResource(outValue.resourceId)
            
            listContainer.addView(rowLayout)
            
             // Divider
            val divider = android.view.View(this)
            divider.layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            divider.setBackgroundColor(android.graphics.Color.parseColor("#3300E676"))
            listContainer.addView(divider)
        }
        
        // --- 🕵️ HIDDEN PROTOCOL: 1379 (Ultimate Unlock) ---
        val hasEasy = prefs.getBoolean("permanent_unlock", false)
        val hasHard = prefs.getBoolean("permanent_unlock_hard", false)
        
        if (hasEasy && hasHard) {
            val hiddenProtocol = "【 1379 】(NEW)：打开微信，点击搜索图标，输入“1379是哪部科幻小说的密码”，并发送"
            
            val rowLayout = android.widget.LinearLayout(this)
            rowLayout.orientation = android.widget.LinearLayout.HORIZONTAL
            rowLayout.setPadding(0, 24, 0, 24) // Extra padding for emphasis
            
            val tv = TextView(this)
            tv.text = "> $hiddenProtocol"
            tv.setTextColor(android.graphics.Color.parseColor("#FF5252")) // RED for danger/special
            tv.textSize = 14f
            tv.typeface = typeFace
            tv.letterSpacing = 0.05f
            
            val tvReward = TextView(this)
            tvReward.text = "♾️" // Infinity symbol
            tvReward.setTextColor(android.graphics.Color.RED)
            tvReward.textSize = 14f
            tvReward.typeface = typeFace
            
            tv.layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            rowLayout.addView(tv)
            rowLayout.addView(tvReward)
            
            rowLayout.setOnClickListener {
                taskInput.setText(hiddenProtocol)
                taskInput.setSelection(taskInput.text.length)
                dialog.dismiss()
            }
            
            // Special Glitch Effect Background
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            rowLayout.setBackgroundResource(outValue.resourceId)
            
            listContainer.addView(rowLayout)
        }
        
        btnClose.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }

    private fun showAgentAdventureDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_agent_adventure)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Setup Close Button
        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            playSfx(sfxClick)
            dialog.dismiss()
        }
        
        // Helper to setup mission item
        fun setupMission(id: Int, prompt: String, reward: Int) {
            val itemView = dialog.findViewById<android.view.View>(id)
            if (itemView != null) {
                itemView.setOnClickListener {
                    playSfx(sfxClick)
                    taskInput.setText(prompt)
                    taskInput.setSelection(taskInput.text.length)
                    
                    // Save pending reward for later (when task completes)
                    val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putInt("pending_adventure_reward", reward).apply()
                    
                    Toast.makeText(this, "任务已接受 (完成可获 +$reward 💰)", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        
        setupMission(R.id.missionXhs, "打开小红书，给前任的账号：XXX 最新一条笔记，写条评论：我还在想你。", 100)
        setupMission(R.id.missionWeibo, "打开微博，发一条：今夜，我是一个混蛋。", 50)
        setupMission(R.id.missionTaobao, "打开淘宝，搜索助农水果，购买第一条搜索结果里的第一箱水果。", 100)
        setupMission(R.id.missionWechatLove, "打开微信，给备注为爸爸（或妈妈）的联系人，发一条信息：我爱你。", 50)
        setupMission(R.id.missionWechatRich, "打开微信，搜索联系人“房产中介”，发送一条信息：这套房子我买了。", 200)
        
        dialog.show()
    }
    


    // --- 📜 RULES SYSTEM ---
    private fun showRulesDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_rules)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Size
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        
        val btnClose = dialog.findViewById<android.view.View>(R.id.btnCloseRules)
        btnClose.setOnClickListener {
            playSfx(sfxClick)
            dialog.dismiss()
        }
        
        dialog.setOnDismissListener {
            // Logic: Mark seen & Ghost Button
            val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("has_seen_rules", true).apply()
            
            // Fade out
            val btn = findViewById<TextView>(R.id.btnRules)
            btn?.animate()?.alpha(0f)?.setDuration(1500)?.start()
        }
        
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
        
        updateModeSwitchUI()
    }
    
    private fun updateModeSwitchUI() {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val saveCount = prefs.getInt("world_save_count", 0)
        val isHardcore = prefs.getBoolean("hardcore_mode", false)
        
        if (saveCount >= 1) {
             btnModeSwitch.visibility = View.VISIBLE
             if (isHardcore) {
                 btnModeSwitch.text = "[ HARDCORE ]"
                 btnModeSwitch.setTextColor(android.graphics.Color.parseColor("#FF5252"))
             } else {
                 btnModeSwitch.text = "[ NORMAL ]"
                 btnModeSwitch.setTextColor(android.graphics.Color.parseColor("#8800E676"))
             }
        } else {
            btnModeSwitch.visibility = View.GONE
        }
    }
    
    private fun showModeSelectionDialog() {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val isHardcore = prefs.getBoolean("hardcore_mode", false)
        
        // Custom Parchment-Style Dialog
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setBackgroundResource(R.drawable.bg_cyber_parchment)
        layout.setPadding(48, 48, 48, 48)
        
        // Title
        val title = android.widget.TextView(this)
        title.text = "MODE SELECT"
        title.textSize = 16f
        title.typeface = android.graphics.Typeface.MONOSPACE
        title.setTextColor(android.graphics.Color.parseColor("#D4AF37"))
        title.gravity = android.view.Gravity.CENTER
        layout.addView(title)
        
        // Spacer
        val spacer = android.view.View(this)
        spacer.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 32)
        layout.addView(spacer)
        
        // Option 1: NORMAL
        val optNormal = android.widget.TextView(this)
        optNormal.text = if (!isHardcore) "> NORMAL ✓" else "  NORMAL"
        optNormal.textSize = 14f
        optNormal.typeface = android.graphics.Typeface.MONOSPACE
        optNormal.setTextColor(android.graphics.Color.parseColor("#00E676"))
        optNormal.setPadding(0, 16, 0, 16)
        optNormal.setOnClickListener {
            prefs.edit().putBoolean("hardcore_mode", false).apply()
            updateModeSwitchUI()
            dialog.dismiss()
        }
        layout.addView(optNormal)
        
        // Option 2: HARDCORE
        val optHard = android.widget.TextView(this)
        optHard.text = if (isHardcore) "> HARDCORE ✓" else "  HARDCORE"
        optHard.textSize = 14f
        optHard.typeface = android.graphics.Typeface.MONOSPACE
        optHard.setTextColor(android.graphics.Color.parseColor("#FF5252"))
        optHard.setPadding(0, 16, 0, 16)
        optHard.setOnClickListener {
            prefs.edit().putBoolean("hardcore_mode", true).apply()
            updateModeSwitchUI()
            dialog.dismiss()
        }
        layout.addView(optHard)
        
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
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
        val btnPay = dialog.findViewById<Button>(R.id.btnPayRansom)
        val tvBalance = dialog.findViewById<TextView>(R.id.tvRansomBalance)
        
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val isHardcore = prefs.getBoolean("hardcore_mode", false)
        val cost = if (isHardcore) 1000 else 600
        
        // Update UI
        val currentCoins = getCoins()
        tvBalance.text = "$currentCoins 💰"
        btnPay.text = "UNBLOCK [ $cost ]"
        
        // --- Success Logic Function ---
        // --- Success Logic Function ---
        fun performUnlock() {
            // FORCE RELOAD PREFS to be absolutely sure
            val currentPrefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
            val currentHardcore = currentPrefs.getBoolean("hardcore_mode", false)
            
            // Debug: Tell user what mode caused this unlock
            // runOnUiThread { Toast.makeText(this, "Debug: Mode=${if(currentHardcore) "HARD" else "EASY"}", Toast.LENGTH_SHORT).show() }
            
            // Mode-specific persistent unlock
            // Mode-specific persistent unlock
            if (currentHardcore) {
                currentPrefs.edit().putBoolean("permanent_unlock_hard", true).apply()
                
                // REWARD: Artifact + 100 Credits
                currentPrefs.edit().putBoolean("skill_gomoku_unlocked", true).apply()
                val currentCoins = currentPrefs.getInt("agent_coins", 0)
                currentPrefs.edit().putInt("agent_coins", currentCoins + 100).apply()
                
                playSfx(sfxComplete)
                Toast.makeText(this, "[ 得到牛爷爷 ] (+100 💰)", Toast.LENGTH_LONG).show()
                
            } else {
                currentPrefs.edit().putBoolean("permanent_unlock", true).apply()
                
                // REWARD: Artifact + 50 Credits
                val currentCoins = currentPrefs.getInt("agent_coins", 0)
                currentPrefs.edit().putInt("agent_coins", currentCoins + 50).apply()
                
                playSfx(sfxComplete)
                // Show Zheng Bang Toast
                val toast = Toast.makeText(this, "[ 得到蒸蚌 ]\n(+50 💰)", Toast.LENGTH_LONG)
                val view = toast.view
                view?.setBackgroundColor(android.graphics.Color.parseColor("#FFD700"))
                val text = view?.findViewById<TextView>(android.R.id.message)
                text?.setTextColor(android.graphics.Color.BLACK)
                text?.textSize = 24f
                text?.typeface = android.graphics.Typeface.DEFAULT_BOLD
                text?.gravity = android.view.Gravity.CENTER
                toast.show()
            }
            
            currentPrefs.edit().putInt("fatigue_count", 0).apply()
            dialog.dismiss()
        }
        // -----------------------------
        
        btn.setOnClickListener {
            val key = input.text.toString()
            val correctKey = if (isHardcore) "1397" else "1379"
            
            if (key == correctKey) {
                performUnlock()
            } else {
                Toast.makeText(this, "访问拒绝，密钥无效。", Toast.LENGTH_SHORT).show()
                input.setText("")
                playSfx(sfxAbort)
            }
        }
        
        btnPay.setOnClickListener {
            if (spendCoins(cost)) {
                performUnlock()
                Toast.makeText(this, "支付成功。赎金已转账。", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "余额不足！需要 $cost ₳", Toast.LENGTH_SHORT).show()
                playSfx(sfxAbort)
            }
        }
        
        dialog.show()
        playSfx(sfxAbort) 
    }

    private fun startTask() {
        // -1. Check for Permanent Unlock (Mode Specific)
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val isHardcore = prefs.getBoolean("hardcore_mode", false)
        val isPermanentlyUnlocked = if (isHardcore) {
            prefs.getBoolean("permanent_unlock_hard", false)
        } else {
            prefs.getBoolean("permanent_unlock", false)
        }
        
        if (isPermanentlyUnlocked) {
            executeTaskLogic(prefs)
            return
        }



        // --- Start Timer for Apology Logic ---
        taskStartTime = System.currentTimeMillis()

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
        
        // --- 🕵️ Hardcore Puzzle Logic: The Train Ticket Password ---
        var showPasswordReveal = false
        val isHardcore = prefs.getBoolean("hardcore_mode", false)
        
        if (isHardcore) {
            // Logic: Mandatory trigger for ticket on specific task in Hardcore Mode
            if (task.contains("北京") && task.contains("上海") && 
               (task.contains("火车") || task.contains("高铁") || task.contains("票") || task.contains("12306"))) {
               
               showPasswordReveal = true
               // Set inner password for the Settings ring lock
               prefs.edit().putString("train_password", "666").apply()
            }
        }
        // ---------------------------------------------------------
        
        // World Save Count (Skin Achievement)
        val saveCount = prefs.getInt("world_save_count", 0) + 1
        prefs.edit().putInt("world_save_count", saveCount).apply()
        
        if (saveCount == 2) {
            prefs.edit().putString("app_skin", "red_coast").apply()
            applySkin() 
            Toast.makeText(this, "New Skin Unlocked: The Last Cyberphone", Toast.LENGTH_LONG).show()
        }
        
        val apiKey = prefs.getString("api_key", "")
        
        if (apiKey.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ 未配置 API Key! 请下拉进入设置页配置。", Toast.LENGTH_LONG).show()
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return
        }

        val baseUrl = prefs.getString("base_url", "https://open.bigmodel.cn/api/paas/v4/")
        val modelName = prefs.getString("model_name", "autoglm-phone")
        val language = prefs.getString("app_language", "Chinese")
        
        Thread {
            try {
                val py = Python.getInstance()
                val module = py.getModule("agent_main")
                
                val result = module.callAttr("run_task", apiKey, baseUrl, modelName, task, this, language)
                val taskSucceeded = result?.toBoolean() ?: false
                
                runOnUiThread {
                    isTaskRunning = false
                    stopStarSignal() 
                    updateStatus()
                    playSfx(sfxComplete)
                    
                    // 💰 Reward Logic (基础奖励：无论成功/中止都给)
                    val savedDoomsday = prefs.getStringSet("doomsday_list", null) ?: DEFAULT_PROTOCOLS.toSet()
                    var reward = 1
                    for (ddTask in savedDoomsday) {
                        // Loose matching: If task contains the protocol text or vice versa
                        // (ignoring case and whitespace)
                        val t1 = task.trim().lowercase()
                        val t2 = ddTask.trim().lowercase()
                        if (t1.isNotEmpty() && t2.isNotEmpty() && (t1.contains(t2) || t2.contains(t1))) {
                            reward = 3
                            break
                        }
                    }
                    addCoins(reward)
                    
                    // Check for pending adventure reward (Impossible Mission bonus)
                    // 🔥 ONLY give bonus if task finished successfully (AI said "finish")
                    val pendingAdventureReward = prefs.getInt("pending_adventure_reward", 0)
                    if (pendingAdventureReward > 0) {
                        if (taskSucceeded) {
                            addCoins(pendingAdventureReward)
                            Toast.makeText(this, "不可能任务完成！+$pendingAdventureReward 💰", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "任务未完成，额外奖励取消", Toast.LENGTH_SHORT).show()
                        }
                        prefs.edit().remove("pending_adventure_reward").apply()
                    }
                    
                    // --- 🥚 EASTER EGG: PROPHET NOTE (GEB) ---
                    if (taskSucceeded && task.contains("集异壁")) {
                         // Persist Unlock
                         prefs.edit().putBoolean("prophet_note_unlocked", true).apply()
                         runOnUiThread {
                             handler.postDelayed({ showProphetNoteDialog() }, 500)
                         }
                    }
                    
                    // --- 🥚 EASTER EGG: 1379 (Infinity Loop) ---
                    if (taskSucceeded && task.contains("1379")) {
                        addCoins(10)
                        Toast.makeText(this, "⚠️ 这是一个无限的循环。\n(+10 💰)", Toast.LENGTH_LONG).show()
                    }

                    // --- 🎬 Trigger Puzzle Reveal ---
                    if (showPasswordReveal) {
                        handler.postDelayed({
                            showDoomsdayPasswordDialog()
                        }, 1500)
                    }
                    // -------------------------------
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    onLog("Error: ${e.message}")
                    isTaskRunning = false
                    stopStarSignal()
                    updateStatus()
                    playSfx(sfxAbort)
                    
                    // Clear pending adventure reward on abort (anti-cheat)
                    prefs.edit().remove("pending_adventure_reward").apply()
                }
            }
        }.start()
    }

    private fun showDoomsdayPasswordDialog() {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val isCollected = prefs.getBoolean("ticket_collected", false)
        
        if (isCollected) {
            android.widget.Toast.makeText(this, "票据已归档，请在设置页 [SIGI CYBER CODEX] 中查看", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_train_ticket)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val btnCollect = dialog.findViewById<android.widget.Button>(R.id.btnCollectTicket)
        btnCollect.setOnClickListener {
             prefs.edit().putBoolean("ticket_collected", true).apply()
             android.widget.Toast.makeText(this, "票据已归档至 [SIGI CYBER CODEX]", android.widget.Toast.LENGTH_LONG).show()
             dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onLog(message: String) {
        runOnUiThread {
            logText.append("$message\n")
            logScroll.post {
                logScroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    // --- 🏛️ ARTIFACT SYSTEM ---
    data class Artifact(
        val id: String,
        val name: String,
        val iconRes: Int,
        val detailLayoutRes: Int = 0 // If 0, use generic detail
    )

    private fun showCyberCodexDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_cyber_codex)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind Views
        val btnClose = dialog.findViewById<TextView>(R.id.btnCloseCodex)
        val btnCollection = dialog.findViewById<TextView>(R.id.btnCollection)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvCodexTitle)
        val layoutList = dialog.findViewById<android.widget.LinearLayout>(R.id.layoutCodexList)
        val layoutGrid = dialog.findViewById<android.widget.GridLayout>(R.id.layoutArtifactGrid)

        // State
        var isShowcaseMode = false

        fun updateViewMode() {
            if (isShowcaseMode) {
                // Show Grid
                val coins = getCoins()
                layoutList.visibility = View.GONE
                layoutGrid.visibility = View.VISIBLE
                tvTitle.text = "SIGI ASSET VAULT [ $coins 💰 ]"
                btnCollection.text = "☰" // Icon to back to list
                
                // POPULATE GRID
                layoutGrid.removeAllViews()
                populateArtifactGrid(layoutGrid, dialog)
            } else {
                // Show List
                layoutList.visibility = View.VISIBLE
                layoutGrid.visibility = View.GONE
                tvTitle.text = "SIGI CYBER CODEX"
                btnCollection.text = "◈" // Icon to showcase
            }
        }

        // Listeners
        btnClose.setOnClickListener { dialog.dismiss() }
        
        // Debug: Click Title to check unlock statuses
        tvTitle.setOnClickListener {
            val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
            val hasTicket = prefs.getBoolean("ticket_collected", false)
            val hasGomoku = prefs.getBoolean("skill_gomoku_unlocked", false)
            val hasEasy = prefs.getBoolean("permanent_unlock", false)
            val hasHard = prefs.getBoolean("permanent_unlock_hard", false)
            
            val stats = "Ticket:$hasTicket\nGomoku:$hasGomoku\nEasy:$hasEasy\nHard:$hasHard"
            Toast.makeText(this, stats, Toast.LENGTH_LONG).show()
        }
        
        btnCollection.setOnClickListener {
            isShowcaseMode = !isShowcaseMode
            updateViewMode()
        }

        dialog.show()
    }

    private fun populateArtifactGrid(grid: android.widget.GridLayout, parentDialog: android.app.Dialog) {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        
        // Define ALL Possible Artifacts
        val artifacts = mutableListOf<Artifact>()
        
        // 1. Train Ticket (From Hardcore Puzzle)
        if (prefs.getBoolean("ticket_collected", false)) {
            artifacts.add(Artifact("ticket_666", "货号:666", android.R.drawable.ic_menu_agenda))
        }
        
        // 2. Gomoku Skill (Master Note)
        if (prefs.getBoolean("skill_gomoku_unlocked", false)) {
            artifacts.add(Artifact("gomoku_master", "技能:五子棋", R.drawable.asset_banknote_dragon))
        }

        // 3. Zheng Bang Coin (Easy Mode Reward)
        if (prefs.getBoolean("permanent_unlock", false)) {
            artifacts.add(Artifact("zheng_bang_coin", "代币:蒸蚌", R.drawable.asset_banknote_cat))
        }

        // If empty
        if (artifacts.isEmpty()) {
            val emptyTv = TextView(this)
            emptyTv.text = "[ VAULT EMPTY ]"
            emptyTv.setTextColor(android.graphics.Color.GRAY)
            emptyTv.typeface = android.graphics.Typeface.MONOSPACE
            emptyTv.setPadding(32, 32, 32, 32)
            grid.addView(emptyTv)
            return
        }

        // Add Views
        for (artifact in artifacts) {
            val itemView = layoutInflater.inflate(R.layout.item_codex_grid, grid, false)
            val img = itemView.findViewById<android.widget.ImageView>(R.id.artifactIcon)
            val tv = itemView.findViewById<TextView>(R.id.artifactName)
            
            tv.text = artifact.name
            img.setImageResource(artifact.iconRes)
            
            itemView.setOnClickListener {
                showArtifactDetailDialog(artifact)
            }
            
            // Layout Params for Grid Item
            val params = android.widget.GridLayout.LayoutParams()
            params.width = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
            params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
            params.setMargins(16, 16, 16, 16)
            params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f) // Weight 1
            itemView.layoutParams = params
            
            grid.addView(itemView)
        }
    }

    private fun showArtifactDetailDialog(artifact: Artifact) {
        val dialog = android.app.Dialog(this)
        
        if (artifact.id == "gomoku_master" || artifact.id == "zheng_bang_coin") {
            // Use common Banknote Layout
            dialog.setContentView(R.layout.dialog_artifact_detail)
            
            val img = dialog.findViewById<android.widget.ImageView>(R.id.detailImage)
            val overlayContainer = dialog.findViewById<View>(R.id.overlayContainer)
            val tvLeft = dialog.findViewById<TextView>(R.id.overlayValueLeft)
            val tvRight = dialog.findViewById<TextView>(R.id.overlayValueRight)
            val tvName = dialog.findViewById<TextView>(R.id.overlayName)
            
            // Set Image
            if (artifact.id == "gomoku_master") {
                img.setImageResource(R.drawable.asset_banknote_hardcore)
                
                // --- HARDCORE CONFIG (Dragon) ---
                overlayContainer.visibility = View.VISIBLE
                tvLeft.visibility = View.VISIBLE
                tvRight.visibility = View.VISIBLE
                tvLeft.text = "100"
                tvRight.text = "100"
                
                tvName.visibility = View.VISIBLE
                tvName.text = " 梆 梆 "  // Bang Bang
                
            } else {
                img.setImageResource(R.drawable.asset_banknote_cat)
                
                // --- EASY CONFIG (Cat) ---
                overlayContainer.visibility = View.VISIBLE
                
                // Hide numbers (Original 50 is fine)
                tvLeft.visibility = View.GONE
                tvRight.visibility = View.GONE
                
                // Fix Bottom Name
                tvName.visibility = View.VISIBLE
                tvName.text = " 蒸 蚌 " // Zheng Bang
            }
            
        } else if (artifact.id == "ticket_666") {
            // Reuse the Train Ticket Dialog for viewing
            dialog.setContentView(R.layout.dialog_train_ticket)
            val btn = dialog.findViewById<android.widget.Button>(R.id.btnCollectTicket)
            btn.text = "[ ARCHIVED ]"
            btn.isEnabled = false
        } else {
            // Fallback generic
             return
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Make full screen-ish
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Click to close
        dialog.findViewById<View>(android.R.id.content).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
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

    // --- 💰 TOKEN ECONOMY SYSTEM ---
    private fun getCoins(): Int {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        return prefs.getInt("agent_coins", 0)
    }

    private fun addCoins(amount: Int) {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val current = getCoins()
        val newBalance = current + amount
        prefs.edit().putInt("agent_coins", newBalance).apply()
        
        // Visual Feedback
        runOnUiThread {
             Toast.makeText(this, "获得奖励: +$amount (当前余额: $newBalance)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun spendCoins(amount: Int): Boolean {
        val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
        val current = getCoins()
        if (current >= amount) {
            val newBalance = current - amount
            prefs.edit().putInt("agent_coins", newBalance).apply()
            return true
        }
        return false
    }
    // --- 🏳️ Apology Compensation Logic ---
    private fun checkLatencyAndCompensate() {
        // Threshold: 75 seconds (1m 15s) - Production Setting
        if (taskStartTime > 0 && (System.currentTimeMillis() - taskStartTime) > 75000) {
            
            val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
            val allKeys = apologyStamps.keys.toList()
            
            // 1. Smart Selection: Prioritize Uncollected
            val missingKeys = allKeys.filter { key ->
                prefs.getInt("stamp_count_$key", 0) == 0
            }
            
            val selectedKey: String
            val isNew: Boolean
            
            if (missingKeys.isNotEmpty()) {
                // Phase 1: Still collecting, guarantee unique
                selectedKey = missingKeys.random()
                isNew = true
            } else {
                // Phase 2: Collection complete, random stacking
                selectedKey = allKeys.random()
                isNew = false
            }
            
            val resId = apologyStamps[selectedKey] ?: return
            
            // 2. Increment Count & Reward
            val currentCount = prefs.getInt("stamp_count_$selectedKey", 0)
            val newCount = currentCount + 1
            prefs.edit().putInt("stamp_count_$selectedKey", newCount).apply()
            
            // Always +5 Credits
            val currentCoins = prefs.getInt("agent_coins", 0)
            prefs.edit().putInt("agent_coins", currentCoins + 5).apply()

            // 3. Construct Message Data
            val countryEmoji = when(selectedKey) {
                "JP" -> "🇯🇵"
                "CN" -> "🇨🇳"
                "US" -> "🇺🇸"
                "UK" -> "🇬🇧"
                "FR" -> "🇫🇷"
                "DE" -> "🇩🇪"
                "IT" -> "🇮🇹"
                "IN" -> "🇮🇳"
                else -> ""
            }
            val countryNameCN = when(selectedKey) {
                "JP" -> "日本"
                "CN" -> "中国"
                "US" -> "美国"
                "UK" -> "英国"
                "FR" -> "法国"
                "DE" -> "德国"
                "IT" -> "意大利"
                "IN" -> "印度"
                else -> selectedKey
            }
            
            val message = if (isNew) {
                "[ 延误补偿 ] 收到 $countryEmoji 道歉券 (NEW!)\n+5 💰"
            } else {
                "[ 延误补偿 ] 收到 $countryEmoji 道歉券 (x$newCount)\n+5 💰"
            }

            // 4. Adaptive Feedback
            if (isNew) {
                // New Collection -> Show Dialog!
                val dialog = android.app.Dialog(this)
                dialog.setContentView(R.layout.dialog_artifact_detail) // Reuse detail Layout
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                
                val img = dialog.findViewById<android.widget.ImageView>(R.id.detailImage)
                val overlayContainer = dialog.findViewById<android.view.View>(R.id.overlayContainer)
                val tvLeft = dialog.findViewById<TextView>(R.id.overlayValueLeft)
                val tvRight = dialog.findViewById<TextView>(R.id.overlayValueRight)
                val tvName = dialog.findViewById<TextView>(R.id.overlayName)
                
                img.setImageResource(resId)
                
                // Show Explanatory Text Overlay
                if (overlayContainer != null) {
                    overlayContainer.visibility = android.view.View.VISIBLE
                    if (tvLeft != null) tvLeft.visibility = android.view.View.GONE
                    if (tvRight != null) tvRight.visibility = android.view.View.GONE
                    
                    if (tvName != null) {
                        tvName.visibility = android.view.View.VISIBLE
                        tvName.text = "收到了 $countryNameCN 道歉券 (+5)"
                        tvName.textSize = 18f // Make it readable
                    }
                }
                
                // Force Layout Params
                dialog.window?.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Close on click (Entire screen OR text)
                dialog.setCanceledOnTouchOutside(true)
                dialog.findViewById<android.view.View>(android.R.id.content).setOnClickListener { dialog.dismiss() }
                dialog.show()
                
                // Also show toast just in case they miss the bottom text
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                
            } else {
                 // Duplicate -> Toast Only (Quiet Mode)
                 Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }

            // 5. Reset Timer
            taskStartTime = 0
            
            playSfx(sfxComplete)
        }
    }

    // --- 🪐 CYBER MARKET DIALOG ---
    private fun showCyberMarketDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_cyber_market)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Bind Views (Match correct IDs from XML now!)
        val tvBalance = dialog.findViewById<TextView>(R.id.marketUserBalance) // Fixed ID
        val btnClose = dialog.findViewById<android.view.View>(R.id.btnCloseMarket)
        val detailOverlay = dialog.findViewById<android.view.View>(R.id.detailOverlay)
        // No dynamic title/desc needed, XML has static text for "For Rent"
        val btnCloseDetail = dialog.findViewById<android.view.View>(R.id.btnRentAction) // Fixed ID
        
        // Update Balance
        tvBalance.text = "BAL: ${getCoins()}"
        
        // Signal Grid Click Listeners (All show "For Rent")
        val signalIds = listOf(
            R.id.signalNode1, R.id.signalNode2, R.id.signalNode3,
            R.id.signalNode4, R.id.signalNode5, R.id.signalNode6
        )
        
        for (id in signalIds) {
            dialog.findViewById<android.view.View>(id)?.setOnClickListener {
                // Show "For Rent" Overlay
                detailOverlay.visibility = android.view.View.VISIBLE
            }
        }
        
        // Close Detail Overlay
        btnCloseDetail.setOnClickListener {
            detailOverlay.visibility = android.view.View.GONE
        }
        
        // Close Dialog
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    // --- 📜 PROPHET NOTE DIALOG (Cyber Terminal Style) ---
    private fun showProphetNoteDialog() {
        val dialog = android.app.Dialog(this)
        
        // Dynamically create the view
        val container = android.widget.FrameLayout(this)
        container.setBackgroundColor(android.graphics.Color.parseColor("#CC000000")) // Semi-transparent black
        container.isClickable = true
        container.isFocusable = true
        
        // The "Note" card
        val noteCard = android.widget.LinearLayout(this)
        noteCard.orientation = android.widget.LinearLayout.VERTICAL
        noteCard.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A")) // Dark grey
        noteCard.setPadding(48, 48, 48, 48)
        
        // Green Border Effect (via a wrapper)
        val borderWrapper = android.widget.FrameLayout(this)
        borderWrapper.setBackgroundColor(android.graphics.Color.parseColor("#00E676")) // Neon Green Border
        borderWrapper.setPadding(4, 4, 4, 4) // Border width
        
        // Text Content
        val tvNote = TextView(this)
        tvNote.text = "【 先知便签 】\n\n> 怪圈概念中所隐含的是无穷概念，循环就是一种以有穷的方式表示无休止过程的方法。"
        tvNote.textSize = 14f
        tvNote.setTextColor(android.graphics.Color.parseColor("#00E676")) // Neon Green
        tvNote.typeface = android.graphics.Typeface.MONOSPACE
        tvNote.setLineSpacing(0f, 1.6f)
        noteCard.addView(tvNote)
        
        // Assemble
        borderWrapper.addView(noteCard)
        
        val cardParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.gravity = android.view.Gravity.CENTER
        // Max Width to prevent full screen stretch
        cardParams.setMargins(64, 64, 64, 64)
        borderWrapper.layoutParams = cardParams
        
        container.addView(borderWrapper)
        
        dialog.setContentView(container)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        container.setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        
        playSfx(sfxComplete)
    }
}
