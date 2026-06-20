package com.cotf.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GameEngine] observable state.
 * Tests only pure-Kotlin behaviour that is visible through the public API
 * (state flow, reset, pause/resume). No coroutine tick is started.
 */
class GameEngineTest {

    // ── Construction ───────────────────────────────────────────────────────────

    @Test
    fun `initial state has objects on the map`() {
        val engine = GameEngine()
        assertTrue(
            "generateMap should produce at least one object",
            engine.state.value.objects.isNotEmpty()
        )
    }

    @Test
    fun `initial state has mobs on the map`() {
        val engine = GameEngine()
        assertTrue(
            "generateMap should produce at least one mob",
            engine.state.value.mobs.isNotEmpty()
        )
    }

    @Test
    fun `initial state is not game over`() {
        assertFalse(GameEngine().state.value.isGameOver)
    }

    @Test
    fun `initial player has full health`() {
        val player = GameEngine().state.value.player
        assertEquals(player.maxHp, player.hp)
    }

    @Test
    fun `initial player is alive (deathAnimTime negative)`() {
        assertTrue(GameEngine().state.value.player.deathAnimTime < 0f)
    }

    @Test
    fun `initial player has full hunger`() {
        assertEquals(100f, GameEngine().state.value.player.hunger, 0f)
    }

    // ── pause / resume ─────────────────────────────────────────────────────────

    @Test
    fun `pause sets isRunning to false`() {
        val engine = GameEngine()
        engine.pause()
        assertFalse(engine.state.value.isRunning)
    }

    @Test
    fun `resume sets isRunning to true`() {
        val engine = GameEngine()
        engine.pause()
        engine.resume()
        assertTrue(engine.state.value.isRunning)
    }

    // ── reset ──────────────────────────────────────────────────────────────────

    @Test
    fun `reset restores full player health`() {
        val engine = GameEngine()
        engine.reset()
        val player = engine.state.value.player
        assertEquals(player.maxHp, player.hp)
    }

    @Test
    fun `reset repopulates objects`() {
        val engine = GameEngine()
        engine.reset()
        assertTrue(engine.state.value.objects.isNotEmpty())
    }

    @Test
    fun `reset repopulates mobs`() {
        val engine = GameEngine()
        engine.reset()
        assertTrue(engine.state.value.mobs.isNotEmpty())
    }

    @Test
    fun `reset clears attackEffects`() {
        val engine = GameEngine()
        engine.reset()
        assertTrue(engine.state.value.attackEffects.isEmpty())
    }

    @Test
    fun `reset resets isGameOver to false`() {
        val engine = GameEngine()
        engine.reset()
        assertFalse(engine.state.value.isGameOver)
    }

    // ── Map invariants ─────────────────────────────────────────────────────────

    @Test
    fun `all map objects have positive radius`() {
        GameEngine().state.value.objects.forEach { obj ->
            assertTrue("${obj.id} radius should be > 0", obj.radius > 0f)
        }
    }

    @Test
    fun `all mobs start alive (hp greater than 0)`() {
        GameEngine().state.value.mobs.forEach { mob ->
            assertTrue("${mob.id} should have hp > 0", mob.hp > 0)
        }
    }

    @Test
    fun `all mob ids are unique`() {
        val ids = GameEngine().state.value.mobs.map { it.id }
        assertEquals("Mob ids must be unique", ids.distinct().size, ids.size)
    }

    @Test
    fun `all object ids are unique`() {
        val ids = GameEngine().state.value.objects.map { it.id }
        assertEquals("Object ids must be unique", ids.distinct().size, ids.size)
    }

    @Test
    fun `all mobs start in non-dying state`() {
        GameEngine().state.value.mobs.forEach { mob ->
            assertFalse("${mob.id} should not start DYING", mob.state == BehaviorState.DYING)
        }
    }

    // ── TimeOfDay constants ────────────────────────────────────────────────────

    @Test
    fun `DEATH_ANIM_DURATION is positive`() {
        assertTrue(DEATH_ANIM_DURATION > 0f)
    }

    @Test
    fun `PLAYER_DEATH_ANIM_DURATION is positive`() {
        assertTrue(PLAYER_DEATH_ANIM_DURATION > 0f)
    }

    @Test
    fun `player death animation is longer than mob death animation`() {
        assertTrue(PLAYER_DEATH_ANIM_DURATION > DEATH_ANIM_DURATION)
    }
}
