package com.cotf.server.service

/**
 * Авторитетный подсчёт счёта партии. Сервер не доверяет клиенту в финальной цифре —
 * клиент шлёт сырые метрики, score считается здесь.
 *
 * Веса подбираем так, чтобы каждая активность имела ощутимый вклад,
 * но выживание оставалось базой:
 * - секунда выживания = 1 очко
 * - убитый моб       = 50 очков
 * - добытый ресурс   = 5 очков
 * - пережитые сутки  = 100 очков
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
}
