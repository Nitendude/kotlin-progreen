package com.progreen.recycleapp.data.repository

import com.progreen.recycleapp.data.local.PrefsManager
import com.progreen.recycleapp.model.HistoryItem
import com.progreen.recycleapp.model.RedemptionRecord
import com.progreen.recycleapp.model.RecyclingCategory
import com.progreen.recycleapp.model.RecyclingSubmission
import com.progreen.recycleapp.model.RewardItem

class RecyclingRepository(private val prefsManager: PrefsManager) {

    fun getCategories(): List<RecyclingCategory> {
        return listOf(
            RecyclingCategory("plastic", "Plastic", 12, "Bottles / Containers"),
            RecyclingCategory("paper", "Paper", 8, "Books / Cartons"),
            RecyclingCategory("glass", "Glass", 10, "Bottles / Jars"),
            RecyclingCategory("metal", "Metal", 16, "Cans / Scrap"),
            RecyclingCategory("electronics", "Electronics", 25, "E-Waste")
        )
    }

    fun getRewards(): List<RewardItem> {
        return listOf(
            RewardItem("rw1", "Coffee Voucher", "$5 cafe coupon", 120),
            RewardItem("rw2", "Eco Tote Bag", "Reusable premium tote", 220),
            RewardItem("rw3", "Transit Card Load", "Public transport credits", 300),
            RewardItem("rw4", "Plant Kit", "Home herb starter kit", 420)
        )
    }

    fun submitRecyclable(category: RecyclingCategory, weightKg: Double): Result<Int> {
        if (weightKg <= 0.0) {
            return Result.failure(IllegalArgumentException("Weight must be greater than 0"))
        }

        val user = prefsManager.getUser() ?: return Result.failure(IllegalStateException("User not found"))
        val points = (category.pointsPerKg * weightKg).toInt()

        val updatedSubmissions = prefsManager.getSubmissions().toMutableList().apply {
            add(
                RecyclingSubmission(
                    id = System.currentTimeMillis(),
                    categoryName = category.name,
                    weightKg = weightKg,
                    earnedPoints = points,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        prefsManager.saveSubmissions(updatedSubmissions)

        user.points += points
        prefsManager.saveUser(user)

        return Result.success(points)
    }

    fun redeemReward(reward: RewardItem): Result<Int> {
        val user = prefsManager.getUser() ?: return Result.failure(IllegalStateException("User not found"))

        if (user.points < reward.costPoints) {
            return Result.failure(IllegalArgumentException("Not enough points"))
        }

        user.points -= reward.costPoints
        prefsManager.saveUser(user)

        val redemptions = prefsManager.getRedemptions().toMutableList().apply {
            add(
                RedemptionRecord(
                    id = System.currentTimeMillis(),
                    rewardName = reward.name,
                    pointsSpent = reward.costPoints,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        prefsManager.saveRedemptions(redemptions)

        return Result.success(user.points)
    }

    fun getCurrentPoints(): Int = prefsManager.getUser()?.points ?: 0

    fun getCurrentUserName(): String = prefsManager.getUser()?.name ?: "Eco User"

    fun getCurrentUserEmail(): String = prefsManager.getUser()?.email ?: "-"

    fun getHistory(): List<HistoryItem> {
        val submissions = prefsManager.getSubmissions().map {
            HistoryItem(
                title = "Recycled ${it.categoryName}",
                meta = "${it.weightKg} kg",
                pointsDelta = it.earnedPoints,
                timestamp = it.timestamp
            )
        }

        val redemptions = prefsManager.getRedemptions().map {
            HistoryItem(
                title = "Redeemed ${it.rewardName}",
                meta = "Store reward",
                pointsDelta = -it.pointsSpent,
                timestamp = it.timestamp
            )
        }

        return (submissions + redemptions).sortedByDescending { it.timestamp }
    }
}
