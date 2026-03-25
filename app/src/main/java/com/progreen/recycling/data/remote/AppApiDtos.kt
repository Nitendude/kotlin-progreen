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

data class RoleApplicationRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("application_type")
    val applicationType: String,
    @SerializedName("organization_name")
    val organizationName: String,
    @SerializedName("office_address")
    val officeAddress: String,
    @SerializedName("contact_person")
    val contactPerson: String,
    @SerializedName("contact_email")
    val contactEmail: String,
    @SerializedName("document_name")
    val documentName: String? = null,
    @SerializedName("document_base64")
    val documentBase64: String? = null
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

data class RemoteRedemptionHistoryItem(
    @SerializedName("reward_title")
    val rewardTitle: String,
    @SerializedName("provider_name")
    val providerName: String? = null,
    @SerializedName("redeem_code")
    val redeemCode: String? = null,
    @SerializedName("claim_token")
    val claimToken: String? = null,
    val status: String,
    @SerializedName("points_spent")
    val pointsSpent: Int,
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

data class ClaimRedemptionRequest(
    @SerializedName("claim_token")
    val claimToken: String
)

data class AdminUpdateAccountRequest(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("approval_status")
    val approvalStatus: String
)

data class AdminReviewApplicationRequest(
    @SerializedName("application_id")
    val applicationId: Long,
    val decision: String
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
    @SerializedName("claim_token")
    val claimToken: String? = null,
    @SerializedName("new_points")
    val newPoints: Int
)

data class RemoteCompanyDashboard(
    val stats: RemoteCompanyStats,
    @SerializedName("recent_redemptions")
    val recentRedemptions: List<RemoteCompanyRedemption>
)

data class RemoteCompanyStats(
    @SerializedName("active_campaigns")
    val activeCampaigns: Int,
    @SerializedName("total_redemptions")
    val totalRedemptions: Int,
    @SerializedName("pending_claims")
    val pendingClaims: Int
)

data class RemoteCompanyRedemption(
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("reward_title")
    val rewardTitle: String,
    @SerializedName("claim_token")
    val claimToken: String,
    val status: String,
    val timestamp: Long
)

data class RemoteAdminDashboard(
    val stats: RemoteAdminStats,
    @SerializedName("pending_accounts")
    val pendingAccounts: List<RemotePendingAccount>,
    @SerializedName("pending_applications")
    val pendingApplications: List<RemoteRoleApplication>
)

data class RemoteAdminStats(
    @SerializedName("users_count")
    val usersCount: Int,
    @SerializedName("lgus_count")
    val lgusCount: Int,
    @SerializedName("companies_count")
    val companiesCount: Int,
    @SerializedName("pending_count")
    val pendingCount: Int
)

data class RemotePendingAccount(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("approval_status")
    val approvalStatus: String,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("created_at")
    val createdAt: Long
)

data class RemoteClaimValidationResult(
    val message: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_email")
    val userEmail: String,
    @SerializedName("reward_title")
    val rewardTitle: String
)

data class RemoteRoleApplication(
    val id: Long,
    @SerializedName("application_type")
    val applicationType: String,
    @SerializedName("organization_name")
    val organizationName: String,
    @SerializedName("office_address")
    val officeAddress: String,
    @SerializedName("contact_person")
    val contactPerson: String,
    @SerializedName("contact_email")
    val contactEmail: String,
    @SerializedName("document_name")
    val documentName: String? = null,
    val status: String,
    @SerializedName("applicant_name")
    val applicantName: String,
    @SerializedName("applicant_email")
    val applicantEmail: String,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("created_at")
    val createdAt: Long
)
