package com.v2x.thing.blescan.adapter

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleSmartGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.v2x.thing.R
import com.v2x.thing.ble.OnNotifyListener
import com.v2x.thing.ble.bleservice.BleService
import com.v2x.thing.ble.bleservice.DeviceWrapper
import com.v2x.thing.blescan.view.BluetoothView
import com.v2x.thing.handleOnUiThreadDelay
import org.jetbrains.anko.find
import java.util.*

class BleDevicesAdapter(
    private val context: Context,
    private val bleView: BluetoothView,
    private val list: MutableList<BleDevice> = mutableListOf()
) : RecyclerView.Adapter<BleDevicesAdapter.ViewHolder>() {
    private val TAG = javaClass.simpleName

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.bluetooth_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        item.name.run {
            holder.tvName.text = this
            holder.tvName.visibility = if (this.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        item.mac.run {
            holder.tvMac.text = this
            holder.tvMac.visibility = if (this.isNullOrBlank()) View.GONE else View.VISIBLE
        }
//        viewHolder.tvMac.visibility = View.GONE
//        val text =
//            if (item.name.isNullOrBlank()) if (item.mac.isNullOrBlank()) "" else item.mac else item.name
//        viewHolder.tvName.text = text
//        viewHolder.tvName.visibility = View.VISIBLE
        val resId =
            if (position == 0 && itemCount == 1) R.drawable.bg_ble_list_item_single
            else if (position == 0) R.drawable.bg_ble_list_item_top
            else if (position != itemCount - 1) R.drawable.bg_ble_list_item_normal
            else R.drawable.bg_ble_list_item_bottom
        holder.itemView.setBackgroundResource(resId)
        holder.pb.visibility = View.GONE
//        viewHolder.tvConnect.visibility = View.VISIBLE
        val isConnected = BleManager.getInstance().isConnected(item)
        val log = "{name=${item.name},mac=${item.mac}}"
        Log.d(TAG, if (isConnected) "已连接:$log" else "未连接:$log")
//        viewHolder.tvNext.visibility = if (isConnected) View.VISIBLE else View.GONE
        if (isConnected) {
            holder.tvConnect.visibility = View.VISIBLE
            holder.tvConnect.text = "已连接"
        } else {
            holder.tvConnect.visibility = View.INVISIBLE
        }
        holder.tvNext.setOnClickListener {
        }
        holder.itemView.setOnClickListener { view ->
            if (BleManager.getInstance().isConnected(item)) {
                BleManager.getInstance().disconnect(item)
                return@setOnClickListener
            } else {
                bleView.stopScan()
                startLoading(holder.pb)
                BleService.INSTANCE.connect(item, object : BleSmartGattCallback() {
                    override fun onStartConnect() {
                        Log.d(TAG, "onStartConnect")
                    }

                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                    override fun onDisConnected(
                        isActiveDisConnected: Boolean,
                        device: BleDevice?,
                        gatt: BluetoothGatt?,
                        status: Int
                    ) {
                        super.onDisConnected(isActiveDisConnected, device, gatt, status)
                        // 移除连接断开的设备
                        BleService.INSTANCE.removeFromConnectedDevices(device)
                        val connect = BleManager.getInstance().isConnected(device)
                        Log.d(TAG, "断开连接,isConnect=$connect,thread:${Thread.currentThread().name}")
                        holder.tvConnect.visibility = View.INVISIBLE
                        Log.d(
                            TAG,
                            "tvConnect visibility:${
                                when (holder.tvConnect.visibility) {
                                    View.INVISIBLE -> "INVISIBLE"
                                    View.GONE -> "GONE"
                                    else -> "VISIBLE"
                                }
                            } "
                        )
                    }

                    override fun onConnectSuccess(
                        bleDevice: BleDevice?,
                        gatt: BluetoothGatt?,
                        status: Int
                    ) {
                        BleService.INSTANCE.addConnectedDeviceWrapper(
                            DeviceWrapper(
                                bleDevice = bleDevice
                            )
                        )
                        val sdkInt = Build.VERSION.SDK_INT
                        Log.d(TAG, "sdkInt------------>$sdkInt")
                        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
                            //设置最大发包、收包的长度为512个字节
                            BleManager.getInstance().setMtu(bleDevice, 512, object :
                                BleMtuChangedCallback() {
                                override fun onSetMTUFailure(exception: BleException?) {
                                    Log.d(TAG, "onSetMTUFailure")
                                    // open notify
                                    openNotify(bleDevice, onNotifyListener)
                                }

                                override fun onMtuChanged(mtu: Int) {
                                    Log.d(TAG, "onMtuChanged:$mtu")
                                    // open notify
                                    openNotify(bleDevice, onNotifyListener)
                                }
                            })
                        } else {
                            Log.d(TAG, "onConnectSuccess mtu:23")
                            // open notify
                            openNotify(bleDevice, onNotifyListener)
                        }
                    }

                    private val onNotifyListener = object : OnNotifyListener {
                        override fun onNotifySuccess() {
                            notifyItemChanged(position)
                            Log.d(TAG, "通知开启成功")
                        }

                        override fun onNotifyFailure(e: Exception?) {
                            notifyItemChanged(position)
                            Log.d(TAG, "通知开启失败: error=${e?.message}")
                        }
                    }

                    private fun openNotify(
                        bleDevice: BleDevice?,
                        onNotifyListener: OnNotifyListener?
                    ) {
                        handleOnUiThreadDelay(
                            {
                                BleService.INSTANCE.openAvailableNotify(bleDevice, onNotifyListener)
                            }, 200
                        )
                    }

                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                    override fun onConnectFail(
                        bleDevice: BleDevice?,
                        gatt: BluetoothGatt?,
                        exception: BleException?
                    ) {
                        super.onConnectFail(bleDevice, gatt, exception)
                        stopLoading(holder.pb)
                        bleView.showDialog("连接失败")
                    }

                })
            }
        }
    }

    private fun connectDevice(
        device: BleDevice,
        connectListener: BleGattCallback?,
    ) {
        if (!BleManager.getInstance().isConnected(device)) {
            bleView.stopScan()
            BleService.INSTANCE.connect(device, connectListener)
        }
    }

    fun addItem(item: BleDevice) {
        val exist = list.find { it.key == item.key }
        if (exist != null) {
            exist.device = item.device
            exist.rssi = item.rssi
            exist.scanRecord = item.scanRecord
            exist.timestampNanos = item.timestampNanos
        } else {
            list.add(item)
        }
    }

    fun addItemAndUpdate(item: BleDevice) {
        var index = list.indexOfFirst { it.mac == item.mac }
        val exist = if (index == -1) null else list[index]
        if (exist != null) {
            exist.device = item.device
            exist.rssi = item.rssi
            exist.scanRecord = item.scanRecord
            exist.timestampNanos = item.timestampNanos
        } else {
            index = if (BleManager.getInstance().isConnected(item)) {
                list.add(0, item)
                0
            } else {
                list.add(item)
                list.size - 1
            }
        }
        notifyItemChanged(index)
    }

    internal class BleComparator : Comparator<BleDevice> {
        override fun compare(o1: BleDevice, o2: BleDevice): Int {
            return if (o1.name.isNullOrBlank() && o2.name.isNullOrBlank())
                o1.mac.compareTo(o2.mac)
            else o1.name.compareTo(o2.name)
        }

    }

    fun removeItemAndUpdate(item: BleDevice) {
        list.removeAll { it.key == item.key }
        notifyDataSetChanged()
    }

    fun clear() {
        list.removeAll {
            !BleManager.getInstance().isConnected(it)
        }
        notifyDataSetChanged()
    }

    private fun startLoading(pb: ImageView) {
        pb.setBackgroundResource(R.drawable.loading_infenite)
        val animation: AnimationDrawable? = pb.background as AnimationDrawable?
        animation?.start()
        pb.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun stopLoading(pb: ImageView) {
        val animation: AnimationDrawable? = pb.background as AnimationDrawable?
        animation?.stop()
        pb.background = null
        pb.visibility = View.GONE
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.find(R.id.tv_name)
        val tvMac: TextView = view.find(R.id.tv_mac)
        val tvConnect: TextView = view.find(R.id.tv_connect)
        val tvNext: TextView = view.find(R.id.tv_next)
        val pb: ImageView = view.find(R.id.pb_connect)
    }
}