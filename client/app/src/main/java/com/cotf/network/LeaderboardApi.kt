package com.cotf.network

import com.cotf.network.dto.GameResultDto
import com.cotf.network.dto.LeaderboardEntryDto
import com.cotf.network.dto.MyLeaderboardDto
import com.cotf.network.dto.SubmitGameResultRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LeaderboardApi {

    @POST("api/leaderboard/results")
    suspend fun submit(@Body request: SubmitGameResultRequest): Response<GameResultDto>

    @GET("api/leaderboard/top")
    suspend fun top(@Query("limit") limit: Int = 20): Response<List<LeaderboardEntryDto>>

    @GET("api/leaderboard/me")
    suspend fun me(): Response<MyLeaderboardDto>
}
