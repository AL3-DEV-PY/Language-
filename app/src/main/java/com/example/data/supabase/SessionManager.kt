package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Profile
import com.example.data.repository.UserSession
import org.json.JSONObject

/**
 * Robust Local Session Storage using SharedPreferences.
 * Stores and restores authenticated user sessions across app restarts,
 * activity recreations, and background/foreground cycles without race conditions.
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "linguax_session_store"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_PROFILE_JSON = "profile_json"
        private const val KEY_COMPLETED_LESSONS = "completed_lesson_ids"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    fun saveAppLanguage(languageCode: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, languageCode).apply()
    }

    fun loadAppLanguageCode(): String? {
        return prefs.getString(KEY_APP_LANGUAGE, null)
    }

    fun saveCompletedLessonId(lessonId: String) {
        val current = getCompletedLessonIds().toMutableSet()
        current.add(lessonId)
        prefs.edit().putStringSet(KEY_COMPLETED_LESSONS, current).apply()
    }

    fun getCompletedLessonIds(): Set<String> {
        return prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()
    }

    fun saveSession(session: UserSession) {
        val profileJson = JSONObject().apply {
            put("id", session.profile.id)
            put("username", session.profile.username ?: "")
            put("display_name", session.profile.displayName)
            put("avatar_url", session.profile.avatarUrl ?: "")
            put("xp", session.profile.xp)
            put("coins", session.profile.coins)
            put("streak", session.profile.streak)
            put("daily_goal", session.profile.dailyGoal)
        }.toString()

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ACCESS_TOKEN, session.accessToken ?: "")
            .putString(KEY_PROFILE_JSON, profileJson)
            .apply()
    }

    fun loadSession(): UserSession? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null).takeIf { !it.isNullOrBlank() }
        val profileJsonStr = prefs.getString(KEY_PROFILE_JSON, null)

        val profile = if (!profileJsonStr.isNullOrBlank()) {
            try {
                val json = JSONObject(profileJsonStr)
                Profile(
                    id = json.optString("id", userId),
                    username = json.optString("username").takeIf { it.isNotBlank() },
                    displayName = json.optString("display_name", email.substringBefore("@")),
                    avatarUrl = json.optString("avatar_url").takeIf { it.isNotBlank() },
                    xp = json.optInt("xp", 0),
                    coins = json.optInt("coins", 0),
                    streak = json.optInt("streak", 1),
                    dailyGoal = json.optInt("daily_goal", 20)
                )
            } catch (_: Exception) {
                Profile(
                    id = userId,
                    username = email.substringBefore("@"),
                    displayName = email.substringBefore("@"),
                    xp = 0,
                    coins = 0,
                    streak = 1,
                    dailyGoal = 20
                )
            }
        } else {
            Profile(
                id = userId,
                username = email.substringBefore("@"),
                displayName = email.substringBefore("@"),
                xp = 0,
                coins = 0,
                streak = 1,
                dailyGoal = 20
            )
        }

        return UserSession(
            userId = userId,
            email = email,
            accessToken = accessToken,
            profile = profile
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
