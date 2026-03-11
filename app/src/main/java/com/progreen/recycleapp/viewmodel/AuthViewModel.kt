package com.progreen.recycleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progreen.recycleapp.data.repository.AuthRepository
import com.progreen.recycleapp.model.User

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authResult = MutableLiveData<Result<User>>()
    val authResult: LiveData<Result<User>> = _authResult

    fun login(email: String, password: String) {
        _authResult.value = authRepository.login(email, password)
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _authResult.value = Result.failure(IllegalArgumentException("Passwords do not match"))
            return
        }
        _authResult.value = authRepository.register(name, email, password)
    }
}
