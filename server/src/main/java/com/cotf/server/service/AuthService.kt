package com.cotf.server.service

import com.cotf.server.dto.AuthResponse
import com.cotf.server.dto.LoginRequest
import com.cotf.server.dto.RegisterRequest
import com.cotf.server.dto.UserDto
import com.cotf.server.model.Role
import com.cotf.server.model.User
import com.cotf.server.repository.UserRepository
import com.cotf.server.security.JwtTokenProvider
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username '${request.username}' is already taken")
        }

        val user = User(
            username = request.username,
            password = BCrypt.hashpw(request.password, BCrypt.gensalt()),
            role = Role.USER
        )
        val savedUser = userRepository.save(user)

        return generateAuthResponse(savedUser)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid username or password")

        if (!BCrypt.checkpw(request.password, user.password)) {
            throw IllegalArgumentException("Invalid username or password")
        }

        return generateAuthResponse(user)
    }

    fun refresh(refreshToken: String): AuthResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid or expired refresh token")
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw IllegalArgumentException("Token is not a refresh token")
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return generateAuthResponse(user)
    }

    fun getMe(userId: UUID): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return UserDto(
            id = user.id.toString(),
            username = user.username,
            role = user.role.name
        )
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val userId = user.id ?: throw IllegalStateException("User ID is null")
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId, user.username, user.role.name
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            username = user.username
        )
    }
}
