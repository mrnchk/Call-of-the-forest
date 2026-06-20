package com.cotf.network.dto

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val username: String
)

data class UserDto(
    val id: String,
    val username: String,
    val role: String
)

data class ErrorResponse(
    val error: String,
    val message: String
)

// ===================== Leaderboard =====================

data class SubmitGameResultRequest(
    val survivedSeconds: Int,
    val mobsKilled: Int,
    val resourcesGathered: Int,
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
    val createdAt: String
)

data class LeaderboardEntryDto(
    val rank: Int,
    val username: String,
    val score: Int,
    val survivedSeconds: Int,
    val mobsKilled: Int,
    val resourcesGathered: Int,
    val daysSurvived: Int,
    val createdAt: String
)

data class MyLeaderboardDto(
    val best: GameResultDto?,
    val rank: Int?,
    val recent: List<GameResultDto>
)
