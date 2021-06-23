package com.v2x.thing.service

import com.clj.fastble.data.BleDevice
import com.v2x.thing.ble.OnWriteMessageListener

@Deprecated("")
interface IMqttDispatcherHandler {
    fun register(dispatcher: MqttDispatcher)

    fun unRegister(dispatcher: MqttDispatcher)

    fun clean()

}