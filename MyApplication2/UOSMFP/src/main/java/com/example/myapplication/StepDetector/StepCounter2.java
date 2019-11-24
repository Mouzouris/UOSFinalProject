package com.example.myapplication.StepDetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Model.MyStepsModel;
import com.example.myapplication.Model.Point3D;
import com.example.myapplication.R;
import com.example.myapplication.SensorTag.IntentNames;
import com.example.myapplication.SensorTag.SensorConversion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import static android.content.Context.SENSOR_SERVICE;


public class StepCounter2 extends Fragment {
    private TextView acceleration, steps2second, distanceText, speedtext, steps, totalstepstoday;
    private String TAG = "STepCounter 2:";
    private int stepno;
    private Long date;
    private String dateString;



    private DocumentReference document;
    private static final String FRAGMENT_POSITION = "com.example.myapplication.Fragments.StepCounter2.FRAGMENT_POSITION";

    private SensorManager sm;

    private float[] accel = new float[3];

    private List<Double> list;

    private List<Double> bigList;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private Button pushtodbase;


    private Boolean running = false;

    private long startTime, endTime;
    private double time, height, stride, speed, distance;

    public StepCounter2() {
        // Required empty public constructor
    }

    public static StepCounter2 newInstance(int position) {
        StepCounter2 fragment = new StepCounter2();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        running = true;
        list = new ArrayList<>();
        bigList = new ArrayList<>();
        startTime = System.currentTimeMillis();
        distance = 0;
        speed = 0;
        stepno = 0;
        date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        dateString = sdf.format(date);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_step_counter2, container, false);
        totalstepstoday = v.findViewById(R.id.TotalStepsToday);
        acceleration = v.findViewById(R.id.Acceleration);
        steps = v.findViewById(R.id.Steps);
        steps2second = v.findViewById(R.id.steps2sec);
        distanceText = v.findViewById(R.id.distance);
        speedtext = v.findViewById(R.id.speed);
        pushtodbase = v.findViewById(R.id.pushtodb);

        // Initialize Accelerometer sensor
        sm = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);


        pushtodbase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushtodb();
            }
        });

        updateview();


        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        getActivity().unregisterReceiver(motionUpdateReceiver);


    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(motionUpdateReceiver, makeMotionUpdateIntentFilter());


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
                float[] accelvalues = new float[3];
                accelvalues[0] = (float) v.x;
                accelvalues[1] = (float) v.y;
                accelvalues[2] = (float) v.z;
                System.arraycopy(accelvalues, 0, accel, 0, 3);


                if (running) {
                    double x = accelvalues[0];
                    double y = accelvalues[1];
                    double z = accelvalues[2];
                    acceleration.setText("X: " + x + "\nY: " + y + "\nZ: " + z);
                    // calculate the magnitude mag^2 = x^2 + y^2 + z^2 and add mag to the list
                    // we deal with mag due to count steps in all directions as magnitude neglects directions.
                    double mag = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(y, 2));
                    list.add(mag);
                    bigList.add(mag);
                    Log.d(TAG, "THIS IS WHAT it has in the main list" + bigList.toString());
                    endTime = System.currentTimeMillis();
                    time = (endTime - startTime) / 1000.0;
                    if ((time >= 2.0) && (bigList.size()==11)){
                        int stepsInTwoSeconds = getminiSteps(list);
                        steps2second.setText("#Steps in 2 second: " + stepsInTwoSeconds);

                        height = 1.75;
                        if (stepsInTwoSeconds > 0 && stepsInTwoSeconds <= 2) {
                            stride = height / 5;
                        } else if (stepsInTwoSeconds > 2 && stepsInTwoSeconds <= 3) {
                            stride = height / 4;
                        } else if (stepsInTwoSeconds > 3 && stepsInTwoSeconds <= 4) {
                            stride = height / 3;
                        } else if (stepsInTwoSeconds > 4 && stepsInTwoSeconds <= 5) {
                            stride = height / 2;
                        } else if (stepsInTwoSeconds > 5 && stepsInTwoSeconds <= 6) {
                            stride = height / 1.2;
                        } else if (stepsInTwoSeconds > 6 && stepsInTwoSeconds < 8) {
                            stride = height;
                        } else if (stepsInTwoSeconds >= 8) {
                            stride = 1.2 * height;
                        }
                        distance += stepsInTwoSeconds * stride;
                        speed = stepsInTwoSeconds * stride / 2.0;
                        speedtext.setText("Speed: " + speed + " m/s");
                        distanceText.setText("Distance: " + distance + " m");
                        steps.setText("#Steps: " + getSteps(bigList));
                        //Log.d(TAG, "THIS IS WHAT it has in the main list after "+bigList.toString());
                        startTime = endTime;


                    }
                    //steps.setText("#Steps: " + getSteps(bigList));
                    //Log.d(TAG, "THIS IS WHAT it has in the main list after "+bigList.toString());

                }

                // steps.setText("#Steps: " + getSteps(bigList));


            }
        }
    };

    private int getminiSteps(List<Double> list) {
        StatisticsUtil su = new StatisticsUtil();
        double mean = su.findMean(list);
        double std = su.standardDeviation(list, mean);
        int stepsNumber = su.finAllPeaks(list, std);
        list.clear();
        return stepsNumber;
    }


    private int getSteps(List<Double> list) {
        if (list.size() > 10) {
            StatisticsUtil su = new StatisticsUtil();
            double mean = su.findMean(list);
            double std = su.standardDeviation(list, mean);
            int stepsNumber = su.finAllPeaks(list, std);
            //list.clear();
            stepno += stepsNumber;

        }
        list.clear();
        return stepno;
    }

    private void pushtodb() {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        final String dateString = sdf.format(date);

        final HashMap<String, Object> stepshashmap = new HashMap<>();
        stepshashmap.put("stepsmade", stepno);
        stepshashmap.put("date", dateString);
        stepshashmap.put("user", fuser.getUid());


        document = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Steps").document(dateString);
        document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
    @Override
    public void onEvent(@androidx.annotation.Nullable DocumentSnapshot documentSnapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
        if (documentSnapshot.exists()) {

            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        final MyStepsModel mystepsModel = Objects.requireNonNull(task.getResult()).toObject(MyStepsModel.class);
                        assert mystepsModel != null;
                        Log.d(TAG, "steps already made are" + mystepsModel.getStepsmade());
                        int newsteps = (mystepsModel.getStepsmade() + stepno);
                        FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Steps").document(dateString).update("stepsmade", newsteps);
                        stepno = 0;
                        distance = 0;
                        distanceText.setText("Distance: " + distance + " m");
                        steps.setText("#Steps: " + 0);
                    }
                }

            });


    }else {
            FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Steps").document(dateString).set(stepshashmap);
            stepno = 0;
            distance = 0;
            distanceText.setText("Distance: " + distance + " m");
            steps.setText("#Steps: " + 0);
        }
    }
});
    }

    private void updateview() {

        final Query query4 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Steps");
        query4.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    MyStepsModel myStepsModel = documentSnapshot.toObject(MyStepsModel.class);
                    if (myStepsModel.getDate().equals(dateString)) {

                        totalstepstoday.setText("Today you made: " + myStepsModel.getStepsmade()+" Steps");
                    }

                }
            }
        });
    }





    private static IntentFilter makeMotionUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentNames.ACTION_MOV_CHANGE);
        return intentFilter;
    }


}
