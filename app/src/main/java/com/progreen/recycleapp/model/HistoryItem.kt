package com.progreen.recycleapp.model

data class HistoryItem(
    val title: String,
    val meta: String,
    val pointsDelta: Int,
    val timestamp: Long
)
