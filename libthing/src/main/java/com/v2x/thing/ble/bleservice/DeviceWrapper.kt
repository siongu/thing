package com.v2x.thing.ble.bleservice

import com.clj.fastble.data.BleDevice
import com.v2x.thing.ble.bleparser.Parser

class DeviceWrapper(
    var bleDevice: BleDevice? = null,
    var parser: Parser? = null,
    var uuidService: String = "",
    var uuidNotify: String = "",
    var uuidWrite: String = "",
) {
    override fun toString(): String {
        return StringBuilder().apply {
            this.append("deviceInfo{")
                .append("deviceName:${bleDevice?.name ?: "null"}")
                .append(",")
                .append("mac:${bleDevice?.mac}")
                .append(",")
                .append("uuidService:${uuidService}")
                .append(",")
                .append("uuidNotify:$uuidNotify")
                .append(",")
                .append("uuidWrite:$uuidWrite")
                .append(",")
                .append("parser:${parser?.javaClass?.simpleName ?: "null"}")
                .append("}")
        }.toString()
    }
}