package com.example.myapplication.Fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.MyElevationModel;
import com.example.myapplication.Model.MyStepsModel;
import com.example.myapplication.Model.RequestsModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.example.myapplication.Services.CustomGraphView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyData extends Fragment implements DatePickerDialog.OnDateSetListener {

    private TextView name;
    private TextView surname;
    private TextView type;
    private TextView email;
    private TextView totalelevation;
    private TextView patientsteps;
    private Button datedialog;
    private TextView datechosen;
    private CircleImageView profpic;
    private String dateString;
    private CollectionReference collection;
    private List<Long> thedates;
    private Long date;

    private UserModel currentuser;
    private Long totaltimeinmsecs;
    private LineGraphSeries<DataPoint> Elevationline;

    private CustomGraphView graph;
    private ArrayList<MyElevationModel> allelevations = new ArrayList<>();
    private ArrayList<MyElevationModel> reducedelevations = new ArrayList<>();

    private DocumentReference document;
    List<RequestsModel> RequestList;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private Query query;
    String TAG = "mypatients frag: ";


    public MyData() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_data, container, false);

        datedialog = view.findViewById(R.id.Patientdatedialog);
        datechosen = view.findViewById(R.id.Patientdatechosen);
        name = view.findViewById(R.id.Patientdataname);
        surname = view.findViewById(R.id.Patientdatasurname);
        type = view.findViewById(R.id.Patientdatatype);
        email = view.findViewById(R.id.Patientdataemail);
        totalelevation = view.findViewById(R.id.Patientdatatotalelevationtime);

        profpic = view.findViewById(R.id.Patientdata_profile_image);
        graph = view.findViewById(R.id.Patientdataview);

        patientsteps = view.findViewById(R.id.Patientdatastepsmade);
        date = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        dateString = sdf.format(date);
        Log.d(TAG, "the date is " + dateString);

        document = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid());
        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                currentuser = task.getResult().toObject(UserModel.class);
                assert currentuser != null;
                name.setText(currentuser.getName());
                surname.setText(currentuser.getSurname());
                type.setText(currentuser.getType());
                email.setText(currentuser.getUsername());
                if (currentuser.getImageURL().equals("default")) {
                    profpic.setImageResource(R.mipmap.ic_launcher);
                }else if (currentuser != null && profpic != null && getContext() != null) {
                    Glide.with(getContext()).load(currentuser.getImageURL()).into(profpic);
                }
            }
      });

        datedialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();

            }
        });
        resetgraph();
        getsteps();
        addgraph();
        getelevations();
        gettotalelevationtime();
        return view;

    }


    private void resetgraph() {
        graph.init();
        totaltimeinmsecs = 0L;
        graph.removeAllSeries();
        allelevations.clear();
        reducedelevations.clear();

        Log.d(TAG, "dismiss called finally");
        if (Elevationline != null) {
            Elevationline.resetData(new DataPoint[]{});
        }
        graph.removeAllSeries();
        graph.getGridLabelRenderer().invalidate(false, false);
        graph.onDataChanged(false, false);
        Log.d(TAG, "cleared series");


    }


    private void getsteps() {
        final Query query4 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Steps");
        query4.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    MyStepsModel myStepsModel = documentSnapshot.toObject(MyStepsModel.class);
                    assert myStepsModel != null;
                    if (myStepsModel.getDate().equals(dateString)) {
                        patientsteps.setText("Today patient made: " + myStepsModel.getStepsmade() + " Steps");


                    } else patientsteps.setText("Today patient made: " + 0 + " Steps");

                }
            }

        });

    }


    private void addgraph() {
        Elevationline = new LineGraphSeries<>();
        graph.addSeries(Elevationline);
        graph.setTitle("Elevation");
        Elevationline.setTitle("Up/Down");
        Elevationline.setColor(Color.CYAN);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        graph.getGridLabelRenderer().setHumanRounding(false,true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        final SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm:ss.SSS");
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Log.d(TAG, "it goes in " + sdf2.format(new Date((long) value)));

                    return sdf2.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }


            }
        });

    }


    private void getelevations() {
        query = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("Elevation").document(dateString).collection("Readings").orderBy("timestamp", Query.Direction.ASCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    final MyElevationModel myElevationModel = documentSnapshot.toObject(MyElevationModel.class);
                    allelevations.add(myElevationModel);
                    Log.d(TAG, "The models i got is:" + myElevationModel);
                }

                reduceelevations();
                for (int y = 0; y < reducedelevations.size(); y++) {
                    Elevationline.appendData(new DataPoint(reducedelevations.get(y).getTimestamp(), reducedelevations.get(y).getStatus()), true, 999999999);
                    graph.getViewport().setMinX(reducedelevations.get(0).getTimestamp());
                    graph.getViewport().setMaxX(reducedelevations.get(reducedelevations.size() - 1).getTimestamp());
                    graph.onDataChanged(false, false);
                    graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

                }

            }


        });
    }


    private void gettotalelevationtime() {

        for (int y = 0; y < allelevations.size(); y++) {
            if ((allelevations.get(y).getStatus()) == 1) {
                Log.d(TAG, "It got the up");
                Long current = allelevations.get(y).getTimestamp();
                if (y + 1 < allelevations.size()) {
                    //if (allelevations.contains(allelevations.get(y + 1))) {
                    if (allelevations.get(y + 1).getStatus() == 1) {
                        Log.d(TAG, "this is up again " + allelevations.get(y + 1).getTimestamp());
                        Long next = allelevations.get(y + 1).getTimestamp();

                        totaltimeinmsecs += (next - current);
                    } else {
                        Long down = allelevations.get(y + 1).getTimestamp();
                        totaltimeinmsecs += down - current;
                    }

                } else {
                    Long currenttime = System.currentTimeMillis();
                    Long lastknownvalue = allelevations.get((allelevations.size() - 1)).getTimestamp();
                    totaltimeinmsecs += (currenttime - lastknownvalue);

                }

            }
        }
        Log.d(TAG, "The total time is: " + totaltimeinmsecs);
        int hours = (int) (totaltimeinmsecs / 3600000);
        int remainder = (int) (totaltimeinmsecs - hours * 3600000);
        int mins = remainder / 60000;
        remainder = remainder - mins * 60000;
        int secs = remainder / 1000;
        remainder = remainder - secs * 1000;
        int msecs = remainder;

        totalelevation.setText("Total Elevation in Seconds is: " + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs) + "." + String.format("%03d", msecs));


    }


    private void reduceelevations() {
        reducedelevations.addAll(allelevations);
        if (reducedelevations != null && reducedelevations.size() > 0) {
            for (int y = 1; y < reducedelevations.size(); y++) {
                if (reducedelevations.get(y).getTimestamp().equals(reducedelevations.get(y - 1).getTimestamp())) {
                    reducedelevations.remove(reducedelevations.get(y));
                }

            }
        }

    }




    public void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Objects.requireNonNull(getContext()),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


// Create a new instance of DatePickerDialog and return it


    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the currentuser
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String date = format.format(calendar.getTime());
        graph.init();

        datechosen.setText(date);
        resetgraph();
        dateString = date;


        final Query query4 = FirebaseFirestore.getInstance().collection("User").document(currentuser.getId()).collection("Steps");
        query4.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    MyStepsModel myStepsModel = documentSnapshot.toObject(MyStepsModel.class);
                    assert myStepsModel != null;
                    if (myStepsModel.getDate().equals(dateString)) {
                        patientsteps.setText("Today patient made: " + myStepsModel.getStepsmade() + " Steps");


                    } else patientsteps.setText("Today patient made: " + 0 + " Steps");

                }
            }

        });

        Elevationline = new LineGraphSeries<>();
        graph.addSeries(Elevationline);
        graph.setTitle("Elevation");
        Elevationline.setTitle("Up/Down");
        Elevationline.setColor(Color.CYAN);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        graph.getGridLabelRenderer().setHumanRounding(false, true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        final SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm:ss.SSS");
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Log.d(TAG, "it goes in " + sdf2.format(new Date((long) value)));

                    return sdf2.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }


            }
        });

        query = FirebaseFirestore.getInstance().collection("User").document(currentuser.getId()).collection("Elevation").document(dateString).collection("Readings").orderBy("timestamp", Query.Direction.ASCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    final MyElevationModel myElevationModel = documentSnapshot.toObject(MyElevationModel.class);
                    allelevations.add(myElevationModel);
                    Log.d(TAG, "The models i got is:" + myElevationModel);
                    assert myElevationModel != null;


                }

                reduceelevations();
                for (int y = 0; y < reducedelevations.size(); y++) {
                    Elevationline.appendData(new DataPoint(reducedelevations.get(y).getTimestamp(), reducedelevations.get(y).getStatus()), true, 999999999);
                    graph.getViewport().setMinX(reducedelevations.get(0).getTimestamp());
                    graph.getViewport().setMaxX(reducedelevations.get(reducedelevations.size() - 1).getTimestamp());
                    graph.onDataChanged(false, false);
                    graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

                }


                for (int y = 0; y < allelevations.size(); y++) {
                    if ((allelevations.get(y).getStatus()) == 1) {
                        Log.d(TAG, "It got the up");
                        Long current = allelevations.get(y).getTimestamp();
                        if (y + 1 < allelevations.size()) {
                            //if (allelevations.contains(allelevations.get(y + 1))) {
                            if (allelevations.get(y + 1).getStatus() == 1) {
                                Log.d(TAG, "this is up again " + allelevations.get(y + 1).getTimestamp());
                                Long next = allelevations.get(y + 1).getTimestamp();

                                totaltimeinmsecs += (next - current);
                            } else {
                                Long down = allelevations.get(y + 1).getTimestamp();
                                totaltimeinmsecs += down - current;
                            }

                        } else {
                            Long currenttime = System.currentTimeMillis();
                            Long lastknownvalue = allelevations.get((allelevations.size() - 1)).getTimestamp();
                            totaltimeinmsecs += (currenttime - lastknownvalue);

                        }

                    }
                }
                Log.d(TAG, "The total time is: " + totaltimeinmsecs);
                int hours = (int) (totaltimeinmsecs / 3600000);
                int remainder = (int) (totaltimeinmsecs - hours * 3600000);
                int mins = remainder / 60000;
                remainder = remainder - mins * 60000;
                int secs = remainder / 1000;
                remainder = remainder - secs * 1000;
                int msecs = remainder;

                totalelevation.setText("Total Elevation in Seconds is: " + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs) + "." + String.format("%03d", msecs));


            }
        });


    }
}