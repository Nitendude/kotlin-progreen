package com.progreen.recycleapp.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progreen.recycleapp.data.repository.AuthRepository
import com.progreen.recycleapp.data.repository.RecyclingRepository
import com.progreen.recycleapp.viewmodel.AuthViewModel
import com.progreen.recycleapp.viewmodel.MainViewModel

class AppViewModelFactory(
    private val authRepository: AuthRepository,
    private val recyclingRepository: RecyclingRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepository) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(recyclingRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
