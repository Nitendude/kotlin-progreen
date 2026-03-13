package com.progreen.recycling.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.progreen.recycling.BuildConfig
import com.progreen.recycling.data.model.DemoProfile
import com.progreen.recycling.data.model.DonationCreditOutcome
import com.progreen.recycling.data.model.LguDashboardStats
import com.progreen.recycling.data.model.LguDonationRecord
import com.progreen.recycling.data.model.LguSite
import com.progreen.recycling.data.model.PlasticDetectionResult
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.data.model.User
import com.progreen.recycling.data.model.UserRole
import com.progreen.recycling.data.remote.GroqApiClient
import com.progreen.recycling.data.remote.GroqChatRequest
import com.progreen.recycling.data.remote.GroqContent
import com.progreen.recycling.data.remote.GroqImageUrl
import com.progreen.recycling.data.remote.GroqMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class AppRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDemoProfiles(): List<DemoProfile> = listOf(
        DemoProfile("user_demo", "Demo User", "user@progreen.app", "123456", UserRole.USER),
        DemoProfile("lgu_demo", "Santo Tomas Batangas", "lgu@progreen.app", "123456", UserRole.LGU),
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

    suspend fun detectPlasticWithGroq(imageDataUrl: String): Result<PlasticDetectionResult> {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Missing GROQ_API_KEY. Add it in local.properties."))
        }

        val acceptedTypes = getAcceptedPlasticTypes().joinToString(", ")
        val prompt = """
            You are a recycling assistant.
            Analyze the provided image of a recyclable material.
            Choose the closest type ONLY from this list: $acceptedTypes.
            If it does not match any accepted type, respond with plasticType as UNKNOWN.

            Respond strictly as JSON with this schema:
            {"plasticType":"PET - WHITE|PET - COLORED|HDPE|LDPE|PE|TIN CANS|CARTONS|UNKNOWN","donatable":true|false,"confidence":0-100,"reason":"short reason"}
        """.trimIndent()

        return try {
            val request = GroqChatRequest(
                model = GROQ_VISION_MODEL,
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            GroqContent(type = "text", text = prompt),
                            GroqContent(type = "image_url", imageUrl = GroqImageUrl(url = imageDataUrl))
                        )
                    )
                )
            )

            val response = GroqApiClient.service.chatCompletions(
                authorization = "Bearer $apiKey",
                body = request
            )
            val raw = response.choices.firstOrNull()?.message?.content?.let(::extractMessageText)
                ?: return Result.failure(IllegalStateException("Empty Groq response"))

            val parsed = parseDetectionJson(raw)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNearestLguSites(): List<LguSite> = listOf(
        LguSite("Santo Tomas Eco Hub", "Poblacion, Santo Tomas, Batangas", 1.2, "Santo Tomas Batangas Eco Hub"),
        LguSite("Batangas City Materials Recovery", "Gov. Carpio Rd, Batangas City", 7.4, "Batangas City MRF"),
        LguSite("Tanauan Recycling Point", "A. Mabini Ave, Tanauan", 9.3, "Tanauan recycling drop off")
    )

    fun getRewards(): List<RewardItem> {
        val base = listOf(
            RewardItem("reward_1", "Eco Tote Bag", 120, "CycleMint", "Reusable shopping tote bag"),
            RewardItem("reward_2", "Plant Seed Kit", 200, "CycleMint", "Starter seed kit for home gardening"),
            RewardItem("reward_3", "Reusable Bottle", 260, "CycleMint", "Insulated reusable water bottle"),
            RewardItem("reward_4", "Coffee Voucher", 300, "CycleMint", "Discount voucher at partner cafes", redeemCode = "COFFEE300"),
            RewardItem("reward_5", "Green Store Gift Card", 500, "CycleMint", "Gift card for eco-friendly store", redeemCode = "GREEN500")
        )
        return base + getCustomRewards()
    }

    fun getLguManagedRewards(): List<RewardItem> {
        val provider = getUserName()
        return getCustomRewards().filter { it.provider == provider }
    }

    fun addLguReward(
        title: String,
        costPoints: Int,
        description: String,
        imageUrl: String?,
        redeemCode: String?
    ): Result<RewardItem> {
        if (getUserRole() != UserRole.LGU) {
            return Result.failure(IllegalStateException("Only LGU accounts can add rewards"))
        }
        if (title.isBlank() || costPoints <= 0) {
            return Result.failure(IllegalArgumentException("Invalid reward details"))
        }

        val trimmedDescription = description.trim().ifBlank { "LGU redeemable reward" }
        val normalizedImageUrl = imageUrl?.trim()?.takeIf { it.isNotBlank() }
        if (normalizedImageUrl != null &&
            !(normalizedImageUrl.startsWith("http://", ignoreCase = true) || normalizedImageUrl.startsWith("https://", ignoreCase = true))
        ) {
            return Result.failure(IllegalArgumentException("Image URL must start with http:// or https://"))
        }

        val normalizedCode = redeemCode?.trim()?.takeIf { it.isNotBlank() }

        val item = RewardItem(
            id = "lgu_${System.currentTimeMillis()}",
            title = title.trim(),
            costPoints = costPoints,
            provider = getUserName(),
            description = trimmedDescription,
            imageUrl = normalizedImageUrl,
            redeemCode = normalizedCode
        )

        val arr = readCustomRewardsArray()
        arr.put(
            JSONObject()
                .put("id", item.id)
                .put("title", item.title)
                .put("costPoints", item.costPoints)
                .put("provider", item.provider)
                .put("description", item.description)
                .put("imageUrl", item.imageUrl)
                .put("redeemCode", item.redeemCode)
        )
        prefs.edit().putString(KEY_LGU_REWARDS, arr.toString()).apply()
        return Result.success(item)
    }

    fun getLguDashboardStats(): LguDashboardStats {
        val records = getLguDonationRecords()
        return LguDashboardStats(
            donatedKgTotal = records.sumOf { it.weightKg },
            pointsCreditedTotal = records.sumOf { it.pointsEarned },
            donationsCount = records.size,
            rewardsCount = getLguManagedRewards().size
        )
    }

    fun getLguDonationRecords(limit: Int = 20): List<LguDonationRecord> {
        val lguName = getUserName()
        val marker = "Recorded by LGU $lguName"
        val accounts = readAccountsObject()
        val records = mutableListOf<LguDonationRecord>()

        val keys = accounts.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val account = accounts.optJSONObject(key) ?: continue
            val userName = account.optString("name", "User")
            val userEmail = account.optString("email", key)
            val history = account.optJSONArray("history") ?: JSONArray()

            for (i in 0 until history.length()) {
                val item = history.optJSONObject(i) ?: continue
                val notes = item.optString("notes", "")
                if (!notes.contains(marker, ignoreCase = true)) continue

                records.add(
                    LguDonationRecord(
                        userName = userName,
                        userEmail = userEmail,
                        categoryName = item.optString("categoryName", "Unknown"),
                        weightKg = item.optDouble("weightKg", 0.0),
                        pointsEarned = item.optInt("pointsEarned", 0),
                        timestamp = item.optLong("timestamp", 0L)
                    )
                )
            }
        }

        return records.sortedByDescending { it.timestamp }.take(limit)
    }

    fun register(user: User): Boolean {
        persistProfile(
            name = user.name,
            email = user.email,
            password = user.password,
            role = user.role
        )
        ensureAccount(user.name, user.email, user.role)
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
            ensureAccount(demo.name, demo.email, demo.role)
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
            ensureAccount(getUserName(), savedEmail, getUserRole())
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
        return "CYCLEMINT|$userId|${getUserName()}|${getUserEmail()}|$role"
    }

    fun resolveUserFromQr(qrPayload: String): Result<Pair<String, String>> {
        val email = extractEmailFromQr(qrPayload)
            ?: return Result.failure(IllegalArgumentException("Invalid QR payload"))
        val account = getAccount(normalizeEmail(email))
            ?: return Result.failure(IllegalArgumentException("User account not found"))

        val role = account.optString("role", UserRole.USER.name)
        if (role != UserRole.USER.name) {
            return Result.failure(IllegalArgumentException("Scanned QR is not a user account"))
        }
        return Result.success(account.optString("name", "User") to account.optString("email", email))
    }

    fun creditUserDonationFromQr(qrPayload: String, categoryId: String, weightKg: Double): Result<DonationCreditOutcome> {
        if (getUserRole() != UserRole.LGU) {
            return Result.failure(IllegalStateException("Only LGU accounts can confirm donations"))
        }
        if (weightKg <= 0.0) {
            return Result.failure(IllegalArgumentException("Weight must be greater than zero"))
        }

        val email = extractEmailFromQr(qrPayload)
            ?: return Result.failure(IllegalArgumentException("Invalid QR payload"))
        val normalizedEmail = normalizeEmail(email)
        val accounts = readAccountsObject()
        val account = accounts.optJSONObject(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("User account not found"))

        val role = account.optString("role", UserRole.USER.name)
        if (role != UserRole.USER.name) {
            return Result.failure(IllegalArgumentException("Scanned QR is not a user account"))
        }

        val category = getCategories().firstOrNull { it.id == categoryId }
            ?: return Result.failure(IllegalArgumentException("Unknown category"))
        val pointsEarned = (weightKg * category.pointsPerKg).toInt()
        val newBalance = account.optInt("points", 0) + pointsEarned

        val historyArray = account.optJSONArray("history") ?: JSONArray()
        historyArray.put(
            JSONObject()
                .put("categoryId", category.id)
                .put("categoryName", category.name)
                .put("weightKg", weightKg)
                .put("pointsEarned", pointsEarned)
                .put("notes", "Recorded by LGU ${getUserName()}")
                .put("timestamp", System.currentTimeMillis())
        )

        account.put("points", newBalance)
        account.put("history", historyArray)
        accounts.put(normalizedEmail, account)
        writeAccountsObject(accounts)

        return Result.success(
            DonationCreditOutcome(
                userName = account.optString("name", "User"),
                userEmail = account.optString("email", email),
                pointsEarned = pointsEarned,
                newBalance = newBalance
            )
        )
    }

    fun getPoints(): Int {
        val account = getCurrentAccountJson() ?: return 0
        return account.optInt("points", 0)
    }

    fun getSubmissionHistory(): List<Submission> {
        val account = getCurrentAccountJson() ?: return emptyList()
        val array = account.optJSONArray("history") ?: JSONArray()
        return parseHistoryArray(array).sortedByDescending { it.timestamp }
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

        val account = getCurrentAccountJson() ?: JSONObject()
        val historyArray = account.optJSONArray("history") ?: JSONArray()
        historyArray.put(
            JSONObject()
                .put("categoryId", submission.categoryId)
                .put("categoryName", submission.categoryName)
                .put("weightKg", submission.weightKg)
                .put("pointsEarned", submission.pointsEarned)
                .put("notes", submission.notes)
                .put("timestamp", submission.timestamp)
        )

        account.put("points", account.optInt("points", 0) + pointsEarned)
        account.put("history", historyArray)
        saveCurrentAccountJson(account)

        return submission
    }

    fun redeemReward(rewardId: String): Result<String?> {
        val reward = getRewards().firstOrNull { it.id == rewardId }
            ?: return Result.failure(IllegalArgumentException("Reward not found"))
        val account = getCurrentAccountJson() ?: return Result.failure(IllegalStateException("User not found"))
        val current = account.optInt("points", 0)

        return if (current >= reward.costPoints) {
            account.put("points", current - reward.costPoints)
            saveCurrentAccountJson(account)
            Result.success(reward.redeemCode)
        } else {
            Result.failure(IllegalStateException("Not enough points"))
        }
    }

    private fun ensureAccount(name: String, email: String, role: UserRole) {
        val normalizedEmail = normalizeEmail(email)
        val accounts = readAccountsObject()
        val existing = accounts.optJSONObject(normalizedEmail)
        if (existing == null) {
            val legacyPoints = prefs.getInt(KEY_POINTS, 0)
            val legacyHistory = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
            accounts.put(
                normalizedEmail,
                JSONObject()
                    .put("name", name)
                    .put("email", email)
                    .put("role", role.name)
                    .put("points", legacyPoints)
                    .put("history", JSONArray(legacyHistory))
            )
            writeAccountsObject(accounts)
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

    private fun parseHistoryArray(array: JSONArray): List<Submission> {
        val list = mutableListOf<Submission>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            list.add(
                Submission(
                    categoryId = item.optString("categoryId"),
                    categoryName = item.optString("categoryName"),
                    weightKg = item.optDouble("weightKg", 0.0),
                    pointsEarned = item.optInt("pointsEarned", 0),
                    notes = item.optString("notes", ""),
                    timestamp = item.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun readAccountsObject(): JSONObject {
        val raw = prefs.getString(KEY_ACCOUNTS, "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (_: JSONException) {
            JSONObject()
        }
    }

    private fun writeAccountsObject(accounts: JSONObject) {
        prefs.edit().putString(KEY_ACCOUNTS, accounts.toString()).apply()
    }

    private fun getAccount(emailNormalized: String): JSONObject? = readAccountsObject().optJSONObject(emailNormalized)

    private fun getCurrentAccountJson(): JSONObject? {
        val email = getUserEmail()
        ensureAccount(getUserName(), email, getUserRole())
        return getAccount(normalizeEmail(email))
    }

    private fun saveCurrentAccountJson(account: JSONObject) {
        val email = normalizeEmail(getUserEmail())
        val accounts = readAccountsObject()
        accounts.put(email, account)
        writeAccountsObject(accounts)
    }

    private fun readCustomRewardsArray(): JSONArray {
        val raw = prefs.getString(KEY_LGU_REWARDS, "[]") ?: "[]"
        return try {
            JSONArray(raw)
        } catch (_: JSONException) {
            JSONArray()
        }
    }

    private fun getCustomRewards(): List<RewardItem> {
        val arr = readCustomRewardsArray()
        val rewards = mutableListOf<RewardItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val providerValue = obj.optString("provider", "")
            val provider = providerValue.takeIf { it.isNotBlank() }
            rewards.add(
                RewardItem(
                    id = obj.optString("id", "lgu_$i"),
                    title = obj.optString("title", "Reward"),
                    costPoints = obj.optInt("costPoints", 100),
                    provider = provider,
                    description = obj.optString("description", "LGU redeemable reward"),
                    imageUrl = obj.optString("imageUrl", "").takeIf { it.isNotBlank() },
                    redeemCode = obj.optString("redeemCode", "").takeIf { it.isNotBlank() }
                )
            )
        }
        return rewards
    }

    private fun extractMessageText(content: com.google.gson.JsonElement): String {
        return when {
            content.isJsonPrimitive -> content.asString
            content.isJsonArray -> {
                val parts = content.asJsonArray.mapNotNull { part ->
                    if (part.isJsonObject && part.asJsonObject.has("text")) {
                        part.asJsonObject.get("text")?.asString
                    } else {
                        null
                    }
                }
                parts.joinToString("\n")
            }
            else -> content.toString()
        }
    }

    private fun parseDetectionJson(rawContent: String): PlasticDetectionResult {
        val jsonChunk = extractJsonObject(rawContent)
        val json = JSONObject(jsonChunk)

        val type = json.optString("plasticType", "UNKNOWN")
        val normalizedType = normalizeDetectedType(type)
        val confidence = json.optInt("confidence", 0).coerceIn(0, 100)
        val reason = json.optString("reason", "No reason provided")
        val donatable = normalizedType != "UNKNOWN"

        return PlasticDetectionResult(
            plasticType = normalizedType,
            donatable = donatable,
            confidence = confidence,
            reason = reason
        )
    }

    private fun normalizeDetectedType(rawType: String): String {
        val normalized = rawType
            .uppercase(Locale.getDefault())
            .replace("_", " ")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.contains("TIN") && normalized.contains("CAN")) return "TIN CANS"
        if (normalized.contains("POLYETHYLENE") || Regex("\\bPE\\b").containsMatchIn(normalized)) return "PE"
        if (normalized.contains("PET") && normalized.contains("WHITE")) return "PET - WHITE"
        if (normalized.contains("PET") && (normalized.contains("COLOR") || normalized.contains("COLOUR"))) return "PET - COLORED"
        if (normalized.contains("HDPE")) return "HDPE"
        if (normalized.contains("LDPE")) return "LDPE"
        if (normalized.contains("CARTON")) return "CARTONS"

        return getAcceptedPlasticTypes().firstOrNull { accepted ->
            accepted.uppercase(Locale.getDefault()).replace("-", " ").replace(Regex("\\s+"), " ").trim() == normalized
        } ?: "UNKNOWN"
    }

    private fun extractJsonObject(rawContent: String): String {
        val start = rawContent.indexOf('{')
        val end = rawContent.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            throw JSONException("Groq output was not valid JSON")
        }
        return rawContent.substring(start, end + 1)
    }

    private fun extractEmailFromQr(qrPayload: String): String? {
        val trimmed = qrPayload.trim()
        val upper = trimmed.uppercase(Locale.getDefault())
        if (trimmed.contains("|") && (upper.startsWith("CYCLEMINT|") || upper.startsWith("PROGREEN|"))) {
            val parts = trimmed.split('|')
            if (parts.size >= 4) return parts[3]
        }
        val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        return emailRegex.find(trimmed)?.value
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.getDefault())

    companion object {
        private const val PREFS_NAME = "progreen_prefs"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ROLE = "role"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_POINTS = "points"
        private const val KEY_HISTORY = "history"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_LGU_REWARDS = "lgu_rewards"
        private const val GROQ_VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"

        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
