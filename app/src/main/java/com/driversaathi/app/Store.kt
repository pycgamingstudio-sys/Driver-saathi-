package com.driversaathi.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class Msg(
    val platform: String,
    val title: String,
    val text: String,
    val draft: String,
    val time: Long
)

object Store {
    var tick by mutableIntStateOf(0)
        private set

    private val main = Handler(Looper.getMainLooper())
    fun bump() { main.post { tick++ } }

    private fun p(c: Context) =
        c.applicationContext.getSharedPreferences("ds", Context.MODE_PRIVATE)

    fun online(c: Context) = p(c).getBoolean("online", false)
    fun setOnline(c: Context, v: Boolean) { p(c).edit().putBoolean("online", v).apply(); bump() }

    fun testMode(c: Context) = p(c).getBoolean("test", false)
    fun setTestMode(c: Context, v: Boolean) { p(c).edit().putBoolean("test", v).apply(); bump() }

    fun consent(c: Context) = p(c).getBoolean("consent", false)
    fun setConsent(c: Context) { p(c).edit().putBoolean("consent", true).apply(); bump() }

    // ---- pending draft (filled into the chat box by DraftAccessibilityService) ----
    fun setPending(c: Context, d: String) {
        p(c).edit().putString("pending", d).putLong("pendingAt", System.currentTimeMillis()).apply()
    }

    fun pendingDraft(c: Context): String? {
        val d = p(c).getString("pending", null) ?: return null
        val at = p(c).getLong("pendingAt", 0L)
        if (d.isBlank() || System.currentTimeMillis() - at > 120_000) return null
        return d
    }

    fun clearPending(c: Context) { p(c).edit().remove("pending").apply() }

    // ---- messages ----
    @Synchronized
    fun messages(c: Context): List<Msg> {
        val s = p(c).getString("msgs", "[]") ?: "[]"
        val arr = try { JSONArray(s) } catch (e: Exception) { JSONArray() }
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Msg(
                o.optString("platform"), o.optString("title"), o.optString("text"),
                o.optString("draft"), o.optLong("time")
            )
        }
    }

    @Synchronized
    private fun save(c: Context, list: List<Msg>) {
        val arr = JSONArray()
        list.take(50).forEach {
            arr.put(
                JSONObject()
                    .put("platform", it.platform).put("title", it.title)
                    .put("text", it.text).put("draft", it.draft).put("time", it.time)
            )
        }
        p(c).edit().putString("msgs", arr.toString()).apply()
    }

    /** Returns false if it is a duplicate of the latest message (notifications often repeat). */
    @Synchronized
    fun addMessage(c: Context, platform: String, title: String, text: String): Boolean {
        val list = messages(c)
        val now = System.currentTimeMillis()
        val last = list.firstOrNull()
        if (last != null && last.platform == platform && last.title == title &&
            last.text == text && now - last.time < 30_000
        ) return false
        save(c, listOf(Msg(platform, title, text, "", now)) + list)
        return true
    }

    @Synchronized
    fun attachDraftToLast(c: Context, draft: String) {
        val list = messages(c)
        if (list.isEmpty()) return
        save(c, listOf(list[0].copy(draft = draft)) + list.drop(1))
    }
}
