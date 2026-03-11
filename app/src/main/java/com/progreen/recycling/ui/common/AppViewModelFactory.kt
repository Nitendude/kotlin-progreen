package com.progreen.recycling.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progreen.recycling.data.repository.AppRepository
import com.progreen.recycling.ui.categories.CategoriesViewModel
import com.progreen.recycling.ui.history.HistoryViewModel
import com.progreen.recycling.ui.home.HomeViewModel
import com.progreen.recycling.ui.profile.ProfileViewModel
import com.progreen.recycling.ui.rewards.RewardsViewModel
import com.progreen.recycling.ui.submit.SubmitViewModel

class AppViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> CategoriesViewModel(repository) as T
            modelClass.isAssignableFrom(SubmitViewModel::class.java) -> SubmitViewModel(repository) as T
            modelClass.isAssignableFrom(RewardsViewModel::class.java) -> RewardsViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
