package com.liquidbubble.launcher.system

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

class LauncherRoleManager(private val context: Context) {
    fun isDefaultLauncher(): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) return@runCatching rm.isRoleHeld(RoleManager.ROLE_HOME)
        }
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val ri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) context.packageManager.resolveActivity(home, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        else @Suppress("DEPRECATION") context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        ri?.activityInfo?.packageName == context.packageName
    }.getOrDefault(false)

    fun createSetDefaultLauncherIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) return rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
        }
        return createSystemHomeSettingsIntent()
    }

    fun createSystemHomeSettingsIntent(): Intent {
        val i = Intent(Settings.ACTION_HOME_SETTINGS)
        return if (i.resolveActivity(context.packageManager) != null) i else Intent(Settings.ACTION_SETTINGS)
    }

    fun getCurrentDefaultLauncherLabel(): String = runCatching {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val ri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) context.packageManager.resolveActivity(home, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        else @Suppress("DEPRECATION") context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        val pkg = ri?.activityInfo?.packageName ?: return@runCatching "System Default"
        if (pkg == context.packageName) "Rii Launcher" else ri.loadLabel(context.packageManager).toString()
    }.getOrDefault("System Default")
}
