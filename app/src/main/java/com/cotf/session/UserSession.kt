package com.cotf.session

import android.content.Context

/**
 * Хранилище сессии пользователя через SharedPreferences.
 * Пока без реальной авторизации — только сохранение имени.
 */
class UserSession(context: Context) {
    private val prefs = context.getSharedPreferences("cotf_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERNAME = "username"
    }

    fun saveUsername(name: String) {
        prefs.edit().putString(KEY_USERNAME, name.trim()).apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun clear() {
        prefs.edit().remove(KEY_USERNAME).apply()
    }
}
