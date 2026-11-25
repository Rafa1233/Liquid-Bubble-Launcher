package com.liquidbubble.launcher.system

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent

class AppWidgetHostManager(private val context: Context) {

    private val appCtx = context.applicationContext
    val appWidgetManager: AppWidgetManager? by lazy {
        runCatching { AppWidgetManager.getInstance(appCtx) }.getOrNull()
    }
    val appWidgetHost: AppWidgetHost? by lazy {
        runCatching { AppWidgetHost(appCtx, HOST_ID) }.getOrNull()
    }

    fun startListening() { runCatching { appWidgetHost?.startListening() } }
    fun stopListening() { runCatching { appWidgetHost?.stopListening() } }

    fun getInstalledProviders(): List<AppWidgetProviderInfo> = runCatching {
        appWidgetManager?.installedProviders?.sortedBy { it.loadLabel(context.packageManager).lowercase() } ?: emptyList()
    }.getOrDefault(emptyList())

    fun allocateWidgetId(): Int = runCatching { appWidgetHost?.allocateAppWidgetId() ?: -1 }.getOrDefault(-1)

    fun tryBindWidget(appWidgetId: Int, provider: AppWidgetProviderInfo): Boolean = runCatching {
        if (appWidgetId == -1) return@runCatching false
        appWidgetManager?.bindAppWidgetIdIfAllowed(appWidgetId, provider.provider) == true
    }.getOrDefault(false)

    fun createBindRequestIntent(appWidgetId: Int, provider: AppWidgetProviderInfo): Intent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
        }

    fun getProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        runCatching { appWidgetManager?.getAppWidgetInfo(appWidgetId) }.getOrNull()

    fun createHostView(appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView =
        runCatching { appWidgetHost?.createView(appCtx, appWidgetId, info)?.apply { setAppWidget(appWidgetId, info) } }
            .getOrNull() ?: AppWidgetHostView(context)

    fun deleteWidgetId(appWidgetId: Int) {
        if (appWidgetId != -1) runCatching { appWidgetHost?.deleteAppWidgetId(appWidgetId) }
    }

    companion object {
        private const val HOST_ID = 202617
    }
}
