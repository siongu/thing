package com.v2x.thing.ble.bleservice

import com.clj.fastble.data.BleDevice
import com.v2x.thing.ble.bleparser.Parser

data class DeviceWrapper(
    var bleDevice: BleDevice? = null,
    var parser: Parser? = null,
    var uuidService: String = "",
    var uuidNotify: String = "",
    var uuidWrite: String = ""
)