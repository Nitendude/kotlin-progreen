package com.progreen.recycling.ui.profile

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.repository.AppRepository

class ProfileViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getName(): String = repository.getUserName()

    fun getEmail(): String = repository.getUserEmail()

    fun getPoints(): Int = repository.getPoints()

    fun getRoleLabel(): String = repository.getUserRole().name

    fun getSubmissionCount(): Int = repository.getSubmissionHistory().size

    fun logout() = repository.logout()
}
