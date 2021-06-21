package com.v2x.thing.service

import android.content.ServiceConnection

interface MqttServiceConnection : ServiceConnection {
    fun removeDispatcher(dispatcher: MqttDispatcher)
    fun sendMessage(msg: String?) {}
}