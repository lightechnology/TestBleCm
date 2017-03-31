package com.util.blecm;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.util.blecm.intf.BleAcListener;
import com.util.blecm.intf.MgBluetoothGatt;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by adolp on 2017/3/30.
 */

public class BleGattCallback extends BluetoothGattCallback {

    public static final String TAG = "BleGCB";

    private MgBluetoothGatt mgBluetoothGatt;
    private String serverId;
    private String readId;
    private String writeId;
    private BleAcListener bleAcListener;

    private Queue<byte[]> sendQueue;

    public BleGattCallback(MgBluetoothGatt mgBluetoothGatt, String serverId, String readId, String writeId, BleAcListener bleAcListener) {
        this.mgBluetoothGatt = mgBluetoothGatt;
        this.serverId = serverId;
        this.readId = readId;
        this.writeId = writeId;
        this.bleAcListener = bleAcListener;
        this.sendQueue = new ConcurrentLinkedQueue<byte[]>();
    }

    // 连接状态改变的回调
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // 连接成功后启动服务发现
            Log.i(TAG, "启动服务发现: " + mgBluetoothGatt.getBluetoothGatt().discoverServices());
        }
        if (null != bleAcListener)
            bleAcListener.onConnectionStateChange(gatt, status, newState);
    }

    // 发现服务的回调
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "成功发现服务");
            BluetoothGattService service = mgBluetoothGatt.getBluetoothGatt().getService(UUID.fromString(serverId));
            BluetoothGattCharacteristic characteristicRead = service.getCharacteristic(UUID.fromString(readId));
            mgBluetoothGatt.getBluetoothGatt().setCharacteristicNotification(characteristicRead, true);
        }else{
            Log.e(TAG, "服务发现失败，错误码为: " + status);
        }
        if (null != bleAcListener)
            bleAcListener.onServicesDiscovered(gatt, status);
    }

    // 写操作的回调
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "写入成功 " + characteristic.getValue());
            send();
        }
        if (null != bleAcListener)
            bleAcListener.onCharacteristicWrite(gatt, characteristic, status);
    }

    // 读操作的回调
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "读取成功 " + characteristic.getValue());
        }
        if (null != bleAcListener)
            bleAcListener.onCharacteristicRead(gatt, characteristic, status);
    }

    // 数据返回的回调（此处接收BLE设备返回数据）
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "回调成功 " + characteristic.getValue());
        if (null != bleAcListener)
            bleAcListener.onCharacteristicChanged(gatt, characteristic);
    }

    public void sendText(String str, String decode) {
        splitStr(str, decode);
        send();
    }

    public void sendBytes(byte[] bytes) {
        splitBytes(bytes);
        send();
    }

    private void splitStr(String sendStr, String decode) {
        String st = sendStr;
        while (20 < st.length()) {
            String x = st.substring(0, 20);
            byte[] b = decodeString(x, decode);
            if (null != b)
                sendQueue.offer(b);
            st = st.substring(20);
        }
        if (0 < st.length()) {
            byte[] b = decodeString(st, decode);
            if (null != b)
                sendQueue.offer(b);
        }
    }

    private void splitBytes(byte[] bytes) {
        int position = 0;
        while (20 < bytes.length) {
            byte[] nb = new byte[20];
            for (int i = 0; i < 20; i++) {
                nb[i] = bytes[position++];
            }
            if (0 < nb.length)
                sendQueue.offer(nb);
        }
        if (position < bytes.length) {
            int len = bytes.length - position;
            byte[] nb = new byte[len];
            for (int i = 0; i < len; i++) {
                nb[i] = bytes[position++];
            }
            if (0 < nb.length)
                sendQueue.offer(nb);
        }
    }

    private byte[] decodeString(String str, String decode) {
        try {
            return str.getBytes(decode);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void send() {
        if (!sendQueue.isEmpty()) {
            byte[] b = sendQueue.poll();
            BluetoothGattService service = mgBluetoothGatt.getBluetoothGatt().getService(UUID.fromString(serverId));
            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(UUID.fromString(writeId));
            mgBluetoothGatt.getBluetoothGatt().setCharacteristicNotification(characteristicWrite, true);
            //将指令放置进特征中
            characteristicWrite.setValue(b);
            //设置回复形式
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            //开始写数据
            mgBluetoothGatt.getBluetoothGatt().writeCharacteristic(characteristicWrite);
        }
    }

    private void printBleDeviceInfo() {
        List<BluetoothGattService> supportedGattServices = mgBluetoothGatt.getBluetoothGatt().getServices();
        for (int i = 0; i < supportedGattServices.size(); i++) {
            Log.i(TAG, i + ":BluetoothGattService UUID=:" + supportedGattServices.get(i).getUuid());
            List<BluetoothGattCharacteristic> listGattCharacteristic = supportedGattServices.get(i).getCharacteristics();
            for (int j = 0; j < listGattCharacteristic.size(); j++) {
                Log.i(TAG, j + ":   BluetoothGattCharacteristic UUID=:" + listGattCharacteristic.get(j).getUuid());
            }
        }
        // 循环遍历服务以及每个服务下面的各个特征，判断读写，通知属性
        for (BluetoothGattService gattService : supportedGattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                int charaProp = gattCharacteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    Log.i(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
                    Log.i(TAG, "gattCharacteristic的属性为:  可读");
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    Log.i(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid());
                    Log.i(TAG, "gattCharacteristic的属性为:  可写");
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    Log.i(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid() + gattCharacteristic);
                    Log.i(TAG, "gattCharacteristic的属性为:  具备通知属性");
                }
            }
        }
    }

}

