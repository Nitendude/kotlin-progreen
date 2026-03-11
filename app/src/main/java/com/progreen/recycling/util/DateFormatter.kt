package com.progreen.recycling.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    fun format(timestamp: Long): String = formatter.format(Date(timestamp))
}
