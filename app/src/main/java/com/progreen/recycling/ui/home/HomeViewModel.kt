package com.progreen.recycling.ui.home

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.repository.AppRepository

class HomeViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getPoints(): Int = repository.getPoints()
}
