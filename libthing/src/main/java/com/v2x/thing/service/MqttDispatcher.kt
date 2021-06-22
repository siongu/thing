package com.v2x.thing.service

interface MqttDispatcher {
    fun dispatchMqttMessage(topic: String, msg: String)

    /**
     * topics for message
     * null for all
     */
    fun filterTopics(): List<String>? {
        return null
    }
}
