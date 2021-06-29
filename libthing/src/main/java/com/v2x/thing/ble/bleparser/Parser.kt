package com.v2x.thing.ble.bleparser

import com.v2x.thing.ble.bleservice.Dispatcher
import com.v2x.thing.ble.bleservice.ServiceType
import java.nio.charset.Charset

interface Parser {
    fun parseData(data: ByteArray)
    fun getType(): ServiceType
    fun setCharset(charset: Charset)
}