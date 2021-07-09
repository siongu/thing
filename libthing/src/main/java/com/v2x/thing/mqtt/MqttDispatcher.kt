package com.v2x.thing.mqtt

interface MqttDispatcher {
    fun dispatchMqttMessage(topic: String, msg: String)

    /**
     *   filter message for topics
     * null for all
     */
    fun filterTopics(): List<String>? {
        return null
    }
}
