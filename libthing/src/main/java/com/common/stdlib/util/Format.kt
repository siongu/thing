package com.common.stdlib.util

import android.annotation.SuppressLint
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun utc(): String {
    val utc = SimpleDateFormat("hhmmss.ss").format(Date())
    println("utc:$utc")
    return utc
}

fun simpleDateStr(): String {
    val utc = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    println("utc:$utc")
    return utc
}