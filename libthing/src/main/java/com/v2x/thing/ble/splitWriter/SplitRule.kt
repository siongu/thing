package com.v2x.thing.ble.splitWriter

import java.util.*

interface SplitRule {
    fun split(source: String): LinkedList<ByteArray>
}