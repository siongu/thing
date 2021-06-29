package com.v2x.thing.blescan.view

import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.v2x.thing.base.BaseView
import com.v2x.thing.ble.OnNotifyListener

interface BluetoothView : BaseView {
    fun stopScan()
    fun showDialog(msg: String)
}