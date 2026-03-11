package com.progreen.recycling.ui.history

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.data.repository.AppRepository

class HistoryViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getHistory(): List<Submission> = repository.getSubmissionHistory()
}
