package com.liquidbubble.launcher.system

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LiquidNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() { super.onListenerConnected(); _isConnected.value = true; recomputeBadgeCounts() }
    override fun onListenerDisconnected() { super.onListenerDisconnected(); _isConnected.value = false; _badgeCounts.value = emptyMap() }
    override fun onNotificationPosted(sbn: StatusBarNotification?) = recomputeBadgeCounts()
    override fun onNotificationRemoved(sbn: StatusBarNotification?) = recomputeBadgeCounts()

    private fun recomputeBadgeCounts() {
        val counts = runCatching {
            activeNotifications?.asSequence()
                ?.filter { val f = it.notification.flags; (f and Notification.FLAG_ONGOING_EVENT) == 0 && (f and Notification.FLAG_GROUP_SUMMARY) == 0 }
                ?.groupingBy { it.packageName }?.eachCount() ?: emptyMap()
        }.getOrDefault(emptyMap())
        _badgeCounts.value = counts
    }

    companion object {
        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
        private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val badgeCounts: StateFlow<Map<String, Int>> = _badgeCounts.asStateFlow()
    }
}
