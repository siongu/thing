package com.common.stdlib.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.text.TextUtils
import java.lang.ref.WeakReference


object LocationUtil {
    var context: WeakReference<Context>? = null
    fun init(context: Context) {
        this.context = WeakReference(context)
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context? = null): Location? {
        if (context == null && this.context?.get() == null) {
            throw IllegalArgumentException("retrieve location must be call init() first or pass a valid context.")
        }
        val ctx = context?.applicationContext ?: this.context?.get()
        //获取地理位置管理器
        val mLocationManager = ctx!!.applicationContext
            .getSystemService(LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.getAccuracy()) {
                bestLocation = l
            }
        }
        // 在一些手机5.0(api21)获取为空后，采用下面去兼容获取。
        if (bestLocation == null) {
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE
            criteria.isAltitudeRequired = false
            criteria.isBearingRequired = false
            criteria.isCostAllowed = true
            criteria.powerRequirement = Criteria.POWER_LOW
            val provider = mLocationManager.getBestProvider(criteria, true)
            if (!TextUtils.isEmpty(provider)) {
                bestLocation = mLocationManager.getLastKnownLocation(provider!!)
            }
        }
        println("location[lat:${bestLocation?.latitude},lon:${bestLocation?.longitude}]")
        return bestLocation
    }
}