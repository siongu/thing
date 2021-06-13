package com.v2x.thing.blescan

import com.clj.fastble.data.BleDevice

data class BluetoothState(
    val state: Int
)

data class BluetoothDeviceConnect(
    var isConnect: Boolean = false,
    var isNotifyOpen: Boolean = false,
    var bleDevice: BleDevice? = null
)

data class CheckPermissionsState(
    var isSuccess: Boolean = false
)

data class VolumeChangeEvent(val type: Int)
