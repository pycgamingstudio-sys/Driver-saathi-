package com.driversaathi.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Reads ride-app notifications and speaks them in Hindi. It only reads. It never taps anything. */
class RideNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName == packageName) return
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val platform = Platforms.of(sbn.packageName)
            ?: (if (Store.testMode(this)) sbn.packageName else return)

        val ex = n.extras
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        if (!Store.addMessage(this, platform, title, text)) return

        if (Store.online(this)) {
            Speaker.init(applicationContext)
            Speaker.speak("$platform। $title। $text")
        }
        Store.bump()
    }
}
