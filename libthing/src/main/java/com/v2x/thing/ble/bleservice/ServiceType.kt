package com.v2x.thing.ble.bleservice

enum class ServiceType(val uuid: String, val desc: String) {
    GXX(UUID_SERVICE_GXX.toString(), "gxx stand"),
    TK1306(UUID_SERVICE_MENG_XIN_TK1306.toString(), "tk1306"),
    CP200(UUID_SERVICE_MENG_XIN_CP200.toString(), "cp200")
}