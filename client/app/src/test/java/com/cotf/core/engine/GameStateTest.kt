package com.cotf.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for pure-Kotlin data/extension properties in GameState.kt.
 * No Android SDK required — runs on the JVM test task.
 */
class GameStateTest {

    // ── Player defaults ────────────────────────────────────────────────────────

    @Test
    fun `Player default hp equals maxHp`() {
        val p = Player()
        assertEquals(p.maxHp, p.hp)
    }

    @Test
    fun `Player default hunger is 100`() {
        assertEquals(100f, Player().hunger, 0f)
    }

    @Test
    fun `Player default deathAnimTime is negative (alive)`() {
        assertTrue(Player().deathAnimTime < 0f)
    }

    @Test
    fun `Player copy preserves unmodified fields`() {
        val base = Player(hp = 50, maxHp = 100)
        val updated = base.copy(hunger = 42f)
        assertEquals(50, updated.hp)
        assertEquals(100, updated.maxHp)
        assertEquals(42f, updated.hunger, 0f)
    }

    // ── MobType.isPredator ─────────────────────────────────────────────────────

    @Test
    fun `WOLF is predator`()  { assertTrue(MobType.WOLF.isPredator) }
    @Test
    fun `BEAR is predator`()  { assertTrue(MobType.BEAR.isPredator) }
    @Test
    fun `FOX is predator`()   { assertTrue(MobType.FOX.isPredator) }
    @Test
    fun `DEER is not predator`()   { assertFalse(MobType.DEER.isPredator) }
    @Test
    fun `RABBIT is not predator`() { assertFalse(MobType.RABBIT.isPredator) }
    @Test
    fun `BIRD is not predator`()   { assertFalse(MobType.BIRD.isPredator) }

    // ── MobType.baseSpeed ──────────────────────────────────────────────────────

    @Test
    fun `predators have positive baseSpeed`() {
        listOf(MobType.WOLF, MobType.BEAR, MobType.FOX).forEach {
            assertTrue("${it.name} baseSpeed should be > 0", it.baseSpeed > 0f)
        }
    }

    @Test
    fun `prey have positive baseSpeed`() {
        listOf(MobType.DEER, MobType.RABBIT, MobType.BIRD).forEach {
            assertTrue("${it.name} baseSpeed should be > 0", it.baseSpeed > 0f)
        }
    }

    @Test
    fun `rabbit is faster than bear`() {
        assertTrue(MobType.RABBIT.baseSpeed > MobType.BEAR.baseSpeed)
    }

    // ── MobType.baseMaxHp ──────────────────────────────────────────────────────

    @Test
    fun `bear has more hp than rabbit`() {
        assertTrue(MobType.BEAR.baseMaxHp > MobType.RABBIT.baseMaxHp)
    }

    @Test
    fun `all mob types have positive baseMaxHp`() {
        MobType.entries.forEach {
            assertTrue("${it.name} baseMaxHp > 0", it.baseMaxHp > 0)
        }
    }

    // ── MobType.meatDropChance ─────────────────────────────────────────────────

    @Test
    fun `all meatDropChance values are in 0_1 range`() {
        MobType.entries.forEach {
            val c = it.meatDropChance
            assertTrue("${it.name} meatDropChance $c out of [0,1]", c in 0f..1f)
        }
    }

    // ── BehaviorState ──────────────────────────────────────────────────────────

    @Test
    fun `DYING is distinct from alive states`() {
        val aliveStates = setOf(
            BehaviorState.IDLE, BehaviorState.PATROL,
            BehaviorState.CHASE, BehaviorState.ATTACK, BehaviorState.FLEE
        )
        assertFalse(aliveStates.contains(BehaviorState.DYING))
    }

    // ── GameState defaults ─────────────────────────────────────────────────────

    @Test
    fun `default GameState is running and not game over`() {
        val s = GameState()
        assertTrue(s.isRunning)
        assertFalse(s.isGameOver)
    }

    @Test
    fun `default GameState has no mobs or objects`() {
        val s = GameState()
        assertTrue(s.mobs.isEmpty())
        assertTrue(s.objects.isEmpty())
    }

    @Test
    fun `timeOfDay starts in valid range`() {
        val t = GameState().timeOfDay
        assertTrue(t in 0f..1f)
    }
}
