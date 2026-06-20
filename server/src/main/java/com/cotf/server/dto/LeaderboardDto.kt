package com.cotf.server.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant

/**
 * Sanity-cap'ы на пользовательский ввод. Используются как в аннотациях DTO,
 * так и в контракте API — все значения должны быть compile-time константами.
 */
object SubmitGameResultLimits {
    const val MAX_SURVIVED_SECONDS: Long = 7L * 24 * 3600   // 7 дней
    const val MAX_MOBS_KILLED: Long = 10_000
    const val MAX_RESOURCES_GATHERED: Long = 100_000
    const val MAX_DAYS_SURVIVED: Long = 365
}

object LeaderboardPaginationLimits {
    const val MIN_TOP: Long = 1
    const val MAX_TOP: Long = 100
}

data class SubmitGameResultRequest(
    @field:NotNull
    @field:Min(value = 0, message = "survivedSeconds must be non-negative")
    @field:Max(value = SubmitGameResultLimits.MAX_SURVIVED_SECONDS, message = "survivedSeconds exceeds sanity cap")
    val survivedSeconds: Int,

    @field:NotNull
    @field:Min(value = 0, message = "mobsKilled must be non-negative")
    @field:Max(value = SubmitGameResultLimits.MAX_MOBS_KILLED, message = "mobsKilled exceeds sanity cap")
    val mobsKilled: Int,

    @field:NotNull
    @field:Min(value = 0, message = "resourcesGathered must be non-negative")
    @field:Max(value = SubmitGameResultLimits.MAX_RESOURCES_GATHERED, message = "resourcesGathered exceeds sanity cap")
    val resourcesGathered: Int,

    @field:NotNull
    @field:Min(value = 0, message = "daysSurvived must be non-negative")
    @field:Max(value = SubmitGameResultLimits.MAX_DAYS_SURVIVED, message = "daysSurvived exceeds sanity cap")
    val daysSurvived: Int
)

data class GameResultDto(
    val id: String,
    val username: String,
    val score: Int,
    val survivedSeconds: Int,
    val mobsKilled: Int,
    val resourcesGathered: Int,
    val daysSurvived: Int,
    val createdAt: Instant
)

data class LeaderboardEntryDto(
    val rank: Int,
    val username: String,
    val score: Int,
    val survivedSeconds: Int,
    val mobsKilled: Int,
    val resourcesGathered: Int,
    val daysSurvived: Int,
    val createdAt: Instant
)

data class MyLeaderboardDto(
    val best: GameResultDto?,
    val rank: Int?,
    val recent: List<GameResultDto>
)
