package com.v2x.thing

import kotlin.math.*

object LocationRuler {
    // 圆周率
    const val PI = 3.14159265358979324

    // 赤道半径(单位m)
    private const val EARTH_RADIUS = 6378137.0

    /**
     * 转化为弧度(rad)
     */
    private fun rad(d: Double): Double {
        return d * Math.PI / 180.0
    }

    /**
     * 基于googleMap中的算法得到两经纬度之间的距离,
     * 计算精度与谷歌地图的距离精度差不多，相差范围在0.2米以下
     *
     * @return 返回的距离，单位km
     */
    fun getDistance(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val radLat1 = rad(startLatitude)
        val radLat2 = rad(endLatitude)
        val a = radLat1 - radLat2
        val b = rad(startLongitude) - rad(endLongitude)
        var s = 2 * asin(
            sqrt(
                sin(a / 2).pow(2.0) + cos(radLat1) * cos(radLat2) * sin(b / 2).pow(2.0)
            )
        )
        s *= EARTH_RADIUS
        s = (s * 10000.0).roundToInt() / 10000.0
        return s
    }
}