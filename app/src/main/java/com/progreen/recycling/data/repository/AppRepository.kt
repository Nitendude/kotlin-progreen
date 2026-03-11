package com.progreen.recycling.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.progreen.recycling.data.model.DemoProfile
import com.progreen.recycling.data.model.LguSite
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.data.model.User
import com.progreen.recycling.data.model.UserRole
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class AppRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDemoProfiles(): List<DemoProfile> = listOf(
        DemoProfile("user_demo", "Demo User", "user@progreen.app", "123456", UserRole.USER),
        DemoProfile("lgu_demo", "Demo LGU Officer", "lgu@progreen.app", "123456", UserRole.LGU),
        DemoProfile("company_demo", "Demo Company", "company@progreen.app", "123456", UserRole.COMPANY),
        DemoProfile("admin_demo", "Demo Admin", "admin@progreen.app", "123456", UserRole.ADMIN)
    )

    fun getCategories(): List<RecyclingCategory> = listOf(
        RecyclingCategory("pet_white", "PET - WHITE", "Clear PET bottles and clean transparent containers", 12),
        RecyclingCategory("pet_colored", "PET - COLORED", "Colored PET bottles sorted by type", 11),
        RecyclingCategory("hdpe", "HDPE", "Detergent, shampoo, and milk jugs", 14),
        RecyclingCategory("ldpe", "LDPE", "Plastic bags and flexible plastic wraps", 10),
        RecyclingCategory("pe", "PE", "General polyethylene packaging", 9),
        RecyclingCategory("tin_cans", "TIN CANS", "Clean empty tin cans", 16),
        RecyclingCategory("cartons", "CARTONS", "Clean food and drink cartons", 8)
    )

    fun getAcceptedPlasticTypes(): List<String> = getCategories().map { it.name }

    fun getNearestLguSites(): List<LguSite> = listOf(
        LguSite("Quezon City Eco Hub", "Elliptical Road, Quezon City", 1.8, "Quezon City Eco Hub"),
        LguSite("Mandaluyong Materials Recovery", "Maysilo Circle, Mandaluyong", 3.1, "Mandaluyong MRF"),
        LguSite("Pasig Green Center", "Caruncho Ave, Pasig", 4.2, "Pasig Green Center"),
        LguSite("San Juan Recycling Dropoff", "N. Domingo, San Juan", 5.0, "San Juan recycling drop off")
    )

    fun getRewards(): List<RewardItem> = listOf(
        RewardItem("reward_1", "Eco Tote Bag", 120),
        RewardItem("reward_2", "Plant Seed Kit", 200),
        RewardItem("reward_3", "Reusable Bottle", 260),
        RewardItem("reward_4", "Coffee Voucher", 300),
        RewardItem("reward_5", "Green Store Gift Card", 500)
    )

    fun register(user: User): Boolean {
        persistProfile(
            name = user.name,
            email = user.email,
            password = user.password,
            role = user.role
        )
        return true
    }

    fun login(email: String, password: String): Boolean {
        val demo = getDemoProfiles().firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }
        if (demo != null) {
            persistProfile(
                name = demo.name,
                email = demo.email,
                password = demo.password,
                role = demo.role
            )
            return true
        }

        val savedEmail = prefs.getString(KEY_EMAIL, null)
        val savedPassword = prefs.getString(KEY_PASSWORD, null)

        if (savedEmail == null || savedPassword == null) {
            register(User("Eco User", email, password, UserRole.USER))
            return true
        }

        val valid = email.equals(savedEmail, ignoreCase = true) && password == savedPassword
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

    fun getUserRole(): UserRole {
        val raw = prefs.getString(KEY_ROLE, UserRole.USER.name) ?: UserRole.USER.name
        return try {
            UserRole.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            UserRole.USER
        }
    }

    fun getProfileQrPayload(): String {
        val role = getUserRole().name
        val userId = getUserEmail().lowercase(Locale.getDefault()).replace("@", "_").replace(".", "_")
        return "PROGREEN|$userId|${getUserName()}|${getUserEmail()}|$role"
    }

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

    private fun persistProfile(name: String, email: String, password: String, role: UserRole) {
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_ROLE, role.name)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
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
        private const val KEY_ROLE = "role"
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
