package com.v2x.thing.service

import com.v2x.thing.model.GGAInfo
import net.sf.marineapi.nmea.sentence.GGASentence

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
