package com.progreen.recycling.data.model

data class Submission(
    val categoryId: String,
    val categoryName: String,
    val weightKg: Double,
    val pointsEarned: Int,
    val notes: String,
    val timestamp: Long
)
