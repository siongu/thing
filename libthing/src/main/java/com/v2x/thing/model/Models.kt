package com.v2x.thing.model

data class TrackParam(
    var Token: String?,
    var MapType: String = "wgs84",
    var Imei: String = "200318120610157"
)

data class LocationInfo(
    val Item: List<Item>,
    val State: Int
)

data class Item(
    val Imei: String,
    val Status: Int,
    val Speed: String,
    val Course: String,
    val Lat: Double,
    val Lng: Double,
    val HeartTime: Long,
    val GpsTime: Long,
    val SysTime: Long,
    val Battery: Int,
    val Altitude: String,
)

data class AuthorizeParam(
    val AppKey: String = "1803baa2845745a1a3fbab",
    val AppSecret: String = "b67636123ed44e35b4e661"
)

data class AuthInfo(
    val AccessToken: String,
    val Expire: Long,
    val State: Int,
)

data class GGAInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val gpsFixQuality: Int,
    val satelliteCount: Int,
    val gpsTimeInMills: Long,
)

