package com.v2x.thing.ble.bleservice

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleSmartGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.v2x.thing.ble.OnNotifyListener
import com.v2x.thing.ble.OnWriteMessageListener
import com.v2x.thing.ble.bleparser.GpsNmeaParser
import com.v2x.thing.ble.bleparser.GxxStandParser
import com.v2x.thing.ble.bleparser.Parser
import com.v2x.thing.ble.splitWriter.SplitWriter
import java.util.*
import kotlin.collections.ArrayList

class BleService private constructor() : IDispatcherHandler {
    companion object {
        val TAG = BleService::class.java.simpleName
        val INSTANCE = BleService()
        private val WRITER = SplitWriter.INSTANCE
    }

    enum class State(s: Int) {
        CONNECTED(0), DISCONNECTED(-1), NOTIFY_OPEN(1), NOTIFY_STOP(-2)
    }

    private val dispatchers = mutableMapOf<String, MutableSet<Dispatcher>>()
    private var onConnectListener: BleGattCallback? = null
    private var notifyListener: OnNotifyListener? = null
    private val connectedDevices: MutableList<DeviceWrapper> = mutableListOf()
    private val bleParsers = mutableMapOf<String, Parser>()

    init {
        bleParsers[UUID_SERVICE_GXX.toString()] = GxxStandParser.newInstance()
        bleParsers[UUID_SERVICE_MENG_XIN_TK1306.toString()] = GpsNmeaParser.newInstance(ServiceType.TK1306)
        bleParsers[UUID_SERVICE_MENG_XIN_CP200.toString()] = GpsNmeaParser.newInstance(ServiceType.CP200)
        addConnectedDeviceWrapper(
            DeviceWrapper(
                null,
                bleParsers[UUID_SERVICE_GXX.toString()],
                UUID_SERVICE_GXX.toString(),
                UUID_NOTIFY_GXX.toString()
            )
        )
        addConnectedDeviceWrapper(
            DeviceWrapper(
                null,
                bleParsers[UUID_SERVICE_MENG_XIN_TK1306.toString()],
                UUID_SERVICE_MENG_XIN_TK1306.toString(),
                UUID_NOTIFY_MENG_XIN_TK1306.toString()
            )
        )
        addConnectedDeviceWrapper(
            DeviceWrapper(
                null,
                bleParsers[UUID_SERVICE_MENG_XIN_CP200.toString()],
                UUID_SERVICE_MENG_XIN_CP200.toString(),
                UUID_NOTIFY_MENG_XIN_CP200.toString()
            )
        )
    }

    var state = State.DISCONNECTED
        private set

    fun initBle(context: Context) {
        BleManager.getInstance().init(context)
        BleManager.getInstance()
            .enableLog(true)
            .setOperateTimeout(5000)
            .setReConnectCount(3, 2000)
    }

    fun enableLog(enable: Boolean): BleService {
        BleManager.getInstance().enableLog(enable)
        return this
    }

    fun setOperateTimeout(timeout: Int): BleService {
        BleManager.getInstance().operateTimeout = timeout
        return this
    }

    fun setReConnectCount(count: Int, interval: Long): BleService {
        BleManager.getInstance().setReConnectCount(count, interval)
        return this
    }

    fun setServiceUuids(uuids: Array<UUID>): BleService {
        BleManager.getInstance().initScanRule(
            BleScanRuleConfig.Builder()
                .setServiceUuids(uuids)
                .build()
        )
        return this
    }


    fun addConnectedDeviceWrapper(deviceWrapper: DeviceWrapper) {
        connectedDevices.add(deviceWrapper)
    }

    fun getConnectedDevices(): List<DeviceWrapper> {
        return connectedDevices
    }

    fun getDispatchers(type: ServiceType): List<Dispatcher> {
        return dispatchers[type.uuid].run {
            if (this == null) ArrayList() else ArrayList(this)
        }
    }

    override fun register(type: ServiceType, dispatcher: Dispatcher) {
        var dis = dispatchers[type.uuid]
        if (dis == null) {
            dis = mutableSetOf()
            dispatchers[type.uuid] = dis
        }
        dis.add(dispatcher)
    }

    override fun unRegister(type: ServiceType, dispatcher: Dispatcher) {
        dispatchers[type.uuid]?.remove(dispatcher)
    }

    override fun clean() {
        dispatchers.clear()
    }

    fun isConnectToDevice(bleDevice: BleDevice?): Boolean {
        val connectedDevice = findDeviceWrapper(bleDevice)?.bleDevice
        return BleManager.getInstance().isConnected(connectedDevice)
    }

    override fun sendMessage(
        bleDevice: BleDevice?,
        msg: String?,
        callback: OnWriteMessageListener?
    ) {
        val connectedWrapper = findDeviceWrapper(bleDevice)
        if (connectedWrapper?.bleDevice != null && msg != null) {
            WRITER.write(connectedWrapper, msg, callback)
        } else {
            println("device not connected")
            callback?.onWriteComplete()
        }
    }

    fun setOnConnectListener(onConnectListener: BleGattCallback) {
        if (this.onConnectListener == null) {
            this.onConnectListener = onConnectListener
        }
    }

    fun setOnNotifyListener(onNotifyListener: OnNotifyListener) {
        if (this.notifyListener == null) {
            this.notifyListener = onNotifyListener
        }
    }

    fun connect(device: BleDevice?, callback: BleGattCallback? = null) {
        if (BleManager.getInstance().isConnected(device)) {
            println("设备已连接")
            return
        }
        BleManager.getInstance().connect(device, object : BleSmartGattCallback() {
            override fun onStartConnect() {
                onConnectListener?.onStartConnect()
                callback?.onStartConnect()
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                super.onDisConnected(isActiveDisConnected, device, gatt, status)
                state = State.DISCONNECTED
                onConnectListener?.onDisConnected(
                    isActiveDisConnected,
                    device,
                    gatt,
                    status
                )
                callback?.onDisConnected(
                    isActiveDisConnected,
                    device,
                    gatt,
                    status
                )
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                state = State.CONNECTED
                val deviceWrapper = DeviceWrapper(bleDevice)
                connectedDevices.add(deviceWrapper)
                onConnectListener?.onConnectSuccess(bleDevice, gatt, status)
                callback?.onConnectSuccess(bleDevice, gatt, status)
            }

            override fun onConnectFail(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                exception: BleException?
            ) {
                super.onConnectFail(bleDevice, gatt, exception)
                onConnectListener?.onConnectFail(bleDevice, gatt, exception)
                callback?.onConnectFail(bleDevice, gatt, exception)
            }

        })
    }

    private fun connectAndOpenNotify(
        device: BleDevice?,
        uuidService: String,
        uuidNotify: String,
        callback: BleGattCallback? = null
    ) {
        // 设备未连接上
        if (!BleManager.getInstance().isConnected(device)) {
            BleManager.getInstance().connect(device, object : BleSmartGattCallback() {
                override fun onStartConnect() {
                    onConnectListener?.onStartConnect()
                    callback?.onStartConnect()
                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    device: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    super.onDisConnected(isActiveDisConnected, device, gatt, status)
                    state = State.DISCONNECTED
                    onConnectListener?.onDisConnected(
                        isActiveDisConnected,
                        device,
                        gatt,
                        status
                    )
                    callback?.onDisConnected(
                        isActiveDisConnected,
                        device,
                        gatt,
                        status
                    )
                }

                override fun onConnectSuccess(
                    bleDevice: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    state = State.CONNECTED
                    connectedDevices.add(DeviceWrapper(bleDevice))
                    onConnectListener?.onConnectSuccess(bleDevice, gatt, status)
                    callback?.onConnectSuccess(bleDevice, gatt, status)
                    openNotify(bleDevice, uuidService, uuidNotify)
                }

                override fun onConnectFail(
                    bleDevice: BleDevice?,
                    gatt: BluetoothGatt?,
                    exception: BleException?
                ) {
                    super.onConnectFail(bleDevice, gatt, exception)
                    onConnectListener?.onConnectFail(bleDevice, gatt, exception)
                    callback?.onConnectFail(bleDevice, gatt, exception)
                }

            })
        } else {// 设备已连接，但还未打开通知
            if (state != State.NOTIFY_OPEN) {
                openNotify(device, uuidService, uuidNotify, null)
            }
        }
    }

    fun openNotify(
        bleDevice: BleDevice?,
        uuidService: String,
        uuidNotify: String,
        listener: OnNotifyListener? = null
    ) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            println("设备未连接上")
            listener?.onNotifyFailure(Exception("设备未连接上"))
            return
        }
        if (state == State.NOTIFY_OPEN) {
            println("通知已开启")
            listener?.onNotifySuccess()
            return
        }
        findDeviceWrapper(uuidService).apply {
            this.bleDevice = bleDevice
            this.uuidService = uuidService
            this.uuidNotify = uuidNotify
            this.parser = bleParsers[uuidService]
        }
        BleManager.getInstance()
            .notify(bleDevice, uuidService, uuidNotify, object : BleNotifyCallback() {
                val parser = getParser(uuidService)
                override fun onCharacteristicChanged(data: ByteArray?) {
                    data?.run {
                        parser?.parseData(data)
                    }
                }

                override fun onNotifyFailure(e: BleException?) {
                    notifyListener?.onNotifyFailure(Exception(e?.description))
                    listener?.onNotifyFailure(Exception(e?.description))
                }

                override fun onNotifySuccess() {
                    state = State.NOTIFY_OPEN
                    notifyListener?.onNotifySuccess()
                    listener?.onNotifySuccess()
                }
            })
    }

    fun openAvailableNotify(
        bleDevice: BleDevice?,
        onNotifyListener: OnNotifyListener?
    ) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            println("设备未连接上")
            onNotifyListener?.onNotifyFailure(Exception("设备未连接上"))
            return
        }
        if (state == State.NOTIFY_OPEN) {
            println("通知已开启")
            onNotifyListener?.onNotifySuccess()
            return
        }
        val gattServices: MutableList<BluetoothGattService>? =
            BleManager.getInstance().getBluetoothGattServices(bleDevice)
        var characteristic: BluetoothGattCharacteristic? = null
        var deviceWrapper: DeviceWrapper? = null
        gattServices?.forEach { service ->
            val uuidService = service.uuid.toString()
            if (contains(uuidService)) {
                deviceWrapper = findDeviceWrapper(uuidService).apply {
                    this.bleDevice = bleDevice
                }
                val uuidNotify = deviceWrapper?.uuidNotify
                val characteristics: MutableList<BluetoothGattCharacteristic>? =
                    service.characteristics
                characteristics?.forEach { chr ->
                    if (chr.uuid.toString() == uuidNotify || (chr.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        characteristic = chr
                        println("find support notify characteristic UUID：${chr.uuid}")
                        return@forEach
                    }
                }
                if (characteristic != null) {
                    deviceWrapper?.uuidNotify = characteristic!!.uuid.toString()
                    return@forEach
                }
            }
        }
        if (characteristic != null) {
            openNotify(
                bleDevice,
                deviceWrapper!!.uuidService,
                deviceWrapper!!.uuidNotify,
                listener = onNotifyListener
            )
        } else {
            println("not find support notification characteristic.")
        }
        Log.d(TAG, "${deviceWrapper.toString()}")
    }

    fun stopNotify(bleDevice: BleDevice?): Boolean {
        val connectedWrapper = findDeviceWrapper(bleDevice)
        return connectedWrapper?.run {
            val stop = BleManager.getInstance()
                .stopNotify(bleDevice, uuidService, uuidNotify)
            if (stop) {
                state = State.NOTIFY_STOP
            }
            stop
        } ?: false
    }

    fun findDeviceWrapper(device: BleDevice?): DeviceWrapper? {
        return connectedDevices.firstOrNull { it.bleDevice?.key == device?.key }
    }

    fun findDeviceWrapper(sid: String?): DeviceWrapper {
        return connectedDevices.find { it.uuidService == sid } ?: DeviceWrapper().apply {
            connectedDevices.add(this)
        }
    }

    private fun getParser(sid: String?): Parser? {
        return bleParsers[sid]
    }

    private fun contains(sid: String?): Boolean {
        return connectedDevices.find { it.uuidService == sid } != null
    }

}