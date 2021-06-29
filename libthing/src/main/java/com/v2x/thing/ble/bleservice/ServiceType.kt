package com.v2x.thing.ble.bleservice

sealed class ServiceType(
    val uuidService: String,
    val uuidNotify: String,
    val uuidWrite: String,
    val desc: String
)

class GenericType private constructor(
    uuidService: String,
    uuidNotify: String,
    uuidWrite: String,
    desc: String
) : ServiceType(uuidService, uuidNotify, uuidWrite, desc) {
    companion object {
        fun getInstance(
            uuidService: String,
            uuidNotify: String,
            uuidWrite: String = "",
            desc: String = "generic"
        ): GenericType {
            return GenericType(uuidService, uuidNotify, uuidWrite, desc)
        }
    }
}

abstract class SpecifiedType(
    uuidService: String,
    uuidNotify: String,
    uuidWrite: String,
    desc: String
) : ServiceType(uuidService, uuidNotify, uuidWrite, desc) {
    companion object {
        val types = mutableListOf<ServiceType>()
    }
}

object GXX : SpecifiedType(
    UUID_SERVICE_GXX.toString(),
    UUID_NOTIFY_GXX.toString(),
    UUID_WRITE_GXX.toString(),
    "gxx_stand"
) {
    init {
        types.add(this)
    }
}

object TK1306 : SpecifiedType(
    UUID_SERVICE_MENG_XIN_TK1306.toString(),
    UUID_NOTIFY_MENG_XIN_TK1306.toString(),
    UUID_WRITE_MENG_XIN_TK1306.toString(), "tk1306"
) {
    init {
        types.add(this)
    }
}

object CP200Single : SpecifiedType(
    UUID_SERVICE_MENG_XIN_CP200.toString(),
    UUID_NOTIFY_MENG_XIN_CP200.toString(),
    UUID_WRITE_MENG_XIN_CP200.toString(), "cp200_single"
) {
    init {
        types.add(this)
    }
}

object CP200Dual : SpecifiedType(
    UUID_SERVICE_MENG_XIN_CP200.toString(),
    UUID_NOTIFY_MENG_XIN_CP200.toString(),
    UUID_WRITE_MENG_XIN_CP200.toString(), "cp200_dual"
) {
    init {
        types.add(this)
    }
}

object UNKNOWN : SpecifiedType(
    "unknown_uuid_service",
    "unknown_uuid_notify",
    "unknown_uuid_write", "unknown_device"
) {
    init {
        types.add(this)
    }
}
