package com.v2x.thing.ble.bleparser

import com.v2x.thing.ble.bleservice.ServiceType

class BasicParser private constructor(serviceType: ServiceType) : AbstractParser(serviceType) {
    companion object {
        fun getInstance(serviceType: ServiceType): AbstractParser {
            return BasicParser(serviceType)
        }
    }
}