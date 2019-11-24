package com.example.myapplication.Fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Activities.SensorTagConnect;
import com.example.myapplication.Adapter.LeDeviceListAdapter;
import com.example.myapplication.R;

import java.util.ArrayList;

public class Scan_BLE extends Fragment {
    private static final String TAG ="Scanning" ;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeScanner;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;


    private LayoutInflater mInflator;
    private ArrayList<Integer> rssiValues;
    private ArrayList<BluetoothDevice> mLeDevices;
    private Button startScan;
    private Button stopScan;
    private static Scan_BLE mThis = null;
    private IBinder mBinder = new LocalBinder();
    private boolean mScanning;
    private ProgressBar scanProgress;

    private ListView listView;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 69;
    public static final int REQUEST_ENABLE_BT = 1;

    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;





    public Scan_BLE(){ }


    public class LocalBinder extends Binder {
        public Scan_BLE getInstance() {
            ////Log.d(TAG, "getInstance local binder");
            return Scan_BLE.this;
        }
    }





    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {

            if(Build.VERSION.SDK_INT>=23){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.updateRssiValue(result.getDevice(), result.getRssi());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    mLeDeviceListAdapter.addDevice(bluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            };
        }
        // initialize bluetooth manager & adapter
        BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.scan_ble,container,false);
        Context context = view.getContext();


        startScan= view.findViewById(R.id.start_scan);
        stopScan= view.findViewById(R.id.stop_scan);


        listView = view.findViewById(R.id.BleDevices);
        mLeDeviceListAdapter=new LeDeviceListAdapter(getContext());
        listView.setAdapter(mLeDeviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), SensorTagConnect.class);
                intent.putExtra("TheDevice", mLeDeviceListAdapter.getDevice(position));
                startActivity(intent);
                if (mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        if (mLeScanner != null) {
                            mLeScanner.stopScan(mScanCallback);

                        }
                    }
                }


            }
        });

        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Pressed start button");
                if (Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                } else {
                    // request BluetoothLeScanner if it hasn't been initialized yet
                    if (mLeScanner == null) mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    // start scan in low latency mode
                    mLeScanner.startScan(new ArrayList<ScanFilter>(), new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
                }

                // Used for Low Energy Bluetooth
                scanLeDevice(true);

            }
        });

        stopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Pressed stop button");
                if (mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        if (mLeScanner!= null) {
                            mLeScanner.stopScan(mScanCallback);

                        }
                        }
                }
                scanLeDevice(false);
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetInvalidated();
                mLeDeviceListAdapter.notifyDataSetChanged();

            }
        });


        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        // ask user to enable bluetooth if necessary
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetInvalidated();
        mLeDeviceListAdapter.notifyDataSetChanged();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.d(TAG, "Starting scan");
            mScanning = true;
            if (Build.VERSION.SDK_INT >= 21) {

                // start scan in low latency mode
                mLeScanner.startScan(new ArrayList<ScanFilter>(), new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
            }
            else{
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }

        } else {
            Log.d(TAG, "Stopping scan and refreshing list");
            mScanning = false;
            if (Build.VERSION.SDK_INT >= 21) {
                if (mLeScanner != null) {
                    mLeScanner.stopScan(mScanCallback);
                }
            }else{
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }




}
