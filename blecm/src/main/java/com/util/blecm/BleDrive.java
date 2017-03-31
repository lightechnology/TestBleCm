package com.util.blecm;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.util.blecm.intf.BleAcListener;
import com.util.blecm.intf.FoundBleDevice;
import com.util.blecm.intf.MgBluetoothGatt;

/**
 * Created by adolp on 2017/3/30.
 */

public class BleDrive implements MgBluetoothGatt {

    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 10000L;

    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private BleGattCallback bleGattCallback;
    private BluetoothGatt bluetoothGatt;
    private BleScanBroadcastReceiver bleScanBroadcastReceiver;

    private AppCompatActivity context;
    private FoundBleDevice foundBleDevice;

    public BleDrive(AppCompatActivity context, FoundBleDevice foundBleDevice) {
        this.mHandler = new Handler();
        this.context = context;
        this.foundBleDevice = foundBleDevice;
    }

    /**
     * 检查当前手机是否支持ble 蓝牙
     *
     * @return
     */
    public boolean checkIsSupportBle() {
        boolean result = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (!result)
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        return result;
    }

    /**
     * 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
     *
     * @return
     */
    public boolean initBleAdapter() {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // 检查设备上是否支持蓝牙
        if (bluetoothAdapter == null) {
            Toast.makeText(context, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 初始变量
     */
    public void init(String serviceId, String readId, String writeId, BleAcListener bleAcListener) {
        IntentFilter bleDeviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bleScanBroadcastReceiver = new BleScanBroadcastReceiver(foundBleDevice);
        context.registerReceiver(bleScanBroadcastReceiver, bleDeviceFoundFilter);
        bleGattCallback = new BleGattCallback(this, serviceId, readId, writeId, bleAcListener);
    }

    /**
     * 视图打开时检查, 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
     */
    public void resume() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        scanBleDevice(true);
    }

    public void pause() {
        scanBleDevice(false);
        foundBleDevice.clear();
    }

    /**
     * 处理掉监听事件
     */
    public void destroy() {
        context.unregisterReceiver(bleScanBroadcastReceiver);
        disconnect();
    }

    /**
     * 扫描周围Ble蓝牙设备
     *
     * @param enable
     */
    private void scanBleDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.cancelDiscovery();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startDiscovery();
        } else {
            mScanning = false;
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public void connect(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, bleGattCallback);
    }

    public void disconnect() {
        if (null != bluetoothGatt)
            bluetoothGatt.disconnect();
    }

    public void sendBytes(byte[] bytes) {
        bleGattCallback.sendBytes(bytes);
    }

    public void sendText(String str, String decode) {
        bleGattCallback.sendText(str, decode);
    }

    @Override
    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
