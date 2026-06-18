package com.cotf.network

import com.cotf.network.dto.AuthResponse
import com.cotf.network.dto.LoginRequest
import com.cotf.network.dto.RefreshRequest
import com.cotf.network.dto.RegisterRequest
import com.cotf.network.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/signin")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): UserDto
}
