package com.progreen.recycling.data.model

data class RewardItem(
    val id: String,
    val title: String,
    val costPoints: Int,
    val provider: String? = null
)
