package com.progreen.recycling.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.data.model.User
import org.json.JSONArray
import org.json.JSONObject

class AppRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCategories(): List<RecyclingCategory> = listOf(
        RecyclingCategory("plastic", "Plastic", "Bottles, food containers, clean packaging", 10),
        RecyclingCategory("paper", "Paper", "Newspapers, cardboard, paper bags", 8),
        RecyclingCategory("glass", "Glass", "Bottles and jars, sorted by color", 12),
        RecyclingCategory("metal", "Metal", "Aluminum cans and mixed metal scraps", 15),
        RecyclingCategory("electronics", "Electronics", "Old gadgets, cables, accessories", 25)
    )

    fun getRewards(): List<RewardItem> = listOf(
        RewardItem("reward_1", "Eco Tote Bag", 120),
        RewardItem("reward_2", "Plant Seed Kit", 200),
        RewardItem("reward_3", "Reusable Bottle", 260),
        RewardItem("reward_4", "Coffee Voucher", 300),
        RewardItem("reward_5", "Green Store Gift Card", 500)
    )

    fun register(user: User): Boolean {
        prefs.edit()
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PASSWORD, user.password)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
        return true
    }

    fun login(email: String, password: String): Boolean {
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        val savedPassword = prefs.getString(KEY_PASSWORD, null)

        if (savedEmail == null || savedPassword == null) {
            register(User("Eco User", email, password))
            return true
        }

        val valid = email == savedEmail && password == savedPassword
        if (valid) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        }
        return valid
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun getUserName(): String = prefs.getString(KEY_NAME, "Eco User") ?: "Eco User"

    fun getUserEmail(): String = prefs.getString(KEY_EMAIL, "user@progreen.app") ?: "user@progreen.app"

    fun getPoints(): Int = prefs.getInt(KEY_POINTS, 0)

    fun getSubmissionHistory(): List<Submission> {
        val raw = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val array = JSONArray(raw)
        val list = mutableListOf<Submission>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(
                Submission(
                    categoryId = item.getString("categoryId"),
                    categoryName = item.getString("categoryName"),
                    weightKg = item.getDouble("weightKg"),
                    pointsEarned = item.getInt("pointsEarned"),
                    notes = item.getString("notes"),
                    timestamp = item.getLong("timestamp")
                )
            )
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun submitRecyclable(categoryId: String, weightKg: Double, notes: String): Submission {
        val category = getCategories().first { it.id == categoryId }
        val pointsEarned = (weightKg * category.pointsPerKg).toInt()

        val submission = Submission(
            categoryId = category.id,
            categoryName = category.name,
            weightKg = weightKg,
            pointsEarned = pointsEarned,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        val oldHistory = getSubmissionHistory()
        val updated = listOf(submission) + oldHistory
        persistHistory(updated)

        val updatedPoints = getPoints() + pointsEarned
        prefs.edit().putInt(KEY_POINTS, updatedPoints).apply()

        return submission
    }

    fun redeemReward(rewardId: String): Result<Unit> {
        val reward = getRewards().first { it.id == rewardId }
        val current = getPoints()

        return if (current >= reward.costPoints) {
            prefs.edit().putInt(KEY_POINTS, current - reward.costPoints).apply()
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Not enough points"))
        }
    }

    private fun persistHistory(history: List<Submission>) {
        val array = JSONArray()
        history.forEach { item ->
            array.put(
                JSONObject()
                    .put("categoryId", item.categoryId)
                    .put("categoryName", item.categoryName)
                    .put("weightKg", item.weightKg)
                    .put("pointsEarned", item.pointsEarned)
                    .put("notes", item.notes)
                    .put("timestamp", item.timestamp)
            )
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "progreen_prefs"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_POINTS = "points"
        private const val KEY_HISTORY = "history"

        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
