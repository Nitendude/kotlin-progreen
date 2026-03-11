package com.progreen.recycling.ui.submit

import androidx.lifecycle.ViewModel
import com.progreen.recycling.data.repository.AppRepository

class SubmitViewModel(
    private val repository: AppRepository
) : ViewModel() {

    fun getProfileQrPayload(): String = repository.getProfileQrPayload()
}
