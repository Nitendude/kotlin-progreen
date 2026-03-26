package com.progreen.recycling.data.model

data class PendingApplication(
    val id: Long,
    val applicationType: String,
    val organizationName: String,
    val officeAddress: String,
    val contactPerson: String,
    val contactEmail: String,
    val documentName: String?,
    val status: String,
    val applicantName: String,
    val applicantEmail: String,
    val isVerified: Boolean,
    val createdAt: Long
)
