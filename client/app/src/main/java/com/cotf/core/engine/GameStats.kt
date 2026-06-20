package com.cotf.core.engine

/**
 * Метрики текущей партии. Иммутабельно, обновляется в GameEngine.
 *
 * survivedSeconds копится как Float (тик dt), а в score/отправку уходит округлённым
 * до целых, чтобы клиент и сервер использовали одну и ту же формулу.
 */
data class GameStats(
    val survivedSeconds: Float = 0f,
    val mobsKilled: Int = 0,
    val resourcesGathered: Int = 0,
    val daysSurvived: Int = 0
)

/**
 * Авторитетный подсчёт счёта — должен совпадать с серверным.
 * см. server/.../service/ScoreCalculator.kt
 */
object ScoreCalculator {
    const val MOB_WEIGHT: Int = 50
    const val RESOURCE_WEIGHT: Int = 5
    const val DAY_WEIGHT: Int = 100

    fun calculate(
        survivedSeconds: Int,
        mobsKilled: Int,
        resourcesGathered: Int,
        daysSurvived: Int
    ): Int =
        survivedSeconds +
            mobsKilled * MOB_WEIGHT +
            resourcesGathered * RESOURCE_WEIGHT +
            daysSurvived * DAY_WEIGHT

    fun calculate(stats: GameStats): Int = calculate(
        survivedSeconds = stats.survivedSeconds.toInt(),
        mobsKilled = stats.mobsKilled,
        resourcesGathered = stats.resourcesGathered,
        daysSurvived = stats.daysSurvived
    )
}
