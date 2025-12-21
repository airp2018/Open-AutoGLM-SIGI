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

    private lateinit var providerGroup: RadioGroup
    private lateinit var rbZhipu: RadioButton
    private lateinit var rbModelScope: RadioButton
    private lateinit var editBaseUrl: EditText
    private lateinit var editApiKey: EditText
    private lateinit var editModelName: EditText
    private lateinit var btnSave: Button

    // Default Configurations
    private val ZHIPU_URL = "https://open.bigmodel.cn/api/paas/v4/"
    private val ZHIPU_KEY = "562eac47fb0c43fa995ee58261d12a52.Y2HAB0eRQPyXKiHI"
    private val ZHIPU_MODEL = "autoglm-phone"

    private val MS_URL = "https://api-inference.modelscope.cn/v1/"
    private val MS_KEY = "ms-9785cb73-b979-45c9-a31f-ec1e26463fc0"
    private val MS_MODEL = "ZhipuAI/AutoGLM-Phone-9B"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        providerGroup = findViewById(R.id.providerGroup)
        rbZhipu = findViewById(R.id.rbZhipu)
        rbModelScope = findViewById(R.id.rbModelScope)
        editBaseUrl = findViewById(R.id.editBaseUrl)
        editApiKey = findViewById(R.id.editApiKey)
        editModelName = findViewById(R.id.editModelName)
        btnSave = findViewById(R.id.btnSave)

        loadSettings()

        // Radio Button Toggle Logic
        providerGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbZhipu) {
                editBaseUrl.setText(ZHIPU_URL)
                editApiKey.setText(ZHIPU_KEY)
                editModelName.setText(ZHIPU_MODEL)
            } else if (checkedId == R.id.rbModelScope) {
                editBaseUrl.setText(MS_URL)
                editApiKey.setText(MS_KEY)
                editModelName.setText(MS_MODEL)
            }
        }

        btnSave.setOnClickListener {
            saveSettings()
            finish() // Close activity and return to Main
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("AutoGLMConfig", Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("base_url", ZHIPU_URL)
        val savedKey = prefs.getString("api_key", ZHIPU_KEY)
        val savedModel = prefs.getString("model_name", ZHIPU_MODEL)
        
        editBaseUrl.setText(savedUrl)
        editApiKey.setText(savedKey)
        editModelName.setText(savedModel)
        
        // Simple heuristic to set radio button
        if (savedUrl?.contains("modelscope") == true) {
            rbModelScope.isChecked = true
        } else {
            rbZhipu.isChecked = true
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
        Toast.makeText(this, "Configuration Saved", Toast.LENGTH_SHORT).show()
    }
}
