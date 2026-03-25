package com.progreen.recycling.data.model

data class CompanyRedemptionRecord(
    val userName: String,
    val userEmail: String,
    val rewardTitle: String,
    val claimToken: String,
    val status: String,
    val timestamp: Long
)
