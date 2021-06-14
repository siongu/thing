package com.v2x.thing.service

import java.util.*

/**
 */
class V2XMqttService : BaseMqttService() {
    companion object {
        private var mqConfig: MqConfig? = null
        fun setConfig(config: MqConfig) {
            mqConfig = config
        }
    }

    private val serverUri = "tcp://kmmnn.top:1885" //服务器地址（协议+地址+端口号）
    override fun getInfo(): MqConfig {
        return mqConfig ?: kotlin.run {
            val clientId = UUID.randomUUID().toString()
            println("clientId:$clientId")
            MqConfig(
                serverUri = serverUri,
                clientId = clientId,
                publishTopics = arrayListOf(
//                    "gps-src-data/tk1306",
                    "gps-src-data/cp200"
                ),
                userName = "admin",
                password = "password"
            )
        }
    }
}
