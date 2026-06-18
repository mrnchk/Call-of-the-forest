package com.cotf.network.dto

import com.google.gson.annotations.SerializedName

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
