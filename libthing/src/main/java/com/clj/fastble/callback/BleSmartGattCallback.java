package com.clj.fastble.callback;

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import androidx.annotation.CallSuper;
import androidx.annotation.RequiresApi;

import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleSmartGattCallback extends BleGattCallback {
    @Override
    public void onStartConnect() {
    }

    @Override
    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
    }

    @Override
    @CallSuper
    public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
//        if (gatt != null) {
//            gatt.disconnect();
//        }
    }

    @Override
    @CallSuper
    public void onConnectFail(BleDevice bleDevice, BluetoothGatt gatt, BleException exception) {
        if (gatt != null) {
            gatt.disconnect();
        }
    }
}
