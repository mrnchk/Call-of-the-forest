package com.cotf.server.security

import com.cotf.server.config.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date
import java.util.UUID

@Component
class JwtTokenProvider(private val jwtConfig: JwtConfig) {

    private val key by lazy {
        Keys.hmacShaKeyFor(Base64.getEncoder().encode(jwtConfig.secret.toByteArray()))
    }

    fun generateAccessToken(userId: UUID, username: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtConfig.accessExpiration)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val expiry = Date(now.time + jwtConfig.refreshExpiration)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun getUserId(token: String): UUID {
        return UUID.fromString(getClaims(token).subject)
    }

    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }
}
