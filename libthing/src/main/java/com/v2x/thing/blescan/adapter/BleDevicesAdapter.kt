package com.v2x.thing.blescan.adapter

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleSmartGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.v2x.thing.R
import com.v2x.thing.ble.OnNotifyListener
import com.v2x.thing.blescan.view.BluetoothView
import org.jetbrains.anko.find

class BleDevicesAdapter(
    private val context: Context,
    private val bleView: BluetoothView,
    private val list: MutableList<BleDevice> = mutableListOf()
) : BaseAdapter() {
    private val TAG = javaClass.simpleName
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any? {
        return if (list.size > position) list[position] else null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun addItem(item: BleDevice) {
        var exsit = list.find { it.key == item.key }
        if (exsit != null) {
            exsit.device = item.device
            exsit.rssi = item.rssi
            exsit.scanRecord = item.scanRecord
            exsit.timestampNanos = item.timestampNanos
        } else {
            list.add(item)
        }
    }

    fun addItemAndUpdate(item: BleDevice) {
        var exsit = list.find { it.key == item.key }
        if (exsit != null) {
            exsit.device = item.device
            exsit.rssi = item.rssi
            exsit.scanRecord = item.scanRecord
            exsit.timestampNanos = item.timestampNanos
        } else {
            list.add(item)
        }
        notifyDataSetChanged()
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = list[position]
        var view: View
        var viewHolder: ViewHolder
        if (convertView == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.bluetooth_item_layout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
//        item.name.run {
//            view.tv_name.text = this
//            viewHolder.tvName.visibility = if (this.isNullOrBlank()) View.GONE else View.VISIBLE
//        }
//        item.mac.run {
//            view.tv_mac.text = this
//            viewHolder.tvMac.visibility = if (this.isNullOrBlank()) View.GONE else View.VISIBLE
//        }
        viewHolder.tvMac.visibility = View.GONE
        val text =
            if (item.name.isNullOrBlank()) if (item.mac.isNullOrBlank()) "" else item.mac else item.name
        viewHolder.tvName.text = text
        viewHolder.tvName.visibility = View.VISIBLE
        val resid =
            if (position == 0 && count == 1) R.drawable.bg_ble_list_item_single
            else if (position == 0) R.drawable.bg_ble_list_item_top
            else if (position != count - 1) R.drawable.bg_ble_list_item_normal
            else R.drawable.bg_ble_list_item_bottom
        view.setBackgroundResource(resid)
        viewHolder.pb.visibility = View.GONE
//        viewHolder.tvConnect.visibility = View.VISIBLE
        val isConnected = BleManager.getInstance().isConnected(item)
        val log = "{name=${item.name},mac=${item.mac}}"
        Log.d(TAG, if (isConnected) "已连接:$log" else "未连接:$log")
//        viewHolder.tvNext.visibility = if (isConnected) View.VISIBLE else View.GONE
        if (isConnected) {
            viewHolder.tvConnect.visibility = View.VISIBLE
            viewHolder.tvConnect.text = "已连接"
        } else {
            viewHolder.tvConnect.visibility = View.INVISIBLE
        }
        viewHolder.tvNext.setOnClickListener {
        }
        /*viewHolder.tvConnect*/view.setOnClickListener { view ->
            if (BleManager.getInstance().isConnected(item)) {
                BleManager.getInstance().disconnect(item)
                return@setOnClickListener
            }
//            view.visibility = View.INVISIBLE
//            viewHolder.pb.visibility = View.VISIBLE
            startLoading(viewHolder.pb)
            bleView?.connectDevice(item, object : BleSmartGattCallback() {
                override fun onStartConnect() {

                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    device: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    Log.d(TAG, "断开连接")
                    super.onDisConnected(isActiveDisConnected, device, gatt, status)
                    viewHolder.tvConnect.visibility = View.INVISIBLE
//                    (tvConnect as TextView).text = "连接"
                }

                override fun onConnectSuccess(
                    bleDevice: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
//                    notifyDataSetChanged()
//                    tvConnect.visibility = View.VISIBLE
//                    viewHolder.pbConnect.visibility = View.GONE
//                    viewHolder.tvNext.visibility = View.VISIBLE
//                    (tvConnect as TextView).text = "已连接"
//                    tvConnect.isEnabled = false
                }

                override fun onConnectFail(
                    bleDevice: BleDevice?,
                    gatt: BluetoothGatt?,
                    exception: BleException?
                ) {
                    super.onConnectFail(bleDevice, gatt, exception)
//                    view.visibility = View.VISIBLE
                    stopLoading(viewHolder.pb)
                }

            }, object : OnNotifyListener {
                override fun onNotifySuccess() {
//                    tvConnect.visibility = View.VISIBLE
//                    viewHolder.pbConnect.visibility = View.GONE
                    notifyDataSetChanged()
                    Log.d(TAG, "通知开启成功")
//                    presenter.view?.showToast("通知开启成功")
                }

                override fun onNotifyFailure(e: Exception?) {
//                    tvConnect.visibility = View.VISIBLE
//                    viewHolder.pbConnect.visibility = View.GONE
                    notifyDataSetChanged()
                    Log.d(TAG, "通知开启失败: error=${e?.message}")
//                    presenter.view?.showError("通知开启失败: error=${e?.message}")
                }
            })

        }
        return view
    }

    private fun startLoading(pb: ImageView) {
        pb.setBackgroundResource(R.drawable.loading_infenite);
        val animation: AnimationDrawable? = pb.background as AnimationDrawable?
        animation?.start()
        pb.visibility = View.VISIBLE
    }

    private fun stopLoading(pb: ImageView) {
        val animation: AnimationDrawable? = pb.background as AnimationDrawable?
        animation?.stop()
        pb.background = null
        pb.visibility = View.GONE
    }

    class ViewHolder {
        var tvName: TextView
        var tvMac: TextView
        var tvConnect: TextView
        var tvNext: TextView
        var pb: ImageView

        constructor(view: View) {
            tvName = view.find(R.id.tv_name)
            tvMac = view.find(R.id.tv_mac)
            tvConnect = view.find(R.id.tv_connect)
            tvNext = view.find(R.id.tv_next)
            pb = view.find(R.id.pb_connect)
        }
    }
}