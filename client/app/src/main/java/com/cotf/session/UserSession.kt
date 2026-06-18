package com.cotf.session

import android.content.Context

/**
 * Хранилище сессии пользователя через SharedPreferences.
 * Хранит access/refresh JWT токены и имя пользователя.
 */
class UserSession(context: Context) {
    private val prefs = context.getSharedPreferences("cotf_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    fun saveAuth(accessToken: String, refreshToken: String, username: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USERNAME, username.trim())
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun logout() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USERNAME)
            .apply()
    }
}
