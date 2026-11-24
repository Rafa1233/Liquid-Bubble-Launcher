package com.liquidbubble.launcher.system

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.LruCache
import android.view.View
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.liquidbubble.launcher.data.model.AppCategory
import com.liquidbubble.launcher.data.model.AppInfo
import com.liquidbubble.launcher.data.model.IconPackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

class AppManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconCache = object : LruCache<String, Bitmap>(180) {}
    private val imageBitmapCache = object : LruCache<String, ImageBitmap>(180) {}

    private var loadedIconPackPackage: String? = null
    private var iconPackComponentMap: Map<String, String> = emptyMap()
    private var iconPackResources: Resources? = null

    fun observePackageChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                clearIconCache()
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }

    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = queryActivitiesCompat(mainIntent)
        val ownPackage = context.packageName

        resolveInfos
            .asSequence()
            .filter { it.activityInfo != null && it.activityInfo.packageName != ownPackage }
            .mapNotNull { info ->
                val activityInfo = info.activityInfo ?: return@mapNotNull null
                val pkg = activityInfo.packageName
                val cls = activityInfo.name
                val key = "$pkg/$cls"
                val label = runCatching { info.loadLabel(packageManager).toString().trim() }.getOrDefault(pkg)
                val pkgInfo = runCatching { getPackageInfoCompat(pkg) }.getOrNull()
                val appInfo = activityInfo.applicationInfo
                AppInfo(
                    label = label.ifEmpty { pkg },
                    packageName = pkg,
                    activityName = cls,
                    componentKey = key,
                    category = mapAndroidCategory(appInfo, pkg, label),
                    firstInstallTime = pkgInfo?.firstInstallTime ?: 0L,
                    lastUpdateTime = pkgInfo?.lastUpdateTime ?: 0L,
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .distinctBy { it.componentKey }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun resolveDefaultDockKeys(installedApps: List<AppInfo>): List<String> {
        val intentCandidates = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        )
        val resolved = mutableListOf<String>()
        for (candidate in intentCandidates) {
            val pkg = resolveActivityCompat(candidate)?.activityInfo?.packageName
            val match = if (pkg != null) installedApps.firstOrNull { it.packageName == pkg } else null
            if (match != null && match.componentKey !in resolved) resolved.add(match.componentKey)
        }
        if (resolved.size < 4) {
            for (app in installedApps) {
                if (app.componentKey !in resolved) resolved.add(app.componentKey)
                if (resolved.size == 4) break
            }
        }
        return resolved
    }

    fun getCachedIconImage(app: AppInfo, iconPackPackage: String?): ImageBitmap? {
        return imageBitmapCache.get("${app.componentKey}|${iconPackPackage.orEmpty()}")
    }

    suspend fun getAppIconImageBitmap(app: AppInfo, iconPackPackage: String?, sizePx: Int = 144): ImageBitmap? {
        val cacheKey = "${app.componentKey}|${iconPackPackage.orEmpty()}"
        imageBitmapCache.get(cacheKey)?.let { return it }
        val bmp = getAppIconBitmap(app, iconPackPackage, sizePx) ?: return null
        return bmp.asImageBitmap().also { imageBitmapCache.put(cacheKey, it) }
    }

    suspend fun getAppIconBitmap(app: AppInfo, iconPackPackage: String?, sizePx: Int = 144): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${app.componentKey}|${iconPackPackage.orEmpty()}"
        iconCache.get(cacheKey)?.let { return@withContext it }

        val drawable: Drawable? = runCatching {
            if (!iconPackPackage.isNullOrBlank()) loadIconFromPack(app, iconPackPackage) else null
        }.getOrNull() ?: runCatching {
            packageManager.getActivityIcon(ComponentName(app.packageName, app.activityName))
        }.getOrNull() ?: runCatching {
            packageManager.getApplicationIcon(app.packageName)
        }.getOrNull()

        val bitmap = drawable?.let { drawableToBitmap(it, sizePx) }
        if (bitmap != null) {
            iconCache.put(cacheKey, bitmap)
            imageBitmapCache.put(cacheKey, bitmap.asImageBitmap())
        }
        bitmap
    }

    fun clearIconCache() {
        iconCache.evictAll()
        imageBitmapCache.evictAll()
    }

    suspend fun discoverIconPacks(): List<IconPackInfo> = withContext(Dispatchers.IO) {
        val themes = listOf("org.adw.launcher.THEMES", "com.novalauncher.THEME", "com.teslacoilsw.launcher.THEME")
        val found = linkedMapOf<String, IconPackInfo>()
        for (action in themes) {
            for (ri in queryActivitiesCompat(Intent(action))) {
                val pkg = ri.activityInfo?.packageName ?: continue
                if (!found.containsKey(pkg)) found[pkg] = IconPackInfo(pkg, ri.loadLabel(packageManager).toString())
            }
        }
        found.values.sortedBy { it.label.lowercase() }
    }

    fun launchApp(app: AppInfo, sourceView: View? = null): Result<Unit> = runCatching {
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        val options = sourceView?.let { ActivityOptions.makeScaleUpAnimation(it, 0, 0, it.width, it.height).toBundle() }
        context.startActivity(launchIntent, options)
    }.recoverCatching {
        val fallback = packageManager.getLaunchIntentForPackage(app.packageName) ?: error("Launch intent unavailable")
        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(fallback)
    }

    fun openAppInfo(packageName: String): Result<Unit> = runCatching {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun requestUninstallApp(packageName: String): Result<Unit> = runCatching {
        context.startActivity(Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun loadIconFromPack(app: AppInfo, packPackage: String): Drawable? {
        if (loadedIconPackPackage != packPackage) parseIconPackAppFilter(packPackage)
        val res = iconPackResources ?: return null
        val drawableName = iconPackComponentMap["ComponentInfo{${app.packageName}/${app.activityName}}"] ?: return null
        val resId = res.getIdentifier(drawableName, "drawable", packPackage)
        return if (resId == 0) null else runCatching { res.getDrawable(resId, null) }.getOrNull()
    }

    private fun parseIconPackAppFilter(packPackage: String) {
        loadedIconPackPackage = packPackage
        val map = mutableMapOf<String, String>()
        val res = runCatching { packageManager.getResourcesForApplication(packPackage) }.getOrNull()
        iconPackResources = res
        if (res == null) { iconPackComponentMap = emptyMap(); return }
        val xmlId = res.getIdentifier("appfilter", "xml", packPackage)
        if (xmlId != 0) {
            runCatching {
                val parser: XmlPullParser = res.getXml(xmlId)
                var event = parser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG && parser.name == "item") {
                        val c = parser.getAttributeValue(null, "component")
                        val d = parser.getAttributeValue(null, "drawable")
                        if (!c.isNullOrBlank() && !d.isNullOrBlank()) map[c] = d
                    }
                    event = parser.next()
                }
            }
        }
        iconPackComponentMap = map
    }

    private fun drawableToBitmap(drawable: Drawable, sizePx: Int): Bitmap {
        val targetSize = sizePx.coerceIn(64, 192)
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, targetSize, targetSize)
        drawable.draw(canvas)
        return bitmap
    }

    private fun mapAndroidCategory(appInfo: ApplicationInfo, pkg: String, label: String): AppCategory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_NEWS, ApplicationInfo.CATEGORY_MAPS -> return AppCategory.TOOLS
            }
        }
        val lower = "$pkg $label".lowercase()
        return when {
            listOf("chat", "social", "gram", "whats", "talk", "messenger", "discord", "reddit").any { it in lower } -> AppCategory.SOCIAL
            listOf("game", "play", "craft", "arena", "pubg", "genshin").any { it in lower } -> AppCategory.GAMES
            listOf("music", "spotify", "video", "youtube", "gallery", "camera", "photo", "netflix").any { it in lower } -> AppCategory.MEDIA
            listOf("mail", "docs", "notes", "calendar", "office", "drive", "notion", "keep").any { it in lower } -> AppCategory.PRODUCTIVITY
            listOf("calc", "clock", "files", "settings", "browser", "chrome", "maps", "weather", "tool").any { it in lower } -> AppCategory.TOOLS
            else -> AppCategory.OTHER
        }
    }

    private fun queryActivitiesCompat(intent: Intent): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
        else @Suppress("DEPRECATION") packageManager.queryIntentActivities(intent, 0)

    private fun resolveActivityCompat(intent: Intent): ResolveInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        else @Suppress("DEPRECATION") packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    private fun getPackageInfoCompat(pkg: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        else @Suppress("DEPRECATION") packageManager.getPackageInfo(pkg, 0)
}
