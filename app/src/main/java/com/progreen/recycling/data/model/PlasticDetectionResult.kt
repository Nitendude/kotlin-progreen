package com.progreen.recycling.data.model

data class PlasticDetectionResult(
    val plasticType: String,
    val donatable: Boolean,
    val confidence: Int,
    val reason: String
)
