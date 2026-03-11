package com.progreen.recycleapp.data.repository

import com.progreen.recycleapp.data.local.PrefsManager
import com.progreen.recycleapp.model.User

class AuthRepository(private val prefsManager: PrefsManager) {

    fun register(name: String, email: String, password: String): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }

        val user = User(name = name.trim(), email = email.trim(), points = 0)
        prefsManager.saveUser(user)
        prefsManager.setLoggedIn(true)
        return Result.success(user)
    }

    fun login(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }

        val existing = prefsManager.getUser()
        val user = existing ?: User(name = "Eco User", email = email.trim(), points = 0)
        prefsManager.saveUser(user)
        prefsManager.setLoggedIn(true)
        return Result.success(user)
    }

    fun logout() = prefsManager.clearSession()

    fun isLoggedIn(): Boolean = prefsManager.isLoggedIn()
}
