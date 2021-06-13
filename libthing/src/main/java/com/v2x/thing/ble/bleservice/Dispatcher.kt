package com.v2x.thing.ble.bleservice

import com.v2x.thing.model.GGAInfo
import net.sf.marineapi.nmea.sentence.GGASentence

interface Dispatcher {
    fun dispatch(msg: String) {}
    fun dispatchData(data: ByteArray) {}
    fun dispatchMessage(topic: String, msg: String) {}
    fun dispatchGGA(gga: GGAInfo) {}
}

enum class State {
    SUCCESS, FAIL
}
