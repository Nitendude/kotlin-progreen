package com.progreen.recycleapp.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.progreen.recycleapp.model.RedemptionRecord
import com.progreen.recycleapp.model.RecyclingSubmission
import com.progreen.recycleapp.model.User

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    fun getUser(): User? {
        val raw = prefs.getString(KEY_USER, null) ?: return null
        return gson.fromJson(raw, User::class.java)
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun saveSubmissions(items: List<RecyclingSubmission>) {
        prefs.edit().putString(KEY_SUBMISSIONS, gson.toJson(items)).apply()
    }

    fun getSubmissions(): List<RecyclingSubmission> {
        val raw = prefs.getString(KEY_SUBMISSIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<RecyclingSubmission>>() {}.type
        return gson.fromJson(raw, type)
    }

    fun saveRedemptions(items: List<RedemptionRecord>) {
        prefs.edit().putString(KEY_REDEMPTIONS, gson.toJson(items)).apply()
    }

    fun getRedemptions(): List<RedemptionRecord> {
        val raw = prefs.getString(KEY_REDEMPTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<RedemptionRecord>>() {}.type
        return gson.fromJson(raw, type)
    }

    fun clearSession() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    companion object {
        private const val PREF_NAME = "recycle_rewards_prefs"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_USER = "user"
        private const val KEY_SUBMISSIONS = "submissions"
        private const val KEY_REDEMPTIONS = "redemptions"
    }
}
