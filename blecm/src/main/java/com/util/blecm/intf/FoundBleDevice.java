package com.util.blecm.intf;

import android.bluetooth.BluetoothDevice;

/**
 * Created by adolp on 2017/3/30.
 */

public interface FoundBleDevice {

    public void recodeBleDevice(BluetoothDevice device);

    public void clear();

}
