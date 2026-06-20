package com.cotf.server.repository

import com.cotf.server.model.GameResult
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GameResultRepository : JpaRepository<GameResult, UUID> {

    /**
     * Returns one row per user — the best-scoring result.
     * Tie-breaking: earliest createdAt wins (first-come-first-served for equal scores).
     * Results are ordered by score DESC, createdAt ASC (for stable ranking).
     */
    @Query("""
        SELECT r FROM GameResult r
        WHERE r.score = (SELECT MAX(r2.score) FROM GameResult r2 WHERE r2.userId = r.userId)
        AND r.createdAt = (
            SELECT MIN(r2.createdAt) FROM GameResult r2
            WHERE r2.userId = r.userId AND r2.score = r.score
        )
        ORDER BY r.score DESC, r.createdAt ASC
    """)
    fun findBestPerUserOrderByScoreDescCreatedAtAsc(pageable: Pageable): List<GameResult>

    fun findFirstByUserIdOrderByScoreDescCreatedAtAsc(userId: UUID): GameResult?

    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): List<GameResult>

    /**
     * Counts how many distinct users have a best score strictly greater than [score].
     * Used to compute the rank of a user in the deduplicated leaderboard.
     */
    @Query("""
        SELECT COUNT(DISTINCT r.userId) FROM GameResult r
        WHERE r.score = (SELECT MAX(r2.score) FROM GameResult r2 WHERE r2.userId = r.userId)
        AND r.score > :score
    """)
    fun countUsersWithBestScoreGreaterThan(@Param("score") score: Int): Long
}
