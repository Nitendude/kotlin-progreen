package com.progreen.recycling.ui.categories

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.repository.AppRepository

class CategoriesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getCategories(): List<RecyclingCategory> = repository.getCategories()
}
