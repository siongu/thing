package com.v2x.thing.base

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import com.clj.fastble.BleManager
import com.v2x.thing.blescan.CheckPermissionsState
import com.common.stdlib.dialog.UDialog
import com.common.stdlib.eventBus.EventBusManager
import com.common.stdlib.permission.PermissionsManager
import com.common.stdlib.permission.PermissionsResultAction
import com.v2x.thing.blescan.view.BluetoothActivity.Companion.REQUEST_CODE_ENABLE_BLUETOOTH
import com.v2x.thing.blescan.view.BluetoothActivity.Companion.REQUEST_CODE_OPEN_BLUETOOTH
import com.v2x.thing.blescan.view.BluetoothActivity.Companion.REQUEST_CODE_OPEN_GPS
import com.v2x.thing.isGPSOpen
import org.jetbrains.anko.toast

open class V2XBaseActivity : BaseActivity() {
    private var isFirst = true
    protected var isBleEnabling = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    open fun isOutScanPage(): Boolean {
        return true
    }

    fun checkPermissions() {
        // 是否支持蓝牙
        if (!BleManager.getInstance().isSupportBle) {
            toast("当前设备不支持蓝牙！")
            return
        }

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(
            this,
            permissions,
            object : PermissionsResultAction() {
                override fun onGranted() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isGPSOpen()) {
                        UDialog(context)
                            .setContent("当前手机扫描蓝牙需要开启定位功能。")
                            .enableCancel(false)
                            .setNegativeButton(
                                "取消",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
                                })
                            .setPositiveButton(
                                "去开启", DialogInterface.OnClickListener { dialog, which ->
                                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                                })
                            .show()

                    } else {
                        if (checkBluetoothOpen()) {
                            EventBusManager.post(CheckPermissionsState(true))
                            if (isFirst && isOutScanPage()) {
                                isFirst = false
//                                handleOnUiThreadDelay({ startActivity(Intent(context, BluetoothActivity::class.java)) }, 500)
                            }
                        }
                    }
                }

                override fun onDenied(permission: String?) {
                    toast("当前手机扫描蓝牙需要开启定位功能！")
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults)
    }

    open fun checkBluetoothOpen(): Boolean {
        // 蓝牙是否开启
        return if (!BleManager.getInstance().isBlueEnable) {
            if (!isBleEnabling) {
                isBleEnabling = true
                requestEnableBluetooth()
            }
            false
        } else {
            true
        }
    }

    fun requestEnableBluetooth() {
        startActivityForResult(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            REQUEST_CODE_ENABLE_BLUETOOTH
        )
    }

    private var bluetoothDialog: UDialog? = null
    fun showBluetoothAlert(
        onNegativeClick: ((dialog: DialogInterface, which: Int) -> Unit)? = null,
        onPositiveClick: ((dialog: DialogInterface, which: Int) -> Unit)? = null
    ) {
        if (bluetoothDialog != null) {
            bluetoothDialog?.show()
            return
        }
        bluetoothDialog = UDialog(this)
            .setContent("当前蓝牙已关闭，请开启蓝牙。")
            .setNegativeButton("取消",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    onNegativeClick?.invoke(dialog, which)
                })
            .setPositiveButton("去开启", DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                startActivityForResult(intent, REQUEST_CODE_OPEN_BLUETOOTH)
                onPositiveClick?.invoke(dialog, which)
            }).apply {
                show()
            }
    }

    fun dismissBluetoothAlert() {
        bluetoothDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissBluetoothAlert()
    }

}