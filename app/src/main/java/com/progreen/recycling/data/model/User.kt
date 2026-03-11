package com.progreen.recycling.data.model

data class User(
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.USER
)
