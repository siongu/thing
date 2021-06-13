package com.v2x.thing.ble.splitWriter

import com.clj.fastble.BleManager
import com.clj.fastble.exception.OtherException
import com.v2x.thing.ble.OnSendNoticeListener
import com.v2x.thing.ble.OnWriteMessageListener
import com.v2x.thing.ble.bleservice.DeviceWrapper
import com.v2x.thing.ble.execute
import com.v2x.thing.handleOnUiThread

class SplitWriter private constructor(val splitRule: SplitRule) {
    companion object {
        val INSTANCE = SplitWriter(V2XSplitRule())
    }

    fun write(
        deviceWrapper: DeviceWrapper,
        data: String,
        writeCallback: OnWriteMessageListener? = null
    ) {
        execute { writeTask(deviceWrapper, data, writeCallback) }
    }

    @Synchronized
    private fun writeTask(
        deviceWrapper: DeviceWrapper,
        data: String,
        writeCallback: OnWriteMessageListener?
    ) {
        val dataBuffer = splitRule.split(data)
        val device = deviceWrapper.bleDevice
        for (i in 0 until dataBuffer.size) {
            val dataFrame = dataBuffer[i]
            val writeSuccess = BleManager.getInstance().write(
                device,
                deviceWrapper.uuidService,
                deviceWrapper.uuidWrite,
                dataFrame
            )
            if (!writeSuccess) {
                handleOnUiThread {
                    writeCallback?.onWriteFailure(OtherException("write fail at index $i with total count ${dataBuffer.size}"))
                }
                break
            }
            handleOnUiThread {
                writeCallback?.onWriteSuccess(i, dataBuffer.size, dataFrame)
            }
            if (i == dataBuffer.size - 1) {
                handleOnUiThread { writeCallback?.onWriteComplete() }
            }
        }
    }

    @Synchronized
    fun sendNotification(
        data: String,
        onNotificationListener: OnSendNoticeListener? = null,
        task: (ByteArray) -> Boolean
    ) {
        val dataBuffer = splitRule.split(data)
        for (i in 0 until dataBuffer.size) {
            val dataFrame = dataBuffer[i]
            val sendSuccess = task(dataFrame)
            if (!sendSuccess) {
                handleOnUiThread {
                    onNotificationListener?.onSendFailure(Exception("write fail at index $i with total count ${dataBuffer.size}"))
                }
                break
            }
            handleOnUiThread {
                onNotificationListener?.onSendSuccess(i, dataBuffer.size, dataFrame)
            }
            if (i == dataBuffer.size - 1) {
                handleOnUiThread { onNotificationListener?.onSendComplete(data) }
            }
        }
    }
}