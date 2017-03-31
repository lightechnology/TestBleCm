package org.adol.blecm.testblecm;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.util.blecm.intf.FoundBleDevice;

import java.util.ArrayList;

/**
 * Created by adolp on 2017/3/30.
 */

public class BleDeviceListAdapter extends BaseAdapter implements FoundBleDevice {

    private ArrayList<BluetoothDevice> mBleDevices;
    private LayoutInflater mInflator;

    public BleDeviceListAdapter(LayoutInflater mInflator) {
        super();
        mBleDevices = new ArrayList<BluetoothDevice>();
        this.mInflator = mInflator;
    }

    public void addDevice(BluetoothDevice device) {
        if(!mBleDevices.contains(device)) {
            mBleDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mBleDevices.get(position);
    }

    @Override
    public void recodeBleDevice(BluetoothDevice device) {
        addDevice(device);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        mBleDevices.clear();
    }

    @Override
    public int getCount() {
        return mBleDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mBleDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mBleDevices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return convertView;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
