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
        val hiddenWorldBgSettings = findViewById<android.widget.ImageView>(R.id.hiddenWorldBgSettings)
        val dragTrigger = findViewById<android.view.View>(R.id.dragTriggerSettings)
        
        // Robust Layout Listener
        uiContainer.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            val height = bottom - top
            if (height > 0 && uiContainer.translationY == 0f) {
                // Initial position: TOP (Sky) - Hidden above screen
                hiddenWorldBgSettings.translationY = -height.toFloat()
            }
        }
        
        // Initial Force Set
        uiContainer.post {
            resetRevealState(uiContainer, hiddenWorldBgSettings)
        }
        
        val revealArrow = findViewById<android.view.View>(R.id.revealArrowSettings)
        
        dragTrigger.setOnTouchListener(object : android.view.View.OnTouchListener {
             var startY = 0f
             var isLockedIn = false
             
             override fun onTouch(v: android.view.View, event: android.view.MotionEvent): Boolean {
                 val screenWidth = resources.displayMetrics.widthPixels
                 val centerX = screenWidth / 2f
                 val touchX = event.rawX
                 
                 // "Dark Door" Logic: Top Center Zone
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
                         // Exploration Mode for Top Door
                         if (!isLockedIn) {
                             if (isInZone) {
                                 isLockedIn = true
                                 startY = event.rawY
                                 revealArrow.animate().alpha(0.3f).setDuration(300).start()
                                 v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                             }
                             return true
                         }
                         
                         val deltaY = event.rawY - startY
                         val maxDrag = resources.displayMetrics.heightPixels * 0.8f
                         
                         // PULL DOWN LOGIC (Scanning the Sky)
                         if (deltaY > 0) {
                             val dampFactor = 0.6f
                             val targetY = deltaY * dampFactor // Positive translation
                             
                             if (kotlin.math.abs(targetY) < maxDrag) {
                                uiContainer.translationY = targetY
                                // Image moves DOWN from TOP (-height)
                                hiddenWorldBgSettings.translationY = -uiContainer.height.toFloat() + targetY
                             }
                         }
                         return true
                     }
                     android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                         isLockedIn = false
                         // Hide visual cue
                         revealArrow.animate().alpha(0f).setDuration(400).start()
                         resetRevealState(uiContainer, hiddenWorldBgSettings, animate = true)
                         return true
                     }
                 }
                 return false
             }
        })
    }
    
    // Self-healing reset function
    private fun resetRevealState(ui: android.view.View, bg: android.view.View, animate: Boolean = false) {
        if (animate) {
            ui.animate()
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(android.view.animation.OvershootInterpolator(0.8f))
                .start()
            bg.animate()
                .translationY(-ui.height.toFloat()) // Reset to TOP
                .setDuration(400)
                .setInterpolator(android.view.animation.OvershootInterpolator(0.8f))
                .start()
        } else {
            ui.translationY = 0f
            bg.translationY = -ui.height.toFloat() // Reset to TOP
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
        val hiddenWorldBgSettings = findViewById<android.widget.ImageView>(R.id.hiddenWorldBgSettings)
        if (uiContainer != null && hiddenWorldBgSettings != null) {
            uiContainer.post { resetRevealState(uiContainer, hiddenWorldBgSettings) }
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
