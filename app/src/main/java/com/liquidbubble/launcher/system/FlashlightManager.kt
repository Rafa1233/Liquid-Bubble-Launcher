package com.liquidbubble.launcher.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FlashlightManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val flashCameraId: String? by lazy {
        runCatching {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    fun isAvailable(): Boolean = flashCameraId != null

    fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun observeTorchState(): Flow<Boolean> = callbackFlow {
        val cm = cameraManager
        val targetId = flashCameraId
        if (cm == null || targetId == null) { trySend(false); close(); return@callbackFlow }
        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) { if (cameraId == targetId) trySend(enabled) }
            override fun onTorchModeUnavailable(cameraId: String) { if (cameraId == targetId) trySend(false) }
        }
        runCatching { cm.registerTorchCallback(callback, Handler(Looper.getMainLooper())) }.onFailure { trySend(false) }
        awaitClose { runCatching { cm.unregisterTorchCallback(callback) } }
    }

    fun setTorchEnabled(enabled: Boolean): Result<Unit> = runCatching {
        val cm = cameraManager ?: error("Camera service unavailable")
        val id = flashCameraId ?: error("No hardware flashlight found")
        if (!hasCameraPermission()) error("Camera permission not granted")
        cm.setTorchMode(id, enabled)
    }
}
