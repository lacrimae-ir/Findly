package com.example.findlynew

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_NAME = "userName"
        const val KEY_USER_EMAIL = "userEmail"
        const val KEY_HAS_COMPLETED_SURVEY = "hasCompletedSurvey"
    }

    fun saveLoginSession(name: String, email: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, "User Name")
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, "email@domain.com")
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun hasCompletedSurvey(email: String): Boolean {
        return prefs.getBoolean(KEY_HAS_COMPLETED_SURVEY + "_" + email, false)
    }

    fun setSurveyCompleted(email: String, completed: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_HAS_COMPLETED_SURVEY + "_" + email, completed)
        editor.apply()
    }

    fun getUserPreferences(email: String): Set<String> {
        return prefs.getStringSet("preferences_$email", emptySet()) ?: emptySet()
    }

    fun saveUserPreferences(email: String, preferences: Set<String>) {
        val editor = prefs.edit()
        editor.putStringSet("preferences_$email", preferences)
        editor.apply()
    }

    fun logout() {
        val editor = prefs.edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)
        editor.apply()
    }
}
