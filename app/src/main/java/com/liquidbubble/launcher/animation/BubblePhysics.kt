package com.liquidbubble.launcher.animation

import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlin.math.sign

@Immutable
data class DockItemPhysicsTransform(
    val scale: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f
)

object BubblePhysics {
    fun computeDockItemTransform(
        index: Int,
        activeIndex: Int?,
        magnificationEnabled: Boolean,
        reducedMotion: Boolean,
        neighborSpreadPx: Float
    ): DockItemPhysicsTransform {
        if (activeIndex == null || reducedMotion || !magnificationEnabled) {
            return DockItemPhysicsTransform()
        }
        val distance = index - activeIndex
        return when (abs(distance)) {
            0 -> DockItemPhysicsTransform(scale = 1.16f, translationX = 0f, translationY = -neighborSpreadPx * 0.45f)
            1 -> DockItemPhysicsTransform(scale = 1.06f, translationX = distance.toFloat().sign * neighborSpreadPx * 0.75f, translationY = -neighborSpreadPx * 0.14f)
            2 -> DockItemPhysicsTransform(scale = 1.0f, translationX = distance.toFloat().sign * neighborSpreadPx * 0.30f, translationY = 0f)
            else -> DockItemPhysicsTransform()
        }
    }
}
