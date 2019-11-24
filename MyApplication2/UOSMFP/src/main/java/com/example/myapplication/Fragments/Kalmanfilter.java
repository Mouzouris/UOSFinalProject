package com.example.myapplication.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.Model.Point3D;
import com.example.myapplication.R;
import com.example.myapplication.SensorTag.IntentNames;
import com.example.myapplication.SensorTag.SensorConversion;

import java.util.Timer;
import java.util.TimerTask;

public class Kalmanfilter extends Fragment {
    private static final String FRAGMENT_POSITION = "com.example.myapplication.Fragments.Kalmanfilter.FRAGMENT_POSITION";

    public static final int TIME_CONSTANT = 100; //In milliseconds
    private Timer kalmanTimer = new Timer();

    private float[] gyro = new float[3];                // angular speeds from gyro
    private float[] accel = new float[3];               // accelerometer vector
    private float[] magnet = new float[3];              // magnetic field vector
    private float[] accMagOrientation = new float[3];   // orientation angles from accel and magnet
    private float[] rotationMatrix = new float[9];      // accelerometer and magnetometer based rotation matrix

    private float accel_roll;
    private float accel_pitch;

    private float kalman_roll;
    private float kalman_pitch;

    private static final float accelStd = 0.01f;   //Standard deviation of accelerometer readings - Used to initialize Pk
    private static final float gyroStd = 0.01f;    //Standard deviation of gyro readings

    private static final float Q_accel = 0.001f;    //process noise variance for accelerometer - Used to set Q
    private static final float Q_gyro = 0.003f;     //process noise variance for gyro

    private static final float R_accel = 0.03f;    //measurement noise variance for accelerometer - Used to set R
    private static final float R_gyro = 0.03f;     //measurement noise variance for gyro

    private float[] A = {1, TIME_CONSTANT/1000f, 0, 1};

    private float[] wk = {Q_accel, Q_gyro};     //process noise
    private float[] vk = {R_accel, R_gyro};     //measurement noise

    //Pitch variables
    private float[] Xk_pitch = {0, 0};        //angle and angular speed

    private float[] Zk_pitch = new float[2];      //measured values

    private float[] Pk_pitch = {accelStd*accelStd, accelStd*gyroStd,
            accelStd*gyroStd,  gyroStd*gyroStd}; //process error covariance

    private float[] R_pitch = {vk[0]*vk[0], vk[0]*vk[1],
            vk[1]*vk[0], vk[1]*vk[1]};      //measurement error covariance

    private float[] Q_pitch = {wk[0]*wk[0], wk[0]*wk[1],
            wk[1]*wk[0], wk[1]*wk[1]};      //process noise covariance

    private float[] K_pitch = new float[4];      //Kalman gain

    //Roll variables
    private float[] Xk_roll = {0, 0};        //angle and angular speed

    private float[] Zk_roll = new float[2];      //measured values

    private float[] Pk_roll = {accelStd*accelStd, accelStd*gyroStd,
            accelStd*gyroStd,  gyroStd*gyroStd}; //process error covariance

    private float[] R_roll = {vk[0]*vk[0], vk[0]*vk[1],
            vk[1]*vk[0], vk[1]*vk[1]};      //measurement error covariance

    private float[] Q_roll = {wk[0]*wk[0], wk[0]*wk[1],
            wk[1]*wk[0], wk[1]*wk[1]};      //process noise covariance

    private float[] K_roll = new float[4];      //Kalman gain

    private float[] I = {1,0,0,1};

    private TextView text1;


    public static Kalmanfilter newInstance (int position) {
        Kalmanfilter fragment = new Kalmanfilter();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public Kalmanfilter() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        kalmanTimer.scheduleAtFixedRate(new calculateKalman(), 1000, TIME_CONSTANT);

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_kalmanfilter, container, false);

        text1 =  v.findViewById(R.id.text1);


        return v;

    }





    @Override
    public void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        getActivity().unregisterReceiver(motionUpdateReceiver);

    }


    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(motionUpdateReceiver, makeMotionUpdateIntentFilter());
    }





    private static IntentFilter makeMotionUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentNames.ACTION_MOV_CHANGE);
        return intentFilter;
    }


    private final BroadcastReceiver motionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            text1.setText("Pitch: " + kalman_pitch +
                    "\nRoll: " + kalman_roll);

            final String action = intent.getAction();

            if (IntentNames.ACTION_MOV_CHANGE.equals(action)) {

                //Log.i(TAG, "***************** MOTION sensed *****************");
                byte[] value = intent.getByteArrayExtra(IntentNames.EXTRAS_MOV_DATA);
                Point3D v;


                v = SensorConversion.MOVEMENT_ACC.convert(value);
                float[] accelvalues = new float[3];
                accelvalues[0] = (float)v.x;
                accelvalues[1] = (float)v.y;
                accelvalues[2] = (float)v.z;
                System.arraycopy(accelvalues, 0, accel, 0, 3);
                calculateAccMagOrientation();
                accel_roll = (float) Math.atan2(accel[0], Math.sqrt(Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
                //Log.d("Roll: ", Float.toString(accel_roll));
                accel_pitch = (float) Math.atan2(accel[1], Math.sqrt(Math.pow(accel[0], 2) + Math.pow(accel[2], 2)));



                v = SensorConversion.MOVEMENT_GYRO.convert(value);
                float[] gyrovalues = new float[3];
                gyrovalues[0] = (float)v.x;
                gyrovalues[1] = (float)v.y;
                gyrovalues[2] = (float)v.z;
                System.arraycopy(gyrovalues, 0, gyro, 0, 3);


                v = SensorConversion.MOVEMENT_MAG.convert(value);
                float[] magvalues = new float[3];
                magvalues[0] = (float)v.x;
                magvalues[1] = (float)v.y;
                magvalues[2] = (float)v.z;
                System.arraycopy(magvalues, 0, magnet, 0, 3);




            }
        }




    };





    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }


    public float[] dot(float[] A, float[] B){
        if (A.length == 4 && B.length == 2){
            float[] C = new float[2];

            C[0] = A[0]*B[0] + A[1]*B[1];
            C[1] = A[2]*B[0] + A[3]*B[1];

            return C;
        }
        else if (A.length == 4 && B.length == 4){
            float[] C = new float [4];

            C[0] = A[0]*B[0] + A[1]*B[2];
            C[1] = A[0]*B[1] + A[1]*B[3];
            C[2] = A[2]*B[0] + A[3]*B[2];
            C[3] = A[2]*B[1] + A[3]*B[3];

            return C;
        }
        return null;
    }

    public float[] transpose(float[] A){
        if (A.length == 4){
            float[] C = new float[4];

            C[0] = A[0];
            C[1] = A[2];
            C[2] = A[1];
            C[3] = A[3];

            return C;
        }
        return null;
    }

    public float[] add(float[] A, float[] B){
        if (A.length == 2 && B.length == 2){
            float[] C = new float[2];

            C[0] = A[0]+B[0];
            C[1] = A[1]+B[1];

            return C;
        }
        else if (A.length == 4 && B.length == 4){
            float[] C = new float [4];

            C[0] = A[0]+B[0];
            C[1] = A[1]+B[1];
            C[2] = A[2]+B[2];
            C[3] = A[3]+B[3];

            return C;
        }
        return null;
    }

    public float[] subtract(float[] A, float[] B){
        if (A.length == 2 && B.length == 2){
            float[] C = new float[2];

            C[0] = A[0]-B[0];
            C[1] = A[1]-B[1];

            return C;
        }
        else if (A.length == 4 && B.length == 4){
            float[] C = new float [4];

            C[0] = A[0]-B[0];
            C[1] = A[1]-B[1];
            C[2] = A[2]-B[2];
            C[3] = A[3]-B[3];

            return C;
        }
        return null;
    }

    public float[] divide(float[] A, float[] B){
        if (A.length == 4 && B.length == 4){
            float[] C = new float [4];

            C[0] = A[0] / B[0];
            C[1] = A[1] / B[1];
            C[2] = A[2] / B[2];
            C[3] = A[3] / B[3];

            return C;
        }
        return null;
    }

    class calculateKalman extends TimerTask {
        public void run() {

            Xk_pitch = add(dot(A, Xk_pitch), wk);
            //Log.d("Xk_pitch1: ", Float.toString(Xk_pitch[0]) + " " + Float.toString(Xk_pitch[1]));

            //Log.d("Pk_pitch1: ", Float.toString(Pk_pitch[0]) + " " + Float.toString(Pk_pitch[1]) + " " +  Float.toString(Pk_pitch[2]) + " " + Float.toString(Pk_pitch[3]));
            Pk_pitch = dot(dot(A, Pk_pitch), transpose(A));
            Pk_pitch = add(Pk_pitch, Q_pitch);
            //Log.d("Pk_pitch1: ", Float.toString(Pk_pitch[0]) + " " + Float.toString(Pk_pitch[1]) + " " +  Float.toString(Pk_pitch[2]) + " " + Float.toString(Pk_pitch[3]));

            K_pitch = divide(Pk_pitch, add(Pk_pitch, R_pitch));
            //Log.d("K_pitch: ", Float.toString(K_pitch[0]) + " " + Float.toString(K_pitch[1]) + " " +  Float.toString(K_pitch[2]) + " " + Float.toString(K_pitch[3]));

            Zk_pitch[0] = 1.0f * accel_pitch;
            Zk_pitch[1] = gyro[0];
            //Log.d("Zk_pitch: ", Float.toString(Zk_pitch[0]) + " " + Float.toString(Zk_pitch[1]));

            Xk_pitch = add(Xk_pitch, dot(K_pitch, subtract(Zk_pitch, Xk_pitch)));
            //Log.d("Xk_pitch2: ", Float.toString(Xk_pitch[0]) + " " + Float.toString(Xk_pitch[1]));

            Pk_pitch = dot(subtract(I, K_pitch), Pk_pitch);
            //Log.d("Pk_pitch2: ", Float.toString(Pk_pitch[0]) + " " + Float.toString(Pk_pitch[1]) + " " +  Float.toString(Pk_pitch[2]) + " " + Float.toString(Pk_pitch[3]));

            kalman_pitch = Xk_pitch[0];


            Xk_roll = add(dot(A, Xk_roll), wk);

            Pk_roll = dot(dot(A, Pk_roll), transpose(A));
            Pk_roll = add(Pk_roll, Q_roll);
            //Log.d("Pk_roll: ", Float.toString(Pk_roll[0]) + " " + Float.toString(Pk_roll[1]) + " " +  Float.toString(Pk_roll[2]) + " " + Float.toString(Pk_roll[3]));

            K_roll = divide(Pk_roll, add(Pk_roll, R_roll));
            //Log.d("K_roll: ", Float.toString(K_roll[0]) + " " + Float.toString(K_roll[1]) + " " +  Float.toString(K_roll[2]) + " " + Float.toString(K_roll[3]));

            Zk_roll[0] = -1.0f * accel_roll;
            Zk_roll[1] = gyro[1];
            //Log.d("Zk_roll: ", Float.toString(Zk_roll[0]) + " " + Float.toString(Zk_roll[1]));

            Xk_roll = add(Xk_roll, dot(K_roll, subtract(Zk_roll, Xk_roll)));

            Pk_roll = dot(subtract(I, K_roll), Pk_roll);

            kalman_roll = Xk_roll[0];
        }
    }


}
