package com.liquidbubble.launcher

import com.liquidbubble.launcher.animation.BubblePhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BubblePhysicsTest {
    @Test
    fun `pressed dock item scales up and lifts`() {
        val transform = BubblePhysics.computeDockItemTransform(1, 1, true, false, 20f)
        assertEquals(1.16f, transform.scale, 0.001f)
        assertEquals(0f, transform.translationX, 0.001f)
        assertTrue(transform.translationY < 0f)
    }

    @Test
    fun `adjacent dock items shift away symmetrically`() {
        val left = BubblePhysics.computeDockItemTransform(0, 1, true, false, 20f)
        val right = BubblePhysics.computeDockItemTransform(2, 1, true, false, 20f)
        assertEquals(1.06f, left.scale, 0.001f)
        assertEquals(1.06f, right.scale, 0.001f)
        assertTrue(left.translationX < 0f)
        assertTrue(right.translationX > 0f)
        assertEquals(-left.translationX, right.translationX, 0.001f)
    }

    @Test
    fun `reduced motion or disabled magnification returns neutral transform`() {
        val disabled = BubblePhysics.computeDockItemTransform(1, 1, false, false, 20f)
        val reduced = BubblePhysics.computeDockItemTransform(1, 1, true, true, 20f)
        assertEquals(1f, disabled.scale, 0.001f)
        assertEquals(0f, disabled.translationY, 0.001f)
        assertEquals(1f, reduced.scale, 0.001f)
        assertEquals(0f, reduced.translationY, 0.001f)
    }
}
