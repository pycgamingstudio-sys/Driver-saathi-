package com.driversaathi.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private var recognizer: SpeechRecognizer? = null

    private val micLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { Store.bump() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Speaker.init(this)
        setContent { AppTheme { Root(this) } }
    }

    override fun onResume() {
        super.onResume()
        Store.bump()
    }

    override fun onDestroy() {
        recognizer?.destroy()
        super.onDestroy()
    }

    fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    fun requestMic() = micLauncher.launch(Manifest.permission.RECORD_AUDIO)

    fun open(action: String) {
        try { startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (e: Exception) { toast("सेटिंग नहीं खुली") }
    }

    fun openBattery() {
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:$packageName"))
            )
        } catch (e: Exception) { open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) }
    }

    fun copy(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("draft", text))
        toast("कॉपी हो गया")
    }

    /** Opens Ola / Uber / Rapido driver app (found by app name, so no package guessing). */
    fun openRide(key: String) {
        val pm = packageManager
        val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = pm.queryIntentActivities(q, 0).filter {
            val label = it.loadLabel(pm).toString().lowercase()
            label.contains(key) || Platforms.of(it.activityInfo.packageName)?.lowercase() == key
        }
        val driverWords = listOf("driver", "captain", "partner", "drive")
        val pick = matches.firstOrNull { m ->
            val l = m.loadLabel(pm).toString().lowercase()
            driverWords.any { l.contains(it) }
        } ?: matches.firstOrNull()
        val intent = pick?.let { pm.getLaunchIntentForPackage(it.activityInfo.packageName) }
        if (intent == null) { toast("$key ऐप नहीं मिला"); return }
        startActivity(intent)
    }

    /** Driver speaks the reply; we make a draft. Driver always presses Send himself. */
    fun listen() {
        if (!Perms.mic(this)) { requestMic(); return }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { toast("आवाज़ पहचान उपलब्ध नहीं"); return }
        recognizer?.destroy()
        val sr = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer = sr
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { toast("बोलिए…") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { Speaker.speak("सुनाई नहीं दिया, दोबारा बोलिए।") }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onResults(results: Bundle?) {
                val said = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (said.isNullOrBlank()) {
                    Speaker.speak("सुनाई नहीं दिया, दोबारा बोलिए।")
                    return
                }
                val draft = Drafter.fromSpeech(said)
                Store.setPending(this@MainActivity, draft)
                Store.attachDraftToLast(this@MainActivity, draft)
                Speaker.speak("ठीक है, लिख रहा हूँ: $draft")
                Store.bump()
            }
        })
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        }
        sr.startListening(i)
    }
}
