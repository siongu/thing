package com.v2x.thing.ble

import java.lang.Exception

interface OnSendNoticeListener {
    fun onSendSuccess(index: Int, count: Int, data: ByteArray)
    fun onSendFailure(exception: Exception)
    fun onSendComplete(result: String?)
}