package com.v2x.thing.ble

interface OnNotifyListener {
    fun onNotifySuccess()
    fun onNotifyFailure(exception: Exception?)
}