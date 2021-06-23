package com.v2x.thing.service

import android.content.ServiceConnection

@Deprecated("")
interface MqttServiceConnection : ServiceConnection {
    fun removeDispatcher(dispatcher: MqttDispatcher)
    fun sendMessage(msg: String?) {}
}