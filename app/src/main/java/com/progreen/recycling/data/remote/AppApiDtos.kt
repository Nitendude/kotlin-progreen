package com.progreen.recycling.data.remote

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResendOtpRequest(
    val email: String
)

data class AuthPayload(
    val token: String,
    val user: RemoteUserProfile
)

data class VerificationStartPayload(
    val email: String,
    val message: String? = null
)

data class RemoteUserProfile(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val points: Int,
    @SerializedName("submission_count")
    val submissionCount: Int = 0
)

data class RemoteCategory(
    val id: String,
    val name: String,
    val description: String,
    @SerializedName("points_per_kg")
    val pointsPerKg: Int
)

data class RemoteReward(
    val id: String,
    val title: String,
    @SerializedName("cost_points")
    val costPoints: Int,
    val provider: String? = null,
    @SerializedName("reward_type")
    val rewardType: String = "Item",
    val description: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("image_base64")
    val imageBase64: String? = null,
    @SerializedName("redeem_code")
    val redeemCode: String? = null
)

data class RemoteSubmission(
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("weight_kg")
    val weightKg: Double,
    @SerializedName("points_earned")
    val pointsEarned: Int,
    val notes: String,
    val timestamp: Long
)

data class RemoteLguSite(
    val name: String,
    val address: String,
    @SerializedName("distance_km")
    val distanceKm: Double,
    @SerializedName("map_query")
    val mapQuery: String
)

data class ResolveQrRequest(
    @SerializedName("qr_payload")
    val qrPayload: String
)

data class CreditDonationRequest(
    @SerializedName("qr_payload")
    val qrPayload: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("weight_kg")
    val weightKg: Double
)

data class RewardCreateRequest(
    val title: String,
    @SerializedName("cost_points")
    val costPoints: Int,
    @SerializedName("reward_type")
    val rewardType: String,
    val description: String,
    @SerializedName("image_base64")
    val imageBase64: String? = null,
    @SerializedName("redeem_code")
    val redeemCode: String? = null
)

data class RedeemRewardRequest(
    @SerializedName("reward_id")
    val rewardId: String
)

data class RemoteDonationCreditOutcome(
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("points_earned")
    val pointsEarned: Int,
    @SerializedName("new_balance")
    val newBalance: Int
)

data class RemoteLguDashboard(
    val stats: RemoteLguStats,
    val records: List<RemoteLguDonationRecord>,
    @SerializedName("managed_rewards")
    val managedRewards: List<RemoteReward>
)

data class RemoteLguStats(
    @SerializedName("donated_kg_total")
    val donatedKgTotal: Double,
    @SerializedName("points_credited_total")
    val pointsCreditedTotal: Int,
    @SerializedName("donations_count")
    val donationsCount: Int,
    @SerializedName("rewards_count")
    val rewardsCount: Int
)

data class RemoteLguDonationRecord(
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("weight_kg")
    val weightKg: Double,
    @SerializedName("points_earned")
    val pointsEarned: Int,
    val timestamp: Long
)

data class RemoteQrResolution(
    val name: String,
    val email: String
)

data class RemoteRedeemResult(
    @SerializedName("redeem_code")
    val redeemCode: String? = null,
    @SerializedName("new_points")
    val newPoints: Int
)
