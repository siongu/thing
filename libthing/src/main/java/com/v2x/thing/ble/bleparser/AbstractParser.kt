package com.v2x.thing.ble.bleparser

import android.util.Log
import com.v2x.thing.ble.bleservice.Dispatcher
import com.v2x.thing.ble.bleservice.ServiceType
import java.nio.charset.Charset

abstract class AbstractParser(
    protected val serviceType: ServiceType,
    protected val dispatcher: Dispatcher? = null
) : Parser {
    protected val TAG = this::class.java.simpleName
    private var cs: Charset = Charset.defaultCharset()
    override fun parseData(data: ByteArray) {
        dispatcher?.dispatchData(data)
        when (cs) {
            Charsets.US_ASCII -> {
            }
            Charsets.UTF_8 -> {
            }
        }
        Log.d(
            TAG,
            """received data: [uuidService:${serviceType.uuidService},uuidNotify:${serviceType.uuidNotify}]
            |${data.contentToString()}""".trimMargin()
        )
    }

    override fun getType(): ServiceType {
        return serviceType
    }

    override fun setCharset(charset: Charset) {
        this.cs = charset
    }
}