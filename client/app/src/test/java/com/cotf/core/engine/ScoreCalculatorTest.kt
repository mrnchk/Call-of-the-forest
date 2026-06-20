package com.cotf.core.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun `zero stats produce zero score`() {
        assertEquals(0, ScoreCalculator.calculate(0, 0, 0, 0))
        assertEquals(0, ScoreCalculator.calculate(GameStats()))
    }

    @Test
    fun `one survived second produces one point`() {
        assertEquals(1, ScoreCalculator.calculate(1, 0, 0, 0))
    }

    @Test
    fun `mob kill applies MOB_WEIGHT`() {
        assertEquals(ScoreCalculator.MOB_WEIGHT, ScoreCalculator.calculate(0, 1, 0, 0))
    }

    @Test
    fun `resource applies RESOURCE_WEIGHT`() {
        assertEquals(ScoreCalculator.RESOURCE_WEIGHT, ScoreCalculator.calculate(0, 0, 1, 0))
    }

    @Test
    fun `day applies DAY_WEIGHT`() {
        assertEquals(ScoreCalculator.DAY_WEIGHT, ScoreCalculator.calculate(0, 0, 0, 1))
    }

    @Test
    fun `combined stats sum components`() {
        val expected = 600 +
            3 * ScoreCalculator.MOB_WEIGHT +
            12 * ScoreCalculator.RESOURCE_WEIGHT +
            2 * ScoreCalculator.DAY_WEIGHT
        assertEquals(expected, ScoreCalculator.calculate(600, 3, 12, 2))
    }

    @Test
    fun `survivedSeconds is floored from Float`() {
        val stats = GameStats(survivedSeconds = 9.99f)
        // 9.99 → 9 seconds (truncated to Int before calculation)
        assertEquals(9, ScoreCalculator.calculate(stats))
    }
}
