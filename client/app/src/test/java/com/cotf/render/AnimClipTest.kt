package com.cotf.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [AnimClip] and [AnimClip.frameAt].
 * Pure Kotlin — no Android SDK required.
 */
class AnimClipTest {

    // ── frameAt basics ─────────────────────────────────────────────────────────

    @Test
    fun `frameAt returns 0 at time zero`() {
        val clip = AnimClip(row = 0, frameCount = 4, fps = 8f)
        assertEquals(0, clip.frameAt(0f))
    }

    @Test
    fun `frameAt advances by fps`() {
        // fps=8 → one frame every 0.125 sec
        val clip = AnimClip(row = 0, frameCount = 4, fps = 8f)
        assertEquals(1, clip.frameAt(0.125f))
        assertEquals(2, clip.frameAt(0.250f))
        assertEquals(3, clip.frameAt(0.375f))
    }

    @Test
    fun `frameAt wraps around after one full cycle`() {
        val clip = AnimClip(row = 0, frameCount = 4, fps = 8f)
        // Full cycle = 4 frames / 8 fps = 0.5 sec → back to frame 0
        assertEquals(0, clip.frameAt(0.5f))
        assertEquals(1, clip.frameAt(0.625f))
    }

    @Test
    fun `frameAt result is always within 0 until frameCount`() {
        val clip = AnimClip(row = 2, frameCount = 6, fps = 10f)
        val times = listOf(0f, 0.05f, 0.1f, 0.55f, 1.0f, 12.345f, 99.99f)
        times.forEach { t ->
            val frame = clip.frameAt(t)
            assertTrue("frame $frame out of range at t=$t", frame in 0 until clip.frameCount)
        }
    }

    @Test
    fun `single-frame clip always returns frame 0`() {
        val clip = AnimClip(row = 0, frameCount = 1, fps = 8f)
        listOf(0f, 0.5f, 10f, 999f).forEach { t ->
            assertEquals("Should always be 0 at t=$t", 0, clip.frameAt(t))
        }
    }

    @Test
    fun `higher fps advances frames faster`() {
        val slow = AnimClip(row = 0, frameCount = 4, fps = 4f)   // 0.25 sec per frame
        val fast = AnimClip(row = 0, frameCount = 4, fps = 16f)  // 0.0625 sec per frame
        // At t=0.1: slow → frame 0, fast → frame 1
        assertEquals(0, slow.frameAt(0.1f))
        assertEquals(1, fast.frameAt(0.1f))
    }

    // ── AnimClip data class ────────────────────────────────────────────────────

    @Test
    fun `default fps is 8`() {
        val clip = AnimClip(row = 0, frameCount = 3)
        assertEquals(8f, clip.fps, 0f)
    }

    @Test
    fun `two clips with same params are equal`() {
        val a = AnimClip(row = 1, frameCount = 5, fps = 10f)
        val b = AnimClip(row = 1, frameCount = 5, fps = 10f)
        assertEquals(a, b)
    }

    @Test
    fun `copy changes only specified field`() {
        val base = AnimClip(row = 0, frameCount = 4, fps = 8f)
        val faster = base.copy(fps = 24f)
        assertEquals(0, faster.row)
        assertEquals(4, faster.frameCount)
        assertEquals(24f, faster.fps, 0f)
    }
}
