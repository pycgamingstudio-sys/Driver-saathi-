package com.driversaathi.app

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * ONLY writes the driver's draft text into a chat box of a ride app.
 * It never clicks, taps, swipes, sends, accepts or rejects. The driver always presses Send.
 */
class DraftAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (Platforms.of(pkg) == null) return
        val draft = Store.pendingDraft(this) ?: return
        val root = rootInActiveWindow ?: return

        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val node = (if (focused != null && focused.isEditable) focused else null)
            ?: findEditable(root) ?: return

        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, draft)
        }
        if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
            Store.clearPending(this)
            Speaker.init(applicationContext)
            Speaker.speak("हो गया, Send दबाइए।")
            Store.bump()
        }
    }

    private fun findEditable(n: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (n == null) return null
        if (n.isEditable) return n
        for (i in 0 until n.childCount) {
            val r = findEditable(n.getChild(i))
            if (r != null) return r
        }
        return null
    }

    override fun onInterrupt() {}
}
