package com.v2x.thing.ble.bleparser

import android.util.Log
import com.cmcc.v2x2019.cmri.newhandler.*
import com.v2x.thing.ble.bleservice.*
import com.v2x.thing.ble.execute
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class V2XStandParser private constructor(serviceType: ServiceType, dispatcher: V2XDispatcher?) :
    AbstractParser(serviceType, dispatcher) {
    companion object {
        fun getInstance(type: ServiceType, dispatcher: V2XDispatcher? = null): V2XStandParser {
            return V2XStandParser(type, dispatcher)
        }
    }

    private val byteBlockingQueue: BlockingQueue<ByteArray> = LinkedBlockingQueue()
    private val pairBlockingQueue: BlockingQueue<Pair<Int, ByteArray>> = LinkedBlockingQueue()
    private var isStarted = false

    @Synchronized
    override fun parseData(data: ByteArray) {
        try {
            byteBlockingQueue.put(data)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (!isStarted) {
            isStarted = true
            startParser()
        }
    }

    private fun startParser() {
        execute(parseTask())
        execute(dispatchTask())
    }

    /**
     *   1byte   1byte     1byte    1byte   16byte
     * +-------+--------+--------+--------+---------+
     * +  Ext  + total  + index  +  size  + data... +
     * +-------+--------+--------+--------+---------+
     *
     **/
    private fun parsePacket() {
        var baos: ByteArrayOutputStream? = null
        var sum = 0
        while (true) {
            try {
                val data = byteBlockingQueue.take()
                if (data != null) {
                    if (data.size < 4) {
                        Log.d(
                            TAG,
                            "data packet is too short,data size is ${data.size}\n data：${
                                Arrays.toString(data)
                            }"
                        )
                        baos?.close()
                        baos = null
                        continue
                    }
                    // 数据类型
                    val type = data[0].toInt()
                    // 分包总数
                    val total = data[1].toInt()
                    // 当前包编号
                    val index = data[2].toInt()
                    // 数据长度
                    val size = data[3].toInt()
                    val startPos = 4
                    if (index < 1 || total < index) {
                        val res = Arrays.toString(data)
                        Log.d(
                            TAG,
                            "data packet format is incorrect,index=$index,count=$total\n data：${res}"
                        )
                        baos?.close()
                        baos = null
                        continue
                    }
                    if (index == 1) { // data beginning ,start from index 1
                        sum = 0
                        baos = ByteArrayOutputStream()
                    }
                    if (baos == null) continue // invalid data
                    sum++
                    if (sum != index) { // may be packet loss
                        baos.close()
                        baos = null
                        continue
                    }
                    val validData = ByteArray(size)
                    System.arraycopy(data, startPos, validData, 0, size)
                    baos.write(validData)
                    if (index == total) {// data end
                        baos.flush()
                        baos.toByteArray().let { bytes ->
                            baos?.close()
                            baos = null
                            dispatchResult(Pair(type, bytes))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseTask(): Runnable {
        return Runnable { parsePacket() }
    }

    private fun dispatchResult(pair: Pair<Int, ByteArray>) {
        try {
            pairBlockingQueue.put(pair)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun dispatchTask(): Runnable {
        return Runnable {
            while (true) {
                try {
                    val pair = pairBlockingQueue.take()
                    val type = convert(pair.first)
                    println("data type = ${type.desc}")
                    val dispatcher = BleService.INSTANCE.getDispatcher(GXX) as V2XDispatcher?
                    when (type) {
                        DataType.BSM -> dispatcher?.dispatchBsm(
                            BsmHandler.handleBsmDecode(
                                pair.second
                            )
                        )
                        DataType.MAP -> dispatcher?.dispatchMap(
                            MapHandler.handleMapDecode(
                                pair.second
                            )
                        )
                        DataType.RSI -> dispatcher?.dispatchRsi(
                            RsiHandler.handleRsiDecode(
                                pair.second
                            )
                        )
                        DataType.RSM -> dispatcher?.dispatchRsm(
                            RsmHandler.handleRsmDecode(
                                pair.second
                            )
                        )
                        DataType.SPAT -> dispatcher?.dispatchSpat(
                            SpatHandler.handleSpatDecode(
                                pair.second
                            )
                        )
                        DataType.OTHER -> dispatcher?.let {
                            val result = String(pair.second, Charsets.UTF_8)
                            Log.d(TAG, "received ble data：$result")
                            it.dispatch(result)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun convert(type: Int): DataType {
        return when (type) {
            DataType.BSM.type -> DataType.BSM
            DataType.MAP.type -> DataType.MAP
            DataType.RSI.type -> DataType.RSI
            DataType.RSM.type -> DataType.RSM
            DataType.SPAT.type -> DataType.SPAT
            else -> DataType.OTHER
        }
    }

    enum class DataType(var type: Int, var desc: String) {
        BSM(0x11, "bsm"),
        MAP(0x22, "map"),
        RSI(0x33, "rsi"),
        RSM(0x44, "rsm"),
        SPAT(0x55, "spat"),
        OTHER(0x00, "other");
    }
}
