package com.liquidbubble.launcher.system

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.liquidbubble.launcher.data.model.SystemMetricsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceMonitor(private val context: Context) {

    suspend fun readSystemMetrics(): SystemMetricsState = withContext(Dispatchers.IO) {
        val (totalRam, availRam) = runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)
            memInfo.totalMem to memInfo.availMem
        }.getOrDefault(0L to 0L)

        val (totalStorage, availStorage) = runCatching {
            val stat = StatFs(Environment.getDataDirectory().path)
            stat.totalBytes to stat.availableBytes
        }.getOrDefault(0L to 0L)

        SystemMetricsState(totalRam, availRam, totalStorage, availStorage)
    }

    fun isWifiConnected(): Boolean = runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }.getOrDefault(false)

    fun isBluetoothEnabled(): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bm?.adapter?.isEnabled == true
    }.getOrDefault(false)

    fun createWifiSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI).takeIf { it.resolveActivity(context.packageManager) != null }
                ?: Intent(Settings.ACTION_WIFI_SETTINGS)
        } else Intent(Settings.ACTION_WIFI_SETTINGS)
    }

    fun createBluetoothSettingsIntent(): Intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
}
