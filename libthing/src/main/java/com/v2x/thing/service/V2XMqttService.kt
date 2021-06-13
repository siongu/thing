package com.v2x.thing.service

import java.util.*

/**
 */
class V2XMqttService : BaseMqttService() {
    private val serverUri = "tcp://kmmnn.top:1885" //服务器地址（协议+地址+端口号）
    override fun getInfo(): MqBaseInfo {
        val clientId = UUID.randomUUID().toString()
        println("clientId:$clientId")
        return MqBaseInfo(
            serverUri = serverUri,
            clientId = clientId,
            publishTopics = arrayListOf(
//                "gps-src-data/tk1306",
                "gps-src-data/cp200"
            ),
            userName = "admin",
            password = "password"
        )
    }
}
