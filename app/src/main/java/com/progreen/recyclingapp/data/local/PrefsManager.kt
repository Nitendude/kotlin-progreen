package com.progreen.recyclingapp.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.progreen.recyclingapp.model.HistoryEntry
import com.progreen.recyclingapp.model.User

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PASSWORD, user.password)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun login(email: String, password: String): Boolean {
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        val savedPassword = prefs.getString(KEY_PASSWORD, null)

        val canUseDemo = savedEmail.isNullOrBlank() && savedPassword.isNullOrBlank() &&
            email.equals("demo@recycle.com", true) && password == "123456"

        val matched = savedEmail == email && savedPassword == password
        if (matched || canUseDemo) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
            if (canUseDemo) {
                prefs.edit()
                    .putString(KEY_NAME, "Demo User")
                    .putString(KEY_EMAIL, email)
                    .putString(KEY_PASSWORD, password)
                    .apply()
            }
            return true
        }
        return false
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun getUserName(): String = prefs.getString(KEY_NAME, "Eco User") ?: "Eco User"

    fun getUserEmail(): String = prefs.getString(KEY_EMAIL, "demo@recycle.com") ?: "demo@recycle.com"

    fun getPoints(): Int = prefs.getInt(KEY_POINTS, 0)

    fun addPoints(points: Int) {
        prefs.edit().putInt(KEY_POINTS, getPoints() + points).apply()
    }

    fun deductPoints(points: Int): Boolean {
        val current = getPoints()
        if (current < points) return false
        prefs.edit().putInt(KEY_POINTS, current - points).apply()
        return true
    }

    fun getHistory(): MutableList<HistoryEntry> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<HistoryEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addHistory(entry: HistoryEntry) {
        val list = getHistory()
        list.add(0, entry)
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    companion object {
        private const val PREF_NAME = "recycle_rewards_prefs"
        private const val KEY_NAME = "key_name"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_PASSWORD = "key_password"
        private const val KEY_LOGGED_IN = "key_logged_in"
        private const val KEY_POINTS = "key_points"
        private const val KEY_HISTORY = "key_history"
    }
}
