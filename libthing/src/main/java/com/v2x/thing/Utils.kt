package com.v2x.thing

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.v2x.thing.model.LatLng
import com.v2x.thing.model.SpeedInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.atan2


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

fun removeCallbacksAndMessages(token: Any? = null) {
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

fun Activity.isGPSOpen(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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

fun headingBetweenPoints(start: LatLng, end: LatLng): Double {
    val y: Double = end.latitude - start.latitude
    val x: Double = end.longitude - start.longitude
    var angle = PI / 2 - atan2(y, x)
    val degree = Math.toDegrees(angle).rm(3)
    Log.d("course", "$degree")
    return degree
}

fun speedBetweenPoints(
    start: LatLng,
    end: LatLng,
    durationInMills: Long,
): Double {
    val startLatitude = start.latitude
    val startLongitude = start.longitude
    val endLatitude = end.latitude
    val endLongitude = end.longitude
    Log.d("speed", "startLocation:[$start],endLocation:[$end]")
    val distance =
        /*LocationRuler.*/getDistance(startLatitude, startLongitude, endLatitude, endLongitude)
    var speedInMeter = -1.0
    Log.d("speed", "distance:${distance}")
    if (durationInMills <= 0 || durationInMills <= Int.MIN_VALUE) {
        Log.d("speed", "duration is invalid:$durationInMills")
        return speedInMeter
    } else {
        speedInMeter = distance * 1000.0 / durationInMills
        Log.d("speed", "speedInMeter:$speedInMeter")
    }
    val kn2meter = 0.5144444
    val speedInKn = (speedInMeter / kn2meter).rm(3)

    Log.d("speed", "distance:$distance,durationInMills:$durationInMills,speedInKn:$speedInKn")
    return speedInKn
}

private fun getDistance(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double
): Float {
    val results = FloatArray(2)
    Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)
    return results[0]
}

fun Number?.rm(scale: Int): Double {
    return if (this == null) {
        BigDecimal(0).setScale(scale, RoundingMode.HALF_UP).toDouble()
    } else {
        BigDecimal(this.toDouble()).setScale(scale, RoundingMode.HALF_UP).toDouble()
    }
}
