package com.liquidbubble.launcher.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.liquidbubble.launcher.data.model.BatteryState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DeviceBatteryMonitor(private val context: Context) {
    fun observeBattery(): Flow<BatteryState> = callbackFlow {
        fun parseIntent(intent: Intent?): BatteryState = runCatching {
            if (intent == null) {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                return@runCatching BatteryState(bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1, bm?.isCharging ?: false)
            }
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            val pct = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else -1
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val source = when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> if (isCharging) "Charging" else "Battery"
            }
            BatteryState(pct, isCharging, temp, source)
        }.getOrDefault(BatteryState())

        val sticky = runCatching { ContextCompat.registerReceiver(context, null, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_EXPORTED) }.getOrNull()
        trySend(parseIntent(sticky))
        val receiver = object : BroadcastReceiver() { override fun onReceive(c: Context?, i: Intent?) { trySend(parseIntent(i)) } }
        runCatching { ContextCompat.registerReceiver(context, receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_EXPORTED) }
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }
}
