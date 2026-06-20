package com.cotf.server.security

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

/**
 * Servlet Filter — проверяет JWT для защищённых эндпоинтов.
 * Без Spring Security — простая проверка Bearer токена.
 */
@Component
class JwtAuthFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val path = httpRequest.requestURI

        // Публичные эндпоинты (без JWT)
        val publicPaths = setOf(
            "/api/auth/register",
            "/api/auth/signin",
            "/api/auth/refresh",
            "/api/leaderboard/top"
        )
        if (path in publicPaths) {
            chain.doFilter(request, response)
            return
        }

        // Для остальных — проверяем JWT
        val token = extractToken(httpRequest)
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.contentType = "application/json"
            httpResponse.writer.write("""{"error":"unauthorized","message":"Missing or invalid token"}""")
            return
        }

        // Извлекаем данные из токена и кладём в атрибуты запроса
        try {
            val claims = jwtTokenProvider.getClaims(token)
            request.setAttribute("userId", claims.subject)
            request.setAttribute("username", claims["username"] as? String ?: "")
            request.setAttribute("role", claims["role"] as? String ?: "USER")
        } catch (e: Exception) {
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.contentType = "application/json"
            httpResponse.writer.write("""{"error":"unauthorized","message":"Invalid token"}""")
            return
        }

        chain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.substring(7) else null
    }
}
