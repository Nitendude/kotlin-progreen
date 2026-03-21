package com.progreen.recycling.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AppApiService {

    @POST("register.php")
    fun register(@Body body: RegisterRequest): Call<ApiEnvelope<VerificationStartPayload>>

    @POST("login.php")
    fun login(@Body body: LoginRequest): Call<ApiEnvelope<AuthPayload>>

    @POST("verify_otp.php")
    fun verifyOtp(@Body body: VerifyOtpRequest): Call<ApiEnvelope<AuthPayload>>

    @POST("resend_otp.php")
    fun resendOtp(@Body body: ResendOtpRequest): Call<ApiEnvelope<VerificationStartPayload>>

    @GET("profile.php")
    fun profile(@Header("Authorization") authorization: String): Call<ApiEnvelope<RemoteUserProfile>>

    @GET("categories.php")
    fun categories(): Call<ApiEnvelope<List<RemoteCategory>>>

    @GET("history.php")
    fun history(@Header("Authorization") authorization: String): Call<ApiEnvelope<List<RemoteSubmission>>>

    @GET("rewards.php")
    fun rewards(@Header("Authorization") authorization: String): Call<ApiEnvelope<List<RemoteReward>>>

    @POST("redeem_reward.php")
    fun redeemReward(
        @Header("Authorization") authorization: String,
        @Body body: RedeemRewardRequest
    ): Call<ApiEnvelope<RemoteRedeemResult>>

    @POST("resolve_qr.php")
    fun resolveQr(
        @Header("Authorization") authorization: String,
        @Body body: ResolveQrRequest
    ): Call<ApiEnvelope<RemoteQrResolution>>

    @POST("credit_donation.php")
    fun creditDonation(
        @Header("Authorization") authorization: String,
        @Body body: CreditDonationRequest
    ): Call<ApiEnvelope<RemoteDonationCreditOutcome>>

    @GET("lgu_dashboard.php")
    fun lguDashboard(@Header("Authorization") authorization: String): Call<ApiEnvelope<RemoteLguDashboard>>

    @POST("create_reward.php")
    fun createReward(
        @Header("Authorization") authorization: String,
        @Body body: RewardCreateRequest
    ): Call<ApiEnvelope<RemoteReward>>

    @GET("lgu_sites.php")
    fun lguSites(@Header("Authorization") authorization: String): Call<ApiEnvelope<List<RemoteLguSite>>>
}
