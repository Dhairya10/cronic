package com.renalize.android.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    val instance = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}
