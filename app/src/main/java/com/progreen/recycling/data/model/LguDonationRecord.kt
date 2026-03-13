package com.progreen.recycling.data.model

data class LguDonationRecord(
    val userName: String,
    val userEmail: String,
    val categoryName: String,
    val weightKg: Double,
    val pointsEarned: Int,
    val timestamp: Long
)
