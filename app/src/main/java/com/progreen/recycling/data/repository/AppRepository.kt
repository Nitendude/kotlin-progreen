package com.progreen.recycling.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.progreen.recycling.BuildConfig
import com.progreen.recycling.data.model.DonationCreditOutcome
import com.progreen.recycling.data.model.AdminDashboardStats
import com.progreen.recycling.data.model.CompanyDashboardStats
import com.progreen.recycling.data.model.CompanyRedemptionRecord
import com.progreen.recycling.data.model.LguDashboardStats
import com.progreen.recycling.data.model.LguDonationRecord
import com.progreen.recycling.data.model.LguSite
import com.progreen.recycling.data.model.PendingAccount
import com.progreen.recycling.data.model.PendingApplication
import com.progreen.recycling.data.model.PlasticDetectionResult
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.data.model.RedemptionHistoryItem
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.data.model.User
import com.progreen.recycling.data.model.UserRole
import com.progreen.recycling.data.remote.ApiEnvelope
import com.progreen.recycling.data.remote.AppApiClient
import com.progreen.recycling.data.remote.GroqApiClient
import com.progreen.recycling.data.remote.GroqChatRequest
import com.progreen.recycling.data.remote.GroqContent
import com.progreen.recycling.data.remote.GroqImageUrl
import com.progreen.recycling.data.remote.GroqMessage
import com.progreen.recycling.data.remote.LoginRequest
import com.progreen.recycling.data.remote.CreditDonationRequest
import com.progreen.recycling.data.remote.AuthPayload
import com.progreen.recycling.data.remote.RegisterRequest
import com.progreen.recycling.data.remote.ResendOtpRequest
import com.progreen.recycling.data.remote.RedeemRewardRequest
import com.progreen.recycling.data.remote.ClaimRedemptionRequest
import com.progreen.recycling.data.remote.AdminUpdateAccountRequest
import com.progreen.recycling.data.remote.AdminReviewApplicationRequest
import com.progreen.recycling.data.remote.RemoteCategory
import com.progreen.recycling.data.remote.RemoteLguDashboard
import com.progreen.recycling.data.remote.RemoteLguDonationRecord
import com.progreen.recycling.data.remote.RemoteLguSite
import com.progreen.recycling.data.remote.RemoteReward
import com.progreen.recycling.data.remote.RemoteSubmission
import com.progreen.recycling.data.remote.RemoteUserProfile
import com.progreen.recycling.data.remote.ResolveQrRequest
import com.progreen.recycling.data.remote.RoleApplicationRequest
import com.progreen.recycling.data.remote.RewardCreateRequest
import com.progreen.recycling.data.remote.VerifyOtpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import java.util.Locale

class AppRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    @Volatile
    private var lastErrorMessage: String? = null

    fun getCategories(): List<RecyclingCategory> {
        val cached = loadCachedCategories()
        if (cached.isNotEmpty()) return cached
        val fetched = executeApi { AppApiClient.service.categories() }
            .getOrElse { return defaultCategories() }
            .map(::mapCategory)
        saveCachedCategories(fetched)
        return fetched
    }

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

            Result.success(parseDetectionJson(raw))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNearestLguSites(): List<LguSite> {
        val token = getToken() ?: return defaultLguSites()
        return executeApi { AppApiClient.service.lguSites(authHeader(token)) }
            .map { items -> items.map(::mapLguSite) }
            .getOrDefault(defaultLguSites())
    }

    fun getRewards(): List<RewardItem> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.rewards(authHeader(token)) }
            .map { items -> items.map(::mapReward) }
            .getOrDefault(emptyList())
    }

    fun getManagedRewardsForCurrentUser(): List<RewardItem> {
        val currentName = getUserName()
        return when (getUserRole()) {
            UserRole.LGU -> getLguManagedRewards()
            UserRole.COMPANY, UserRole.ADMIN -> getRewards().filter { it.provider == currentName }
            UserRole.USER -> emptyList()
        }
    }

    fun getLguManagedRewards(): List<RewardItem> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.lguDashboard(authHeader(token)) }
            .map { it.managedRewards.map(::mapReward) }
            .getOrDefault(emptyList())
    }

    fun addLguReward(
        title: String,
        costPoints: Int,
        rewardType: String,
        description: String,
        imageBase64: String?,
        redeemCode: String?
    ): Result<RewardItem> = addManagedReward(
        title = title,
        costPoints = costPoints,
        rewardType = rewardType,
        description = description,
        imageBase64 = imageBase64,
        redeemCode = redeemCode
    )

    fun addManagedReward(
        title: String,
        costPoints: Int,
        rewardType: String,
        description: String,
        imageBase64: String?,
        redeemCode: String?
    ): Result<RewardItem> {
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.createReward(
                authHeader(token),
                RewardCreateRequest(
                    title = title.trim(),
                    costPoints = costPoints,
                    rewardType = rewardType.trim(),
                    description = description.trim(),
                    imageBase64 = imageBase64,
                    redeemCode = redeemCode
                )
            )
        }.map(::mapReward)
    }

    fun register(user: User): Boolean {
        val result = executeApi {
            AppApiClient.service.register(
                RegisterRequest(
                    name = user.name.trim(),
                    email = user.email.trim(),
                    password = user.password,
                    role = user.role.name
                )
            )
        }
        return if (result.isSuccess) {
            lastErrorMessage = null
            prefs.edit().putString(KEY_PENDING_VERIFICATION_EMAIL, result.getOrThrow().email).apply()
            true
        } else {
            lastErrorMessage = result.exceptionOrNull()?.message
            false
        }
    }

    fun submitRoleApplication(
        name: String,
        email: String,
        password: String,
        applicationType: String,
        organizationName: String,
        officeAddress: String,
        contactPerson: String,
        contactEmail: String,
        documentName: String?,
        documentBase64: String?
    ): Boolean {
        val result = executeApi {
            AppApiClient.service.submitApplication(
                RoleApplicationRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    applicationType = applicationType.trim(),
                    organizationName = organizationName.trim(),
                    officeAddress = officeAddress.trim(),
                    contactPerson = contactPerson.trim(),
                    contactEmail = contactEmail.trim(),
                    documentName = documentName,
                    documentBase64 = documentBase64
                )
            )
        }
        return if (result.isSuccess) {
            lastErrorMessage = null
            prefs.edit().putString(KEY_PENDING_VERIFICATION_EMAIL, result.getOrThrow().email).apply()
            true
        } else {
            lastErrorMessage = result.exceptionOrNull()?.message
            false
        }
    }

    fun login(email: String, password: String): Boolean {
        val result = executeApi {
            AppApiClient.service.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            )
        }
        return if (result.isSuccess) {
            lastErrorMessage = null
            persistAuth(result.getOrThrow())
            true
        } else {
            val message = result.exceptionOrNull()?.message
            lastErrorMessage = if (message.equals("Invalid credentials", ignoreCase = true)) {
                "Incorrect Email or Password"
            } else {
                message
            }
            false
        }
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun getLastErrorMessage(): String? = lastErrorMessage

    fun getPendingVerificationEmail(): String? = prefs.getString(KEY_PENDING_VERIFICATION_EMAIL, null)

    fun verifyOtp(email: String, otp: String): Boolean {
        val result = executeApi {
            AppApiClient.service.verifyOtp(
                VerifyOtpRequest(
                    email = email.trim(),
                    otp = otp.trim()
                )
            )
        }
        return if (result.isSuccess) {
            lastErrorMessage = null
            prefs.edit().remove(KEY_PENDING_VERIFICATION_EMAIL).apply()
            persistAuth(result.getOrThrow())
            true
        } else {
            lastErrorMessage = result.exceptionOrNull()?.message
            false
        }
    }

    fun resendOtp(email: String): Boolean {
        val result = executeApi {
            AppApiClient.service.resendOtp(
                ResendOtpRequest(email.trim())
            )
        }
        return if (result.isSuccess) {
            lastErrorMessage = null
            prefs.edit().putString(KEY_PENDING_VERIFICATION_EMAIL, email.trim()).apply()
            true
        } else {
            lastErrorMessage = result.exceptionOrNull()?.message
            false
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
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
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.resolveQr(authHeader(token), ResolveQrRequest(qrPayload))
        }.map { it.name to it.email }
    }

    fun creditUserDonationFromQr(qrPayload: String, categoryId: String, weightKg: Double): Result<DonationCreditOutcome> {
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.creditDonation(
                authHeader(token),
                CreditDonationRequest(
                    qrPayload = qrPayload,
                    categoryId = categoryId,
                    weightKg = weightKg
                )
            )
        }.map {
            DonationCreditOutcome(
                userName = it.userName,
                userEmail = it.userEmail,
                pointsEarned = it.pointsEarned,
                newBalance = it.newBalance
            )
        }
    }

    fun getLguDashboardStats(): LguDashboardStats {
        val token = getToken() ?: return LguDashboardStats(0.0, 0, 0, 0)
        return executeApi { AppApiClient.service.lguDashboard(authHeader(token)) }
            .map { mapLguStats(it) }
            .getOrDefault(LguDashboardStats(0.0, 0, 0, 0))
    }

    fun getCollectionSiteCount(): Int = getNearestLguSites().size

    fun getCompanyDashboardStats(): CompanyDashboardStats {
        val token = getToken() ?: return CompanyDashboardStats(0, 0, 0)
        return executeApi { AppApiClient.service.companyDashboard(authHeader(token)) }
            .map {
                CompanyDashboardStats(
                    activeCampaigns = it.stats.activeCampaigns,
                    totalRedemptions = it.stats.totalRedemptions,
                    pendingClaims = it.stats.pendingClaims
                )
            }
            .getOrDefault(CompanyDashboardStats(0, 0, 0))
    }

    fun getCompanyRedemptionRecords(): List<CompanyRedemptionRecord> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.companyDashboard(authHeader(token)) }
            .map { dashboard ->
                dashboard.recentRedemptions.map {
                    CompanyRedemptionRecord(
                        userName = it.userName,
                        userEmail = it.userEmail,
                        rewardTitle = it.rewardTitle,
                        claimToken = it.claimToken,
                        status = it.status,
                        timestamp = it.timestamp
                    )
                }
            }
            .getOrDefault(emptyList())
    }

    fun getLguDonationRecords(limit: Int = 20): List<LguDonationRecord> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.lguDashboard(authHeader(token)) }
            .map { dashboard -> dashboard.records.map(::mapLguRecord).take(limit) }
            .getOrDefault(emptyList())
    }

    fun claimRewardToken(claimToken: String): Result<String> {
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.claimRedemption(authHeader(token), ClaimRedemptionRequest(claimToken))
        }.map { "${it.message}: ${it.rewardTitle} for ${it.userName}" }
    }

    fun getAdminDashboardStats(): AdminDashboardStats {
        val token = getToken() ?: return AdminDashboardStats(0, 0, 0, 0)
        return executeApi { AppApiClient.service.adminDashboard(authHeader(token)) }
            .map {
                AdminDashboardStats(
                    usersCount = it.stats.usersCount,
                    lgusCount = it.stats.lgusCount,
                    companiesCount = it.stats.companiesCount,
                    pendingCount = it.stats.pendingCount
                )
            }
            .getOrDefault(AdminDashboardStats(0, 0, 0, 0))
    }

    fun getPendingAccounts(): List<PendingAccount> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.adminDashboard(authHeader(token)) }
            .map { dashboard ->
                dashboard.pendingAccounts.map {
                    PendingAccount(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        approvalStatus = it.approvalStatus,
                        isVerified = it.isVerified,
                        createdAt = it.createdAt
                    )
                }
            }
            .getOrDefault(emptyList())
    }

    fun getPendingApplications(): List<PendingApplication> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.adminDashboard(authHeader(token)) }
            .map { dashboard ->
                dashboard.pendingApplications.map {
                    PendingApplication(
                        id = it.id,
                        applicationType = it.applicationType,
                        organizationName = it.organizationName,
                        officeAddress = it.officeAddress,
                        contactPerson = it.contactPerson,
                        contactEmail = it.contactEmail,
                        documentName = it.documentName,
                        status = it.status,
                        applicantName = it.applicantName,
                        applicantEmail = it.applicantEmail,
                        isVerified = it.isVerified,
                        createdAt = it.createdAt
                    )
                }
            }
            .getOrDefault(emptyList())
    }

    fun updateAccountApproval(userId: Long, approvalStatus: String): Result<Unit> {
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.adminUpdateAccount(
                authHeader(token),
                AdminUpdateAccountRequest(userId, approvalStatus)
            )
        }.map { Unit }
    }

    fun reviewRoleApplication(applicationId: Long, decision: String): Result<Unit> {
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.adminReviewApplication(
                authHeader(token),
                AdminReviewApplicationRequest(applicationId, decision)
            )
        }.map { Unit }
    }

    fun getPoints(): Int {
        refreshProfile()
        return prefs.getInt(KEY_POINTS, 0)
    }

    fun getSubmissionHistory(): List<Submission> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.history(authHeader(token)) }
            .map { items -> items.map(::mapSubmission).sortedByDescending { it.timestamp } }
            .getOrDefault(emptyList())
    }

    fun getRedemptionHistory(): List<RedemptionHistoryItem> {
        val token = getToken() ?: return emptyList()
        return executeApi { AppApiClient.service.redemptions(authHeader(token)) }
            .map { items ->
                items.map {
                    RedemptionHistoryItem(
                        rewardTitle = it.rewardTitle,
                        providerName = it.providerName,
                        redeemCode = it.redeemCode,
                        claimToken = it.claimToken,
                        status = it.status,
                        pointsSpent = it.pointsSpent,
                        timestamp = it.timestamp
                    )
                }
            }
            .getOrDefault(emptyList())
    }

    fun submitRecyclable(categoryId: String, weightKg: Double, notes: String): Submission {
        val category = getCategories().first { it.id == categoryId }
        return Submission(
            categoryId = category.id,
            categoryName = category.name,
            weightKg = weightKg,
            pointsEarned = (weightKg * category.pointsPerKg).toInt(),
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
    }

    fun redeemReward(rewardId: String): Result<String?> {
        val token = getToken() ?: return Result.failure(IllegalStateException("You must log in first"))
        return executeApi {
            AppApiClient.service.redeemReward(authHeader(token), RedeemRewardRequest(rewardId))
        }.map {
            prefs.edit().putInt(KEY_POINTS, it.newPoints).apply()
            buildString {
                if (!it.redeemCode.isNullOrBlank()) {
                    append("Code: ${it.redeemCode}")
                }
                if (!it.claimToken.isNullOrBlank()) {
                    if (isNotEmpty()) append(" | ")
                    append("Claim token: ${it.claimToken}")
                }
            }.ifBlank { null }
        }
    }

    private fun refreshProfile() {
        val token = getToken() ?: return
        executeApi { AppApiClient.service.profile(authHeader(token)) }
            .onSuccess { profile ->
                persistUserProfile(profile, token)
            }
    }

    private fun persistAuth(payload: AuthPayload) {
        persistUserProfile(payload.user, payload.token)
    }

    private fun persistUserProfile(profile: RemoteUserProfile, token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_NAME, profile.name)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_ROLE, profile.role.uppercase(Locale.getDefault()))
            .putInt(KEY_POINTS, profile.points)
            .putInt(KEY_SUBMISSION_COUNT, profile.submissionCount)
            .apply()
    }

    private fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    private fun authHeader(token: String): String = "Bearer $token"

    private fun mapCategory(item: RemoteCategory) = RecyclingCategory(
        id = item.id,
        name = item.name,
        description = item.description,
        pointsPerKg = item.pointsPerKg
    )

    private fun mapReward(item: RemoteReward) = RewardItem(
        id = item.id,
        title = item.title,
        costPoints = item.costPoints,
        provider = item.provider,
        rewardType = item.rewardType,
        description = item.description,
        imageUrl = item.imageUrl,
        imageBase64 = item.imageBase64,
        redeemCode = item.redeemCode
    )

    private fun mapSubmission(item: RemoteSubmission) = Submission(
        categoryId = item.categoryId,
        categoryName = item.categoryName,
        weightKg = item.weightKg,
        pointsEarned = item.pointsEarned,
        notes = item.notes,
        timestamp = item.timestamp
    )

    private fun mapLguSite(item: RemoteLguSite) = LguSite(
        name = item.name,
        address = item.address,
        distanceKm = item.distanceKm,
        mapQuery = item.mapQuery
    )

    private fun mapLguStats(item: RemoteLguDashboard) = LguDashboardStats(
        donatedKgTotal = item.stats.donatedKgTotal,
        pointsCreditedTotal = item.stats.pointsCreditedTotal,
        donationsCount = item.stats.donationsCount,
        rewardsCount = item.stats.rewardsCount
    )

    private fun mapLguRecord(item: RemoteLguDonationRecord) = LguDonationRecord(
        userName = item.userName,
        userEmail = item.userEmail,
        categoryName = item.categoryName,
        weightKg = item.weightKg,
        pointsEarned = item.pointsEarned,
        timestamp = item.timestamp
    )

    private fun defaultCategories(): List<RecyclingCategory> = listOf(
        RecyclingCategory("pet_white", "PET - WHITE", "Clear PET bottles and clean transparent containers", 6),
        RecyclingCategory("pet_colored", "PET - COLORED", "Colored PET bottles sorted by type", 3),
        RecyclingCategory("hdpe", "HDPE", "Detergent, shampoo, and milk jugs", 10),
        RecyclingCategory("ldpe", "LDPE", "Plastic bags and flexible plastic wraps", 1),
        RecyclingCategory("pe", "PE", "General polyethylene packaging", 5),
        RecyclingCategory("tin_cans", "TIN CANS", "Clean empty tin cans", 5),
        RecyclingCategory("cartons", "CARTONS", "Clean food and drink cartons", 1)
    )

    private fun defaultLguSites(): List<LguSite> = listOf(
        LguSite("Santo Tomas Eco Hub", "Poblacion, Santo Tomas, Batangas", 1.2, "Santo Tomas Eco Hub"),
        LguSite("Batangas City Materials Recovery", "Gov. Carpio Rd, Batangas City", 7.4, "Batangas City Materials Recovery"),
        LguSite("Tanauan Recycling Point", "A. Mabini Ave, Tanauan", 9.3, "Tanauan Recycling Point")
    )

    private fun saveCachedCategories(items: List<RecyclingCategory>) {
        val root = JSONObject()
        items.forEachIndexed { index, category ->
            root.put(
                index.toString(),
                JSONObject()
                    .put("id", category.id)
                    .put("name", category.name)
                    .put("description", category.description)
                    .put("pointsPerKg", category.pointsPerKg)
            )
        }
        prefs.edit().putString(KEY_CATEGORIES_CACHE, root.toString()).apply()
    }

    private fun loadCachedCategories(): List<RecyclingCategory> {
        val raw = prefs.getString(KEY_CATEGORIES_CACHE, null) ?: return emptyList()
        return try {
            val json = JSONObject(raw)
            json.keys().asSequence().sorted().mapNotNull { key ->
                val item = json.optJSONObject(key) ?: return@mapNotNull null
                RecyclingCategory(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    description = item.optString("description"),
                    pointsPerKg = item.optInt("pointsPerKg")
                )
            }.toList()
        } catch (_: JSONException) {
            emptyList()
        }
    }

    private fun <T> executeApi(callFactory: () -> Call<ApiEnvelope<T>>): Result<T> {
        return runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    val response = callFactory().execute()
                    if (!response.isSuccessful) {
                        val errorMessage = response.errorBody()?.string()?.let(::extractErrorMessage)
                        return@withContext Result.failure(
                            IllegalStateException(errorMessage ?: "Server error ${response.code()}")
                        )
                    }

                    val body = response.body()
                        ?: return@withContext Result.failure(IllegalStateException("Empty server response"))

                    if (!body.success || body.data == null) {
                        return@withContext Result.failure(
                            IllegalStateException(body.message ?: "Request failed")
                        )
                    }

                    Result.success(body.data)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
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

    private fun extractErrorMessage(rawContent: String): String? {
        return try {
            JSONObject(rawContent).optString("message").takeIf { it.isNotBlank() }
        } catch (_: JSONException) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "progreen_prefs"
        private const val KEY_TOKEN = "api_token"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_POINTS = "points"
        private const val KEY_SUBMISSION_COUNT = "submission_count"
        private const val KEY_CATEGORIES_CACHE = "categories_cache"
        private const val KEY_PENDING_VERIFICATION_EMAIL = "pending_verification_email"
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
