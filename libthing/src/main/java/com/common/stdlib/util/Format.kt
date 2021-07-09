package com.common.stdlib.util

import android.annotation.SuppressLint
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun utc(): String {
    val utc = SimpleDateFormat("hhmmss.sss").format(Date())
    println("utc:$utc")
    return utc
}

fun simpleDateStr(): String {
    val utc = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    println("simpleDateStr:$utc")
    return utc
}

fun simpleDateMillsStr(time: Long? = null): String {
    val utc = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(time.let { t ->
        if (t == null) {
            Date()
        } else {
            Date().apply {
                this.time = t
            }
        }
    })
    println("simpleDateMillsStr:$utc")
    return utc
}

@SuppressLint("SimpleDateFormat")
fun simpleTime(): String {
    val utc = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss").format(Date(System.currentTimeMillis()))
    println("simpleTime:$utc")
    return utc
}