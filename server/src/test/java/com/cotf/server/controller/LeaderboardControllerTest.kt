package com.cotf.server.controller

import com.cotf.server.dto.GameResultDto
import com.cotf.server.dto.LeaderboardEntryDto
import com.cotf.server.dto.MyLeaderboardDto
import com.cotf.server.dto.SubmitGameResultRequest
import com.cotf.server.model.Role
import com.cotf.server.model.User
import com.cotf.server.repository.GameResultRepository
import com.cotf.server.repository.UserRepository
import com.cotf.server.security.JwtTokenProvider
import com.cotf.server.service.ScoreCalculator
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaderboardControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var jwtTokenProvider: JwtTokenProvider
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var gameResultRepository: GameResultRepository

    @BeforeEach
    fun clean() {
        gameResultRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST results without token returns 401`() {
        val body = objectMapper.writeValueAsString(
            SubmitGameResultRequest(survivedSeconds = 60, mobsKilled = 1, resourcesGathered = 1, daysSurvived = 0)
        )
        mockMvc.perform(
            post("/api/leaderboard/results")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST results with token persists run and returns computed score`() {
        val (token, _) = registerUserAndToken("hero")
        val body = objectMapper.writeValueAsString(
            SubmitGameResultRequest(survivedSeconds = 120, mobsKilled = 2, resourcesGathered = 5, daysSurvived = 1)
        )

        val responseJson = mockMvc.perform(
            post("/api/leaderboard/results")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val dto = objectMapper.readValue(responseJson, GameResultDto::class.java)
        val expectedScore = ScoreCalculator.calculate(120, 2, 5, 1)
        assertEquals(expectedScore, dto.score)
        assertEquals("hero", dto.username)
        assertEquals(1, gameResultRepository.count())
    }

    @Test
    fun `POST results validates payload — negative metric rejected`() {
        val (token, _) = registerUserAndToken("hero")
        val body = objectMapper.writeValueAsString(
            SubmitGameResultRequest(survivedSeconds = -1, mobsKilled = 0, resourcesGathered = 0, daysSurvived = 0)
        )
        mockMvc.perform(
            post("/api/leaderboard/results")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `POST results enforces sanity caps`() {
        val (token, _) = registerUserAndToken("hero")
        val body = objectMapper.writeValueAsString(
            SubmitGameResultRequest(
                survivedSeconds = 10_000_000, // way above 7 days
                mobsKilled = 1, resourcesGathered = 1, daysSurvived = 0
            )
        )
        mockMvc.perform(
            post("/api/leaderboard/results")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `GET top rejects out-of-range limit`() {
        mockMvc.perform(get("/api/leaderboard/top?limit=0"))
            .andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/leaderboard/top?limit=10000"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET top is public — no token required`() {
        mockMvc.perform(get("/api/leaderboard/top"))
            .andExpect(status().isOk)
    }

    @Test
    fun `GET top returns results sorted by score descending with ranks`() {
        val (tokenA, _) = registerUserAndToken("alice")
        val (tokenB, _) = registerUserAndToken("bob")

        submit(tokenA, 100, 1, 0, 0) // score = 100 + 50 = 150
        submit(tokenB, 100, 5, 0, 0) // score = 100 + 250 = 350
        submit(tokenA, 60, 0, 10, 1) // score = 60 + 50 + 100 = 210

        val json = mockMvc.perform(get("/api/leaderboard/top?limit=10"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val entries: List<LeaderboardEntryDto> = objectMapper.readValue(
            json,
            objectMapper.typeFactory.constructCollectionType(List::class.java, LeaderboardEntryDto::class.java)
        )
        assertEquals(3, entries.size)
        assertEquals(1, entries[0].rank)
        assertEquals("bob", entries[0].username)
        assertEquals(350, entries[0].score)
        assertEquals(2, entries[1].rank)
        assertEquals(210, entries[1].score)
        assertEquals(3, entries[2].rank)
        assertEquals(150, entries[2].score)
    }

    @Test
    fun `GET me returns best run and rank`() {
        val (tokenA, _) = registerUserAndToken("alice")
        val (tokenB, _) = registerUserAndToken("bob")
        submit(tokenB, 100, 5, 0, 0) // bob: 350
        submit(tokenA, 100, 1, 0, 0) // alice: 150
        submit(tokenA, 60, 0, 10, 1) // alice: 210 ← best

        val json = mockMvc.perform(
            get("/api/leaderboard/me").header("Authorization", "Bearer $tokenA")
        ).andExpect(status().isOk).andReturn().response.contentAsString

        val me = objectMapper.readValue(json, MyLeaderboardDto::class.java)
        assertNotNull(me.best)
        assertEquals(210, me.best!!.score)
        assertEquals(2, me.rank) // позади bob (350)
        assertEquals(2, me.recent.size)
        assertTrue(me.recent.first().createdAt >= me.recent.last().createdAt)
    }

    @Test
    fun `GET me without runs returns empty payload`() {
        val (token, _) = registerUserAndToken("loner")
        val json = mockMvc.perform(
            get("/api/leaderboard/me").header("Authorization", "Bearer $token")
        ).andExpect(status().isOk).andReturn().response.contentAsString

        val me = objectMapper.readValue(json, MyLeaderboardDto::class.java)
        assertEquals(null, me.best)
        assertEquals(null, me.rank)
        assertTrue(me.recent.isEmpty())
    }

    private fun submit(token: String, seconds: Int, mobs: Int, res: Int, days: Int) {
        val body = objectMapper.writeValueAsString(
            SubmitGameResultRequest(seconds, mobs, res, days)
        )
        mockMvc.perform(
            post("/api/leaderboard/results")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated)
    }

    private fun registerUserAndToken(username: String): Pair<String, User> {
        val user = userRepository.save(User(username = username, password = "ignored", role = Role.USER))
        val token = jwtTokenProvider.generateAccessToken(user.id!!, user.username, user.role.name)
        return token to user
    }
}
