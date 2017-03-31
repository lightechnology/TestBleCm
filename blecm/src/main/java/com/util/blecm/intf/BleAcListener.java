package com.util.blecm.intf;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by adolp on 2017/3/30.
 */

public interface BleAcListener {

    // 连接状态改变的回调
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

    // 发现服务的回调
    public void onServicesDiscovered(BluetoothGatt gatt, int status);

    // 写操作的回调
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    // 读操作的回调
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    // 数据返回的回调（此处接收BLE设备返回数据）
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

}
