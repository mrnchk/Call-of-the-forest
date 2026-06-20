package com.cotf.server.service

import com.cotf.server.dto.GameResultDto
import com.cotf.server.dto.LeaderboardEntryDto
import com.cotf.server.dto.MyLeaderboardDto
import com.cotf.server.dto.SubmitGameResultRequest
import com.cotf.server.model.GameResult
import com.cotf.server.repository.GameResultRepository
import com.cotf.server.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeaderboardService(
    private val gameResultRepository: GameResultRepository,
    private val userRepository: UserRepository
) {

    fun submit(userId: UUID, request: SubmitGameResultRequest): GameResultDto {
        validate(request)

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val score = ScoreCalculator.calculate(
            survivedSeconds = request.survivedSeconds,
            mobsKilled = request.mobsKilled,
            resourcesGathered = request.resourcesGathered,
            daysSurvived = request.daysSurvived
        )

        val saved = gameResultRepository.save(
            GameResult(
                userId = user.id ?: throw IllegalStateException("User id is null"),
                username = user.username,
                score = score,
                survivedSeconds = request.survivedSeconds,
                mobsKilled = request.mobsKilled,
                resourcesGathered = request.resourcesGathered,
                daysSurvived = request.daysSurvived
            )
        )
        return saved.toDto()
    }

    fun top(limit: Int): List<LeaderboardEntryDto> {
        require(limit in MIN_TOP_LIMIT..MAX_TOP_LIMIT) {
            "limit must be in $MIN_TOP_LIMIT..$MAX_TOP_LIMIT"
        }
        return gameResultRepository
            .findAllByOrderByScoreDescCreatedAtAsc(PageRequest.of(0, limit))
            .mapIndexed { idx, result -> result.toLeaderboardEntry(rank = idx + 1) }
    }

    fun forUser(userId: UUID): MyLeaderboardDto {
        val best = gameResultRepository.findFirstByUserIdOrderByScoreDescCreatedAtAsc(userId)
        val recent = gameResultRepository
            .findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, RECENT_RUNS_LIMIT))
            .map { it.toDto() }

        val rank = best?.let { gameResultRepository.countByScoreGreaterThan(it.score).toInt() + 1 }

        return MyLeaderboardDto(
            best = best?.toDto(),
            rank = rank,
            recent = recent
        )
    }

    private fun validate(request: SubmitGameResultRequest) {
        require(request.survivedSeconds >= 0) { "survivedSeconds must be non-negative" }
        require(request.mobsKilled >= 0) { "mobsKilled must be non-negative" }
        require(request.resourcesGathered >= 0) { "resourcesGathered must be non-negative" }
        require(request.daysSurvived >= 0) { "daysSurvived must be non-negative" }

        require(request.survivedSeconds <= MAX_SURVIVED_SECONDS) {
            "survivedSeconds exceeds sanity cap ($MAX_SURVIVED_SECONDS)"
        }
        require(request.mobsKilled <= MAX_MOBS_KILLED) {
            "mobsKilled exceeds sanity cap ($MAX_MOBS_KILLED)"
        }
        require(request.resourcesGathered <= MAX_RESOURCES_GATHERED) {
            "resourcesGathered exceeds sanity cap ($MAX_RESOURCES_GATHERED)"
        }
        require(request.daysSurvived <= MAX_DAYS_SURVIVED) {
            "daysSurvived exceeds sanity cap ($MAX_DAYS_SURVIVED)"
        }
    }

    private fun GameResult.toDto(): GameResultDto = GameResultDto(
        id = (id ?: throw IllegalStateException("GameResult id is null")).toString(),
        username = username,
        score = score,
        survivedSeconds = survivedSeconds,
        mobsKilled = mobsKilled,
        resourcesGathered = resourcesGathered,
        daysSurvived = daysSurvived,
        createdAt = createdAt
    )

    private fun GameResult.toLeaderboardEntry(rank: Int): LeaderboardEntryDto = LeaderboardEntryDto(
        rank = rank,
        username = username,
        score = score,
        survivedSeconds = survivedSeconds,
        mobsKilled = mobsKilled,
        resourcesGathered = resourcesGathered,
        daysSurvived = daysSurvived,
        createdAt = createdAt
    )

    companion object {
        const val MIN_TOP_LIMIT: Int = 1
        const val MAX_TOP_LIMIT: Int = 100
        const val RECENT_RUNS_LIMIT: Int = 10

        const val MAX_SURVIVED_SECONDS: Int = 7 * 24 * 3600
        const val MAX_MOBS_KILLED: Int = 10_000
        const val MAX_RESOURCES_GATHERED: Int = 100_000
        const val MAX_DAYS_SURVIVED: Int = 365
    }
}
