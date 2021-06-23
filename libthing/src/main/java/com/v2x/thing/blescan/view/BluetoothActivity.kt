package com.v2x.thing.blescan.view

import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleSmartGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.common.stdlib.dialog.SimpleDialog
import com.common.stdlib.eventBus.EventBusManager
import com.common.stdlib.system.dp2px
import com.v2x.thing.R
import com.v2x.thing.base.V2XBaseActivity
import com.v2x.thing.ble.OnNotifyListener
import com.v2x.thing.ble.bleservice.BleService
import com.v2x.thing.blescan.CheckPermissionsState
import com.v2x.thing.blescan.adapter.BleDevicesAdapter
import com.v2x.thing.databinding.ActivityBluetoothBinding
import com.v2x.thing.handleOnUiThreadDelay
import com.v2x.thing.removeCallbacksAndMessages
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class BluetoothActivity : V2XBaseActivity(), BluetoothView {
    companion object {
        val REQUEST_CODE_OPEN_GPS = 0x00011
        val REQUEST_CODE_OPEN_BLUETOOTH = 0x00012
        val REQUEST_CODE_ENABLE_BLUETOOTH = 0x00013
    }

    private lateinit var adapter: BleDevicesAdapter
    private lateinit var binding: ActivityBluetoothBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth)
        EventBusManager.register(this)
        initView()
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        handleOnUiThreadDelay({ checkPermissions() }, 500)
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
        stopScanBle()
        removeCallbacksAndMessages()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBusManager.unregister(this)
    }

    var isManual = false
    private fun initView() {
        adapter = BleDevicesAdapter(this, this)
        binding.list.adapter = adapter
        binding.list.addFooterView(View(context).apply {
            layoutParams =
                AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, dp2px(context, 20f))
            setBackgroundColor(Color.TRANSPARENT)
        })
        binding.refresh.setOnRefreshListener {
            handleOnUiThreadDelay(Runnable { binding.refresh.isRefreshing = false }, 50)
            checkPermissions()
        }
        binding.ivBack.setOnClickListener {
            handleBack()
//            showFailDialog("连接失败")
        }
    }

    override fun isOutScanPage(): Boolean {
        return false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun checkPermissionsSuccess(event: CheckPermissionsState) {
        if (event.isSuccess) {
            startScanBle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_OPEN_GPS -> {
                handleOnUiThreadDelay({ checkPermissions() }, 200)
            }
            REQUEST_CODE_OPEN_BLUETOOTH -> {
                handleOnUiThreadDelay({ checkPermissions() }, 200)
            }
        }
    }

    private fun startScanBle() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                Log.d(TAG, "onScanStarted")
                handleOnUiThreadDelay(Runnable { binding.refresh.isRefreshing = true }, 100)
                adapter.clear()
                BleService.INSTANCE.getConnectedDevices().forEach {
                    val connectedDevice = it.bleDevice
                    if (BleManager.getInstance().isConnected(connectedDevice)) {
                        connectedDevice?.run {
                            handleOnUiThreadDelay(Runnable { adapter.addItemAndUpdate(this) }, 100)
                        }
                    }
                }
            }

            override fun onScanning(bleDevice: BleDevice?) {
                Log.d(
                    TAG,
                    "onScanning,bleDevice=${if (bleDevice == null) "null" else "{${bleDevice.name},${bleDevice.mac}}"}"
                )
                bleDevice?.run {
//                    if (!this.name.isNullOrBlank())
                    handleOnUiThreadDelay(Runnable { adapter.addItemAndUpdate(this) }, 100)
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                Log.d(TAG, "onScanFinished")
                handleOnUiThreadDelay(Runnable {
                    binding.refresh.isRefreshing = false
                }, 200)
            }
        })
    }

    private fun stopScanBle() {
        BleManager.getInstance().cancelScan()
    }

    override fun connectDevice(
        device: BleDevice,
        connectListener: BleGattCallback?,
        onNotifyListener: OnNotifyListener?
    ) {
        if (!BleManager.getInstance().isConnected(device)) {
            stopScanBle()
            BleService.INSTANCE.connect(device, object : BleSmartGattCallback() {
                override fun onStartConnect() {
                    connectListener?.onStartConnect()
                }

                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                override fun onConnectFail(
                    bleDevice: BleDevice?,
                    gatt: BluetoothGatt?,
                    exception: BleException
                ) {
                    super.onConnectFail(bleDevice, gatt, exception)
                    connectListener?.onConnectFail(bleDevice, gatt, exception)
//                    showToast("连接失败")
                    showFailDialog("连接失败")
                }

                override fun onConnectSuccess(
                    bleDevice: BleDevice,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    connectListener?.onConnectSuccess(bleDevice, gatt, status)
                    val sdkInt = Build.VERSION.SDK_INT
                    println("sdkInt------------>$sdkInt")
                    if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
                        //设置最大发包、收包的长度为512个字节
                        BleManager.getInstance().setMtu(bleDevice, 512, object :
                            BleMtuChangedCallback() {
                            override fun onSetMTUFailure(exception: BleException?) {
                                println("set mtu fail")
                                // open notify
                                openNotify(bleDevice, onNotifyListener)
                            }

                            override fun onMtuChanged(mtu: Int) {
                                println("set mtu success,ble最大传输长度:$mtu")
                                // open notify
                                openNotify(bleDevice, onNotifyListener)
                            }
                        })
                    } else {
                        println("ble最大传输长度:23")
                        // open notify
                        openNotify(bleDevice, onNotifyListener)
                    }
                }

                private fun openNotify(bleDevice: BleDevice, onNotifyListener: OnNotifyListener?) {
                    handleOnUiThreadDelay(
                        {
                            BleService.INSTANCE.openAvailableNotify(bleDevice, onNotifyListener)
                        }, 200
                    )
                }

                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    bleDevice: BleDevice,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    val connect = BleManager.getInstance().isConnected(bleDevice)
                    Log.d(TAG, "断开连接,isConnect=$connect")
                    super.onDisConnected(isActiveDisConnected, bleDevice, gatt, status)
                    connectListener?.onDisConnected(isActiveDisConnected, bleDevice, gatt, status)
                }
            })
        }
    }

    var sDialog: SimpleDialog? = null

    fun showFailDialog(msg: String) {
        if (sDialog == null) {
            sDialog = SimpleDialog(context).apply {
                setContentView(R.layout.simple_dialog_layout)
                findViewById<TextView>(R.id.tv_msg).text = msg
            }
        }
        sDialog?.show()
    }

}