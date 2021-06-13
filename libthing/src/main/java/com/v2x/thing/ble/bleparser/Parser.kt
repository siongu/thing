package com.v2x.thing.ble.bleparser

interface Parser {
    fun parseData(data: ByteArray)
}