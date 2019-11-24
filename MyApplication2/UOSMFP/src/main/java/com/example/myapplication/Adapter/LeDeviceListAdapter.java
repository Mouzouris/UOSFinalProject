package com.example.myapplication.Adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<Integer> rssiValues;

    private LayoutInflater mInflator;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        rssiValues = new ArrayList<>();
        mInflator = LayoutInflater.from(context);
    }

    /**
     * Adds device to {@code mLeDevices} if not already in list.
     * @param device Device to be added to list
     */
    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public void updateRssiValue(BluetoothDevice device, int RSSI) {
        if (mLeDevices.contains(device)) {
            rssiValues.add(mLeDevices.indexOf(device), RSSI);
        } else {
            Log.w("updateRssiValue", "Didn't add RSSI");
        }
    }



    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }
    public void clear() {
        mLeDevices.clear();
    }
    @Override
    public int getCount() {
        return mLeDevices.size();
    }
    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null,true);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = view.findViewById(R.id.device_name);
            viewHolder.deviceAddress =  view.findViewById(R.id.device_address);
            viewHolder.imageView = view.findViewById(R.id.device_image);
            viewHolder.deviceRSSI =  view.findViewById(R.id.device_rssi);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        BluetoothDevice device = mLeDevices.get(i);
        int rssiValue = rssiValues.get(i);
        final String deviceName = device.getName();

        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);

            if ((deviceName.equals("CC2650 SensorTag")) || (deviceName.equals("CC2650 SensorTag LED")) || (deviceName.equals("SensorTag2")) || (deviceName.equals("SensorTag2.0")))
                viewHolder.imageView.setImageResource(R.drawable.ic_sensortag2_300);
                viewHolder.deviceName.setText("SensorTag");
        }

        else
            viewHolder.imageView.setImageResource(R.drawable.ic_bluetooth_black_24dp);
            viewHolder.deviceName.setText("Unknown device");
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.deviceRSSI.setText("RSSI: " + rssiValue);

        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRSSI;
        ImageView imageView;
    }


}
