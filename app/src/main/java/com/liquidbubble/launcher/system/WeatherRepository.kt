package com.liquidbubble.launcher.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.liquidbubble.launcher.data.model.WeatherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GeocodedLocation(val name: String, val country: String, val latitude: Double, val longitude: Double)

class WeatherRepository(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun getLastKnownCoordinates(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        return runCatching {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            var best: Location? = null
            for (provider in lm.getProviders(true)) {
                @Suppress("MissingPermission")
                val loc = lm.getLastKnownLocation(provider) ?: continue
                if (best == null || loc.accuracy < best.accuracy) best = loc
            }
            best?.let { it.latitude to it.longitude }
        }.getOrNull()
    }

    suspend fun fetchCurrentWeather(latitude: Double, longitude: Double, locationLabel: String): Result<WeatherState> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply { connectTimeout = 8_000; readTimeout = 8_000; requestMethod = "GET" }
            try {
                if (conn.responseCode !in 200..299) error("Weather HTTP ${conn.responseCode}")
                val current = JSONObject(conn.inputStream.bufferedReader().use { it.readText() }).getJSONObject("current")
                val wmoCode = current.optInt("weather_code", -1)
                WeatherState(
                    temperatureCelsius = current.getDouble("temperature_2m"),
                    conditionLabel = mapWmoCodeToLabel(wmoCode),
                    weatherCode = wmoCode,
                    humidityPercent = current.optInt("relative_humidity_2m", -1).takeIf { it >= 0 },
                    windSpeedKmh = current.optDouble("wind_speed_10m", Double.NaN).takeIf { !it.isNaN() },
                    locationName = locationLabel,
                    lastUpdatedEpochMs = System.currentTimeMillis()
                )
            } finally { conn.disconnect() }
        }
    }

    suspend fun searchCity(query: String): List<GeocodedLocation> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.length < 2) return@withContext emptyList()
        runCatching {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val conn = (URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=6&language=en&format=json").openConnection() as HttpURLConnection).apply {
                connectTimeout = 7_000; readTimeout = 7_000
            }
            try {
                val results = JSONObject(conn.inputStream.bufferedReader().use { it.readText() }).optJSONArray("results") ?: return@runCatching emptyList()
                buildList {
                    for (i in 0 until results.length()) {
                        val obj = results.getJSONObject(i)
                        add(GeocodedLocation(obj.getString("name"), obj.optString("country", ""), obj.getDouble("latitude"), obj.getDouble("longitude")))
                    }
                }
            } finally { conn.disconnect() }
        }.getOrDefault(emptyList())
    }

    companion object {
        fun mapWmoCodeToLabel(code: Int): String = when (code) {
            0 -> "Clear Sky"; 1 -> "Mainly Clear"; 2 -> "Partly Cloudy"; 3 -> "Overcast"; 45, 48 -> "Foggy"
            51, 53, 55, 56, 57 -> "Drizzle"; 61, 63, 65, 66, 67 -> "Rain"; 71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain Showers"; 85, 86 -> "Snow Showers"; 95, 96, 99 -> "Thunderstorm"; else -> "Cloudy"
        }
    }
}
