package com.progreen.recyclingapp.data.repository

import com.progreen.recyclingapp.data.local.PrefsManager
import com.progreen.recyclingapp.model.Category
import com.progreen.recyclingapp.model.HistoryEntry
import com.progreen.recyclingapp.model.Reward
import kotlin.math.roundToInt

class RecyclingRepository(private val prefsManager: PrefsManager) {

    fun getCategories(): List<Category> = listOf(
        Category("plastic", "Plastic", "Bottles, containers, and packaging", 12),
        Category("paper", "Paper", "Newspapers, cartons, office paper", 8),
        Category("glass", "Glass", "Bottles and glass jars", 10),
        Category("metal", "Metal", "Cans, tins, aluminum and steel", 15),
        Category("electronics", "Electronics", "Small e-waste and accessories", 20)
    )

    fun getRewards(): List<Reward> = listOf(
        Reward("1", "Eco Tote Bag", "Reusable premium tote", 120),
        Reward("2", "Coffee Voucher", "Partner cafe voucher", 200),
        Reward("3", "Plant Kit", "Starter indoor plant kit", 280),
        Reward("4", "Transit Card Load", "Public transport credit", 350)
    )

    fun submitRecyclable(category: Category, quantityKg: Double): Int {
        val earnedPoints = (quantityKg * category.pointsPerKg).roundToInt().coerceAtLeast(1)
        prefsManager.addPoints(earnedPoints)
        prefsManager.addHistory(
            HistoryEntry(
                id = System.currentTimeMillis().toString(),
                title = "Recycled ${category.name}",
                details = "${"%.2f".format(quantityKg)} kg submitted",
                pointsChange = earnedPoints,
                timestamp = System.currentTimeMillis(),
                isRewardRedemption = false
            )
        )
        return earnedPoints
    }

    fun redeemReward(reward: Reward): Boolean {
        val success = prefsManager.deductPoints(reward.cost)
        if (success) {
            prefsManager.addHistory(
                HistoryEntry(
                    id = System.currentTimeMillis().toString(),
                    title = "Redeemed ${reward.name}",
                    details = reward.description,
                    pointsChange = -reward.cost,
                    timestamp = System.currentTimeMillis(),
                    isRewardRedemption = true
                )
            )
        }
        return success
    }

    fun getPoints(): Int = prefsManager.getPoints()

    fun getHistory(): List<HistoryEntry> = prefsManager.getHistory()
}
