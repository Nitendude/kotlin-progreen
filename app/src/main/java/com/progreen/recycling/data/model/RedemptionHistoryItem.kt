package com.progreen.recycling.data.model

data class RedemptionHistoryItem(
    val rewardTitle: String,
    val providerName: String?,
    val redeemCode: String?,
    val claimToken: String?,
    val status: String,
    val pointsSpent: Int,
    val timestamp: Long
)
