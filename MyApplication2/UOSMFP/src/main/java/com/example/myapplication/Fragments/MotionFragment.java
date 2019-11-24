package com.example.myapplication.Fragments;


import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.example.myapplication.Model.Point3D;
import com.example.myapplication.R;
import com.example.myapplication.SensorTag.IntentNames;
import com.example.myapplication.SensorTag.SensorConversion;
import com.example.myapplication.SensorTag.SensorTagGatt;
import com.example.myapplication.Services.BleService;

import java.util.ArrayList;

import static com.example.myapplication.R.xml.point_formatter;


/**
 * A simple {@link Fragment} subclass that displays motion data.
 *
 * Potential problems with getting data:
 *  Battery- http://mobilemodding.info/2015/06/ti-sensortag-2-power-consumption-analysys/
 */
public class MotionFragment extends Fragment {

    //private static final String TAG = MotionFragment.class.getSimpleName();
    private static final String FRAGMENT_POSITION = "com.example.myapplication.MotionFragment.FRAGMENT_POSITION";

    /**
     * UI related variables
     */
    String TAG= "the motion fragment: ";

    double bias_x = 0;
    double bias_y = 0;
    double bias_z = 0;
    double delta_x = 0;
    double delta_y = 0;
    double delta_z = 0;
    double scalex = 0;
    double scaley = 0;
    double scalez = 0;
    double corrected_x =0;
    double corrected_y =0;
    double corrected_z =0;


    private int sectionNumber;
    int lastreceived =0;
    private TextView positionText;
    private TextView accelData;
    private TextView gyroData;
    private TextView magData;
    private SeekBar periodBar;
    private TextView periodLength;
    private Switch sensorSwitch;
    private Switch wakeOnShakeSwitch;
    private float[] magnet = new float[3];
    private ArrayList<Float> magx = new ArrayList<>();
    private ArrayList<Float> magy = new ArrayList<>();
    private ArrayList<Float> magz = new ArrayList<>();





    /**
     * BLE related variables
     */
    private BleService mBleService;
    private BluetoothGattService mThis;
    private static boolean mFirstTime = true;
    private static final int periodMinVal = 100;

    private SimpleXYSeries seriesaccelxy;
    private SimpleXYSeries seriesaccelxz;
    private SimpleXYSeries seriesaccelyz;
    private SimpleXYSeries seriesgyroxy;
    private SimpleXYSeries seriesgyroxz;
    private SimpleXYSeries seriesgyroyz;
    private SimpleXYSeries seriesmagnetxy;
    private SimpleXYSeries seriesmagnetxz;
    private SimpleXYSeries seriesmagnetyz;
    private XYPlot plot;



    public MotionFragment() {
        // Required empty public constructor
    }

    /**
     * Instantiates a new fragment. Puts fragment number into a bundle that will be retrieved in {@code onCreate}
     *
     * @param position  The fragment number
     * @return          MotionFragment
     */
    public static MotionFragment newInstance(int position) {
        Bundle args = new Bundle();
        MotionFragment fragment = new MotionFragment();
        args.putInt(FRAGMENT_POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Retrieves bundle and instantiates BLE service and motion service
     *
     * @param savedInstanceState
     */
    @Override
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
        View v = inflater.inflate(R.layout.fragment_motion, container, false);

        positionText = v.findViewById(R.id.fragment_position);
        positionText.setText("Fragment " + (sectionNumber) + ": 9-Axis Motion Sensor");

        accelData = v.findViewById(R.id.accel_data);
        accelData.setText("X:0.00G, Y:0.00G, Z:0.00G");

        gyroData = v.findViewById(R.id.gyro_data);
        gyroData.setText("X:0.00°/s, Y:0.00°/s, Z:0.00°/s");

        magData = v.findViewById(R.id.mag_data);
        magData.setText("X:0.00mT, Y:0.00mT, Z:0.00mT");

        periodLength = v.findViewById(R.id.periodLength);
        periodLength.setText("Sensor period (currently : " + ((90 * 10) + periodMinVal) + "ms)");

        periodBar = v.findViewById(R.id.periodBar);
        periodBar.setMax(245); // because 0-245 corresponds to 100-2550     formula: * 10 + 100
        periodBar.setProgress(90);
        periodBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        sensorSwitch = v.findViewById(R.id.sensorSwitch);
        sensorSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        sensorSwitch.setChecked(true);

        wakeOnShakeSwitch = v.findViewById(R.id.wakeOnShakeSwitch);
        wakeOnShakeSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        wakeOnShakeSwitch.setChecked(true);

        plot = v.findViewById(R.id.rawplot);
        seriesaccelxy = new SimpleXYSeries("seriesaccelxy");
        seriesaccelxz = new SimpleXYSeries("seriesaccelxz");
        seriesaccelyz = new SimpleXYSeries("seriesaccelyz");
        seriesgyroxy = new SimpleXYSeries("seriesgyroxy");
        seriesgyroxz = new SimpleXYSeries("seriesgyroxz");
        seriesgyroyz = new SimpleXYSeries("seriesgyroyz");
        seriesmagnetxy = new SimpleXYSeries("seriesmagnetxy");
        seriesmagnetxz = new SimpleXYSeries("seriesmagnetxz");
        seriesmagnetyz = new SimpleXYSeries("seriesmagnetyz");


        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(getContext(), point_formatter);

        LineAndPointFormatter series2Format =
                new LineAndPointFormatter(getContext(), R.xml.point_formatter_2);
        LineAndPointFormatter series3Format =
                new LineAndPointFormatter(getContext(), R.xml.point_formatter_3);

        plot.addSeries(seriesaccelxy, series1Format);
        plot.addSeries(seriesaccelxz, series2Format);
        plot.addSeries(seriesaccelyz, series3Format);

        plot.addSeries(seriesgyroxy, series1Format);
        plot.addSeries(seriesgyroxz, series2Format);
        plot.addSeries(seriesgyroyz, series3Format);

        plot.addSeries(seriesmagnetxy, series1Format);
        plot.addSeries(seriesmagnetxz, series2Format);
        plot.addSeries(seriesmagnetyz, series3Format);
        plot.setLinesPerRangeLabel(3);

        plot.getLegend().setVisible(false);




        return v;
    }

    /**
     * Handles slider touches which change period of service
     * Resolution 10 ms. Range 100 ms (0x0A) to 2.55 sec (0xFF). Default 1 second (0x64).
     */
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            periodLength.setText("Sensor period (currently : " + ((progress * 10) + periodMinVal) + "ms)");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Log.d(TAG, "Period Stop");
            int period = periodMinVal + (seekBar.getProgress() * 10);

            if (period > 2450) period = 2450;
            if (period < 100) period = 100;
            byte p = (byte)((period / 10) + 10);

            //Log.d(TAG, "Period characteristic set to: " + period);
            mBleService.changePeriod(mBleService.getCharacteristicFromUUID(SensorTagGatt.UUID_MOV_PERI.toString()), p);
        }
    };

    /**
     * Handles switch clicks which enable/disable service or turns on the wake on shake feature.
     */
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (!isChecked) {

                switch (buttonView.getId()) {
                    case R.id.sensorSwitch:
                        mBleService.disableNotifications(mThis, SensorTagGatt.UUID_MOV_DATA);
                        positionText.setAlpha(0.4f);
                        accelData.setAlpha(0.4f);
                        gyroData.setAlpha(0.4f);
                        magData.setAlpha(0.4f);
                        periodLength.setAlpha(0.4f);
                        periodBar.setEnabled(false);
                        break;

                    case R.id.wakeOnShakeSwitch:
                        mBleService.enableMotionService(mThis, SensorTagGatt.UUID_MOV_CONF, false);
                        break;

                    default:
                        break;
                }

            } else {
                switch (buttonView.getId()) {

                    case R.id.sensorSwitch:
                        if (!mFirstTime) {
                            mBleService.enableNotifications(mThis, SensorTagGatt.UUID_MOV_DATA);
                            positionText.setAlpha(1.0f);
                            accelData.setAlpha(1.0f);
                            gyroData.setAlpha(1.0f);
                            magData.setAlpha(1.0f);
                            periodLength.setAlpha(1.0f);
                            periodBar.setEnabled(true);
                        } else {
                            mFirstTime = false;
                            //Log.d(TAG, "FIRST TIME");
                        }
                        break;

                    case R.id.wakeOnShakeSwitch:
                        mBleService.enableMotionService(mThis, SensorTagGatt.UUID_MOV_CONF, true);
                        break;

                    default:
                        break;

                }
            }
        }
    };

    /**
     * Registers the broadcast receiver
     */
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(motionUpdateReceiver, makeMotionUpdateIntentFilter());
        //Log.i(TAG, "Registering MOTION receiver");
    }

    /**
     * Unregisters the broadcast receiver
     */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(motionUpdateReceiver);
        //Log.i(TAG, "Unregistering MOTION receiver");
    }

    /**
     * Receives updates from {@code mGattUpdateReceiver#ACTION_DATA_NOTIFY} and displays updated data
     * Converts accel, gyro, and mag data separately using different conversions of the same byte[]
     */
    private final BroadcastReceiver motionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (IntentNames.ACTION_MOV_CHANGE.equals(action)) {

                //Log.i(TAG, "***************** MOTION sensed *****************");
                byte[] value = intent.getByteArrayExtra(IntentNames.EXTRAS_MOV_DATA);
                Point3D v;

                v = SensorConversion.MOVEMENT_ACC.convert(value);
                accelData.setText(Html.fromHtml(String.format("<font color=#FF0000>X:%.2fG</font>," +
                        "<font color=#00967D>Y:%.2fG</font>, <font color=#00000>Z:%.2fG</font>", v.x,v.y,v.z)));

//                seriesaccelxy.addLast(v.x, v.y);
//                seriesaccelxz.addLast(v.x, v.z);
//                seriesaccelyz.addLast(v.y, v.z);




                v = SensorConversion.MOVEMENT_GYRO.convert(value);
                gyroData.setText(Html.fromHtml(String.format("<font color=#FF0000>X:%.2f°/s</font>, " +
                        "<font color=#00967D>Y:%.2f°/s</font>, <font color=#00000>Z:%.2f°/s</font>", v.x, v.y, v.z)));

                v = SensorConversion.MOVEMENT_MAG.convert(value);
                magData.setText(Html.fromHtml(String.format("<font color=#FF0000>X:%.2fuT</font>, " +
                        "<font color=#00967D>Y:%.2fuT</font>, <font color=#00000>Z:%.2fuT</font>", v.x, v.y, v.z)));
                float[] magvalues = new float[3];
                magvalues[0] = (float)v.x;
                magvalues[1] = (float)v.y;
                magvalues[2] = (float)v.z;
                System.arraycopy(magvalues, 0, magnet, 0, 3);
                int howmanytogetforcalibration = 150;


                lastreceived++;


                if (lastreceived < howmanytogetforcalibration) {

                    Log.d(TAG, "this is where it is now" + lastreceived);
                    magx.add(magvalues[0]);
                    magy.add(magvalues[1]);
                    magz.add(magvalues[2]);
                } else if (lastreceived == howmanytogetforcalibration) {
                    calibration();
                }

                        corrected_x = (v.x - bias_x)*scalex;
                        corrected_y = (v.y - bias_y)*scaley;
                        corrected_z = (v.z - bias_z)*scalez;
                        //hard iron correction


                    seriesmagnetxy.addLast(corrected_x, corrected_y);
                    seriesmagnetxz.addLast(corrected_x, corrected_z);
                    seriesmagnetyz.addLast(corrected_y, corrected_z);
                    Log.d(TAG, "Corrected magnetometer x: " + corrected_x + " y: " + corrected_y + " z: " + corrected_z);



                if (seriesaccelxy.size() > 150) {
                    seriesaccelxy.removeFirst();
                    seriesaccelxz.removeFirst();
                    seriesaccelyz.removeFirst();

                } else if (seriesmagnetxy.size() > 150) {
                    seriesmagnetxy.removeFirst();
                    seriesmagnetxz.removeFirst();
                    seriesmagnetyz.removeFirst();

                } else if (seriesgyroxy.size() > 150) {
                    seriesgyroxy.removeFirst();
                    seriesgyroxz.removeFirst();
                    seriesgyroyz.removeFirst();

                }


                plot.post(new Runnable() {
                    public void run() {

                        plot.redraw();
                    }
                });




            }

        }
    };
    private void calibration(){
        float maxx = -999999999999999999999999f;
        float minx = 9999999999999999999999999f;
        float maxy = -999999999999999999999999f;
        float miny = 9999999999999999999999999f;
        float maxz = -999999999999999999999999f;
        float minz = 9999999999999999999999999f;

        for (int i =1; i<149; i++) {
            Log.d(TAG, "this is where this one is now" + i);

            if (magx.get(i) < minx)
                minx = magx.get(i);
            if (magx.get(i) > maxx)
                maxx = magx.get(i);

            if (magy.get(i) < miny)
                miny = magy.get(i);
            if (magy.get(i) > maxy)
                maxy = magy.get(i);

            if (magz.get(i) < minz)
                minz = magz.get(i);
            if (magz.get(i) > maxz)
                maxz = magz.get(i);
        }
//hard iron
         bias_x = (maxx + minx) / 2;
         bias_y = (maxy + miny) / 2;
         bias_z = (maxz + minz) / 2;

         delta_x = (maxx -minx)/2;
         delta_y = (maxy -miny)/2;
         delta_z = (maxz -minz)/2;
         double
                 avg_delta = (delta_x+delta_y+delta_z)/3;
         scalex = avg_delta/delta_x;
         scaley =avg_delta/delta_y;
         scalez = avg_delta/delta_z;


    }



        private static IntentFilter makeMotionUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentNames.ACTION_MOV_CHANGE);
        return intentFilter;
    }

}
