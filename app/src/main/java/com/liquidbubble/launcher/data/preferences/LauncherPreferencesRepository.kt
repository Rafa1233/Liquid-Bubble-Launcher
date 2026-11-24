package com.liquidbubble.launcher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liquidbubble.launcher.data.model.BackupPayload
import com.liquidbubble.launcher.data.model.LauncherSettingsState
import com.liquidbubble.launcher.data.model.WeatherState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "liquid_bubble_launcher_prefs"
)

@Serializable
private data class CachedWeatherDto(
    val temperatureCelsius: Double,
    val conditionLabel: String,
    val weatherCode: Int,
    val humidityPercent: Int?,
    val windSpeedKmh: Double?,
    val locationName: String,
    val lastUpdatedEpochMs: Long
)

class LauncherPreferencesRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private object Keys {
        val SETTINGS_JSON = stringPreferencesKey("settings_json")
        val CACHED_WEATHER_JSON = stringPreferencesKey("cached_weather_json")
    }

    val settingsFlow: Flow<LauncherSettingsState> = context.launcherDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val raw = prefs[Keys.SETTINGS_JSON]
            if (raw.isNullOrBlank()) {
                LauncherSettingsState()
            } else {
                runCatching { json.decodeFromString<LauncherSettingsState>(raw) }
                    .getOrElse { LauncherSettingsState() }
            }
        }

    val cachedWeatherFlow: Flow<WeatherState?> = context.launcherDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val raw = prefs[Keys.CACHED_WEATHER_JSON] ?: return@map null
            runCatching {
                val dto = json.decodeFromString<CachedWeatherDto>(raw)
                WeatherState(
                    temperatureCelsius = dto.temperatureCelsius,
                    conditionLabel = dto.conditionLabel,
                    weatherCode = dto.weatherCode,
                    humidityPercent = dto.humidityPercent,
                    windSpeedKmh = dto.windSpeedKmh,
                    locationName = dto.locationName,
                    lastUpdatedEpochMs = dto.lastUpdatedEpochMs
                )
            }.getOrNull()
        }

    suspend fun updateSettings(transform: (LauncherSettingsState) -> LauncherSettingsState) {
        context.launcherDataStore.edit { prefs ->
            val current = prefs[Keys.SETTINGS_JSON]?.let { raw ->
                runCatching { json.decodeFromString<LauncherSettingsState>(raw) }
                    .getOrElse { LauncherSettingsState() }
            } ?: LauncherSettingsState()
            val updated = transform(current)
            prefs[Keys.SETTINGS_JSON] = json.encodeToString(updated)
        }
    }

    suspend fun saveCachedWeather(state: WeatherState) {
        val temp = state.temperatureCelsius ?: return
        val updatedAt = state.lastUpdatedEpochMs ?: System.currentTimeMillis()
        val dto = CachedWeatherDto(
            temperatureCelsius = temp,
            conditionLabel = state.conditionLabel,
            weatherCode = state.weatherCode,
            humidityPercent = state.humidityPercent,
            windSpeedKmh = state.windSpeedKmh,
            locationName = state.locationName,
            lastUpdatedEpochMs = updatedAt
        )
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.CACHED_WEATHER_JSON] = json.encodeToString(dto)
        }
    }

    suspend fun exportBackupJson(): String {
        val current = settingsFlow.first()
        val payload = BackupPayload(
            schemaVersion = 1,
            exportedAtEpochMs = System.currentTimeMillis(),
            settings = current
        )
        return json.encodeToString(payload)
    }

    suspend fun importBackupJson(rawJson: String): Result<LauncherSettingsState> {
        return runCatching {
            val payload = json.decodeFromString<BackupPayload>(rawJson)
            val sanitized = payload.settings.copy(onboardingCompleted = true)
            updateSettings { sanitized }
            sanitized
        }
    }

    suspend fun resetAllSettings() {
        updateSettings { LauncherSettingsState(onboardingCompleted = true) }
    }
}
