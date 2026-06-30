package com.example.findlynew

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_NAME = "userName"
        const val KEY_USER_EMAIL = "userEmail"
        const val KEY_USER_UID = "userUid"
        const val KEY_HAS_COMPLETED_SURVEY = "hasCompletedSurvey"
        const val KEY_PASSWORD_HASH = "passwordHash"
    }

    fun saveLoginSession(name: String, email: String, uid: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_UID, uid)
        editor.apply()
    }

    fun saveUserName(name: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, "User Name")
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, "email@domain.com")
    }

    fun getUserUid(): String? {
        return prefs.getString(KEY_USER_UID, "")
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

    fun savePhone(email: String, phone: String) {
        val editor = prefs.edit()
        editor.putString("phone_$email", phone)
        editor.apply()
    }

    fun getPhone(email: String): String? {
        return prefs.getString("phone_$email", "")
    }

    fun isPushNotificationsEnabled(): Boolean {
        return prefs.getBoolean("push_notifications_enabled", false)
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean("push_notifications_enabled", enabled)
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

    fun saveProfilePic(email: String, url: String) {
        val editor = prefs.edit()
        editor.putString("profile_pic_$email", url)
        editor.apply()
    }

    fun getProfilePic(email: String): String? {
        return prefs.getString("profile_pic_$email", null)
    }

    fun savePasswordHash(hash: String) {
        val editor = prefs.edit()
        editor.putString(KEY_PASSWORD_HASH, hash)
        editor.apply()
    }

    fun getPasswordHash(): String? {
        return prefs.getString(KEY_PASSWORD_HASH, "")
    }

    fun logout() {
        val editor = prefs.edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_USER_UID)
        editor.remove(KEY_PASSWORD_HASH)
        editor.apply()
    }
}
