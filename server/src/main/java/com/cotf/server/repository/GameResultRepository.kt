package com.cotf.server.repository

import com.cotf.server.model.GameResult
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameResultRepository : JpaRepository<GameResult, UUID> {

    fun findAllByOrderByScoreDescCreatedAtAsc(pageable: Pageable): List<GameResult>

    fun findFirstByUserIdOrderByScoreDescCreatedAtAsc(userId: UUID): GameResult?

    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): List<GameResult>

    fun countByScoreGreaterThan(score: Int): Long
}
