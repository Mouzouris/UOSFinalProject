package com.example.myapplication.Fragments;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.example.myapplication.Madgwick.Madgwick_c;
import com.example.myapplication.Madgwick.Mahony;
import com.example.myapplication.Model.Point3D;
import com.example.myapplication.R;
import com.example.myapplication.SensorTag.IntentNames;
import com.example.myapplication.SensorTag.SensorConversion;
import com.example.myapplication.SensorTag.SensorTagGatt;
import com.example.myapplication.Services.BleService;

public class DisplayReadings extends Fragment {
    private static final String FRAGMENT_POSITION = "com.example.myapplication.Fragments.DisplayReadings.FRAGMENT_POSITION";

    int i = 0;

    String TAG = "the displayreading frag: ";
    private TextView labelaccx;
    private TextView labelaccy;
    private TextView labelaccz;
    private TextView labeldt;
    private XYPlot plot;

    private SimpleXYSeries seriesAccx;
    private SimpleXYSeries seriesAccy;
    private SimpleXYSeries seriesAccz;
    private static final int HISTORY_SIZE = 500;

    int plotCount = 0;

    private int sectionNumber;
    private BleService mBleService;
    private BluetoothGattService mThis;

    private double[] gyro = new double[3];
    private double[] magnet = new double[3];
    private double[] accel = new double[3];

    private Madgwick_c madgwick_c = new Madgwick_c(250f, 0.1f);
    private Mahony mahony = new Mahony(250f);
    double lpPitch = 0, lpRoll = 0, lpYaw = 0;

    public DisplayReadings() {
        // Required empty public constructor
    }

    public static DisplayReadings newInstance(int position) {
        DisplayReadings fragment = new DisplayReadings();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sectionNumber = getArguments().getInt(FRAGMENT_POSITION);
        mBleService = BleService.getInstance();
        mThis = mBleService.getServiceFromUUID(SensorTagGatt.UUID_MOV_SERV.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_display_readings, container, false);


        labelaccx = v.findViewById(R.id.labelaccx);
        labelaccy = v.findViewById(R.id.labelaccy);
        labelaccz = v.findViewById(R.id.labelaccz);
        labeldt = v.findViewById(R.id.labeldt);
        plot = v.findViewById(R.id.mySimpleXYPlot);


        plot.setDomainStepValue(5);
        plot.setLinesPerRangeLabel(3);


        plot.setRangeBoundaries(-360, 360, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 500, BoundaryMode.FIXED);

        seriesAccx = new SimpleXYSeries("accx");
        seriesAccx.useImplicitXVals();
        plot.addSeries(seriesAccx, new LineAndPointFormatter(Color.rgb(100, 100, 0), null, null, null));

        seriesAccy = new SimpleXYSeries("accy");
        seriesAccy.useImplicitXVals();
        plot.addSeries(seriesAccy, new LineAndPointFormatter(Color.rgb(0, 100, 200), null, null, null));

        seriesAccz = new SimpleXYSeries("accz");
        seriesAccz.useImplicitXVals();
        plot.addSeries(seriesAccz, new LineAndPointFormatter(Color.rgb(100, 0, 200), null, null, null));





        return v;
    }


    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(motionUpdateReceiver, makeMotionUpdateIntentFilter());

    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(motionUpdateReceiver);

    }


    private final BroadcastReceiver motionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (IntentNames.ACTION_MOV_CHANGE.equals(action)) {

                //Log.i(TAG, "***************** MOTION sensed *****************");
                byte[] value = intent.getByteArrayExtra(IntentNames.EXTRAS_MOV_DATA);
                Point3D v;


                v = SensorConversion.MOVEMENT_ACC.convert(value);
                double[] accelvalues = new double[3];
                accelvalues[0] = v.x;
                accelvalues[1] = v.y;
                accelvalues[2] = v.z;
                System.arraycopy(accelvalues, 0, accel, 0, 3);


                v = SensorConversion.MOVEMENT_GYRO.convert(value);
                double[] gyrovalues = new double[3];
                gyrovalues[0] = v.x;
                gyrovalues[1] = v.y;
                gyrovalues[2] = v.z;
                System.arraycopy(gyrovalues, 0, gyro, 0, 3);


                v = SensorConversion.MOVEMENT_MAG.convert(value);
                double[] magvalues = new double[3];
                magvalues[0] = v.x;
                magvalues[1] = v.y;
                magvalues[2] = v.z;
                System.arraycopy(magvalues, 0, magnet, 0, 3);
                //Mahony();
                Madgwick3();
            }
        }
    };



    private void Mayhony() {


        mahony.MahonyAHRSupdate((float) gyro[0], (float) gyro[1], (float) gyro[2], (float) accel[0], (float) accel[1], (float) accel[2], (float) magnet[0], (float) magnet[1], (float) magnet[2]);

        if (seriesAccx.size() > HISTORY_SIZE) {
            seriesAccx.removeFirst();
            seriesAccy.removeFirst();
            seriesAccz.removeFirst();
        }

        lpPitch = mahony.MayhPitch;
        lpRoll = mahony.MayhRoll;
        lpYaw = mahony.MayhYaw;

        seriesAccx.addLast(null, lpPitch);
        seriesAccy.addLast(null, lpRoll);
        seriesAccz.addLast(null, lpYaw);


        plot.post(new Runnable() {
            public void run() {


                labelaccx.setText(Double.toString( lpPitch));
                labelaccy.setText(Double.toString(lpRoll));
                labelaccz.setText(Double.toString(lpYaw));
                labeldt.setText(Double.toString(mahony.sampleFreq));

                plot.redraw();
            }
        });

    }


    private void Madgwick3() {

       madgwick_c.MadgwickAHRSupdate((float) gyro[0], (float) gyro[1], (float) gyro[2], (float) accel[0], (float) accel[1], (float) accel[2], (float) magnet[0], (float) magnet[1], (float) magnet[2]);

        if (seriesAccx.size() > HISTORY_SIZE) {
            seriesAccx.removeFirst();
            seriesAccy.removeFirst();
            seriesAccz.removeFirst();
        }

        lpPitch = madgwick_c.MadgPitch;
        lpRoll = madgwick_c.MadgRoll;
        lpYaw = madgwick_c.MadgYaw;

        seriesAccx.addLast(null, lpPitch);
        seriesAccy.addLast(null, lpRoll);
        seriesAccz.addLast(null, lpYaw);


        plot.post(new Runnable() {
            public void run() {


                labelaccx.setText(Double.toString( madgwick_c.MadgPitch));
                labelaccy.setText(Double.toString(madgwick_c.MadgRoll));
                labelaccz.setText(Double.toString(madgwick_c.MadgYaw));
                labeldt.setText(Double.toString(madgwick_c.sampleFreq));

                plot.redraw();
            }
        });

    }


    private static IntentFilter makeMotionUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentNames.ACTION_MOV_CHANGE);
        return intentFilter;
    }


}
