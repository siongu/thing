package com.v2x.thing.ble.bleservice

import com.clj.fastble.data.BleDevice
import com.v2x.thing.ble.OnWriteMessageListener

interface IDispatcherHandler {
    fun register(type: ServiceType, dispatcher: Dispatcher)

    fun unRegister(type: ServiceType, dispatcher: Dispatcher)

    fun clean()

    fun sendMessage(bleDevice: BleDevice?, msg: String?, callback: OnWriteMessageListener? = null)
}