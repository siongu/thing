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
import com.v2x.thing.ble.bleparser.*
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

    private val dispatchers = mutableMapOf<SpecifiedType, Dispatcher>()
    private var onConnectListener: BleGattCallback? = null
    private var notifyListener: OnNotifyListener? = null
    private val connectedDevices: MutableList<DeviceWrapper> = mutableListOf()
    private val bleParsers = mutableListOf<Parser>()

    init {
        registerParser(V2XStandParser.getInstance(GXX))
        registerParser(NmeaParser.getInstance(TK1306))
        registerParser(NmeaParser.getInstance(CP200Dual))
        registerParser(BasicParser.getInstance(UNKNOWN))
//        bleParsers[ServiceType.GXX.name] = GxxStandParser.newInstance()
//        bleParsers[ServiceType.TK1306.name] = GpsNmeaParser.newInstance(ServiceType.TK1306)
//        bleParsers[ServiceType.CP200_SINGLE_OUTPUT.name] =
//            GpsNmeaParser.newInstance(ServiceType.CP200_SINGLE_OUTPUT)
//        bleParsers[ServiceType.CP200_DUAL_OUTPUT.name] =
//            GpsNmeaParser.newInstance(ServiceType.CP200_DUAL_OUTPUT)
//        addConnectedDeviceWrapper(
//            DeviceWrapper(
//                null,
//                bleParsers[ServiceType.GXX.name],
//                UUID_SERVICE_GXX.toString(),
//                UUID_NOTIFY_GXX.toString()
//            )
//        )
//        addConnectedDeviceWrapper(
//            DeviceWrapper(
//                null,
//                bleParsers[ServiceType.TK1306.name],
//                UUID_SERVICE_MENG_XIN_TK1306.toString(),
//                UUID_NOTIFY_MENG_XIN_TK1306.toString()
//            )
//        )
//        addConnectedDeviceWrapper(
//            DeviceWrapper(
//                null,
//                bleParsers[ServiceType.CP200_SINGLE_OUTPUT.name],
//                UUID_SERVICE_MENG_XIN_CP200.toString(),
//                UUID_NOTIFY_MENG_XIN_CP200.toString()
//            )
//        )
    }

    private fun registerParser(parser: Parser) {
        bleParsers.add(parser)
    }

    var state = State.DISCONNECTED
        private set

    fun initBle(context: Context) {
        BleManager.getInstance().init(context)
        BleManager.getInstance()
            .enableLog(true)
            .setOperateTimeout(5000)
            .setReConnectCount(3, 2000)
            .initScanRule(BleScanRuleConfig.Builder().setAutoConnect(true).build())
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

    fun removeFromConnectedDevices(device: BleDevice?) {
        connectedDevices.removeAll { it.bleDevice?.mac == device?.mac }
    }

    fun removeFromConnectedDevices(device: DeviceWrapper?) {
        connectedDevices.removeAll { it.bleDevice?.mac == device?.bleDevice?.mac }
    }

    fun getConnectedDevices(): List<DeviceWrapper> {
        return connectedDevices
    }

    fun getDispatcher(type: SpecifiedType): Dispatcher? {
        return dispatchers[type]
    }

    override fun register(type: SpecifiedType, dispatcher: Dispatcher) {
        dispatchers[type] = dispatcher
    }

    override fun unRegister(type: SpecifiedType, dispatcher: Dispatcher) {
        dispatchers.remove(type)
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
            Log.d(TAG, "openNotify: 设备未连接上")
            listener?.onNotifyFailure(Exception("设备未连接上"))
            return
        }
//        if (state == State.NOTIFY_OPEN) {
//            Log.d(TAG, "openNotify: 通知已开启")
//            listener?.onNotifySuccess()
//            return
//        }
        BleManager.getInstance()
            .notify(bleDevice, uuidService, uuidNotify, object : BleNotifyCallback() {
                val deviceWrapper = findDeviceWrapper(bleDevice)
                val parser = getParser(uuidService, uuidNotify)
                override fun onCharacteristicChanged(data: ByteArray?) {
                    data?.run {
                        parser.parseData(data)
                    }
                }

                override fun onNotifyFailure(e: BleException?) {
                    Log.d(TAG, "onNotifyFailure: ${deviceWrapper.toString()},error:${e.toString()}")
                    notifyListener?.onNotifyFailure(Exception(e?.description))
                    listener?.onNotifyFailure(Exception(e?.description))
                }

                override fun onNotifySuccess() {
                    Log.d(TAG, "onNotifySuccess: ${deviceWrapper.toString()}")
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
        // finding specified notify to open it
        gattServices?.forEach { service ->
            val uuidService = service.uuid.toString()
            val serviceType = findSpecifiedServiceType(uuidService)
            if (serviceType != null) {
                deviceWrapper = findDeviceWrapper(bleDevice)?.apply {
                    this.uuidService = serviceType.uuidService
                }
                val uuidNotify = serviceType.uuidNotify
                val characteristics: MutableList<BluetoothGattCharacteristic>? =
                    service.characteristics
                characteristics?.forEach { chr ->
                    if (chr.uuid.toString() == uuidNotify /*|| (chr.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0*/) {
                        characteristic = chr
                        deviceWrapper?.uuidNotify = uuidNotify
                        println("found specified notify characteristic UUID：${chr.uuid}")
                        return@forEach
                    }
                }
                if (characteristic != null) {
                    openNotify(
                        bleDevice,
                        uuidService,
                        uuidNotify,
                        listener = onNotifyListener
                    )
                    return@forEach
                }
            }
        }
        if (characteristic != null) {
            Log.d(TAG, "specified device info: ${deviceWrapper.toString()}")
        } else {
            // finding available notify to open it
            gattServices?.forEach { service ->
                val uuidService = service.uuid.toString()
                deviceWrapper = findDeviceWrapper(bleDevice)?.apply {
                    this.uuidService = uuidService
                }
                val characteristics: MutableList<BluetoothGattCharacteristic>? =
                    service.characteristics
                characteristics?.forEach { chr ->
                    if ((chr.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        characteristic = chr
                        deviceWrapper?.uuidNotify = chr.uuid.toString()
                        println("found available notify characteristic UUID：${chr.uuid}")
                        return@forEach
                    }
                }
                if (characteristic != null) {
                    var uuidNotify = characteristic!!.uuid.toString()
                    openNotify(
                        bleDevice,
                        uuidService,
                        uuidNotify,
                        listener = onNotifyListener
                    )
                    return@forEach
                }
            }
            if (characteristic != null) {
                Log.d(TAG, "available device info: ${deviceWrapper.toString()}")
            } else {
                println("not found available notification characteristic.")
            }
        }
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
        return connectedDevices.find { it.uuidService.equals(sid, true) } ?: DeviceWrapper().apply {
            connectedDevices.add(this)
        }
    }

    private fun getParser(uuidService: String, uuidNotify: String): Parser {
        val serviceType = GenericType.getInstance(uuidService, uuidNotify)
        val parser =
            bleParsers.find {
                it.getType().uuidService.equals(uuidService, true) && it.getType().uuidNotify.equals(
                    uuidNotify,
                    true
                )
            }
                ?: BasicParser.getInstance(serviceType).apply {
                    bleParsers.add(this)
                }
        Log.d(
            TAG,
            "getParser type: ${parser.getType().desc},[uuidService:$uuidService,uuidNotify:$uuidNotify]"
        )
        return parser
    }

    private fun getParserByType(type: ServiceType): Parser? {
        return bleParsers.find {
            it.getType().uuidService.equals(type.uuidService, true) && it.getType().uuidNotify.equals(
                type.uuidNotify,
                true
            )
        }
    }

    private fun contains(sid: String?, nid: String?): Boolean {
        return connectedDevices.find { it.uuidService.equals(sid, true) && it.uuidNotify.equals(nid, true) } != null
    }

    private fun findSpecifiedServiceType(uuidService: String?): ServiceType? {
        val type = SpecifiedType.types.find { it.uuidService.equals(uuidService, true) }
        Log.d(
            TAG,
            "findSpecifiedServiceType:[uuidService:${type?.uuidService},uuidNotify:${type?.uuidNotify}]"
        )
        return type
    }

}