package com.zyvix.zorinm4

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.widget.*

class SettingsActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("zorin_settings", MODE_PRIVATE)
        val layout = LinearLayout(this).apply { orientation = 1; setPadding(dp(22), dp(28), dp(22), dp(20)); setBackgroundColor(Color.rgb(7,16,34)) }
        fun label(t: String) = TextView(this).apply { text=t; textSize=14f; setTextColor(Color.LTGRAY); setPadding(0,dp(12),0,dp(5)) }
        fun field(hint: String, value: String, secret: Boolean=false) = EditText(this).apply {
            this.hint=hint; setText(value); textSize=15f; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
            setSingleLine(true); setPadding(dp(12),dp(10),dp(12),dp(10)); setBackgroundColor(Color.rgb(20,35,59))
            if(secret) inputType= android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(TextView(this).apply { text="ZORIN-M4 SETTINGS"; textSize=22f; setTextColor(Color.WHITE) })
        layout.addView(label("AI provider"))
        val provider = Spinner(this)
        val providers = listOf("Groq", "Gemini", "OpenAI-compatible")
        provider.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        val saved = prefs.getString("provider","Groq")
        provider.setSelection(providers.indexOf(saved).coerceAtLeast(0))
        layout.addView(provider)
        layout.addView(label("API base URL"))
        val base = field("https://api.groq.com/openai/v1", prefs.getString("base","https://api.groq.com/openai/v1")!!)
        layout.addView(base)
        layout.addView(label("Model name"))
        val model = field("openai/gpt-oss-120b", prefs.getString("model","openai/gpt-oss-120b")!!)
        layout.addView(model)
        layout.addView(label("API key (stored locally on this device)"))
        val key = field("Paste API key", prefs.getString("key","")!!, true)
        layout.addView(key)
        val save = Button(this).apply { text="SAVE SETTINGS"; setOnClickListener {
            prefs.edit().putString("provider",provider.selectedItem.toString()).putString("base",base.text.toString().trim())
                .putString("model",model.text.toString().trim()).putString("key",key.text.toString()).apply()
            Toast.makeText(this@SettingsActivity,"Settings saved on this phone",Toast.LENGTH_SHORT).show(); finish()
        } }
        layout.addView(save)
        layout.addView(TextView(this).apply { text="Gemini OpenAI-compatible base URL: https://generativelanguage.googleapis.com/v1beta/openai\nOpenAI: https://api.openai.com/v1\nKeep your API key private."; setTextColor(Color.GRAY); textSize=12f; setPadding(0,dp(14),0,0) })
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
