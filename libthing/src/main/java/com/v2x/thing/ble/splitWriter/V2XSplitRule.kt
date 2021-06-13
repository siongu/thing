package com.v2x.thing.ble.splitWriter

import java.util.*
import kotlin.math.ceil

class V2XSplitRule : SplitRule {
    private val metaSize = 4
    private val mtu = 20
    private val maxDataSize = mtu - metaSize
    override fun split(source: String): LinkedList<ByteArray> {
        val dataBytes = source.toByteArray(Charsets.UTF_8)
        val dataBuffer = LinkedList<ByteArray>()
//        if (dataBytes.size <= maxDataSize) {
//            dataBuffer.add(dataBytes)
//            return dataBuffer
//        }
        val count: Int = ceil(dataBytes.size / maxDataSize.toDouble()).toInt()
        var srcPos = 0
        for (i in 0 until count) {
            var dataSize = maxDataSize
            if (i == count - 1) {
                dataSize = dataBytes.size - i * maxDataSize
            }
            val temp = ByteArray(mtu)
            temp[0] = 0.toByte() // default
            temp[1] = count.toByte()// total count
            temp[2] = (i + 1).toByte()// index
            temp[3] = dataSize.toByte() // data size
            val destPos = 4
            System.arraycopy(dataBytes, srcPos, temp, destPos, dataSize)
            dataBuffer.add(temp)
            srcPos += dataSize
        }
        return dataBuffer
    }

}