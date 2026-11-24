package com.liquidbubble.launcher

import com.liquidbubble.launcher.data.model.SystemMetricsState
import com.liquidbubble.launcher.system.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMappingTest {
    @Test
    fun `WMO weather codes map to human readable labels`() {
        assertEquals("Clear Sky", WeatherRepository.mapWmoCodeToLabel(0))
        assertEquals("Partly Cloudy", WeatherRepository.mapWmoCodeToLabel(2))
        assertEquals("Rain", WeatherRepository.mapWmoCodeToLabel(61))
        assertEquals("Thunderstorm", WeatherRepository.mapWmoCodeToLabel(95))
    }

    @Test
    fun `system metrics fractions handle valid and zero totals safely`() {
        val valid = SystemMetricsState(8_000L, 2_000L, 100_000L, 40_000L)
        assertEquals(0.75f, valid.usedRamFraction, 0.001f)
        assertEquals(0.60f, valid.usedStorageFraction, 0.001f)
        val empty = SystemMetricsState()
        assertEquals(0f, empty.usedRamFraction, 0.001f)
        assertEquals(0f, empty.usedStorageFraction, 0.001f)
    }
}
