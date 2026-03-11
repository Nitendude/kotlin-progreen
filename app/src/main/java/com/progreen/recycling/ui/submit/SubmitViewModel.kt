package com.progreen.recycling.ui.submit

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.data.repository.AppRepository

class SubmitViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getCategories(): List<RecyclingCategory> = repository.getCategories()

    fun submit(categoryId: String, weightKg: Double, notes: String): Submission {
        return repository.submitRecyclable(categoryId, weightKg, notes)
    }
}
