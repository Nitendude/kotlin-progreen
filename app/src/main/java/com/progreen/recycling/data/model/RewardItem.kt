package com.progreen.recycling.data.model

data class RewardItem(
    val id: String,
    val title: String,
    val costPoints: Int,
    val provider: String? = null,
    val rewardType: String = "Item",
    val description: String? = null,
    val imageUrl: String? = null,
    val imageBase64: String? = null,
    val redeemCode: String? = null
)
