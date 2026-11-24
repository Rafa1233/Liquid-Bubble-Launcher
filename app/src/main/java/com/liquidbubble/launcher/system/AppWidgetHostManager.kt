package com.liquidbubble.launcher.system

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent

class AppWidgetHostManager(private val context: Context) {

    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost: AppWidgetHost = AppWidgetHost(context, HOST_ID)

    fun startListening() { runCatching { appWidgetHost.startListening() } }
    fun stopListening() { runCatching { appWidgetHost.stopListening() } }

    fun getInstalledProviders(): List<AppWidgetProviderInfo> = runCatching {
        appWidgetManager.installedProviders.sortedBy { it.loadLabel(context.packageManager).lowercase() }
    }.getOrDefault(emptyList())

    fun allocateWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

    fun tryBindWidget(appWidgetId: Int, provider: AppWidgetProviderInfo): Boolean = runCatching {
        appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider.provider)
    }.getOrDefault(false)

    fun createBindRequestIntent(appWidgetId: Int, provider: AppWidgetProviderInfo): Intent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
        }

    fun getProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        appWidgetManager.getAppWidgetInfo(appWidgetId)

    fun createHostView(appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView =
        appWidgetHost.createView(context, appWidgetId, info).apply { setAppWidget(appWidgetId, info) }

    fun deleteWidgetId(appWidgetId: Int) {
        runCatching { appWidgetHost.deleteAppWidgetId(appWidgetId) }
    }

    companion object {
        private const val HOST_ID = 202617
    }
}
