package com.util.blecm;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.util.blecm.intf.FoundBleDevice;

/**
 * Created by adolp on 2017/3/30.
 */

public class BleScanBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "BSBR";

    private FoundBleDevice foundBleDevice;

    public BleScanBroadcastReceiver(FoundBleDevice foundBleDevice) {
        this.foundBleDevice = foundBleDevice;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i(TAG, "device's name: " + device.getName() + " device's address: " + device.getAddress());
            foundBleDevice.recodeBleDevice(device);
        }
    }

}
