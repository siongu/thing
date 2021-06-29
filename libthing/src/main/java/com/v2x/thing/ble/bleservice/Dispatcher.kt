package com.v2x.thing.ble.bleservice

import com.cmcc.v2x2019.csae.*
import com.v2x.thing.model.NmeaInfo

interface Dispatcher {
    fun dispatchData(data: ByteArray) {}
}

interface V2XDispatcher : Dispatcher {
    fun dispatch(msg: String)
    fun dispatchBsm(bsm: BasicSafetyMessage)
    fun dispatchMap(map: MapData)
    fun dispatchRsi(rsi: RoadSideInformation)
    fun dispatchRsm(rsm: RoadsideSafetyMessage)
    fun dispatchSpat(spat: SPAT)
}

interface NmeaDispatcher : Dispatcher {
    fun dispatchNmea(nmeaInfo: NmeaInfo)
}

enum class State {
    SUCCESS, FAIL
}
