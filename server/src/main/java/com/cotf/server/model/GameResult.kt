package com.cotf.server.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "game_results",
    indexes = [
        Index(name = "idx_game_results_score", columnList = "score"),
        Index(name = "idx_game_results_user_score", columnList = "user_id, score"),
        Index(name = "idx_game_results_user_created", columnList = "user_id, created_at")
    ]
)
class GameResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID(0, 0)

    @Column(nullable = false, length = 50)
    var username: String = ""

    @Column(nullable = false)
    var score: Int = 0

    @Column(name = "survived_seconds", nullable = false)
    var survivedSeconds: Int = 0

    @Column(name = "mobs_killed", nullable = false)
    var mobsKilled: Int = 0

    @Column(name = "resources_gathered", nullable = false)
    var resourcesGathered: Int = 0

    @Column(name = "days_survived", nullable = false)
    var daysSurvived: Int = 0

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    constructor()

    constructor(
        userId: UUID,
        username: String,
        score: Int,
        survivedSeconds: Int,
        mobsKilled: Int,
        resourcesGathered: Int,
        daysSurvived: Int
    ) {
        this.userId = userId
        this.username = username
        this.score = score
        this.survivedSeconds = survivedSeconds
        this.mobsKilled = mobsKilled
        this.resourcesGathered = resourcesGathered
        this.daysSurvived = daysSurvived
    }
}
