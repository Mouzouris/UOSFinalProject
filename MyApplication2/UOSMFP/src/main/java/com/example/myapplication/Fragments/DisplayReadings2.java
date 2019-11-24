package com.example.myapplication.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.example.myapplication.Model.Point3D;
import com.example.myapplication.R;
import com.example.myapplication.SensorTag.IntentNames;
import com.example.myapplication.SensorTag.SensorConversion;
import com.example.myapplication.Services.BleService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class DisplayReadings2 extends Fragment  implements  RadioGroup.OnCheckedChangeListener {
    private static final String FRAGMENT_POSITION = "com.example.myapplication.Fragments.DisplayReadings2.FRAGMENT_POSITION";
    private BleService mBleService;
    String TAG= "the displayreading2 frag: ";
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

    private String dateString;
    private Long date;





    private ArrayList<Float> f1startpitchvalues = new ArrayList<>();
    private ArrayList<Float> f1startyawvalues = new ArrayList<>();
    private ArrayList<Float> f1startrollvalues = new ArrayList<>();
    private ArrayList<Float> f2startpitchvalues = new ArrayList<>();
    private ArrayList<Float> f2startyawvalues = new ArrayList<>();
    private ArrayList<Float> f2startrollvalues = new ArrayList<>();
    private ArrayList<Float> f3startpitchvalues = new ArrayList<>();
    private ArrayList<Float> f3startyawvalues = new ArrayList<>();
    private ArrayList<Float> f3startrollvalues = new ArrayList<>();

    private ArrayList<Float> f1pitchvalues = new ArrayList<>();
    private ArrayList<Float> f1yawvalues = new ArrayList<>();
    private ArrayList<Float> f1rollvalues = new ArrayList<>();
    private ArrayList<Float> f2pitchvalues = new ArrayList<>();
    private ArrayList<Float> f2yawvalues = new ArrayList<>();
    private ArrayList<Float> f2rollvalues = new ArrayList<>();
    private ArrayList<Float> f3pitchvalues = new ArrayList<>();
    private ArrayList<Float> f3yawvalues = new ArrayList<>();
    private ArrayList<Float> f3rollvalues = new ArrayList<>();

    private float f1meanpitch =0f;
    private float f1meanroll =0f;
    private float f1meanyaw =0f;

    private float f2meanpitch =0f;
    private float f2meanroll =0f;
    private float f2meanyaw =0f;

    private float f3meanpitch =0f;
    private float f3meanroll =0f;
    private float f3meanyaw =0f;
    private boolean stationary =false;
    private boolean isStationary =false;

    private float f1startingpitch =0f;
    private float f1startingroll =0f;
    private float f1startingyaw =0f;

    private float f2startingpitch =0f;
    private float f2startingroll =0f;
    private float f2startingyaw =0f;

    private float f3startingpitch =0f;
    private float f3startingroll =0f;
    private float f3startingyaw =0f;
    private boolean isup = false;


    private float f1sumpitch =0f;
    private float f1sumroll =0f;
    private float f1sumyaw =0f;

    private float f2sumpitch =0f;
    private float f2sumroll =0f;
    private float f2sumyaw =0f;

    private float f3sumpitch =0f;
    private float f3sumroll =0f;
    private float f3sumyaw =0f;

    private float f1currentpitch;
    private float f1currentyaw;
    private float f1currentroll;

    private float f2currentpitch;
    private float f2currentyaw;
    private float f2currentroll;

    private float f3currentpitch;
    private float f3currentyaw;
    private float f3currentroll;

    //for graphing purposes
    private float currentpitch;
    private float currentyaw;
    private float currentroll;

//chcek for stationary
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;
    private int hitCount = 0;
    private double hitSum = 0;
    private double hitResult = 0;


    // angular speeds from gyro
    private float[] gyro = new float[3];

    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];

    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];

    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;

    public static final int TIME_CONSTANT = 30;
    public static final float FILTER_COEFFICIENT = 0.98f;
    private Timer fuseTimer = new Timer();

    // The following members are only for displaying the sensor output.
    public Handler mHandler;

    private LineGraphSeries<DataPoint> PitchLine;
    private LineGraphSeries<DataPoint> YawLine;
    private LineGraphSeries<DataPoint> RollLine;
    public GraphView graph;

    private SimpleXYSeries seriesPitch;
    private SimpleXYSeries seriesRoll;
    private SimpleXYSeries seriesYaw;
    private XYPlot plot;






    private double lastreceived = 0;

    private RadioGroup mRadioGroup;
    private TextView mAzimuthView;
    private TextView mPitchView;
    private TextView mRollView;
    private int radioSelection;
    DecimalFormat d = new DecimalFormat("#.##");

    public static DisplayReadings2 newInstance (int position) {
        DisplayReadings2 fragment = new DisplayReadings2();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.display_readings2_fragmet, container, false);
        graph =  v.findViewById(R.id.graphview);
        graph.setTitle("Pitch/Roll/Yaw");

        PitchLine = new LineGraphSeries<>();
        graph.addSeries(PitchLine);
        PitchLine.setTitle("Pitch");
        PitchLine.setColor(Color.CYAN);

        YawLine= new LineGraphSeries<>();
        graph.addSeries(YawLine);
        YawLine.setTitle("Yaw");
        YawLine.setColor(Color.GREEN);


        RollLine= new LineGraphSeries<>();
        graph.addSeries(RollLine);
        RollLine.setTitle("Roll");
        RollLine.setColor(Color.RED);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxX(lastreceived);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMinY(-180);
        graph.getViewport().setMaxY(180);


        graph.getViewport().setScalable(true);

        graph.getViewport().setScrollable(true);


        //androidplot
        plot = v.findViewById(R.id.mySimpleXYPlot2);
        plot.setBackgroundColor(0);

        seriesPitch = new SimpleXYSeries("Pitch");
        seriesRoll = new SimpleXYSeries("Roll");
        seriesYaw = new SimpleXYSeries("Yaw");

        plot.setTitle("Roll/Pitch/Yaw");
        plot.setRangeLabel("Degrees");
        plot.setDomainLabel("Time");
        plot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        plot.setRangeStepValue(10);




        seriesPitch.useImplicitXVals();
        seriesRoll.useImplicitXVals();
        seriesYaw.useImplicitXVals();
        plot.getLegend().setVisible(false);




        plot.setRangeBoundaries(-200,200, BoundaryMode.FIXED);
        plot.addSeries( seriesPitch, new LineAndPointFormatter((Color.rgb(0,200,200)),null,null,null));
        plot.addSeries( seriesRoll, new LineAndPointFormatter((Color.rgb(200,0,0)),null,null, null));
        plot.addSeries( seriesYaw, new LineAndPointFormatter((Color.rgb(0,200,0)),null,null, null));








        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;



        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);

        // GUI stuff
        mHandler = new Handler();
        radioSelection = 0;
        d.setRoundingMode(RoundingMode.HALF_UP);
        d.setMaximumFractionDigits(3);
        d.setMinimumFractionDigits(3);
        mRadioGroup = v.findViewById(R.id.radioGroup1);
        mAzimuthView = v.findViewById(R.id.textView4);
        mPitchView =  v.findViewById(R.id.textView5);
        mRollView =  v.findViewById(R.id.textView6);
        mRadioGroup.setOnCheckedChangeListener(this);

        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        dateString = sdf.format(date);


        return v;
    }




    @Override
    public void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        getActivity().unregisterReceiver(motionUpdateReceiver);
        PitchLine.resetData(new DataPoint[]{});
        YawLine.resetData(new DataPoint[]{});
        RollLine.resetData(new DataPoint[]{});
        lastreceived = 0;

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(motionUpdateReceiver, makeMotionUpdateIntentFilter());
        // we're going to simulate real time with thread that append data to the graph

        new Thread(new Runnable() {

            @Override
            public void run() {
                            updategraph();
                    }

        }).start();

    }
private void checkforstationary(float[] accelerometer){

    double x = accelerometer[0];
    double y = accelerometer[1];
    double z = accelerometer[2];
    mAccelLast = mAccelCurrent;
    mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
    double delta = mAccelCurrent - mAccelLast;
    mAccel = mAccel * 0.9f + delta;

    int SAMPLE_SIZE = 1;
    if (hitCount <= SAMPLE_SIZE) {
        hitCount++;
        hitSum += Math.abs(mAccel);
    } else {
        hitResult = hitSum / SAMPLE_SIZE;

        Log.d(TAG, String.valueOf(hitResult));

        double THRESHOLD = 0.2;
        isStationary = !(hitResult > THRESHOLD);

        hitCount = 0;
        hitSum = 0;
        hitResult = 0;
    }
}


    // This function registers sensor listeners for the accelerometer, magnetometer and gyroscope.

    private final BroadcastReceiver motionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            final String action = intent.getAction();
            lastreceived++;
            if (lastreceived == 30) {
                startingposition();

            }
            if (lastreceived > 30){
                Log.d(TAG, "last received going to check for up");
                getconstantvaluesmean();
                //if(Math.round(lastreceived)%13==0) {


                    if (!isup&& stationary&&isStationary) {
                        //checkforup();

                        mHandler.postDelayed(Checkforup, 1500);
                    } else if (isup&& stationary&&isStationary ) {
                        //checkfordown();


                        mHandler.postDelayed(CheckforDown, 1500);
                    }
               // }

            }

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
                checkforstationary(accelvalues);






                v = SensorConversion.MOVEMENT_GYRO.convert(value);
                float[] gyrovalues = new float[3];
                gyrovalues[0] = (float)v.x;
                gyrovalues[1] = (float)v.y;
                gyrovalues[2] = (float)v.z;
                System.arraycopy(gyrovalues, 0, gyro, 0, 3);
                Long now = System.currentTimeMillis();
                gyroFunction(gyrovalues, now);


                v = SensorConversion.MOVEMENT_MAG.convert(value);
                float[] magvalues = new float[3];
                magvalues[0] = (float)v.x;
                magvalues[1] = (float)v.y;
                magvalues[2] = (float)v.z;
                System.arraycopy(magvalues, 0, magnet, 0, 3);



                checkifstationary();

            }
        }
    };




    // calculates orientation angles from accelerometer and magnetometer output
    public void calculateAccMagOrientation() {

        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }


    private void getRotationVectorFromGyro(float[] gyroValues,  float[] deltaRotationVector,  float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    public void gyroFunction(float[] gyrovalues, Long eventtimestamp) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (eventtimestamp - timestamp) * NS2S;
            System.arraycopy(gyrovalues, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = eventtimestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (yaw)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, yaw)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;


            // yaw
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }

            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }

            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);


            // update sensor output in GUI
            mHandler.post(updateOreintationDisplayTask);
        }
    }


    // **************************** GUI FUNCTIONS & ALGORITHMS*********************************

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch(checkedId) {
            case R.id.radio0:
                radioSelection = 0;
                break;
            case R.id.radio1:
                radioSelection = 1;
                break;
            case R.id.radio2:
                radioSelection = 2;
                break;
        }
    }

    public void updateOrientationDisplay() {
        switch(radioSelection) {
            case 0:

                mAzimuthView.setText(d.format(accMagOrientation[0] * 180/Math.PI) );
                mPitchView.setText(d.format(accMagOrientation[1] * 180/Math.PI) );
                mRollView.setText(d.format(accMagOrientation[2] * 180/Math.PI));
                break;
            case 1:
                mAzimuthView.setText(d.format(gyroOrientation[0] * 180/Math.PI) );
                mPitchView.setText(d.format(gyroOrientation[1] * 180/Math.PI) );
                mRollView.setText(d.format(gyroOrientation[2] * 180/Math.PI) );

                break;
            case 2:
                mAzimuthView.setText(d.format(fusedOrientation[0] * 180/Math.PI)  );
                mPitchView.setText(d.format(fusedOrientation[1] * 180/Math.PI) );
                mRollView.setText(d.format(fusedOrientation[2] * 180/Math.PI) );
               break;
        }
    }

    private Runnable Checkforup = new Runnable() {
        public void run() {
          checkforup();




        }
    };
    private Runnable CheckforDown = new Runnable() {
        public void run() {
            checkfordown();


        }
    };

    private Runnable updateOreintationDisplayTask = new Runnable() {
        public void run() {
            updateOrientationDisplay();

                    updategraph();




        }
    };



private void startingposition() {


    int initialvalues = 30;
    if ((accMagOrientation[0] * 180 / Math.PI) != 0) {


        for (int y = 0; y < initialvalues; y++) {

            f1startpitchvalues.add((float) (accMagOrientation[1] * 180 / Math.PI));
            f1startrollvalues.add((float) (accMagOrientation[2] * 180 / Math.PI));
            f1startyawvalues.add((float) (accMagOrientation[0] * 180 / Math.PI));



            f1sumroll = (f1startrollvalues.get(y) + f1sumroll);
            f1sumyaw = (f1startyawvalues.get(y) + f1sumyaw);
            f1sumpitch = (f1startpitchvalues.get(y) + f1sumpitch);
        }


        f1startingroll = f1sumroll / initialvalues;
        f1startingyaw = f1sumyaw / initialvalues;
        f1startingpitch = f1sumpitch / initialvalues;
        Log.d(TAG, "the f1roll: "+f1startingroll+"the f1yaw: "+f1startingyaw+"the f1pitch: "+f1startingpitch);


    }

    if ((gyroOrientation[0] * 180 / Math.PI) != 0) {


        for (int y = 0; y < initialvalues; y++) {

            f2startpitchvalues.add((float) (gyroOrientation[1] * 180 / Math.PI));
            f2startrollvalues.add((float) (gyroOrientation[2] * 180 / Math.PI));
            f2startyawvalues.add((float) (gyroOrientation[0] * 180 / Math.PI));

            f2sumroll = (f2startrollvalues.get(y) + f2sumroll);
            f2sumyaw = (f2startyawvalues.get(y) + f2sumyaw);
            f2sumpitch = (f2startpitchvalues.get(y) + f2sumpitch);

        }


        f2startingroll = f2sumroll / initialvalues;
        f2startingyaw = f2sumyaw / initialvalues;
        f2startingpitch = f2sumpitch / initialvalues;
        Log.d(TAG, "the f2roll: "+f2startingroll+"the f2yaw: "+f2startingyaw+"the f2pitch: "+f2startingpitch);


    }


    if ((fusedOrientation[0] * 180 / Math.PI) != 0) {


        for (int y = 0; y < initialvalues; y++) {

            f3startpitchvalues.add((float) (fusedOrientation[1] * 180 / Math.PI));
            f3startrollvalues.add((float) (fusedOrientation[2] * 180 / Math.PI));
            f3startyawvalues.add((float) (fusedOrientation[0] * 180 / Math.PI));

            f3sumroll = (f3startrollvalues.get(y) + f3sumroll);
            f3sumyaw = (f3startyawvalues.get(y) + f3sumyaw);
            f3sumpitch = (f3startpitchvalues.get(y) + f3sumpitch);
        }


        f3startingroll = f3sumroll / initialvalues;
        f3startingyaw = f3sumyaw / initialvalues;
        f3startingpitch = f3sumpitch / initialvalues;
        Log.d(TAG, "the f3roll: "+f3startingroll+"the f3yaw: "+f3startingyaw+"the f3pitch: "+f3startingpitch);




    }

}


private void checkifstationary(){
    boolean pitchstationary = false;
    boolean rollstationary = false;
    boolean yawstationary = false;

    Log.d(TAG, "the what is now pitch "+f1currentpitch+"what is mean pitch"+ f1meanpitch);
    Log.d(TAG, "the what is now roll "+f1currentroll+"what is mean roll"+ f1meanroll);
    Log.d(TAG, "the what is now yaw "+f1currentyaw+"what is mean yaw"+ f1meanyaw);

    //if rounding = 0 then put valious of two decimals up and two decimals down
     if (((((Math.round(f1currentpitch) == 0)|| (Math.round(f1currentpitch) == 1)) || ((Math.round(f1meanpitch) == 0)|| (Math.round(f1meanpitch) == 1)))
     || (((Math.round(f2currentpitch) == 0)|| (Math.round(f2currentpitch) == 1)) || ((Math.round(f2meanpitch) == 0)|| (Math.round(f2meanpitch) == 1)))
     || (((Math.round(f3currentpitch) == 0)|| (Math.round(f3currentpitch) == 1)) || ((Math.round(f3meanpitch) == 0)|| (Math.round(f3meanpitch) == 1))))
             //negative values
     || ((( Math.round(f1currentpitch) == -1) || (Math.round(f1meanpitch) == -1))
     || ( ( Math.round(f2currentpitch) == -1) || (Math.round(f2meanpitch) == -1))
     || ( ( Math.round(f3currentpitch) == -1) || (Math.round(f3meanpitch) == -1))))

    {
         if ((((f1currentpitch < f1meanpitch + 3) && (f1currentpitch > f1meanpitch - 3)) || ((f1currentpitch > f1meanpitch + 3) && (f1currentpitch < f1meanpitch - 3)))
                 || (((f2currentpitch < f2meanpitch + 3) && (f2currentpitch > f2meanpitch - 3)) || ((f2currentpitch > f2meanpitch + 3) && (f2currentpitch < f2meanpitch - 3)))
                 || (((f3currentpitch < f3meanpitch + 3) && (f3currentpitch > f3meanpitch - 3)) || ((f3currentpitch > f3meanpitch + 3) && (f3currentpitch < f3meanpitch - 3))))
                 {
             Log.d(TAG, "the pitch is stationary now ");
             pitchstationary=true;
         }else
         {
             Log.d(TAG, "the pitch is not stationary yet moves to second class ");

         }

     } else{                                                                                //negative values
     if ((((f1currentpitch < f1meanpitch * 1.03) && (f1currentpitch > f1meanpitch * 0.97)) || ((f1currentpitch > f1meanpitch * 1.03) && (f1currentpitch < f1meanpitch * 0.97)))
             || (((f2currentpitch < f2meanpitch * 1.03) && (f2currentpitch > f2meanpitch * 0.97)) || ((f2currentpitch > f2meanpitch * 1.03) && (f2currentpitch < f2meanpitch * 0.97)))
             ||(((f3currentpitch < f3meanpitch * 1.03) && (f3currentpitch > f3meanpitch * 0.97)) || ((f3currentpitch > f3meanpitch * 1.03) && (f3currentpitch < f3meanpitch * 0.97))))

     {
         Log.d(TAG, "the pitch is stationary now ");
         pitchstationary=true;
     }else
     {
         Log.d(TAG, "the pitch is not stationary on second class fail");

     }



     }

    if (((((Math.round(f1currentroll) == 0)|| (Math.round(f1currentroll) == 1)) || ((Math.round(f1meanroll) == 0)|| (Math.round(f1meanroll) == 1)))
    || (((Math.round(f2currentroll) == 0)|| (Math.round(f2currentroll) == 1)) || ((Math.round(f2meanroll) == 0)|| (Math.round(f2meanroll) == 1)))
    || (((Math.round(f3currentroll) == 0)|| (Math.round(f3currentroll) == 1)) || ((Math.round(f3meanroll) == 0)|| (Math.round(f3meanroll) == 1))))
        //negative values
    || ((( Math.round(f1currentroll) == -1) || (Math.round(f1meanroll) == -1))
    || ( ( Math.round(f2currentroll) == -1) || (Math.round(f2meanroll) == -1))
    || ( ( Math.round(f3currentroll) == -1) || (Math.round(f3meanroll) == -1))))
    {
        if ((((f1currentroll < f1meanroll + 3) && (f1currentroll > f1meanroll - 3)) || ((f1currentroll > f1meanroll + 3) && (f1currentroll < f1meanroll - 3)))
        || (((f2currentroll < f2meanroll + 3) && (f2currentroll > f2meanroll - 3)) || ((f2currentroll > f2meanroll + 3) && (f2currentroll < f2meanroll - 3)))
        || (((f3currentroll < f3meanroll + 3) && (f3currentroll > f3meanroll - 3)) || ((f3currentroll > f3meanroll + 3) && (f3currentroll < f3meanroll - 3)))){

                Log.d(TAG, "the roll is stationary now ");
                rollstationary=true;
            }else
            {
                Log.d(TAG, "the roll is not stationary yet moves to second class ");

            }
        }else
    {
        if    ((((f1currentroll < f1meanroll * 1.03) && (f1currentroll > f1meanroll * 0.97)) || ((f1currentroll > f1meanroll * 1.03) && (f1currentroll < f1meanroll * 0.97)))
            || (((f2currentroll < f2meanroll * 1.03) && (f2currentroll > f2meanroll * 0.97))||((f2currentroll > f2meanroll * 1.03) && (f2currentroll < f2meanroll * 0.97)))
            || (((f3currentroll < f3meanroll * 1.03) && (f3currentroll > f3meanroll * 0.97))||((f3currentroll > f3meanroll * 1.03) && (f3currentroll < f3meanroll * 0.97))))
        {
            Log.d(TAG, "the roll is stationary now ");
            rollstationary=true;
        }else
        {
            Log.d(TAG, "the roll is not stationary failed ");

        }

        }



    if (((((Math.round(f1currentyaw) == 0)|| (Math.round(f1currentyaw) == 1)) || ((Math.round(f1meanyaw) == 0)|| (Math.round(f1meanyaw) == 1)))
    || (((Math.round(f2currentyaw) == 0)|| (Math.round(f2currentyaw) == 1)) || ((Math.round(f2meanyaw) == 0)|| (Math.round(f2meanyaw) == 1)))
    || (((Math.round(f3currentyaw) == 0)|| (Math.round(f3currentyaw) == 1)) || ((Math.round(f3meanyaw) == 0)|| (Math.round(f3meanyaw) == 1))))
            //negative values
    || ((( Math.round(f1currentyaw) == -1) || (Math.round(f1meanyaw) == -1))
    || ( ( Math.round(f2currentyaw) == -1) || (Math.round(f2meanyaw) == -1))
    || ( ( Math.round(f3currentyaw) == -1) || (Math.round(f3meanyaw) == -1))))
    {


     if ((((f1currentyaw < f1meanyaw + 3) && (f1currentyaw > f1meanyaw - 3)) || ((f1currentyaw > f1meanyaw + 3) && (f1currentyaw < f1meanyaw - 3)))
    || (((f2currentyaw < f2meanyaw + 3) && (f2currentyaw > f2meanyaw - 3)) || ((f2currentyaw > f2meanyaw + 3) && (f2currentyaw < f2meanyaw - 3)))
    || (((f3currentyaw < f3meanyaw + 3) && (f3currentyaw > f3meanyaw - 3)) || ((f3currentyaw > f3meanyaw + 3) && (f3currentyaw < f3meanyaw - 3)))){

         Log.d(TAG, "the yaw is stationary now ");
         yawstationary=true;
     }else
     {
         Log.d(TAG, "the yaw is not stationary yet moves to second class ");

     }
    }else


    {
        if ((((f1currentyaw < f1meanyaw * 1.03) && (f1currentyaw > f1meanyaw * 0.97)) || ((f1currentyaw > f1meanyaw * 1.03) && (f1currentyaw < f1meanyaw * 0.97)))
            ||(((f2currentyaw < f2meanyaw * 1.03) && (f2currentyaw > f2meanyaw * 0.97))||((f2currentyaw > f2meanyaw * 1.03) && (f2currentyaw < f2meanyaw * 0.97)))
            ||(((f3currentyaw < f3meanyaw * 1.03) && (f3currentyaw > f3meanyaw * 0.97))||((f3currentyaw > f3meanyaw * 1.03) && (f3currentyaw < f3meanyaw * 0.97))))
        {
            Log.d(TAG, "the yaw is stationary now ");
            yawstationary=true;
        }else
        {
            Log.d(TAG, "the yaw is not stationary failed");

        }
    }


    if (rollstationary && pitchstationary && yawstationary)
    {
        Log.d(TAG, "finally stationary ");
        stationary=true;
        Log.d(TAG, "stationary: "+(stationary));
    }
    else {
        stationary = false;
        Log.d(TAG, "stationary: "+(stationary));





    }

}


private void getconstantvaluesmean() {
        int howmanytogetformean = 8;
        f1sumpitch = 0;
        f1sumroll = 0;
        f1sumyaw = 0;
        f2sumpitch = 0;
        f2sumroll = 0;
        f2sumyaw = 0;
        f3sumpitch = 0;
        f3sumroll = 0;
        f3sumyaw = 0;


        f1pitchvalues.add((float) (accMagOrientation[1] * 180 / Math.PI));
        f1rollvalues.add((float) (accMagOrientation[2] * 180 / Math.PI));
        f1yawvalues.add((float) (accMagOrientation[0] * 180 / Math.PI));

        f1currentpitch = (float)(accMagOrientation[1] * 180 / Math.PI);
        f1currentroll = (float)(accMagOrientation[2] * 180 / Math.PI);
        f1currentyaw = (float)(accMagOrientation[0] * 180 / Math.PI);

        int f1listsize = f1pitchvalues.size();

        if (f1listsize > howmanytogetformean) {
            int f1startfrom = f1listsize - howmanytogetformean;

            for (int y = f1startfrom; y < f1listsize; y++) {
                f1sumroll = (f1rollvalues.get(y) + f1sumroll);
                f1sumyaw = (f1yawvalues.get(y) + f1sumyaw);
                f1sumpitch = (f1pitchvalues.get(y) + f1sumpitch);
            }


            f1meanroll = f1sumroll / howmanytogetformean;
            f1meanyaw = f1sumyaw / howmanytogetformean;
            f1meanpitch = f1sumpitch / howmanytogetformean;
           // Log.d(TAG, "the f1meanroll: " + f1meanroll + "the f1meanyaw: " + f1meanyaw + "the f1meanpitch: " + f1meanpitch);

        } else {
            for (int y = 0; y < f1listsize; y++) {
                f1sumroll = (f1rollvalues.get(y) + f1sumroll);
                f1sumyaw = (f1yawvalues.get(y) + f1sumyaw);
                f1sumpitch = (f1pitchvalues.get(y) + f1sumpitch);


            }
            f1meanroll = f1sumroll / f1listsize;
            f1meanyaw = f1sumyaw / f1listsize;
            f1meanpitch = f1sumpitch / f1listsize;
            //Log.d(TAG, "the f1meanroll: " + f1meanroll + "the f1meanyaw: " + f1meanyaw + "the f1meanpitch: " + f1meanpitch);


        }


        f2pitchvalues.add((float) (gyroOrientation[1] * 180 / Math.PI));
        f2rollvalues.add((float) (gyroOrientation[2] * 180 / Math.PI));
        f2yawvalues.add((float) (gyroOrientation[0] * 180 / Math.PI));

        f2currentpitch = (float) (gyroOrientation[1] * 180 / Math.PI);
        f2currentroll = (float) (gyroOrientation[2] * 180 / Math.PI);
        f2currentyaw = (float) (gyroOrientation[0] * 180 / Math.PI);


        int f2listsize = f2pitchvalues.size();

        if (f2listsize > howmanytogetformean) {
           int f2startfrom = f2listsize - howmanytogetformean;

            for (int y = f2startfrom; y < f2listsize; y++) {
                f2sumroll = (f2rollvalues.get(y) + f2sumroll);
                f2sumyaw = (f2yawvalues.get(y) + f2sumyaw);
                f2sumpitch = (f2pitchvalues.get(y) + f2sumpitch);

            }


            f2meanroll = f2sumroll / howmanytogetformean;
            f2meanyaw = f2sumyaw / howmanytogetformean;
            f2meanpitch = f2sumpitch / howmanytogetformean;
            //Log.d(TAG, "the f2meanroll: " + f2meanroll + "the f2meanyaw: " + f2meanyaw + "the f2meanpitch: " + f2meanpitch);

        } else {
            for (int y = 0; y < f2listsize; y++) {
                f2sumroll = (f2rollvalues.get(y) + f2sumroll);
                f2sumyaw = (f2yawvalues.get(y) + f2sumyaw);
                f2sumpitch = (f2pitchvalues.get(y) + f2sumpitch);


            }
            f2meanroll = f2sumroll / f2listsize;
            f2meanyaw = f2sumyaw / f2listsize;
            f2meanpitch = f2sumpitch / f2listsize;
            //Log.d(TAG, "the f2meanroll: " + f2meanroll + "the f2meanyaw: " + f2meanyaw + "the f2meanpitch: " + f2meanpitch);


        }




                f3pitchvalues.add((float) (fusedOrientation[1] * 180 / Math.PI));
                f3rollvalues.add((float) (fusedOrientation[2] * 180 / Math.PI));
                f3yawvalues.add((float) (fusedOrientation[0] * 180 / Math.PI));

                f3currentpitch = (float) (fusedOrientation[1] * 180 / Math.PI);
                f3currentroll = (float) (fusedOrientation[2] * 180 / Math.PI);
                f3currentyaw = (float) (fusedOrientation[0] * 180 / Math.PI);



        int f3listsize = f3pitchvalues.size();

                if (f3listsize > howmanytogetformean) {
                  int f3startfrom = f3listsize - howmanytogetformean;

                    for (int y = f3startfrom; y < f3listsize; y++) {

                        f3sumroll = (f3rollvalues.get(y) + f3sumroll);
                        f3sumyaw = (f3yawvalues.get(y) + f3sumyaw);
                        f3sumpitch = (f3pitchvalues.get(y) + f3sumpitch);


                    }


                    f3meanroll = f3sumroll / howmanytogetformean;
                    f3meanyaw = f3sumyaw / howmanytogetformean;
                    f3meanpitch = f3sumpitch / howmanytogetformean;
                   // Log.d(TAG, "the f3meanroll: " + f3meanroll + "the f3meanyaw: " + f3meanyaw + "the f3pitch: " + f3meanpitch);


                } else {
                    for (int y = 0; y < f3listsize; y++) {
                        f3sumroll = (f3rollvalues.get(y) + f3sumroll);
                        f3sumyaw = (f3yawvalues.get(y) + f3sumyaw);
                        f3sumpitch = (f3pitchvalues.get(y) + f3sumpitch);


                    }
                    f3meanroll = f3sumroll / f3listsize;
                    f3meanyaw = f3sumyaw / f3listsize;
                    f3meanpitch = f3sumpitch / f3listsize;
                   // Log.d(TAG, "the f3meanroll: " + f3meanroll + "the f3meanyaw: " + f3meanyaw + "the f3meanpitch: " + f3meanpitch);

            }
        }






    private void checkforup() {
        int howmanytogetforcheckingup = 8;


        int up = 0;
        int listsize = f1pitchvalues.size();

        if (listsize > howmanytogetforcheckingup) {
            int startfrom = listsize - howmanytogetforcheckingup;

            for (int y = startfrom; y < listsize; y++) {
                if (y + 7 < listsize) {
                    float f1pitch1 = f1pitchvalues.get(y);
                    float f1pitch2 = f1pitchvalues.get(y + 1);
                    float f1pitch3 = f1pitchvalues.get(y + 2);
                    float f1pitch4 = f1pitchvalues.get(y + 3);
                    float f1pitch5 = f1pitchvalues.get(y + 4);
                    float f1pitch6 = f1pitchvalues.get(y + 5);
                    float f1pitch7 = f1pitchvalues.get(y + 6);
                    float f1pitch8 = f1pitchvalues.get(y + 7);

                    float f1roll1 = f1rollvalues.get(y);
                    float f1roll2 = f1rollvalues.get(y + 1);
                    float f1roll3 = f1rollvalues.get(y + 2);
                    float f1roll4 = f1rollvalues.get(y + 3);
                    float f1roll5 = f1rollvalues.get(y + 4);
                    float f1roll6 = f1rollvalues.get(y + 5);
                    float f1roll7 = f1rollvalues.get(y + 6);
                    float f1roll8 = f1rollvalues.get(y + 7);


                    float f1yaw1 = f1yawvalues.get(y);
                    float f1yaw2 = f1yawvalues.get(y + 1);
                    float f1yaw3 = f1yawvalues.get(y + 2);
                    float f1yaw4 = f1yawvalues.get(y + 3);
                    float f1yaw5 = f1yawvalues.get(y + 4);
                    float f1yaw6 = f1yawvalues.get(y + 5);
                    float f1yaw7 = f1yawvalues.get(y + 6);
                    float f1yaw8 = f1yawvalues.get(y + 7);

                    float f2pitch1 = f2pitchvalues.get(y);
                    float f2pitch2 = f2pitchvalues.get(y + 1);
                    float f2pitch3 = f2pitchvalues.get(y + 2);
                    float f2pitch4 = f2pitchvalues.get(y + 3);
                    float f2pitch5 = f2pitchvalues.get(y + 4);
                    float f2pitch6 = f2pitchvalues.get(y + 5);
                    float f2pitch7 = f2pitchvalues.get(y + 6);
                    float f2pitch8 = f2pitchvalues.get(y + 7);

                    float f2roll1 = f2rollvalues.get(y);
                    float f2roll2 = f2rollvalues.get(y + 1);
                    float f2roll3 = f2rollvalues.get(y + 2);
                    float f2roll4 = f2rollvalues.get(y + 3);
                    float f2roll5 = f2rollvalues.get(y + 4);
                    float f2roll6 = f2rollvalues.get(y + 5);
                    float f2roll7 = f2rollvalues.get(y + 6);
                    float f2roll8 = f2rollvalues.get(y + 7);

                    float f2yaw1 = f2yawvalues.get(y);
                    float f2yaw2 = f2yawvalues.get(y + 1);
                    float f2yaw3 = f2yawvalues.get(y + 2);
                    float f2yaw4 = f2yawvalues.get(y + 3);
                    float f2yaw5 = f2yawvalues.get(y + 4);
                    float f2yaw6 = f2yawvalues.get(y + 5);
                    float f2yaw7 = f2yawvalues.get(y + 6);
                    float f2yaw8 = f2yawvalues.get(y + 7);

                    float f3pitch1 = f3pitchvalues.get(y);
                    float f3pitch2 = f3pitchvalues.get(y + 1);
                    float f3pitch3 = f3pitchvalues.get(y + 2);
                    float f3pitch4 = f3pitchvalues.get(y + 3);
                    float f3pitch5 = f3pitchvalues.get(y + 4);
                    float f3pitch6 = f3pitchvalues.get(y + 5);
                    float f3pitch7 = f3pitchvalues.get(y + 6);
                    float f3pitch8 = f3pitchvalues.get(y + 7);

                    float f3roll1 = f3rollvalues.get(y);
                    float f3roll2 = f3rollvalues.get(y + 1);
                    float f3roll3 = f3rollvalues.get(y + 2);
                    float f3roll4 = f3rollvalues.get(y + 3);
                    float f3roll5 = f3rollvalues.get(y + 4);
                    float f3roll6 = f3rollvalues.get(y + 5);
                    float f3roll7 = f3rollvalues.get(y + 6);
                    float f3roll8 = f3rollvalues.get(y + 7);

                    float f3yaw1 = f3yawvalues.get(y);
                    float f3yaw2 = f3yawvalues.get(y + 1);
                    float f3yaw3 = f3yawvalues.get(y + 2);
                    float f3yaw4 = f3yawvalues.get(y + 3);
                    float f3yaw5 = f3yawvalues.get(y + 4);
                    float f3yaw6 = f3yawvalues.get(y + 5);
                    float f3yaw7 = f3yawvalues.get(y + 6);
                    float f3yaw8 = f3yawvalues.get(y + 7);


                    boolean f1pitchuptodown = (f1pitch1 > f1pitch2) && (f1pitch2 > f1pitch3) && (f1pitch3 > f1pitch4) && (f1pitch4 > f1pitch5) &&
                            (f1pitch5 > f1pitch6) && (f1pitch6 > f1pitch7) && (f1pitch7 > f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / 10) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / 10)));
                    boolean f1pitchdowntoup = (f1pitch1 < f1pitch2) && (f1pitch2 < f1pitch3) && (f1pitch3 < f1pitch4) && (f1pitch4 < f1pitch5) &&
                            (f1pitch5 < f1pitch6) && (f1pitch6 < f1pitch7) && (f1pitch7 < f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / 10) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / 10)));

                    boolean f2pitchuptodown = (f2pitch1 > f2pitch2) && (f2pitch2 > f2pitch3) && (f2pitch3 > f2pitch4) && (f2pitch4 > f2pitch5) &&
                            (f2pitch5 > f2pitch6) && (f2pitch6 > f2pitch7) && (f2pitch7 > f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / 10) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / 10)));
                    boolean f2pitchdowntoup = (f2pitch1 < f2pitch2) && (f2pitch2 < f2pitch3) && (f2pitch3 < f2pitch4) && (f2pitch4 < f2pitch5) &&
                            (f2pitch5 < f2pitch6) && (f2pitch6 < f2pitch7) && (f2pitch7 < f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / 10) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / 10)));

                    boolean f3pitchuptodown = (f3pitch1 > f3pitch2) && (f3pitch2 > f3pitch3) && (f3pitch3 > f3pitch4) && (f3pitch4 > f3pitch5) &&
                            (f3pitch5 > f3pitch6) && (f3pitch6 > f3pitch7) && (f3pitch7 > f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / 10) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / 10)));
                    boolean f3pitchdowntoup = (f3pitch1 < f3pitch2) && (f3pitch2 < f3pitch3) && (f3pitch3 < f3pitch4) && (f3pitch4 < f3pitch5) &&
                            (f3pitch5 < f3pitch6) && (f3pitch6 < f3pitch7) && (f3pitch7 < f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / 10) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / 10)));


                    boolean f1rolluptodown = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 > f1roll5) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));
                    boolean f1rolldowntoup = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 < f1roll5) &&
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));
                    boolean f1rolluptodownflip = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 < f1roll5) && (f1roll4 < -140 && f1roll5 > 140) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));
                    boolean f1rolldowntoupflip = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 > f1roll5) && (f1roll4 > 140 && f1roll5 < -140) &&
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));

                    boolean f2rolluptodown = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 > f2roll5) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));
                    boolean f2rolldowntoup = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 < f2roll5) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));
                    boolean f2rolluptodownflip = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 < f2roll5) && (f2roll4 < -140 && f2roll5 > 140) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));
                    boolean f2rolldowntoupflip = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 > f2roll5) && (f2roll4 > 140 && f2roll5 < -140) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));


                    boolean f3rolluptodown = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 > f3roll5) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));
                    boolean f3rolldowntoup = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 < f3roll5) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));
                    boolean f3rolluptodownflip = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 < f3roll5) && (f3roll4 < -140 && f3roll5 > 140) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));
                    boolean f3rolldowntoupflip = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 > f3roll5) && (f3roll4 > 140 && f3roll5 < -140) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));


                    boolean f1yawuptodown = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 > f1yaw5) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));

                    boolean f1yawdowntoup = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 < f1yaw5) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));

                    boolean f1yawuptodownflip = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 < f1yaw5) && (f1yaw4 < -140 && f1yaw5 > 140) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));
                    boolean f1yawdowntoupflip = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 > f1yaw5) && (f1yaw4 > 140 && f1yaw5 < -140) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));

                    boolean f2yawuptodown = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 > f2yaw5) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));
                    boolean f2yawdowntoup = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 < f2yaw5) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));
                    boolean f2yawuptodownflip = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 < f2yaw5) && (f2yaw4 < -140 && f2yaw5 > 140) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));
                    boolean f2yawdowntoupflip = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 > f2yaw5) && (f2yaw4 > 140 && f2yaw5 < -140) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));


                    boolean f3yawuptodown = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 > f3yaw5) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));
                    boolean f3yawdowntoup = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 < f3yaw5) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));
                    boolean f3yawuptodownflip = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 < f3yaw5) && (f3yaw4 < -140 && f3yaw5 > 140) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));
                    boolean f3yawdowntoupflip = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 > f3yaw5) && (f3yaw4 > 140 && f3yaw5 < -140) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));


                    boolean fpwup = false;
                    boolean fpar = false;
                    boolean fpwdown = false;
                    boolean frevpar = false;

                    boolean lpwup = false;
                    boolean lpar = false;
                    boolean lpwdown = false;
                    boolean lrevpar = false;

                    boolean rpwup = false;
                    boolean rpar = false;
                    boolean rpwdown = false;
                    boolean rrevpar = false;

//works great
                    if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))


                            && (f1yawuptodownflip || f1yawuptodown || f1yawdowntoup)

                    )

                            || (f2pitchuptodown

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                            && (f2yawuptodownflip || f2yawuptodown || f2yawdowntoup))

                            || (f3pitchuptodown

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                            && (f3yawuptodownflip || f3yawuptodown || f3yawdowntoup))) {

                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up front for pwup");


                            fpwup = true;
                        }


                    }

//works great
                    else    if (((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))


                            && (f1rolldowntoup

                            && f1yawuptodownflip || f1yawdowntoup) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f2rolldowntoup

                                    && f2yawuptodownflip || f2yawdowntoup) ||

                            (((f3pitch1 >= f3pitch8) || (f3pitch1 <= f3pitch8))

                                    && f3rolldowntoup

                                    && f2yawuptodownflip || f3yawdowntoup)) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's up front for par");
                            fpar = true;
                        }

                    }

//works great
                    else   if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && (f1yawuptodown || f1yawdowntoup))

                            || (f2pitchdowntoup

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                            && (f2yawuptodown || f2yawdowntoup))

                            || (f3pitchdowntoup

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                            && (f3yawuptodown || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up front for pwdown");

                            fpwdown = true;
                        }

                    }

//works great
                    else   if (((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))

                            && f1rolluptodown

                            && (f1yawdowntoupflip || f1yawuptodown) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))
                                    && f2rolluptodown

                                    && f2yawdowntoupflip || f2yawuptodown) ||

                            (((f3pitch1 >= f3pitch8) || (f3pitch1 <= f3pitch8))
                                    && f3rolluptodown

                                    && f3yawdowntoupflip || f3yawuptodown)) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up front for rev par");

                            frevpar = true;
                        }


                    }


//left either side left leg or right leg
//works great
                    else   if ((f1pitchuptodown

                            && (f1rolluptodown || f1rolldowntoupflip) &&

                            (f1yawuptodown || f1yawdowntoup)) ||

                            (f2pitchuptodown

                                    && (f2rolluptodown || f2rolldowntoupflip) &&

                                    (f2yawuptodown || f2yawdowntoup)) ||

                            (f3pitchuptodown

                                    && (f3rolluptodown || f3rolldowntoupflip) &&

                                    (f3yawuptodown || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up left for pwup");


                            lpwup = true;
                        }

                    }

//works
                    else   if ((f1pitchuptodown

                            && (f1rolldowntoup || f1rolluptodownflip)

                            && (f1yawuptodownflip || f1yawdowntoup))

                            || (f2pitchuptodown

                            && (f2rolldowntoup || f2rolluptodownflip)

                            && (f2yawuptodownflip || f2yawdowntoup))

                            || (f3pitchuptodown

                            && (f3rolldowntoup || f3rolluptodownflip)

                            && (f3yawuptodownflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up left for par");

                            lpar = true;
                        }

                    }
//works on for down also sometimes.
                    else  if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchdowntoup

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchdowntoup

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's up left for pwdown");
                            lpwdown = true;
                        }

                    }

//works amazing
                    else  if ((f1pitchdowntoup

                            && (f1rolldowntoupflip || f1rolluptodown || f1rolldowntoup)

                            && (f1yawuptodown || f1yawdowntoupflip))

                            || (f2pitchdowntoup

                            && (f2rolldowntoupflip || f2rolluptodown || f2rolldowntoup)

                            && (f2yawuptodown || f2yawdowntoupflip))

                            || (f3pitchdowntoup

                            && (f3rolldowntoupflip || f3rolluptodown || f3rolldowntoup)

                            && (f3yawuptodown || f3yawdowntoupflip))) {
                        if ((!isStationary) && (!stationary)) {


                            Log.d(TAG, "it's up left for rev par");

                            lrevpar = true;
                        }

                    }

//works for up and down
                    //Right leg either side
                    else if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchuptodown

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchuptodown

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's up Right for pwup");
                            rpwup = true;
                        }

                    }
//works amazing
                    else if ((f1pitchdowntoup
                            && f1rolldowntoup


                            && f1yawdowntoup) ||

                            (f2pitchdowntoup

                                    && f2rolldowntoup


                                    && f2yawdowntoup) ||

                            (f3pitchdowntoup

                                    && f3rolldowntoup


                                    && f3yawdowntoup)) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up Right for par");

                            rpar = true;
                        }

                    }
//works amazing
                    else if ((f1pitchdowntoup

                            && (f1rolluptodown || f1rolldowntoupflip)

                            && (f1yawuptodown)) ||

                            (f2pitchdowntoup

                                    && (f2rolluptodown || f2rolldowntoupflip)

                                    && (f2yawuptodown)) ||

                            (f3pitchdowntoup

                                    && (f3rolluptodown || f3rolldowntoupflip)

                                    && (f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up Right for pwdown");

                            rpwdown = true;
                        }

                    }
//works amazing
                    else  if ((f1pitchuptodown

                            && (f1rolldowntoupflip || f1rolldowntoup)


                            && (f1yawdowntoupflip || f1yawdowntoup)) ||

                            (f2pitchuptodown

                                    && (f2rolldowntoupflip || f2rolldowntoup)


                                    && (f2yawdowntoupflip || f2yawdowntoup)) ||

                            (f3pitchuptodown

                                    && (f3rolldowntoupflip || f3rolldowntoup)


                                    && (f3yawdowntoupflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up Right for rev par");
                            rrevpar = true;
                        }
                    }

                    if (fpwup || fpar || fpwdown || frevpar || lpwup || lpar || lpwdown || lrevpar || rpwup || rpar || rpwdown || rrevpar) {

                        up++;
                        if (up == 1) {
                            isup = true;
                            Log.d(TAG, "It would post up to database");
                                    final HashMap<String, Object> uphashmap = new HashMap<>();
                                    uphashmap.put("status", 1);
                                    uphashmap.put("date", dateString);
                                    uphashmap.put("user", fuser.getUid());
                                    Long date = System.currentTimeMillis();
                                    uphashmap.put("timestamp", date);
                                    FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Elevation").document(dateString).collection("Readings").add(uphashmap);
                        }
                        Log.d(TAG, "The leg is up");

                    }else{

                        Log.d(TAG, "The leg is down and doing nothing");

                    }


                }
            }


        } else {
            for (int y = 0; y < listsize; y++) {
                if (y + 7 < listsize) {
                    float f1pitch1 = f1pitchvalues.get(y);
                    float f1pitch2 = f1pitchvalues.get(y + 1);
                    float f1pitch3 = f1pitchvalues.get(y + 2);
                    float f1pitch4 = f1pitchvalues.get(y + 3);
                    float f1pitch5 = f1pitchvalues.get(y + 4);
                    float f1pitch6 = f1pitchvalues.get(y + 5);
                    float f1pitch7 = f1pitchvalues.get(y + 6);
                    float f1pitch8 = f1pitchvalues.get(y + 7);

                    float f1roll1 = f1rollvalues.get(y);
                    float f1roll2 = f1rollvalues.get(y + 1);
                    float f1roll3 = f1rollvalues.get(y + 2);
                    float f1roll4 = f1rollvalues.get(y + 3);
                    float f1roll5 = f1rollvalues.get(y + 4);
                    float f1roll6 = f1rollvalues.get(y + 5);
                    float f1roll7 = f1rollvalues.get(y + 6);
                    float f1roll8 = f1rollvalues.get(y + 7);


                    float f1yaw1 = f1yawvalues.get(y);
                    float f1yaw2 = f1yawvalues.get(y + 1);
                    float f1yaw3 = f1yawvalues.get(y + 2);
                    float f1yaw4 = f1yawvalues.get(y + 3);
                    float f1yaw5 = f1yawvalues.get(y + 4);
                    float f1yaw6 = f1yawvalues.get(y + 5);
                    float f1yaw7 = f1yawvalues.get(y + 6);
                    float f1yaw8 = f1yawvalues.get(y + 7);

                    float f2pitch1 = f2pitchvalues.get(y);
                    float f2pitch2 = f2pitchvalues.get(y + 1);
                    float f2pitch3 = f2pitchvalues.get(y + 2);
                    float f2pitch4 = f2pitchvalues.get(y + 3);
                    float f2pitch5 = f2pitchvalues.get(y + 4);
                    float f2pitch6 = f2pitchvalues.get(y + 5);
                    float f2pitch7 = f2pitchvalues.get(y + 6);
                    float f2pitch8 = f2pitchvalues.get(y + 7);

                    float f2roll1 = f2rollvalues.get(y);
                    float f2roll2 = f2rollvalues.get(y + 1);
                    float f2roll3 = f2rollvalues.get(y + 2);
                    float f2roll4 = f2rollvalues.get(y + 3);
                    float f2roll5 = f2rollvalues.get(y + 4);
                    float f2roll6 = f2rollvalues.get(y + 5);
                    float f2roll7 = f2rollvalues.get(y + 6);
                    float f2roll8 = f2rollvalues.get(y + 7);

                    float f2yaw1 = f2yawvalues.get(y);
                    float f2yaw2 = f2yawvalues.get(y + 1);
                    float f2yaw3 = f2yawvalues.get(y + 2);
                    float f2yaw4 = f2yawvalues.get(y + 3);
                    float f2yaw5 = f2yawvalues.get(y + 4);
                    float f2yaw6 = f2yawvalues.get(y + 5);
                    float f2yaw7 = f2yawvalues.get(y + 6);
                    float f2yaw8 = f2yawvalues.get(y + 7);

                    float f3pitch1 = f3pitchvalues.get(y);
                    float f3pitch2 = f3pitchvalues.get(y + 1);
                    float f3pitch3 = f3pitchvalues.get(y + 2);
                    float f3pitch4 = f3pitchvalues.get(y + 3);
                    float f3pitch5 = f3pitchvalues.get(y + 4);
                    float f3pitch6 = f3pitchvalues.get(y + 5);
                    float f3pitch7 = f3pitchvalues.get(y + 6);
                    float f3pitch8 = f3pitchvalues.get(y + 7);

                    float f3roll1 = f3rollvalues.get(y);
                    float f3roll2 = f3rollvalues.get(y + 1);
                    float f3roll3 = f3rollvalues.get(y + 2);
                    float f3roll4 = f3rollvalues.get(y + 3);
                    float f3roll5 = f3rollvalues.get(y + 4);
                    float f3roll6 = f3rollvalues.get(y + 5);
                    float f3roll7 = f3rollvalues.get(y + 6);
                    float f3roll8 = f3rollvalues.get(y + 7);

                    float f3yaw1 = f3yawvalues.get(y);
                    float f3yaw2 = f3yawvalues.get(y + 1);
                    float f3yaw3 = f3yawvalues.get(y + 2);
                    float f3yaw4 = f3yawvalues.get(y + 3);
                    float f3yaw5 = f3yawvalues.get(y + 4);
                    float f3yaw6 = f3yawvalues.get(y + 5);
                    float f3yaw7 = f3yawvalues.get(y + 6);
                    float f3yaw8 = f3yawvalues.get(y + 7);

                    //front up implemented


                    boolean f1pitchuptodown = (f1pitch1 > f1pitch2) && (f1pitch2 > f1pitch3) && (f1pitch3 > f1pitch4) && (f1pitch4 > f1pitch5) &&
                            (f1pitch5 > f1pitch6) && (f1pitch6 > f1pitch7) && (f1pitch7 > f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / 10) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / 10)));
                    boolean f1pitchdowntoup = (f1pitch1 < f1pitch2) && (f1pitch2 < f1pitch3) && (f1pitch3 < f1pitch4) && (f1pitch4 < f1pitch5) &&
                            (f1pitch5 < f1pitch6) && (f1pitch6 < f1pitch7) && (f1pitch7 < f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / 10) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / 10)));

                    boolean f2pitchuptodown = (f2pitch1 > f2pitch2) && (f2pitch2 > f2pitch3) && (f2pitch3 > f2pitch4) && (f2pitch4 > f2pitch5) &&
                            (f2pitch5 > f2pitch6) && (f2pitch6 > f2pitch7) && (f2pitch7 > f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / 10) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / 10)));
                    boolean f2pitchdowntoup = (f2pitch1 < f2pitch2) && (f2pitch2 < f2pitch3) && (f2pitch3 < f2pitch4) && (f2pitch4 < f2pitch5) &&
                            (f2pitch5 < f2pitch6) && (f2pitch6 < f2pitch7) && (f2pitch7 < f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / 10) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / 10)));

                    boolean f3pitchuptodown = (f3pitch1 > f3pitch2) && (f3pitch2 > f3pitch3) && (f3pitch3 > f3pitch4) && (f3pitch4 > f3pitch5) &&
                            (f3pitch5 > f3pitch6) && (f3pitch6 > f3pitch7) && (f3pitch7 > f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / 10) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / 10)));
                    boolean f3pitchdowntoup = (f3pitch1 < f3pitch2) && (f3pitch2 < f3pitch3) && (f3pitch3 < f3pitch4) && (f3pitch4 < f3pitch5) &&
                            (f3pitch5 < f3pitch6) && (f3pitch6 < f3pitch7) && (f3pitch7 < f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / 10) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / 10)));


                    boolean f1rolluptodown = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 > f1roll5) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));
                    boolean f1rolldowntoup = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 < f1roll5) && //flipping still from 0 to 15 0 is smaller
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));
                    boolean f1rolluptodownflip = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 < f1roll5) && (f1roll4 < -140 && f1roll5 > 140) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));
                    boolean f1rolldowntoupflip = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 > f1roll5) && (f1roll4 > 140 && f1roll5 < -140) &&
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / 10) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / 10)));

                    boolean f2rolluptodown = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 > f2roll5) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));
                    boolean f2rolldowntoup = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 < f2roll5) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));
                    boolean f2rolluptodownflip = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 < f2roll5) && (f2roll4 < -140 && f2roll5 > 140) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));
                    boolean f2rolldowntoupflip = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 > f2roll5) && (f2roll4 > 140 && f2roll5 < -140) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / 10) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / 10)));


                    boolean f3rolluptodown = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 > f3roll5) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));
                    boolean f3rolldowntoup = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 < f3roll5) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));
                    boolean f3rolluptodownflip = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 < f3roll5) && (f3roll4 < -140 && f3roll5 > 140) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));
                    boolean f3rolldowntoupflip = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 > f3roll5) && (f3roll4 > 140 && f3roll5 < -140) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / 10) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / 10)));


                    boolean f1yawuptodown = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 > f1yaw5) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));

                    boolean f1yawdowntoup = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 < f1yaw5) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));

                    boolean f1yawuptodownflip = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 < f1yaw5) && (f1yaw4 < -140 && f1yaw5 > 140) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));
                    boolean f1yawdowntoupflip = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 > f1yaw5) && (f1yaw4 > 140 && f1yaw5 < -140) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / 10) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / 10)));

                    boolean f2yawuptodown = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 > f2yaw5) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));
                    boolean f2yawdowntoup = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 < f2yaw5) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));
                    boolean f2yawuptodownflip = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 < f2yaw5) && (f2yaw4 < -140 && f2yaw5 > 140) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));
                    boolean f2yawdowntoupflip = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 > f2yaw5) && (f2yaw4 > 140 && f2yaw5 < -140) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / 10) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / 10)));


                    boolean f3yawuptodown = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 > f3yaw5) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));
                    boolean f3yawdowntoup = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 < f3yaw5) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));
                    boolean f3yawuptodownflip = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 < f3yaw5) && (f3yaw4 < -140 && f3yaw5 > 140) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));
                    boolean f3yawdowntoupflip = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 > f3yaw5) && (f3yaw4 > 140 && f3yaw5 < -140) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / 10) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / 10)));


                    boolean fpwup = false;
                    boolean fpar = false;
                    boolean fpwdown = false;
                    boolean frevpar = false;

                    boolean lpwup = false;
                    boolean lpar = false;
                    boolean lpwdown = false;
                    boolean lrevpar = false;

                    boolean rpwup = false;
                    boolean rpar = false;
                    boolean rpwdown = false;
                    boolean rrevpar = false;


//works great
                    if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))


                            && (f1yawuptodownflip || f1yawuptodown || f1yawdowntoup)

                    )

                            || (f2pitchuptodown

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                            && (f2yawuptodownflip || f2yawuptodown || f2yawdowntoup))

                            || (f3pitchuptodown

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                            && (f3yawuptodownflip || f3yawuptodown || f3yawdowntoup))) {

                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up front for pwup");


                            fpwup = true;
                        }


                    }

//works great
                    else  if (((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))


                            && (f1rolldowntoup

                            && f1yawuptodownflip || f1yawdowntoup) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f2rolldowntoup

                                    && f2yawuptodownflip || f2yawdowntoup) ||

                            (((f3pitch1 >= f3pitch8) || (f3pitch1 <= f3pitch8))

                                    && f3rolldowntoup

                                    && f2yawuptodownflip || f3yawdowntoup)) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's up front for par");
                            fpar = true;
                        }

                    }

//works great
                    else  if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && (f1yawuptodown || f1yawdowntoup))

                            || (f2pitchdowntoup

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                            && (f2yawuptodown || f2yawdowntoup))

                            || (f3pitchdowntoup

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                            && (f3yawuptodown || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up front for pwdown");

                            fpwdown = true;
                        }

                    }

//works great
                    else if (((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))

                            && f1rolluptodown

                            && (f1yawdowntoupflip || f1yawuptodown) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))
                                    && f2rolluptodown

                                    && f2yawdowntoupflip || f2yawuptodown) ||

                            (((f3pitch1 >= f3pitch8) || (f3pitch1 <= f3pitch8))
                                    && f3rolluptodown

                                    && f3yawdowntoupflip || f3yawuptodown)) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up front for rev par");

                            frevpar = true;
                        }


                    }


//left either side left leg or right leg
//works great
                    else    if ((f1pitchuptodown

                            && (f1rolluptodown || f1rolldowntoupflip) &&

                            (f1yawuptodown || f1yawdowntoup)) ||

                            (f2pitchuptodown

                                    && (f2rolluptodown || f2rolldowntoupflip) &&

                                    (f2yawuptodown || f2yawdowntoup)) ||

                            (f3pitchuptodown

                                    && (f3rolluptodown || f3rolldowntoupflip) &&

                                    (f3yawuptodown || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up left for pwup");


                            lpwup = true;
                        }

                    }

//works
                    else   if ((f1pitchuptodown

                            && (f1rolldowntoup || f1rolluptodownflip)

                            && (f1yawuptodownflip || f1yawdowntoup))

                            || (f2pitchuptodown

                            && (f2rolldowntoup || f2rolluptodownflip)

                            && (f2yawuptodownflip || f2yawdowntoup))

                            || (f3pitchuptodown

                            && (f3rolldowntoup || f3rolluptodownflip)

                            && (f3yawuptodownflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up left for par");

                            lpar = true;
                        }

                    }
//works on for down also sometimes.
                    else   if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchdowntoup

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchdowntoup

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's up left for pwdown");
                            lpwdown = true;
                        }

                    }

//works amazing
                    else    if ((f1pitchdowntoup

                            && (f1rolldowntoupflip || f1rolluptodown || f1rolldowntoup)

                            && (f1yawuptodown || f1yawdowntoupflip))

                            || (f2pitchdowntoup

                            && (f2rolldowntoupflip || f2rolluptodown || f2rolldowntoup)

                            && (f2yawuptodown || f2yawdowntoupflip))

                            || (f3pitchdowntoup

                            && (f3rolldowntoupflip || f3rolluptodown || f3rolldowntoup)

                            && (f3yawuptodown || f3yawdowntoupflip))) {
                        if ((!isStationary) && (!stationary)) {


                            Log.d(TAG, "it's up left for rev par");

                            lrevpar = true;
                        }

                    }

//works for up and down
                    //Right leg either side
                    else   if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchuptodown

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchuptodown

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's up Right for pwup");
                            rpwup = true;
                        }

                    }
//works amazing
                    else   if ((f1pitchdowntoup
                            && f1rolldowntoup


                            && f1yawdowntoup) ||

                            (f2pitchdowntoup

                                    && f2rolldowntoup


                                    && f2yawdowntoup) ||

                            (f3pitchdowntoup

                                    && f3rolldowntoup


                                    && f3yawdowntoup)) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up Right for par");

                            rpar = true;
                        }

                    }
//works amazing
                    else  if ((f1pitchdowntoup

                            && (f1rolluptodown || f1rolldowntoupflip)

                            && (f1yawuptodown)) ||

                            (f2pitchdowntoup

                                    && (f2rolluptodown || f2rolldowntoupflip)

                                    && (f2yawuptodown)) ||

                            (f3pitchdowntoup

                                    && (f3rolluptodown || f3rolldowntoupflip)

                                    && (f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up Right for pwdown");

                            rpwdown = true;
                        }

                    }
//works amazing
                    else if ((f1pitchuptodown

                            && (f1rolldowntoupflip || f1rolldowntoup)


                            && (f1yawdowntoupflip || f1yawdowntoup)) ||

                            (f2pitchuptodown

                                    && (f2rolldowntoupflip || f2rolldowntoup)


                                    && (f2yawdowntoupflip || f2yawdowntoup)) ||

                            (f3pitchuptodown

                                    && (f3rolldowntoupflip || f3rolldowntoup)


                                    && (f3yawdowntoupflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's up Right for rev par");
                            rrevpar = true;
                        }
                    }

                    if (fpwup || fpar || fpwdown || frevpar || lpwup || lpar || lpwdown || lrevpar || rpwup || rpar || rpwdown || rrevpar) {
                        up++;
                        if (up == 1) {
                            isup = true;
                            Log.d(TAG, "It would post up to database");
                                    final HashMap<String, Object> uphashmap = new HashMap<>();
                                    uphashmap.put("status", 1);
                                    uphashmap.put("date", dateString);
                                    uphashmap.put("user", fuser.getUid());
                                    Long date = System.currentTimeMillis();
                                    uphashmap.put("timestamp", date);
                                    FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Elevation").document(dateString).collection("Readings").add(uphashmap);
                        }
                        Log.d(TAG, "The leg is up");

                    }else{

                        Log.d(TAG, "The leg is down and doing nothing");

                    }


                }
            }
        }
    }









    private void checkfordown() {
        int howmanytogetforcheckingdown = 8;


        int down = 0;
        int listsize = f1pitchvalues.size();

        if (listsize > howmanytogetforcheckingdown) {
            int startfrom = listsize - howmanytogetforcheckingdown;

            for (int y = startfrom; y < listsize; y++) {
                if (y + 7 < listsize) {
                    float f1pitch1 = f1pitchvalues.get(y);
                    float f1pitch2 = f1pitchvalues.get(y + 1);
                    float f1pitch3 = f1pitchvalues.get(y + 2);
                    float f1pitch4 = f1pitchvalues.get(y + 3);
                    float f1pitch5 = f1pitchvalues.get(y + 4);
                    float f1pitch6 = f1pitchvalues.get(y + 5);
                    float f1pitch7 = f1pitchvalues.get(y + 6);
                    float f1pitch8 = f1pitchvalues.get(y + 7);

                    float f1roll1 = f1rollvalues.get(y);
                    float f1roll2 = f1rollvalues.get(y + 1);
                    float f1roll3 = f1rollvalues.get(y + 2);
                    float f1roll4 = f1rollvalues.get(y + 3);
                    float f1roll5 = f1rollvalues.get(y + 4);
                    float f1roll6 = f1rollvalues.get(y + 5);
                    float f1roll7 = f1rollvalues.get(y + 6);
                    float f1roll8 = f1rollvalues.get(y + 7);


                    float f1yaw1 = f1yawvalues.get(y);
                    float f1yaw2 = f1yawvalues.get(y + 1);
                    float f1yaw3 = f1yawvalues.get(y + 2);
                    float f1yaw4 = f1yawvalues.get(y + 3);
                    float f1yaw5 = f1yawvalues.get(y + 4);
                    float f1yaw6 = f1yawvalues.get(y + 5);
                    float f1yaw7 = f1yawvalues.get(y + 6);
                    float f1yaw8 = f1yawvalues.get(y + 7);

                    float f2pitch1 = f2pitchvalues.get(y);
                    float f2pitch2 = f2pitchvalues.get(y + 1);
                    float f2pitch3 = f2pitchvalues.get(y + 2);
                    float f2pitch4 = f2pitchvalues.get(y + 3);
                    float f2pitch5 = f2pitchvalues.get(y + 4);
                    float f2pitch6 = f2pitchvalues.get(y + 5);
                    float f2pitch7 = f2pitchvalues.get(y + 6);
                    float f2pitch8 = f2pitchvalues.get(y + 7);

                    float f2roll1 = f2rollvalues.get(y);
                    float f2roll2 = f2rollvalues.get(y + 1);
                    float f2roll3 = f2rollvalues.get(y + 2);
                    float f2roll4 = f2rollvalues.get(y + 3);
                    float f2roll5 = f2rollvalues.get(y + 4);
                    float f2roll6 = f2rollvalues.get(y + 5);
                    float f2roll7 = f2rollvalues.get(y + 6);
                    float f2roll8 = f2rollvalues.get(y + 7);

                    float f2yaw1 = f2yawvalues.get(y);
                    float f2yaw2 = f2yawvalues.get(y + 1);
                    float f2yaw3 = f2yawvalues.get(y + 2);
                    float f2yaw4 = f2yawvalues.get(y + 3);
                    float f2yaw5 = f2yawvalues.get(y + 4);
                    float f2yaw6 = f2yawvalues.get(y + 5);
                    float f2yaw7 = f2yawvalues.get(y + 6);
                    float f2yaw8 = f2yawvalues.get(y + 7);

                    float f3pitch1 = f3pitchvalues.get(y);
                    float f3pitch2 = f3pitchvalues.get(y + 1);
                    float f3pitch3 = f3pitchvalues.get(y + 2);
                    float f3pitch4 = f3pitchvalues.get(y + 3);
                    float f3pitch5 = f3pitchvalues.get(y + 4);
                    float f3pitch6 = f3pitchvalues.get(y + 5);
                    float f3pitch7 = f3pitchvalues.get(y + 6);
                    float f3pitch8 = f3pitchvalues.get(y + 7);

                    float f3roll1 = f3rollvalues.get(y);
                    float f3roll2 = f3rollvalues.get(y + 1);
                    float f3roll3 = f3rollvalues.get(y + 2);
                    float f3roll4 = f3rollvalues.get(y + 3);
                    float f3roll5 = f3rollvalues.get(y + 4);
                    float f3roll6 = f3rollvalues.get(y + 5);
                    float f3roll7 = f3rollvalues.get(y + 6);
                    float f3roll8 = f3rollvalues.get(y + 7);

                    float f3yaw1 = f3yawvalues.get(y);
                    float f3yaw2 = f3yawvalues.get(y + 1);
                    float f3yaw3 = f3yawvalues.get(y + 2);
                    float f3yaw4 = f3yawvalues.get(y + 3);
                    float f3yaw5 = f3yawvalues.get(y + 4);
                    float f3yaw6 = f3yawvalues.get(y + 5);
                    float f3yaw7 = f3yawvalues.get(y + 6);
                    float f3yaw8 = f3yawvalues.get(y + 7);
                    //front up implemented
                    int dividant = 10;


                    boolean f1pitchuptodown = (f1pitch1 > f1pitch2) && (f1pitch2 > f1pitch3) && (f1pitch3 > f1pitch4) && (f1pitch4 > f1pitch5) &&
                            (f1pitch5 > f1pitch6) && (f1pitch6 > f1pitch7) && (f1pitch7 > f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / dividant) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / dividant)));
                    boolean f1pitchdowntoup = (f1pitch1 < f1pitch2) && (f1pitch2 < f1pitch3) && (f1pitch3 < f1pitch4) && (f1pitch4 < f1pitch5) &&
                            (f1pitch5 < f1pitch6) && (f1pitch6 < f1pitch7) && (f1pitch7 < f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / dividant) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / dividant)));

                    boolean f2pitchuptodown = (f2pitch1 > f2pitch2) && (f2pitch2 > f2pitch3) && (f2pitch3 > f2pitch4) && (f2pitch4 > f2pitch5) &&
                            (f2pitch5 > f2pitch6) && (f2pitch6 > f2pitch7) && (f2pitch7 > f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / dividant) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / dividant)));
                    boolean f2pitchdowntoup = (f2pitch1 < f2pitch2) && (f2pitch2 < f2pitch3) && (f2pitch3 < f2pitch4) && (f2pitch4 < f2pitch5) &&
                            (f2pitch5 < f2pitch6) && (f2pitch6 < f2pitch7) && (f2pitch7 < f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / dividant) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / dividant)));

                    boolean f3pitchuptodown = (f3pitch1 > f3pitch2) && (f3pitch2 > f3pitch3) && (f3pitch3 > f3pitch4) && (f3pitch4 > f3pitch5) &&
                            (f3pitch5 > f3pitch6) && (f3pitch6 > f3pitch7) && (f3pitch7 > f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / dividant) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / dividant)));
                    boolean f3pitchdowntoup = (f3pitch1 < f3pitch2) && (f3pitch2 < f3pitch3) && (f3pitch3 < f3pitch4) && (f3pitch4 < f3pitch5) &&
                            (f3pitch5 < f3pitch6) && (f3pitch6 < f3pitch7) && (f3pitch7 < f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / dividant) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / dividant)));


                    boolean f1rolluptodown = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 > f1roll5) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));
                    boolean f1rolldowntoup = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 < f1roll5) && //flipping still from 0 to 15 0 is smaller
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));
                    boolean f1rolluptodownflip = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 < f1roll5) && (f1roll4 < -140 && f1roll5 > 140) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));
                    boolean f1rolldowntoupflip = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 > f1roll5) && (f1roll4 > 140 && f1roll5 < -140) &&
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));

                    boolean f2rolluptodown = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 > f2roll5) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));
                    boolean f2rolldowntoup = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 < f2roll5) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));
                    boolean f2rolluptodownflip = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 < f2roll5) && (f2roll4 < -140 && f2roll5 > 140) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));
                    boolean f2rolldowntoupflip = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 > f2roll5) && (f2roll4 > 140 && f2roll5 < -140) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));


                    boolean f3rolluptodown = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 > f3roll5) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));
                    boolean f3rolldowntoup = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 < f3roll5) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));
                    boolean f3rolluptodownflip = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 < f3roll5) && (f3roll4 < -140 && f3roll5 > 140) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));
                    boolean f3rolldowntoupflip = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 > f3roll5) && (f3roll4 > 140 && f3roll5 < -140) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));


                    boolean f1yawuptodown = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 > f1yaw5) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));

                    boolean f1yawdowntoup = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 < f1yaw5) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));

                    boolean f1yawuptodownflip = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 < f1yaw5) && (f1yaw4 < -140 && f1yaw5 > 140) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));
                    boolean f1yawdowntoupflip = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 > f1yaw5) && (f1yaw4 > 140 && f1yaw5 < -140) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));

                    boolean f2yawuptodown = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 > f2yaw5) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));
                    boolean f2yawdowntoup = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 < f2yaw5) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));
                    boolean f2yawuptodownflip = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 < f2yaw5) && (f2yaw4 < -140 && f2yaw5 > 140) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));
                    boolean f2yawdowntoupflip = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 > f2yaw5) && (f2yaw4 > 140 && f2yaw5 < -140) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));


                    boolean f3yawuptodown = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 > f3yaw5) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));
                    boolean f3yawdowntoup = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 < f3yaw5) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));
                    boolean f3yawuptodownflip = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 < f3yaw5) && (f3yaw4 < -140 && f3yaw5 > 140) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));
                    boolean f3yawdowntoupflip = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 > f3yaw5) && (f3yaw4 > 140 && f3yaw5 < -140) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));


                    boolean fpwupdown = false;
                    boolean fpardown = false;
                    boolean downfpwdown = false;
                    boolean frevpardown = false;

                    boolean lpwupdown = false;
                    boolean lpardown = false;
                    boolean downlpwdown = false;
                    boolean lrevpardown = false;

                    boolean rpwupdown = false;
                    boolean rpardown = false;
                    boolean downrpwdown = false;
                    boolean rrevpardown = false;

//works great
                    if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))


                            && (f1yawdowntoupflip || f1yawuptodown || f1yawdowntoup)

                    )

                            || (f2pitchdowntoup

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))


                            && (f2yawdowntoupflip || f2yawuptodown || f2yawdowntoup)

                    )

                            || (f3pitchdowntoup

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))


                            && (f3yawdowntoupflip || f3yawuptodown || f3yawdowntoup)

                    )) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's down front for pwup");
                            fpwupdown = true;
                        }


                    }

//works great //allittle open

                    else if ((((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))

                            && (f1rolluptodown)

                            && f1yawdowntoupflip || f1yawuptodown) ||


                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f2rolluptodown

                                    && f2yawdowntoupflip || f2yawuptodown) ||


                            (((f3pitch1 >= f3pitch8) || (f3pitch1 <= f3pitch8))

                                    && f3rolluptodown

                                    && f3yawdowntoupflip || f3yawuptodown)) {

                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down front for par");

                            fpardown = true;
                        }

                    }

//great
                    else if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && (f1yawuptodown || f1yawdowntoup))

                            || (f2pitchuptodown

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                            && (f2yawuptodown || f2yawdowntoup))

                            || (f3pitchuptodown

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                            && (f3yawuptodown || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down front for pwdown");

                            downfpwdown = true;
                        }

                    }

//works amazing
                    else if ((((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))

                            && f1rolldowntoup

                            && (f1yawuptodownflip || f1yawdowntoup)) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f2rolldowntoup

                                    && (f2yawuptodownflip || f2yawdowntoup)) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f3rolldowntoup

                                    && (f3yawuptodownflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down front for rev par");

                            frevpardown = true;
                        }


                    }


//left either side left leg or right leg
//great

                    else if ((f1pitchdowntoup

                            && (f1rolldowntoup || f1rolldowntoupflip || ((f1roll1 <= f1meanroll * 1.2) || (f1roll1 >= f1meanroll * 0.8))) &&

                            (f1yawdowntoup || f1yawuptodown)) ||

                            (f2pitchdowntoup

                                    && (f2rolldowntoup || f2rolldowntoupflip || ((f2roll1 <= f2meanroll * 1.2) || (f2roll1 >= f2meanroll * 0.8))) &&

                                    (f2yawdowntoup || f2yawuptodown)) ||

                            (f3pitchdowntoup

                                    && (f3rolldowntoup || f3rolldowntoupflip || ((f3roll1 <= f3meanroll * 1.2) || (f3roll1 >= f3meanroll * 0.8))) &&

                                    (f3yawdowntoup || f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for pwup");


                            lpwupdown = true;
                        }

                    }
//works good
                    else if ((f1pitchdowntoup

                            && (f1rolluptodown || f1rolluptodownflip)

                            && (f1yawdowntoupflip || f1yawuptodown))

                            || (f2pitchdowntoup

                            && (f2rolldowntoup || f2rolluptodownflip)

                            && (f2yawdowntoupflip || f2yawuptodown))

                            || (f3pitchdowntoup

                            && (f3rolldowntoup || f3rolluptodownflip)

                            && (f3yawdowntoupflip || f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for par");

                            lpardown = true;
                        }

                    }

//works great /rightleg maybe

                    else if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchuptodown

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchuptodown

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for pwdown");

                            downlpwdown = true;
                        }

                    }


//works great
                    else if ((f1pitchuptodown

                            && (f1rolluptodownflip || f1rolluptodown || f1rolldowntoup)

                            && (f1yawdowntoup || f1yawuptodownflip))

                            || ((f2pitchuptodown)

                            && (f2rolluptodownflip || f2rolluptodown || f2rolldowntoup)

                            && (f2yawdowntoup || f2yawuptodownflip))

                            || (f3pitchuptodown

                            && (f3rolluptodownflip || f3rolluptodown || f3rolldowntoup)

                            && (f3yawdowntoup || f3yawuptodownflip))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for rev par");

                            lrevpardown = true;
                        }


                    }


                    //works amazing                   //Right leg either side
                    else if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchdowntoup

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchdowntoup

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for pwup");
                            rpwupdown = true;
                        }

                    }


// works great
                    else if ((f1pitchuptodown
                            && f1rolluptodown


                            && (f1yawdowntoupflip || f1yawuptodown)) ||

                            (f2pitchuptodown

                                    && f2rolluptodown


                                    && (f2yawdowntoupflip || f2yawuptodown)) ||

                            (f3pitchuptodown

                                    && f3rolluptodown


                                    && (f3yawdowntoupflip || f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for par");

                            rpardown = true;
                        }

                    }


// works great
                    else if ((f1pitchuptodown

                            && f1rolldowntoup

                            && (f1yawdowntoup)) ||

                            (f2pitchuptodown

                                    && f2rolldowntoup
                                    && (f2yawdowntoup)) ||

                            (f3pitchuptodown

                                    && f3rolldowntoup

                                    && (f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for pwdown");

                            downrpwdown = true;
                        }

                    }
//works great //doesn't work if flipping but should
                    else if ((f1pitchdowntoup

                            && (f1rolluptodownflip || f1rolldowntoup)


                            && (f1yawuptodownflip || f1yawdowntoup)) ||

                            (f2pitchdowntoup

                                    && (f2rolluptodownflip || f2rolldowntoup)


                                    && (f2yawuptodownflip || f2yawdowntoup)) ||

                            (f3pitchdowntoup

                                    && (f3rolluptodownflip || f3rolldowntoup)


                                    && (f3yawuptodownflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for rev par");
                            rrevpardown = true;
                        }
                    }

                    if (fpwupdown || fpardown || downfpwdown || frevpardown || lpwupdown || lpardown || downlpwdown || lrevpardown || rpwupdown || rpardown || downrpwdown || rrevpardown) {
                        down++;
                        if (down == 1) {
                            isup = false;
                            final HashMap<String, Object> uphashmap = new HashMap<>();
                            uphashmap.put("status", 0);
                            uphashmap.put("date", dateString);
                            uphashmap.put("user", fuser.getUid());
                            Long date = System.currentTimeMillis();
                            uphashmap.put("timestamp", date);
                            FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Elevation").document(dateString).collection("Readings").add(uphashmap);
                            Log.d(TAG, "It would write to the database down");

                        }
                        Log.d(TAG, "The leg is down");

                    }else{

                        Log.d(TAG, "The leg is up and doing nothing");

                    }


                }

            }


        } else {
            for (int y = 0; y < listsize; y++) {
                if (y + 7 < listsize) {
                    float f1pitch1 = f1pitchvalues.get(y);
                    float f1pitch2 = f1pitchvalues.get(y + 1);
                    float f1pitch3 = f1pitchvalues.get(y + 2);
                    float f1pitch4 = f1pitchvalues.get(y + 3);
                    float f1pitch5 = f1pitchvalues.get(y + 4);
                    float f1pitch6 = f1pitchvalues.get(y + 5);
                    float f1pitch7 = f1pitchvalues.get(y + 6);
                    float f1pitch8 = f1pitchvalues.get(y + 7);

                    float f1roll1 = f1rollvalues.get(y);
                    float f1roll2 = f1rollvalues.get(y + 1);
                    float f1roll3 = f1rollvalues.get(y + 2);
                    float f1roll4 = f1rollvalues.get(y + 3);
                    float f1roll5 = f1rollvalues.get(y + 4);
                    float f1roll6 = f1rollvalues.get(y + 5);
                    float f1roll7 = f1rollvalues.get(y + 6);
                    float f1roll8 = f1rollvalues.get(y + 7);


                    float f1yaw1 = f1yawvalues.get(y);
                    float f1yaw2 = f1yawvalues.get(y + 1);
                    float f1yaw3 = f1yawvalues.get(y + 2);
                    float f1yaw4 = f1yawvalues.get(y + 3);
                    float f1yaw5 = f1yawvalues.get(y + 4);
                    float f1yaw6 = f1yawvalues.get(y + 5);
                    float f1yaw7 = f1yawvalues.get(y + 6);
                    float f1yaw8 = f1yawvalues.get(y + 7);

                    float f2pitch1 = f2pitchvalues.get(y);
                    float f2pitch2 = f2pitchvalues.get(y + 1);
                    float f2pitch3 = f2pitchvalues.get(y + 2);
                    float f2pitch4 = f2pitchvalues.get(y + 3);
                    float f2pitch5 = f2pitchvalues.get(y + 4);
                    float f2pitch6 = f2pitchvalues.get(y + 5);
                    float f2pitch7 = f2pitchvalues.get(y + 6);
                    float f2pitch8 = f2pitchvalues.get(y + 7);

                    float f2roll1 = f2rollvalues.get(y);
                    float f2roll2 = f2rollvalues.get(y + 1);
                    float f2roll3 = f2rollvalues.get(y + 2);
                    float f2roll4 = f2rollvalues.get(y + 3);
                    float f2roll5 = f2rollvalues.get(y + 4);
                    float f2roll6 = f2rollvalues.get(y + 5);
                    float f2roll7 = f2rollvalues.get(y + 6);
                    float f2roll8 = f2rollvalues.get(y + 7);

                    float f2yaw1 = f2yawvalues.get(y);
                    float f2yaw2 = f2yawvalues.get(y + 1);
                    float f2yaw3 = f2yawvalues.get(y + 2);
                    float f2yaw4 = f2yawvalues.get(y + 3);
                    float f2yaw5 = f2yawvalues.get(y + 4);
                    float f2yaw6 = f2yawvalues.get(y + 5);
                    float f2yaw7 = f2yawvalues.get(y + 6);
                    float f2yaw8 = f2yawvalues.get(y + 7);

                    float f3pitch1 = f3pitchvalues.get(y);
                    float f3pitch2 = f3pitchvalues.get(y + 1);
                    float f3pitch3 = f3pitchvalues.get(y + 2);
                    float f3pitch4 = f3pitchvalues.get(y + 3);
                    float f3pitch5 = f3pitchvalues.get(y + 4);
                    float f3pitch6 = f3pitchvalues.get(y + 5);
                    float f3pitch7 = f3pitchvalues.get(y + 6);
                    float f3pitch8 = f3pitchvalues.get(y + 7);

                    float f3roll1 = f3rollvalues.get(y);
                    float f3roll2 = f3rollvalues.get(y + 1);
                    float f3roll3 = f3rollvalues.get(y + 2);
                    float f3roll4 = f3rollvalues.get(y + 3);
                    float f3roll5 = f3rollvalues.get(y + 4);
                    float f3roll6 = f3rollvalues.get(y + 5);
                    float f3roll7 = f3rollvalues.get(y + 6);
                    float f3roll8 = f3rollvalues.get(y + 7);

                    float f3yaw1 = f3yawvalues.get(y);
                    float f3yaw2 = f3yawvalues.get(y + 1);
                    float f3yaw3 = f3yawvalues.get(y + 2);
                    float f3yaw4 = f3yawvalues.get(y + 3);
                    float f3yaw5 = f3yawvalues.get(y + 4);
                    float f3yaw6 = f3yawvalues.get(y + 5);
                    float f3yaw7 = f3yawvalues.get(y + 6);
                    float f3yaw8 = f3yawvalues.get(y + 7);

                    int dividant = 10;


                    boolean f1pitchuptodown = (f1pitch1 > f1pitch2) && (f1pitch2 > f1pitch3) && (f1pitch3 > f1pitch4) && (f1pitch4 > f1pitch5) &&
                            (f1pitch5 > f1pitch6) && (f1pitch6 > f1pitch7) && (f1pitch7 > f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / dividant) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / dividant)));
                    boolean f1pitchdowntoup = (f1pitch1 < f1pitch2) && (f1pitch2 < f1pitch3) && (f1pitch3 < f1pitch4) && (f1pitch4 < f1pitch5) &&
                            (f1pitch5 < f1pitch6) && (f1pitch6 < f1pitch7) && (f1pitch7 < f1pitch8)
                            && (Math.abs(f1pitch8 - f1pitch1) > (f1pitch8 / dividant) || (Math.abs(f1pitch1 - f1pitch8) > (f1pitch1 / dividant)));

                    boolean f2pitchuptodown = (f2pitch1 > f2pitch2) && (f2pitch2 > f2pitch3) && (f2pitch3 > f2pitch4) && (f2pitch4 > f2pitch5) &&
                            (f2pitch5 > f2pitch6) && (f2pitch6 > f2pitch7) && (f2pitch7 > f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / dividant) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / dividant)));
                    boolean f2pitchdowntoup = (f2pitch1 < f2pitch2) && (f2pitch2 < f2pitch3) && (f2pitch3 < f2pitch4) && (f2pitch4 < f2pitch5) &&
                            (f2pitch5 < f2pitch6) && (f2pitch6 < f2pitch7) && (f2pitch7 < f2pitch8)
                            && (Math.abs(f2pitch8 - f2pitch1) > (f2pitch8 / dividant) || (Math.abs(f2pitch1 - f2pitch8) > (f2pitch1 / dividant)));

                    boolean f3pitchuptodown = (f3pitch1 > f3pitch2) && (f3pitch2 > f3pitch3) && (f3pitch3 > f3pitch4) && (f3pitch4 > f3pitch5) &&
                            (f3pitch5 > f3pitch6) && (f3pitch6 > f3pitch7) && (f3pitch7 > f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / dividant) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / dividant)));
                    boolean f3pitchdowntoup = (f3pitch1 < f3pitch2) && (f3pitch2 < f3pitch3) && (f3pitch3 < f3pitch4) && (f3pitch4 < f3pitch5) &&
                            (f3pitch5 < f3pitch6) && (f3pitch6 < f3pitch7) && (f3pitch7 < f3pitch8)
                            && (Math.abs(f3pitch8 - f3pitch1) > (f3pitch8 / dividant) || (Math.abs(f3pitch1 - f3pitch8) > (f3pitch1 / dividant)));


                    boolean f1rolluptodown = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 > f1roll5) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));
                    boolean f1rolldowntoup = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 < f1roll5) && //flipping still from 0 to 15 0 is smaller
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));
                    boolean f1rolluptodownflip = (f1roll1 > f1roll2) && (f1roll2 > f1roll3) && (f1roll3 > f1roll4) && (f1roll4 < f1roll5) && (f1roll4 < -140 && f1roll5 > 140) &&
                            (f1roll5 > f1roll6) && (f1roll6 > f1roll7) && (f1roll7 > f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));
                    boolean f1rolldowntoupflip = (f1roll1 < f1roll2) && (f1roll2 < f1roll3) && (f1roll3 < f1roll4) && (f1roll4 > f1roll5) && (f1roll4 > 140 && f1roll5 < -140) &&
                            (f1roll5 < f1roll6) && (f1roll6 < f1roll7) && (f1roll7 < f1roll8)
                            && (Math.abs(f1roll8 - f1roll1) > (f1roll8 / dividant) || (Math.abs(f1roll1 - f1roll8) > (f1roll1 / dividant)));

                    boolean f2rolluptodown = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 > f2roll5) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));
                    boolean f2rolldowntoup = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 < f2roll5) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));
                    boolean f2rolluptodownflip = (f2roll1 > f2roll2) && (f2roll2 > f2roll3) && (f2roll3 > f2roll4) && (f2roll4 < f2roll5) && (f2roll4 < -140 && f2roll5 > 140) &&
                            (f2roll5 > f2roll6) && (f2roll6 > f2roll7) && (f2roll7 > f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));
                    boolean f2rolldowntoupflip = (f2roll1 < f2roll2) && (f2roll2 < f2roll3) && (f2roll3 < f2roll4) && (f2roll4 > f2roll5) && (f2roll4 > 140 && f2roll5 < -140) &&
                            (f2roll5 < f2roll6) && (f2roll6 < f2roll7) && (f2roll7 < f2roll8)
                            && (Math.abs(f2roll8 - f2roll1) > (f2roll8 / dividant) || (Math.abs(f2roll1 - f2roll8) > (f2roll1 / dividant)));


                    boolean f3rolluptodown = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 > f3roll5) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));
                    boolean f3rolldowntoup = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 < f3roll5) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));
                    boolean f3rolluptodownflip = (f3roll1 > f3roll2) && (f3roll2 > f3roll3) && (f3roll3 > f3roll4) && (f3roll4 < f3roll5) && (f3roll4 < -140 && f3roll5 > 140) &&
                            (f3roll5 > f3roll6) && (f3roll6 > f3roll7) && (f3roll7 > f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));
                    boolean f3rolldowntoupflip = (f3roll1 < f3roll2) && (f3roll2 < f3roll3) && (f3roll3 < f3roll4) && (f3roll4 > f3roll5) && (f3roll4 > 140 && f3roll5 < -140) &&
                            (f3roll5 < f3roll6) && (f3roll6 < f3roll7) && (f3roll7 < f3roll8)
                            && (Math.abs(f3roll8 - f3roll1) > (f3roll8 / dividant) || (Math.abs(f3roll1 - f3roll8) > (f3roll1 / dividant)));


                    boolean f1yawuptodown = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 > f1yaw5) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));

                    boolean f1yawdowntoup = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 < f1yaw5) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));

                    boolean f1yawuptodownflip = (f1yaw1 > f1yaw2) && (f1yaw2 > f1yaw3) && (f1yaw3 > f1yaw4) && (f1yaw4 < f1yaw5) && (f1yaw4 < -140 && f1yaw5 > 140) &&
                            (f1yaw5 > f1yaw6) && (f1yaw6 > f1yaw7) && (f1yaw7 > f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));
                    boolean f1yawdowntoupflip = (f1yaw1 < f1yaw2) && (f1yaw2 < f1yaw3) && (f1yaw3 < f1yaw4) && (f1yaw4 > f1yaw5) && (f1yaw4 > 140 && f1yaw5 < -140) &&
                            (f1yaw5 < f1yaw6) && (f1yaw6 < f1yaw7) && (f1yaw7 < f1yaw8)
                            && (Math.abs(f1yaw8 - f1yaw1) > (f1yaw8 / dividant) || (Math.abs(f1yaw1 - f1yaw8) > (f1yaw1 / dividant)));

                    boolean f2yawuptodown = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 > f2yaw5) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));
                    boolean f2yawdowntoup = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 < f2yaw5) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));
                    boolean f2yawuptodownflip = (f2yaw1 > f2yaw2) && (f2yaw2 > f2yaw3) && (f2yaw3 > f2yaw4) && (f2yaw4 < f2yaw5) && (f2yaw4 < -140 && f2yaw5 > 140) &&
                            (f2yaw5 > f2yaw6) && (f2yaw6 > f2yaw7) && (f2yaw7 > f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));
                    boolean f2yawdowntoupflip = (f2yaw1 < f2yaw2) && (f2yaw2 < f2yaw3) && (f2yaw3 < f2yaw4) && (f2yaw4 > f2yaw5) && (f2yaw4 > 140 && f2yaw5 < -140) &&
                            (f2yaw5 < f2yaw6) && (f2yaw6 < f2yaw7) && (f2yaw7 < f2yaw8)
                            && (Math.abs(f2yaw8 - f2yaw1) > (f2yaw8 / dividant) || (Math.abs(f2yaw1 - f2yaw8) > (f2yaw1 / dividant)));


                    boolean f3yawuptodown = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 > f3yaw5) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));
                    boolean f3yawdowntoup = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 < f3yaw5) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));
                    boolean f3yawuptodownflip = (f3yaw1 > f3yaw2) && (f3yaw2 > f3yaw3) && (f3yaw3 > f3yaw4) && (f3yaw4 < f3yaw5) && (f3yaw4 < -140 && f3yaw5 > 140) &&
                            (f3yaw5 > f3yaw6) && (f3yaw6 > f3yaw7) && (f3yaw7 > f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));
                    boolean f3yawdowntoupflip = (f3yaw1 < f3yaw2) && (f3yaw2 < f3yaw3) && (f3yaw3 < f3yaw4) && (f3yaw4 > f3yaw5) && (f3yaw4 > 140 && f3yaw5 < -140) &&
                            (f3yaw5 < f3yaw6) && (f3yaw6 < f3yaw7) && (f3yaw7 < f3yaw8)
                            && (Math.abs(f3yaw8 - f3yaw1) > (f3yaw8 / dividant) || (Math.abs(f3yaw1 - f3yaw8) > (f3yaw1 / dividant)));


                    boolean fpwupdown = false;
                    boolean fpardown = false;
                    boolean downfpwdown = false;
                    boolean frevpardown = false;

                    boolean lpwupdown = false;
                    boolean lpardown = false;
                    boolean downlpwdown = false;
                    boolean lrevpardown = false;

                    boolean rpwupdown = false;
                    boolean rpardown = false;
                    boolean downrpwdown = false;
                    boolean rrevpardown = false;

//works great
                    if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))


                            && (f1yawdowntoupflip || f1yawuptodown || f1yawdowntoup)

                    )

                            || (f2pitchdowntoup

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))


                            && (f2yawdowntoupflip || f2yawuptodown || f2yawdowntoup)

                    )

                            || (f3pitchdowntoup

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))


                            && (f3yawdowntoupflip || f3yawuptodown || f3yawdowntoup)

                    )) {
                        if ((!isStationary) && (!stationary)) {
                            Log.d(TAG, "it's down front for pwup");
                            fpwupdown = true;
                        }


                    }

//works great //allittle open

                    else if ((((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))

                            && (f1rolluptodown)

                            && f1yawdowntoupflip || f1yawuptodown) ||


                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f2rolluptodown

                                    && f2yawdowntoupflip || f2yawuptodown) ||


                            (((f3pitch1 >= f3pitch8) || (f3pitch1 <= f3pitch8))

                                    && f3rolluptodown

                                    && f3yawdowntoupflip || f3yawuptodown)) {

                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down front for par");

                            fpardown = true;
                        }

                    }

//great
                    else if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && (f1yawuptodown || f1yawdowntoup))

                            || (f2pitchuptodown

                            && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                            && (f2yawuptodown || f2yawdowntoup))

                            || (f3pitchuptodown

                            && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                            && (f3yawuptodown || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down front for pwdown");

                            downfpwdown = true;
                        }

                    }

//works amazing
                    else if ((((f1pitch1 >= f1pitch8) || (f1pitch1 <= f1pitch8))

                            && f1rolldowntoup

                            && (f1yawuptodownflip || f1yawdowntoup)) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f2rolldowntoup

                                    && (f2yawuptodownflip || f2yawdowntoup)) ||

                            (((f2pitch1 >= f2pitch8) || (f2pitch1 <= f2pitch8))

                                    && f3rolldowntoup

                                    && (f3yawuptodownflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down front for rev par");

                            frevpardown = true;
                        }


                    }


//left either side left leg or right leg
//great

                    else if ((f1pitchdowntoup

                            && (f1rolldowntoup || f1rolldowntoupflip || ((f1roll1 <= f1meanroll * 1.2) || (f1roll1 >= f1meanroll * 0.8))) &&

                            (f1yawdowntoup || f1yawuptodown)) ||

                            (f2pitchdowntoup

                                    && (f2rolldowntoup || f2rolldowntoupflip || ((f2roll1 <= f2meanroll * 1.2) || (f2roll1 >= f2meanroll * 0.8))) &&

                                    (f2yawdowntoup || f2yawuptodown)) ||

                            (f3pitchdowntoup

                                    && (f3rolldowntoup || f3rolldowntoupflip || ((f3roll1 <= f3meanroll * 1.2) || (f3roll1 >= f3meanroll * 0.8))) &&

                                    (f3yawdowntoup || f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for pwup");


                            lpwupdown = true;
                        }

                    }
//works good
                    else if ((f1pitchdowntoup

                            && (f1rolluptodown || f1rolluptodownflip)

                            && (f1yawdowntoupflip || f1yawuptodown))

                            || (f2pitchdowntoup

                            && (f2rolldowntoup || f2rolluptodownflip)

                            && (f2yawdowntoupflip || f2yawuptodown))

                            || (f3pitchdowntoup

                            && (f3rolldowntoup || f3rolluptodownflip)

                            && (f3yawdowntoupflip || f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for par");

                            lpardown = true;
                        }

                    }

//works great /rightleg maybe

                    else if ((f1pitchuptodown

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchuptodown

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchuptodown

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for pwdown");

                            downlpwdown = true;
                        }

                    }


//works great
                    else if ((f1pitchuptodown

                            && (f1rolluptodownflip || f1rolluptodown || f1rolldowntoup)

                            && (f1yawdowntoup || f1yawuptodownflip))

                            || ((f2pitchuptodown)

                            && (f2rolluptodownflip || f2rolluptodown || f2rolldowntoup)

                            && (f2yawdowntoup || f2yawuptodownflip))

                            || (f3pitchuptodown

                            && (f3rolluptodownflip || f3rolluptodown || f3rolldowntoup)

                            && (f3yawdowntoup || f3yawuptodownflip))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down left for rev par");

                            lrevpardown = true;
                        }


                    }


                    //works amazing                   //Right leg either side
                    else if ((f1pitchdowntoup

                            && ((f1roll1 >= f1roll8) || (f1roll1 <= f1roll8))

                            && ((f1yaw1 >= f1yaw8) || (f1yaw1 <= f1yaw8))) ||

                            (f2pitchdowntoup

                                    && ((f2roll1 >= f2roll8) || (f2roll1 <= f2roll8))

                                    && ((f2yaw1 >= f2yaw8) || (f2yaw1 <= f2yaw8))) ||

                            (f3pitchdowntoup

                                    && ((f3roll1 >= f3roll8) || (f3roll1 <= f3roll8))

                                    && ((f3yaw1 >= f3yaw8) || (f3yaw1 <= f3yaw8)))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for pwup");
                            rpwupdown = true;
                        }

                    }


// works great
                    else if ((f1pitchuptodown
                            && f1rolluptodown


                            && (f1yawdowntoupflip || f1yawuptodown)) ||

                            (f2pitchuptodown

                                    && f2rolluptodown


                                    && (f2yawdowntoupflip || f2yawuptodown)) ||

                            (f3pitchuptodown

                                    && f3rolluptodown


                                    && (f3yawdowntoupflip || f3yawuptodown))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for par");

                            rpardown = true;
                        }

                    }


// works great
                    else if ((f1pitchuptodown

                            && f1rolldowntoup

                            && (f1yawdowntoup)) ||

                            (f2pitchuptodown

                                    && f2rolldowntoup
                                    && (f2yawdowntoup)) ||

                            (f3pitchuptodown

                                    && f3rolldowntoup

                                    && (f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for pwdown");

                            downrpwdown = true;
                        }

                    }
//works great //doesn't work if flipping but should
                    else if ((f1pitchdowntoup

                            && (f1rolluptodownflip || f1rolldowntoup)


                            && (f1yawuptodownflip || f1yawdowntoup)) ||

                            (f2pitchdowntoup

                                    && (f2rolluptodownflip || f2rolldowntoup)


                                    && (f2yawuptodownflip || f2yawdowntoup)) ||

                            (f3pitchdowntoup

                                    && (f3rolluptodownflip || f3rolldowntoup)


                                    && (f3yawuptodownflip || f3yawdowntoup))) {
                        if ((!isStationary) && (!stationary)) {

                            Log.d(TAG, "it's down Right for rev par");
                            rrevpardown = true;
                        }
                    }

                    if (fpwupdown || fpardown || downfpwdown || frevpardown || lpwupdown || lpardown || downlpwdown || lrevpardown || rpwupdown || rpardown || downrpwdown || rrevpardown) {
                        down++;
                        if (down == 1) {
                            isup = false;
                            final HashMap<String, Object> uphashmap = new HashMap<>();
                            uphashmap.put("status", 0);
                            uphashmap.put("date", dateString);
                            uphashmap.put("user", fuser.getUid());
                            Long date = System.currentTimeMillis();
                            uphashmap.put("timestamp", date);
                            FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Elevation").document(dateString).collection("Readings").add(uphashmap);
                            Log.d(TAG, "It would write to the database down");

                        }
                        Log.d(TAG, "The leg is down");

                    }else{

                        Log.d(TAG, "The leg is up and doing nothing");
                if (isup&& isStationary&&stationary)
                {
                    //secondupboolean = true;
                }
                    }


                }


            }
        }
    }





    private void updategraph() {
        switch (radioSelection) {
            case 0:
                if (PitchLine != null && RollLine != null & YawLine != null) {

                    PitchLine.appendData(new DataPoint(lastreceived, (accMagOrientation[1] * 180 / Math.PI)), true, 999999999);
                    RollLine.appendData(new DataPoint(lastreceived, (accMagOrientation[2] * 180 / Math.PI)), true, 999999999);
                    YawLine.appendData(new DataPoint(lastreceived, (accMagOrientation[0] * 180 / Math.PI)), true, 999999999);
                    currentpitch = (float) (accMagOrientation[1] * 180 / Math.PI);
                    currentroll = (float) (accMagOrientation[2] * 180 / Math.PI);
                    currentyaw = (float) (accMagOrientation[0] * 180 / Math.PI);


                }
                break;
            case 1:
                if (PitchLine != null && RollLine != null & YawLine != null) {


                    PitchLine.appendData(new DataPoint(lastreceived, (gyroOrientation[1] * 180 / Math.PI)), true, 999999999);
                    RollLine.appendData(new DataPoint(lastreceived, (gyroOrientation[2] * 180 / Math.PI)), true, 999999999);
                    YawLine.appendData(new DataPoint(lastreceived, (gyroOrientation[0] * 180 / Math.PI)), true, 999999999);
                    currentpitch = (float) (gyroOrientation[1] * 180 / Math.PI);
                    currentroll = (float) (gyroOrientation[2] * 180 / Math.PI);
                    currentyaw = (float) (gyroOrientation[0] * 180 / Math.PI);



                }
                break;
            case 2:


                if (PitchLine != null && RollLine != null & YawLine != null) {
                    PitchLine.appendData(new DataPoint(lastreceived, (fusedOrientation[1] * 180 / Math.PI)), true, 999999999);
                    RollLine.appendData(new DataPoint(lastreceived, (fusedOrientation[2] * 180 / Math.PI)), true, 999999999);
                    YawLine.appendData(new DataPoint(lastreceived, (fusedOrientation[0] * 180 / Math.PI)), true, 999999999);
                    currentpitch = (float) (fusedOrientation[1] * 180 / Math.PI);
                    currentroll = (float) (fusedOrientation[2] * 180 / Math.PI);
                    currentyaw = (float) (fusedOrientation[0] * 180 / Math.PI);



                }
                break;
        }


            if (seriesPitch.size() > 500) {
                seriesPitch.removeFirst();
                seriesYaw.removeFirst();
                seriesRoll.removeFirst();

            }

            seriesPitch.addLast(null, currentpitch);
            seriesRoll.addLast(null, currentroll);
            seriesYaw.addLast(null, currentyaw);

            plot.post(new Runnable() {
                public void run() {

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