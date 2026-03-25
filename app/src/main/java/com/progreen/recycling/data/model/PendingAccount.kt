package com.progreen.recycling.data.model

data class PendingAccount(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val approvalStatus: String,
    val isVerified: Boolean,
    val createdAt: Long
)
