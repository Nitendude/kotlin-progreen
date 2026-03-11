package com.progreen.recycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progreen.recycleapp.data.repository.RecyclingRepository
import com.progreen.recycleapp.model.HistoryItem
import com.progreen.recycleapp.model.RecyclingCategory
import com.progreen.recycleapp.model.RewardItem

class MainViewModel(private val repository: RecyclingRepository) : ViewModel() {

    private val _points = MutableLiveData<Int>()
    val points: LiveData<Int> = _points

    fun refreshPoints() {
        _points.value = repository.getCurrentPoints()
    }

    fun categories(): List<RecyclingCategory> = repository.getCategories()

    fun rewards(): List<RewardItem> = repository.getRewards()

    fun submit(category: RecyclingCategory, weightKg: Double): Result<Int> {
        val result = repository.submitRecyclable(category, weightKg)
        refreshPoints()
        return result
    }

    fun redeem(reward: RewardItem): Result<Int> {
        val result = repository.redeemReward(reward)
        refreshPoints()
        return result
    }

    fun history(): List<HistoryItem> = repository.getHistory()

    fun userName(): String = repository.getCurrentUserName()

    fun userEmail(): String = repository.getCurrentUserEmail()
}
