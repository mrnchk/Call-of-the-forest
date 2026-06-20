package com.cotf.server.controller

import com.cotf.server.dto.GameResultDto
import com.cotf.server.dto.LeaderboardEntryDto
import com.cotf.server.dto.LeaderboardPaginationLimits
import com.cotf.server.dto.MyLeaderboardDto
import com.cotf.server.dto.SubmitGameResultRequest
import com.cotf.server.service.LeaderboardService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/leaderboard")
@Validated
class LeaderboardController(private val leaderboardService: LeaderboardService) {

    @PostMapping("/results")
    fun submit(
        request: HttpServletRequest,
        @Valid @RequestBody body: SubmitGameResultRequest
    ): ResponseEntity<GameResultDto> {
        val userId = currentUserId(request)
        val result = leaderboardService.submit(userId, body)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/top")
    fun top(
        @RequestParam(defaultValue = "20")
        @Min(LeaderboardPaginationLimits.MIN_TOP)
        @Max(LeaderboardPaginationLimits.MAX_TOP)
        limit: Int
    ): ResponseEntity<List<LeaderboardEntryDto>> {
        return ResponseEntity.ok(leaderboardService.top(limit))
    }

    @GetMapping("/me")
    fun me(request: HttpServletRequest): ResponseEntity<MyLeaderboardDto> {
        val userId = currentUserId(request)
        return ResponseEntity.ok(leaderboardService.forUser(userId))
    }

    private fun currentUserId(request: HttpServletRequest): UUID =
        UUID.fromString(request.getAttribute("userId") as String)
}
