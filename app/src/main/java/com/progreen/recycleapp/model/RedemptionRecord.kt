package com.progreen.recycleapp.model

data class RedemptionRecord(
    val id: Long,
    val rewardName: String,
    val pointsSpent: Int,
    val timestamp: Long
)
