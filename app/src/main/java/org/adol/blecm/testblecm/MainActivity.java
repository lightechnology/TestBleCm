package org.adol.blecm.testblecm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.util.blecm.BleDrive;
import com.util.blecm.intf.BleAcListener;
import com.util.blecm.intf.FoundBleDevice;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.UnsupportedEncodingException;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppActivity implements BleAcListener {

    public final  static String TAG = "MainActivity";

    private final static String SERVICEID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final static String READID = "0000fff4-0000-1000-8000-00805f9b34fb";
    private final static String WRITEID = "0000fff1-0000-1000-8000-00805f9b34fb";

    @ViewInject(R.id.bleScan_lst)
    private ListView bleScan_lst;
    @ViewInject(R.id.send_edt)
    private EditText send_edt;
    @ViewInject(R.id.read_txt)
    private TextView read_txt;

    private BleDeviceListAdapter bleDeviceListAdapter;
    private BleDrive bleDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bleDeviceListAdapter = new BleDeviceListAdapter(MainActivity.this.getLayoutInflater());
        bleScan_lst.setAdapter(bleDeviceListAdapter);
        bleDrive = new BleDrive(this, bleDeviceListAdapter);
        if (!BleDrive.checkIsSupportBle(this, true) || null == BleDrive.initBleAdapter(this, true))
            finish();
        bleDrive.init(SERVICEID, READID, WRITEID, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleDrive.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bleDrive.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleDrive.destroy();
    }

    @Event(value = R.id.bleScan_lst, type = AdapterView.OnItemClickListener.class)
    private void itemOnClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice bleDevice = bleDeviceListAdapter.getDevice(position);
        bleDrive.connect(bleDevice);
    }

    @Event(R.id.send_btn)
    private void send_btn(View v) {
        bleDrive.sendText(send_edt.getText().toString(), "GBK");
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.e(TAG, "status: " + status + " newState: " + newState + " BluetoothProfile.STATE_DISCONNECTED: " + BluetoothProfile.STATE_DISCONNECTED + " BluetoothProfile.STATE_DISCONNECTING: " + BluetoothProfile.STATE_DISCONNECTING);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] datat = characteristic.getValue();
        if ((byte) 0x8F == datat[0])
        Log.e(TAG, "xxxxxxxxxxxxxx");
        try {
            final String str = new String(characteristic.getValue(), "GBK");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    read_txt.setText(read_txt.getText() + str);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
