package com.progreen.recycleapp.model

data class User(
    val name: String,
    val email: String,
    var points: Int = 0
)
