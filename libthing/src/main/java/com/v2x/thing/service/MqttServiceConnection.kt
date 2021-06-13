package com.v2x.thing.service

import android.content.ServiceConnection
import com.v2x.thing.ble.bleservice.Dispatcher

interface MqttServiceConnection : ServiceConnection {
    fun removeDispatcher(dispatcher: Dispatcher)
    fun sendMessage(msg: String?) {}
}