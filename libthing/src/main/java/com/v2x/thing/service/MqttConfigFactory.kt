package com.v2x.thing.service

interface MqttConfigFactory {
    fun create(): MqttConfig
}