package com.liquidbubble.launcher.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp

object LiquidSprings {
    val DynamicIslandSpec: SpringSpec<Float> = spring(dampingRatio = 0.68f, stiffness = 280f)
    val DynamicIslandDpSpec: SpringSpec<Dp> = spring(dampingRatio = 0.68f, stiffness = 280f)
    val BubbleBounceSpec: SpringSpec<Float> = spring(dampingRatio = 0.58f, stiffness = 420f)
    val AdjacentShiftSpec: SpringSpec<Float> = spring(dampingRatio = 0.66f, stiffness = 340f)
    val FolderMorphSpec: SpringSpec<Float> = spring(dampingRatio = 0.72f, stiffness = 310f)
    val GentleSnapSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}
