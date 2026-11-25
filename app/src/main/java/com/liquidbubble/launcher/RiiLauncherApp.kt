package com.liquidbubble.launcher

import android.app.Application
import java.io.File

class RiiLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                File(filesDir, "last_crash.txt").writeText(
                    "Thread: ${thread.name}\n${throwable.stackTraceToString().take(3500)}"
                )
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun readLastCrash(): String? = runCatching {
        val f = File(filesDir, "last_crash.txt")
        if (f.exists()) f.readText() else null
    }.getOrNull()

    fun clearLastCrash() {
        runCatching { File(filesDir, "last_crash.txt").delete() }
    }
}
