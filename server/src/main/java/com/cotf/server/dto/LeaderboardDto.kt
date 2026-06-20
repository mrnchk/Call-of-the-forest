package com.cotf.server.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class SubmitGameResultRequest(
    @field:NotNull
    @field:Min(value = 0, message = "survivedSeconds must be non-negative")
    val survivedSeconds: Int,

    @field:NotNull
    @field:Min(value = 0, message = "mobsKilled must be non-negative")
    val mobsKilled: Int,

    @field:NotNull
    @field:Min(value = 0, message = "resourcesGathered must be non-negative")
    val resourcesGathered: Int,

    @field:NotNull
    @field:Min(value = 0, message = "daysSurvived must be non-negative")
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
