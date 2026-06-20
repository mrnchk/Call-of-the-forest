package com.cotf.server.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScoreCalculatorTest {

    @Test
    fun `zero stats produce zero score`() {
        assertEquals(0, ScoreCalculator.calculate(0, 0, 0, 0))
    }

    @Test
    fun `one survived second produces one point`() {
        assertEquals(1, ScoreCalculator.calculate(1, 0, 0, 0))
    }

    @Test
    fun `one mob kill produces MOB_WEIGHT points`() {
        assertEquals(ScoreCalculator.MOB_WEIGHT, ScoreCalculator.calculate(0, 1, 0, 0))
    }

    @Test
    fun `one resource produces RESOURCE_WEIGHT points`() {
        assertEquals(ScoreCalculator.RESOURCE_WEIGHT, ScoreCalculator.calculate(0, 0, 1, 0))
    }

    @Test
    fun `one day survived produces DAY_WEIGHT points`() {
        assertEquals(ScoreCalculator.DAY_WEIGHT, ScoreCalculator.calculate(0, 0, 0, 1))
    }

    @Test
    fun `combined score equals sum of components`() {
        val expected = 600 +
            3 * ScoreCalculator.MOB_WEIGHT +
            12 * ScoreCalculator.RESOURCE_WEIGHT +
            2 * ScoreCalculator.DAY_WEIGHT
        assertEquals(expected, ScoreCalculator.calculate(600, 3, 12, 2))
    }
}
