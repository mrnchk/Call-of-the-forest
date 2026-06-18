package com.cotf.network

import com.cotf.session.UserSession
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * OkHttp Interceptor — добавляет JWT access token к запросам
 * и обрабатывает 401 (пытается обновить токен через refresh).
 */
class AuthInterceptor(
    private val userSession: UserSession
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Пропускаем запросы авторизации (у них нет токена)
        val isAuthRequest = originalRequest.url.encodedPath.contains("/api/auth/signin") ||
                originalRequest.url.encodedPath.contains("/api/auth/register") ||
                originalRequest.url.encodedPath.contains("/api/auth/refresh")

        val accessToken = userSession.getAccessToken()

        val request = if (!isAuthRequest && accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        // Если 401 и это не refresh-запрос — пробуем обновить токен
        if (response.code == 401 && !isAuthRequest) {
            response.close()

            val refreshToken = userSession.getRefreshToken()
            if (refreshToken != null) {
                val refreshed = refreshAccessToken(chain, refreshToken)
                if (refreshed != null) {
                    // Повторяем оригинальный запрос с новым токеном
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $refreshed")
                        .build()
                    return chain.proceed(newRequest)
                }
            }

            // Refresh не удался — разлогиниваем
            userSession.logout()
        }

        return response
    }

    private fun refreshAccessToken(chain: Interceptor.Chain, refreshToken: String): String? {
        return try {
            val requestBody = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${chain.request().url.scheme}://${chain.request().url.host}:${chain.request().url.port}/api/auth/refresh")
                .post(requestBody)
                .build()

            val response = chain.proceed(request)
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val gson = com.google.gson.Gson()
                val authResponse = gson.fromJson(body, com.cotf.network.dto.AuthResponse::class.java)
                userSession.saveAuth(authResponse.accessToken, authResponse.refreshToken, authResponse.username)
                authResponse.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
