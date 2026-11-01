package com.zyvix.zorinm4

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var store: ChatStore
    private lateinit var transcript: LinearLayout
    private lateinit var input: EditText
    private lateinit var status: TextView
    private var tts: TextToSpeech? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val blue = Color.rgb(37, 155, 255)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ChatStore(this); tts = TextToSpeech(this, this)
        buildUi()
        store.recent(80).forEach { addBubble(it.role, it.content, false) }
        if (store.recent().isEmpty()) addBubble("assistant", "ZORIN-M4 mobile is ready. Add an AI provider and API key in Settings to begin.", false)
    }
    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation=1; setBackgroundColor(Color.rgb(7,16,34)); setPadding(dp(14),dp(18),dp(14),dp(12)) }
        val top = LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL; orientation=0 }
        top.addView(TextView(this).apply { text="ZORIN-M4"; textSize=23f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD })
        top.addView(TextView(this).apply { text="  MOBILE"; textSize=10f; setTextColor(blue) })
        val settings = Button(this).apply { text="⚙"; setOnClickListener { startActivity(Intent(this@MainActivity,SettingsActivity::class.java)) } }
        top.addView(settings, LinearLayout.LayoutParams(-2,dp(44)))
        root.addView(top)
        status = TextView(this).apply { text="NATIVE ANDROID • READY"; textSize=10f; setTextColor(Color.rgb(80,190,255)); setPadding(0,dp(4),0,dp(8)) }
        root.addView(status)
        val scroll = ScrollView(this).apply { isFillViewport=true }
        transcript = LinearLayout(this).apply { orientation=1; setPadding(0,dp(8),0,dp(8)) }
        scroll.addView(transcript)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        val row = LinearLayout(this).apply { gravity=Gravity.CENTER_VERTICAL }
        input = EditText(this).apply { hint="Message ZORIN-M4…"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); textSize=15f; setSingleLine(false); maxLines=4; setPadding(dp(12),dp(8),dp(12),dp(8)); setBackgroundColor(Color.rgb(20,35,59)) }
        row.addView(input,LinearLayout.LayoutParams(0,dp(52),1f))
        row.addView(Button(this).apply { text="🎙"; setOnClickListener { listen() } })
        row.addView(Button(this).apply { text="➤"; setOnClickListener { send() } })
        root.addView(row)
        setContentView(root)
    }
    private fun addBubble(role: String, text: String, save: Boolean=true) {
        val isUser = role=="user"
        val bubble = TextView(this).apply {
            this.text = (if(isUser) "YOU\n" else "ZORIN-M4\n") + text
            textSize=14f; setTextColor(Color.WHITE); setPadding(dp(13),dp(10),dp(13),dp(10))
            setBackgroundColor(if(isUser) Color.rgb(20,67,112) else Color.rgb(20,35,59))
        }
        val params=LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,dp(5),0,dp(5)) }
        transcript.addView(bubble,params)
        if(save) store.add(role,text)
    }
    private fun send() {
        val prompt=input.text.toString().trim(); if(prompt.isEmpty()) return
        input.setText(""); addBubble("user",prompt); status.text="THINKING…"
        if(prompt.startsWith("remember ",true)) {
            val fact=prompt.substringAfter(" ", "").trim()
            if(fact.isNotBlank()) { store.remember(fact); addBubble("assistant","Saved to this phone's local memory."); status.text="READY"; return }
        }
        scope.launch {
            try {
                val prefs=getSharedPreferences("zorin_settings",MODE_PRIVATE)
                val base=prefs.getString("base","https://api.groq.com/openai/v1")!!
                val model=prefs.getString("model","openai/gpt-oss-120b")!!
                val key=prefs.getString("key","")!!
                val history=store.recent(24)
                val answer=withContext(Dispatchers.IO) { AiClient().reply(base,model,key,history,store.memories()) }
                addBubble("assistant",answer); status.text="READY"; tts?.speak(answer,TextToSpeech.QUEUE_FLUSH,null,"zorin_reply")
            } catch(e: Exception) {
                addBubble("assistant","I couldn't reach the AI provider. ${e.message ?: "Check your connection and Settings."}")
                status.text="CONNECTION ISSUE"
            }
        }
    }
    private fun listen() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),7); return
        }
        try {
            val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to ZORIN-M4")
            }
            startActivityForResult(intent,8)
        } catch(_: Exception) { Toast.makeText(this,"Speech recognition is not available on this device.",Toast.LENGTH_LONG).show() }
    }
    @Deprecated("Deprecated by Android but broadly supported")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==8 && resultCode==RESULT_OK) {
            val words=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if(!words.isNullOrBlank()) { input.setText(words); send() }
        }
    }
    override fun onInit(status:Int) { if(status==TextToSpeech.SUCCESS) tts?.language=Locale.getDefault() }
    override fun onDestroy() { scope.cancel(); tts?.shutdown(); super.onDestroy() }
}
