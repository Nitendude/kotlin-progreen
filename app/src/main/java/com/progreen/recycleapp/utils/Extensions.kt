package com.progreen.recycleapp.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Long.toReadableDate(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    return formatter.format(Date(this))
}
