package com.driversaathi.app

/** Turns what the driver said into a short chat draft. Rule based for now (AI/Gemini can plug in here later). */
object Drafter {
    private val words = listOf(
        "लिख दो", "लिखो", "कह दो", "बोल दो", "बता दो",
        "likh do", "likho", "keh do", "kah do", "bol do"
    )

    fun fromSpeech(raw: String): String {
        var s = raw.trim()
        var changed = true
        while (changed) {
            changed = false
            for (w in words) {
                if (s.startsWith(w, ignoreCase = true)) { s = s.substring(w.length).trim(); changed = true }
                if (s.endsWith(w, ignoreCase = true)) { s = s.substring(0, s.length - w.length).trim(); changed = true }
            }
        }
        if (s.isBlank()) s = raw.trim()
        return s.replaceFirstChar { it.uppercase() }
    }
}
