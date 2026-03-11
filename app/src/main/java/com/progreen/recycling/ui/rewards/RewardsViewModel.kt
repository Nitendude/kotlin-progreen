package com.progreen.recycling.ui.rewards

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.data.repository.AppRepository

class RewardsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getRewards(): List<RewardItem> = repository.getRewards()

    fun getPoints(): Int = repository.getPoints()

    fun redeem(rewardId: String): Result<Unit> = repository.redeemReward(rewardId)
}
