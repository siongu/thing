package com.v2x.thing

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type


val handler = Handler(Looper.getMainLooper())

fun handleOnUiThread(task: () -> Unit) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        handler.post(task)
    } else {
        task()
    }
}

fun handleOnUiThread(task: Runnable) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        handler.post(task)
    } else {
        task.run()
    }
}

fun handleOnUiThreadDelay(task: () -> Unit, delayInMillis: Long = 0L) {
    handler.postDelayed(task, delayInMillis)
}

fun handleOnUiThreadDelay(task: Runnable, delayInMillis: Long = 0L) {
    handler.postDelayed(task, delayInMillis)
}

fun removeCallbacksAndMessages(token: Any?=null) {
    handler.removeCallbacksAndMessages(token)
}

fun sendMessage(message: Message, delayInMillis: Long = 0L) {
    if (delayInMillis <= 0) {
        handler.sendMessage(message)
    } else {
        handler.sendMessageDelayed(message, delayInMillis)
    }
}

fun isPositiveNumeric(text: String?): Boolean {
    val result: Boolean = text.let {
        val regex = Regex("([1-9]\\d*\\.?\\d*)|(0?.\\d*)")
        it?.matches(regex) ?: false
    }
    return result
}

fun Any?.toJson(): String {
    return GsonBuilder().serializeNulls().create().toJson(this ?: "null")
}

fun <T> String.fromJson(clazz: Class<T>): T {
    return Gson().fromJson(this, clazz)
}

fun <T> String.fromJson(type: Type): T {
    return Gson().fromJson(this, type)
}

//读取asset中的文件成一个data bean
inline fun <reified T> getAssetAsData(context: Context, fileName: String): T {
    val stringBuilder = StringBuilder();
    try {
        val inputStreamReader = context.assets.open(fileName);
        val bufferedReader = BufferedReader(InputStreamReader(inputStreamReader))
        var line: String? = null
        while (run {
                line = bufferedReader.readLine()
                (line)
            } != null) {
            stringBuilder.append(line);
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return Gson().fromJson(stringBuilder.toString(), T::class.java)
}

fun Activity.isGPSOpen(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}
