package com.progreen.recyclingapp.model

data class HistoryEntry(
    val id: String,
    val title: String,
    val details: String,
    val pointsChange: Int,
    val timestamp: Long,
    val isRewardRedemption: Boolean
)
