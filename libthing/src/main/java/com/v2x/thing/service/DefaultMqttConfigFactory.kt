package com.v2x.thing.service

import java.util.*

class DefaultMqttConfigFactory : MqttConfigFactory {
    private val serverUri = "tcp://kmmnn.top:1885" //服务器地址（协议+地址+端口号）
    override fun create(): MqttConfig {
        val clientId = UUID.randomUUID().toString()
        println("clientId:$clientId")
        return MqttConfig(
            serverUri = serverUri,
            clientId = clientId,
            subscribeTopics = arrayListOf(
                "gps-src-data/cp200"
            ),
            userName = "admin",
            password = "password"
        )
    }
}