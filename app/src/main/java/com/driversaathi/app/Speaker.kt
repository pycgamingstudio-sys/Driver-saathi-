package com.driversaathi.app

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object Speaker {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val queue = mutableListOf<String>()

    @Synchronized
    fun init(c: Context) {
        if (tts != null) return
        tts = TextToSpeech(c.applicationContext) { status ->
            synchronized(Speaker) {
                if (status == TextToSpeech.SUCCESS) {
                    tts?.setLanguage(Locale("hi", "IN"))
                    ready = true
                    queue.forEach { tts?.speak(it, TextToSpeech.QUEUE_ADD, null, "ds") }
                    queue.clear()
                }
            }
        }
    }

    @Synchronized
    fun speak(text: String) {
        val t = text.take(250)
        if (ready) tts?.speak(t, TextToSpeech.QUEUE_ADD, null, "ds") else queue.add(t)
    }
}
