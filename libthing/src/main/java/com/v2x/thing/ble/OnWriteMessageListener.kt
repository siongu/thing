package com.v2x.thing.ble

import com.clj.fastble.callback.BleWriteCallback

abstract class OnWriteMessageListener : BleWriteCallback() {
    override fun onWriteSuccess(index: Int, total: Int, data: ByteArray?) {
    }

    abstract fun onWriteComplete()
}