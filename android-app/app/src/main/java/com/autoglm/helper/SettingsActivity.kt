package com.autoglm.helper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast

class SettingsActivity : Activity() {

    private lateinit var providerSpinner: android.widget.Spinner
    private lateinit var editBaseUrl: EditText
    private lateinit var editApiKey: EditText
    private lateinit var editModelName: EditText
    private lateinit var skinSpinner: android.widget.Spinner
    private lateinit var sfxSpinner: android.widget.Spinner
    private lateinit var worldSaveCounter: android.widget.TextView
    private lateinit var settingsStar: android.widget.ImageView
    
    // Background Music Player
    private var bgmPlayer: android.media.MediaPlayer? = null

    // Default Configurations
    private val ZHIPU_URL = "https://open.bigmodel.cn/api/paas/v4"
    private val ZHIPU_KEY = ""
    private val ZHIPU_MODEL = "autoglm-phone"

    private val MS_URL = "https://api-inference.modelscope.cn/v1"
    private val MS_KEY = ""
    private val MS_MODEL = "ZhipuAI/AutoGLM-Phone-9B"
    
    // private var isInitialLoad = true // Removed strict binding check requirement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        providerSpinner = findViewById(R.id.providerSpinner)
        editBaseUrl = findViewById(R.id.editBaseUrl)
        editApiKey = findViewById(R.id.editApiKey)
        editModelName = findViewById(R.id.editModelName)
        skinSpinner = findViewById(R.id.skinSpinner)
        sfxSpinner = findViewById(R.id.sfxSpinner)
        worldSaveCounter = findViewById(R.id.worldSaveCounter)
        worldSaveCounter = findViewById(R.id.worldSaveCounter)
        settingsStar = findViewById(R.id.settingsStar)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        
        setupAutoSave()
        loadSettings()
        setupSkinSpinner()
        setupSfxSpinner()
        startStarSignal()

        settingsStar.setOnClickListener {
             // 📜 Open Cyber Codex (The Artifact)
             showCyberCodexDialog()
        }

        // Radio Button Toggle Logic
        // Initialize Spinner Adapter
        // Initialize Spinner Adapter (Simplified: Only ZhipuAI)
        val providers = arrayOf("ZhipuAI (Official)")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        providerSpinner.adapter = adapter
        
         // Handle User Interaction (Spinner Selection)
         providerSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
              override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                  // Only one provider, so always Zhipu logic
                  editBaseUrl.setText(ZHIPU_URL)
                  editModelName.setText(ZHIPU_MODEL)
                  // Smart Preserve: Don't wipe user's key
                  if (ZHIPU_KEY.isNotEmpty() || editApiKey.text.toString().isEmpty()) {
                      editApiKey.setText(ZHIPU_KEY)
                  }
              }
 
              override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
         }
         
         // --- Elastic Drag Logic for Hidden World (Settings) ---
        val uiContainer = findViewById<android.widget.FrameLayout>(R.id.uiContainerSettings)
        
        // New Dual Containers
        val hiddenWorldContainerTop = findViewById<android.view.View>(R.id.hiddenWorldContainerTop)
        val hiddenWorldContainerBottom = findViewById<android.view.View>(R.id.hiddenWorldContainerBottom)
        
        // Triggers
        val dragTriggerTop = findViewById<android.view.View>(R.id.dragTriggerSettings)
        val dragTriggerBottom = findViewById<android.view.View>(R.id.dragTriggerBottom)
        val revealArrowTop = findViewById<android.view.View>(R.id.revealArrowSettings)
        val revealArrowBottom = findViewById<android.view.View>(R.id.revealArrowBottom)
        
        // Hardcore Overlays
        val vBlurMask = findViewById<android.view.View>(R.id.vBlurMask)
        val hardcore97Overlay = findViewById<android.view.View>(R.id.hardcore97Overlay)
        val hiddenWorldBgTop = findViewById<android.widget.ImageView>(R.id.hiddenWorldBgTop)
        val hiddenWorldBgBottom = findViewById<android.widget.ImageView>(R.id.hiddenWorldBgBottom)
        
        // Mode-Based Image & Overlay Logic
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        val isHardcore = prefs.getBoolean("hardcore_mode", false)
        if (isHardcore) {
            // Hardcore: Use different image (97 at top, empty water drop)
            hiddenWorldBgTop.setImageResource(R.drawable.bg_trisolaris_city_hardcore)
            hiddenWorldBgBottom.setImageResource(R.drawable.bg_trisolaris_city_hardcore)
            vBlurMask.visibility = android.view.View.GONE  // No mask needed, image has no 79
            hardcore97Overlay.visibility = android.view.View.GONE  // No overlay needed, image has 97
        } else {
            // Normal: Use original image (79 reflection in water drop)
            hiddenWorldBgTop.setImageResource(R.drawable.bg_trisolaris_city)
            hiddenWorldBgBottom.setImageResource(R.drawable.bg_trisolaris_city)
            vBlurMask.visibility = android.view.View.GONE
            hardcore97Overlay.visibility = android.view.View.GONE
        }
        
        // Layout Listener
        uiContainer.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val height = bottom - top
            if (height > 0 && uiContainer.translationY == 0f) {
                hiddenWorldContainerTop.translationY = -height.toFloat()
                hiddenWorldContainerBottom.translationY = height.toFloat()
            }
        }
        
        // Initial Force Set
        uiContainer.post {
            resetRevealState(uiContainer, hiddenWorldContainerTop, hiddenWorldContainerBottom)
        }
        
        // --- TOP DRAG (Always Unlocked - Shows 79 Clue) ---
        dragTriggerTop.setOnTouchListener(object : android.view.View.OnTouchListener {
             var startY = 0f
             var isLockedIn = false
             
             override fun onTouch(v: android.view.View, event: android.view.MotionEvent): Boolean {
                 val screenWidth = resources.displayMetrics.widthPixels
                 val centerX = screenWidth / 2f
                 val touchX = event.rawX
                 val threshold = screenWidth * 0.15f
                 val isInZone = kotlin.math.abs(touchX - centerX) < threshold
                 
                 when (event.action) {
                     android.view.MotionEvent.ACTION_DOWN -> {
                         if (isInZone) {
                             isLockedIn = true
                             startY = event.rawY
                             revealArrowTop.animate().alpha(0.6f).setDuration(300).start()
                             v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                         }
                         return true
                     }
                     android.view.MotionEvent.ACTION_MOVE -> {
                         if (!isLockedIn) {
                             if (isInZone) { isLockedIn = true; startY = event.rawY }
                             return true
                         }
                         val deltaY = event.rawY - startY
                         val maxDrag = resources.displayMetrics.heightPixels * 0.8f
                         
                         if (deltaY > 0) {
                             val targetY = deltaY * 0.6f
                             if (targetY < maxDrag) {
                                uiContainer.translationY = targetY
                                hiddenWorldContainerTop.translationY = -uiContainer.height.toFloat() + targetY
                             }
                         }
                         return true
                     }
                     android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                         isLockedIn = false
                         revealArrowTop.animate().alpha(0f).start()
                         resetRevealState(uiContainer, hiddenWorldContainerTop, hiddenWorldContainerBottom, true)
                         return true
                     }
                 }
                 return false
             }
        })
        
        // --- BOTTOM DRAG (The Puzzle - Needs Password) ---
        dragTriggerBottom.setOnTouchListener(object : android.view.View.OnTouchListener {
             var startY = 0f
             var isLockedIn = false
             var tapCount = 0
             var lastTapTime = 0L
             
             override fun onTouch(v: android.view.View, event: android.view.MotionEvent): Boolean {
                 val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
                 val isUnlocked = prefs.getBoolean("settings_ring_unlocked", false)
                 
                 // Locked State: 3 Taps to Summon Lock
                 if (!isUnlocked) {
                     if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                         val screenWidth = resources.displayMetrics.widthPixels
                         if (kotlin.math.abs(event.rawX - screenWidth/2f) < screenWidth * 0.2f) {
                             // 1. Show Visual Feedback (Arrow appears)
                             revealArrowBottom.animate().alpha(1f).setDuration(200).start()
                             revealArrowBottom.animate().scaleX(1.2f).scaleY(1.2f).withEndAction {
                                 revealArrowBottom.animate().scaleX(1f).scaleY(1f).start()
                             }.start()
                             
                             // 2. Haptic/Audio Feedback
                             v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                             
                             // 3. Tap Logic
                             val curr = System.currentTimeMillis()
                             if (curr - lastTapTime < 600) tapCount++ else tapCount = 1
                             lastTapTime = curr
                             
                             if (tapCount >= 3) {
                                 showRingLockDialog()
                                 tapCount = 0
                             }
                         }
                         return true
                     } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                         // Hide on release
                         revealArrowBottom.animate().alpha(0f).setDuration(300).start()
                         return true
                     }
                     return false // Consume nothing else
                 }

                 
                 // Unlocked: Allow Drag UP
                 when (event.action) {
                     android.view.MotionEvent.ACTION_DOWN -> {
                         isLockedIn = true
                         startY = event.rawY
                         revealArrowBottom.animate().alpha(1f).start()
                         return true
                     }
                     android.view.MotionEvent.ACTION_MOVE -> {
                         if (!isLockedIn) return true
                         val deltaY = event.rawY - startY
                         val maxDrag = resources.displayMetrics.heightPixels * 0.8f
                         
                         if (deltaY < 0) {
                             val targetY = deltaY * 0.6f
                             if (kotlin.math.abs(targetY) < maxDrag) {
                                uiContainer.translationY = targetY
                                hiddenWorldContainerBottom.translationY = uiContainer.height.toFloat() + targetY
                             }
                         }
                         return true
                     }
                     android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                         isLockedIn = false
                         revealArrowBottom.animate().alpha(0f).start()
                         resetRevealState(uiContainer, hiddenWorldContainerTop, hiddenWorldContainerBottom, true)
                         return true
                     }
                 }
                 return false
             }
        })
    } // End of onCreate
    
    private fun showRingLockDialog() {
         // Activate the "Sequence"
         val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
         prefs.edit().putBoolean("ring_lock_activated", true).apply() // Mark as seen
         
         val dialog = android.app.Dialog(this)
         dialog.setContentView(R.layout.dialog_proton_lock)
         dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
         
         val dial1 = dialog.findViewById<android.widget.TextView>(R.id.dial1)
         val dial2 = dialog.findViewById<android.widget.TextView>(R.id.dial2)
         val dial3 = dialog.findViewById<android.widget.TextView>(R.id.dial3)
         val btn = dialog.findViewById<android.view.View>(R.id.btnProtonUnlock)
         
         // Dial Interaction Logic
         val dials = listOf(dial1, dial2, dial3)
         dials.forEach { dial ->
             dial.setOnClickListener {
                 try {
                     val current = dial.text.toString().toInt()
                     val next = if (current == 9) 0 else current + 1
                     dial.text = next.toString()
                     dial.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                 } catch (e: Exception) {}
             }
         }
         
         btn.setOnClickListener {
             val code = "${dial1.text}${dial2.text}${dial3.text}"
             val storedPass = prefs.getString("train_password", "") ?: ""
             
             if (code == "666" || (storedPass.isNotEmpty() && code == storedPass)) {
                 // UNLOCK SUCCESS
                 prefs.edit().putBoolean("settings_ring_unlocked", true).apply()
                 android.widget.Toast.makeText(this, "质子封锁已解除 [PROTON LOCK DISENGAGED]", android.widget.Toast.LENGTH_SHORT).show()
                 
                 // Provide feedback
                 val revealArrowBottom = findViewById<android.view.View>(R.id.revealArrowBottom)
                 revealArrowBottom.animate().alpha(1f).setDuration(500).start()
                 
                 dialog.dismiss()
             } else {
                 android.widget.Toast.makeText(this, "密钥无效 [ACCESS DENIED]", android.widget.Toast.LENGTH_SHORT).show()
                 // Shake animation for feedback
                 val shake = android.view.animation.TranslateAnimation(0f, 20f, 0f, 0f)
                 shake.duration = 50
                 shake.repeatCount = 3
                 shake.repeatMode = android.view.animation.Animation.REVERSE
                 btn.startAnimation(shake)
             }
         }
         dialog.show()
    }
    
    // Self-healing reset function
    private fun resetRevealState(ui: android.view.View, topBg: android.view.View, bottomBg: android.view.View, animate: Boolean = false) {
        if (animate) {
            ui.animate().translationY(0f).setDuration(400)
                .setInterpolator(android.view.animation.OvershootInterpolator(0.8f)).start()
            topBg.animate().translationY(-ui.height.toFloat()).setDuration(400).start()
            bottomBg.animate().translationY(ui.height.toFloat()).setDuration(400).start()
        } else {
            ui.translationY = 0f
            topBg.translationY = -ui.height.toFloat()
            bottomBg.translationY = ui.height.toFloat()
        }
    }
    
    private fun setupAutoSave() {
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                saveSettings()
            }
        }
        
        editBaseUrl.addTextChangedListener(watcher)
        editApiKey.addTextChangedListener(watcher)
        editModelName.addTextChangedListener(watcher)
    }
    
    private fun setupSkinSpinner() {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        val saveCount = prefs.getInt("world_save_count", 0)
        val currentSkin = prefs.getString("app_skin", "black") ?: "black"
        
        // Update Counter Text
        
        // Update Counter (Just the number)
        worldSaveCounter.text = String.format("%03d", saveCount)
        
        // Skin Options
        val skins = mutableListOf("Dark Void (Default)")
        val isSkinUnlocked = saveCount >= 2
        
        if (isSkinUnlocked) {
            skins.add("The Last Cyberphone")
        } else {
            skins.add("The Last Cyberphone [LOCKED]")
        }
        
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, skins)
        skinSpinner.adapter = adapter
        
        // Set Selection
        if (currentSkin == "red_coast" && isSkinUnlocked) {
            skinSpinner.setSelection(1)
        } else {
            skinSpinner.setSelection(0)
        }
        
        skinSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                 if (position == 1) { // The Last Cyberphone
                     if (!isSkinUnlocked) {
                         Toast.makeText(this@SettingsActivity, "Save the world 2 times to unlock!", Toast.LENGTH_SHORT).show()
                         skinSpinner.setSelection(0) // Revert
                     } else {
                         saveSkin("red_coast")
                     }
                 } else {
                     saveSkin("black")
                 }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }
    
    private fun saveSkin(skin: String) {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        prefs.edit().putString("app_skin", skin).apply()
    }
    
    private fun setupSfxSpinner() {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        val isSfxEnabled = prefs.getBoolean("app_sfx_enabled", true) // Default ON
        
        val options = arrayOf("Cosmic Ripples / ON", "Cosmic Ripples / OFF")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        sfxSpinner.adapter = adapter
        
        // Set Selection
        sfxSpinner.setSelection(if (isSfxEnabled) 0 else 1)
        
        sfxSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                 val enable = (position == 0)
                 prefs.edit().putBoolean("app_sfx_enabled", enable).apply()
                 
                 // Immediate effect on BGM
                 if (enable) {
                     if (bgmPlayer != null && !bgmPlayer!!.isPlaying) {
                         bgmPlayer?.start()
                     } else if (bgmPlayer == null) {
                         // Initialize if not ready
                         try {
                             bgmPlayer = android.media.MediaPlayer.create(this@SettingsActivity, R.raw.bgm_cosmic_ripples)
                             bgmPlayer?.isLooping = true
                             bgmPlayer?.setVolume(0.5f, 0.5f)
                             bgmPlayer?.start()
                         } catch(e: Exception) {}
                     }
                 } else {
                     if (bgmPlayer != null && bgmPlayer!!.isPlaying) {
                         bgmPlayer?.pause()
                     }
                 }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("base_url", ZHIPU_URL)
        val savedKey = prefs.getString("api_key", ZHIPU_KEY)
        val savedModel = prefs.getString("model_name", ZHIPU_MODEL)
        
        // Temporarily disable watcher to prevent overwriting during load? 
        // Actually, setText triggers watcher -> saveSettings.
        // It's fine because it saves what we just loaded, which is a no-op functionally.
        
        editBaseUrl.setText(savedUrl)
        editApiKey.setText(savedKey)
        editModelName.setText(savedModel)
        
        // Simple heuristic to set radio button
        
        // Determine which provider matches the saved settings purely by URL/Key check
        // Or default to ZhipuAI
        if (savedUrl?.contains("modelscope") == true || savedKey?.startsWith("ms-") == true) {
            // Check if actual selection is needed to avoid redundant firing if possible, 
            // but for simplicity we set it. The listener will fire and reset values to "Perfect Constants".
            // If user had slightly custom values, they will be reset. This aligns with "Strict Binding".
            providerSpinner.setSelection(1) // ModelScope
        } else {
            providerSpinner.setSelection(0) // ZhipuAI
        }
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("base_url", editBaseUrl.text.toString().trim())
            putString("api_key", editApiKey.text.toString().trim())
            putString("model_name", editModelName.text.toString().trim())
            apply()
        }
        // Toast removed for auto-save to allow silent saving
    }
    // --- 🏛️ ARTIFACT SYSTEM (Mirrored from MainActivity) ---
    data class Artifact(
        val id: String,
        val name: String,
        val iconRes: Int
    )
    
    private fun getCoins(): Int {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        return prefs.getInt("agent_coins", 0)
    }

    private fun showCyberCodexDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_cyber_codex)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind Views
        val btnClose = dialog.findViewById<android.widget.TextView>(R.id.btnCloseCodex)
        val btnCollection = dialog.findViewById<android.widget.TextView>(R.id.btnCollection)
        val tvTitle = dialog.findViewById<android.widget.TextView>(R.id.tvCodexTitle)
        val layoutList = dialog.findViewById<android.widget.LinearLayout>(R.id.layoutCodexList)
        val layoutGrid = dialog.findViewById<android.widget.GridLayout>(R.id.layoutArtifactGrid)

        // State
        var isShowcaseMode = false

        fun updateViewMode() {
            if (isShowcaseMode) {
                // Show Grid
                val coins = getCoins()
                layoutList.visibility = android.view.View.GONE
                layoutGrid.visibility = android.view.View.VISIBLE
                tvTitle.text = "SIGI ASSET VAULT [ CREDITS: $coins ]"
                btnCollection.text = "☰" // Icon to back to list
                
                // POPULATE GRID
                layoutGrid.removeAllViews()
                populateArtifactGrid(layoutGrid)
            } else {
                // Show List
                layoutList.visibility = android.view.View.VISIBLE
                layoutGrid.visibility = android.view.View.GONE
                tvTitle.text = "SIGI CYBER CODEX"
                btnCollection.text = "◈" // Icon to showcase
            }
        }

        // Listeners
        btnClose.setOnClickListener { dialog.dismiss() }
        
        // Debug: Click Title to check unlock statuses
        tvTitle.setOnClickListener {
            val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
            val hasTicket = prefs.getBoolean("ticket_collected", false)
            val hasGomoku = prefs.getBoolean("skill_gomoku_unlocked", false)
            val hasEasy = prefs.getBoolean("permanent_unlock", false)
            val hasHard = prefs.getBoolean("permanent_unlock_hard", false)
            
            val stats = "Ticket: $hasTicket | Gomoku: $hasGomoku | Easy: $hasEasy"
            android.widget.Toast.makeText(this, stats, android.widget.Toast.LENGTH_LONG).show()
        }
        
        btnCollection.setOnClickListener {
            isShowcaseMode = !isShowcaseMode
            updateViewMode()
        }

        dialog.show()
    }

    private fun populateArtifactGrid(grid: android.widget.GridLayout) {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        
        // Define ALL Possible Artifacts
        val artifacts = mutableListOf<Artifact>()
        
        // 1. Train Ticket (From Hardcore Puzzle)
        if (prefs.getBoolean("ticket_collected", false)) {
            artifacts.add(Artifact("ticket_666", "货号:666", android.R.drawable.ic_menu_recent_history))
        }
        
        // 2. Gomoku Skill (Master Note - Hardcore)
        if (prefs.getBoolean("skill_gomoku_unlocked", false)) {
            artifacts.add(Artifact("gomoku_master", "牛爷爷", R.drawable.asset_banknote_hardcore))
        }

        // 3. Zheng Bang Coin (Easy Mode Reward)
        if (prefs.getBoolean("permanent_unlock", false)) {
            artifacts.add(Artifact("zheng_bang_coin", "蒸蚌", R.drawable.asset_banknote_cat))
        }

        // If empty
        if (artifacts.isEmpty()) {
            val emptyTv = android.widget.TextView(this)
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
            val tv = itemView.findViewById<android.widget.TextView>(R.id.artifactName)
            
            tv.text = artifact.name
            img.setImageResource(artifact.iconRes)
            
            itemView.setOnClickListener {
                showArtifactDetailDialog(artifact)
            }
            
            // Just add view, let XML handle size (fixed 100dp width in XML)
            // Or simple margin if needed
            val params = android.widget.GridLayout.LayoutParams()
            params.setMargins(24, 24, 24, 24)
            // params.columnSpec =  android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f) // REMOVED WEIGHT
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
            val overlayContainer = dialog.findViewById<android.view.View>(R.id.overlayContainer)
            val tvLeft = dialog.findViewById<android.widget.TextView>(R.id.overlayValueLeft)
            val tvRight = dialog.findViewById<android.widget.TextView>(R.id.overlayValueRight)
            val tvName = dialog.findViewById<android.widget.TextView>(R.id.overlayName)
            
            // Set Image
            if (artifact.id == "gomoku_master") {
                img.setImageResource(R.drawable.asset_banknote_hardcore)
                
                // --- HARDCORE CONFIG (Dragon) ---
                overlayContainer.visibility = android.view.View.VISIBLE
                tvLeft.visibility = android.view.View.VISIBLE
                tvRight.visibility = android.view.View.VISIBLE
                tvLeft.text = "100"
                tvRight.text = "100"
                
                tvName.visibility = android.view.View.VISIBLE
                tvName.text = " 梆 梆 "  // Bang Bang
                
            } else {
                img.setImageResource(R.drawable.asset_banknote_cat)
                
                // --- EASY CONFIG (Cat) ---
                // User requested: NO overlay, show original image only
                overlayContainer.visibility = android.view.View.GONE
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
        
        // Make full screen-ish (responsive to orientation)
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.95).toInt()
        val maxHeight = (metrics.heightPixels * 0.8).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        
        dialog.setCanceledOnTouchOutside(true)
        
        // Click anywhere to close
        dialog.findViewById<android.view.View>(android.R.id.content)?.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun startStarSignal() {
        // Continuous gentle flashing
        val blinkAnim = android.view.animation.AlphaAnimation(0.2f, 1.0f)
        blinkAnim.duration = 2000 // Slow breath
        blinkAnim.repeatMode = android.view.animation.Animation.REVERSE
        blinkAnim.repeatCount = android.view.animation.Animation.INFINITE
        settingsStar.startAnimation(blinkAnim)
        settingsStar.startAnimation(blinkAnim)
    }

    override fun onResume() {
        super.onResume()
        // Start Cosmic Ripples BGM
        try {
            val prefs = getSharedPreferences("AutoGLMConfig", android.content.Context.MODE_PRIVATE)
            val isBgmEnabled = prefs.getBoolean("sfx_enabled", true)
            
            if (isBgmEnabled) {
                if (bgmPlayer == null) {
                    bgmPlayer = android.media.MediaPlayer.create(this, R.raw.bgm_cosmic_ripples)
                    bgmPlayer?.isLooping = true
                    bgmPlayer?.setVolume(0.5f, 0.5f) 
                }
                bgmPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Fix stuck UI
        val uiContainer = findViewById<android.widget.FrameLayout>(R.id.uiContainerSettings)
        val hiddenWorldContainerTop = findViewById<android.view.View>(R.id.hiddenWorldContainerTop)
        val hiddenWorldContainerBottom = findViewById<android.view.View>(R.id.hiddenWorldContainerBottom)
        if (uiContainer != null && hiddenWorldContainerTop != null && hiddenWorldContainerBottom != null) {
            uiContainer.post { resetRevealState(uiContainer, hiddenWorldContainerTop, hiddenWorldContainerBottom) }
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause/Stop BGM
        bgmPlayer?.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bgmPlayer?.release()
        bgmPlayer = null
    }
}
