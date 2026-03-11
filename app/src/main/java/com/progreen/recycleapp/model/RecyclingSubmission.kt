package com.progreen.recycleapp.model

data class RecyclingSubmission(
    val id: Long,
    val categoryName: String,
    val weightKg: Double,
    val earnedPoints: Int,
    val timestamp: Long
)
