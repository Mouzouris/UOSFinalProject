package com.example.myapplication.Adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.MyElevationModel;
import com.example.myapplication.Model.MyStepsModel;
import com.example.myapplication.Model.RequestsModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.example.myapplication.Services.CustomGraphView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPatientsAdapter extends RecyclerView.Adapter<MyPatientsAdapter.ViewHolder> implements DatePickerDialog.OnDateSetListener {
    private Context mContext;
    private List<UserModel> mUsers;
    private String dateString;
    private CollectionReference collection;
    private List<Long>thedates;
    private boolean click = true;
    private Long date;
    private TextView datechosen;
    private UserModel user;
    private Long totaltimeinmsecs;
    private PopupWindow popupWindow;
    private TextView patientsteps;
    private TextView totalelevation;
    private LineGraphSeries<DataPoint> Elevationline;



    private CustomGraphView graph;
    private ArrayList<MyElevationModel> allelevations = new ArrayList<>();
    private ArrayList<MyElevationModel> reducedelevations = new ArrayList<>();

    private DocumentReference document;
    List<RequestsModel> RequestList;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private Query query;
    String TAG = "mypatients frag: ";

    public MyPatientsAdapter(Context mContext, List<UserModel> mUsers) {
        this.mUsers = mUsers;
        this.mContext = mContext;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_mypatients, parent, false);
        return new MyPatientsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        user = mUsers.get(position);
        holder.name.setText(user.getName());
        holder.username.setText(user.getUsername());

        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (click) {


                    final View popupView = LayoutInflater.from(mContext).inflate(R.layout.popuprofilepatient, null);
                    popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    final Button datedialog = popupView.findViewById(R.id.datedialog);
                    datechosen = popupView.findViewById(R.id.datechosen);
                    TextView name = popupView.findViewById(R.id.popupname);
                    TextView surname = popupView.findViewById(R.id.popupsurname);
                    TextView type = popupView.findViewById(R.id.popuptype);
                    TextView email = popupView.findViewById(R.id.popupemail);
                    totalelevation = popupView.findViewById(R.id.totalelevationtime);

                    CircleImageView profpic = popupView.findViewById(R.id.popup_profile_image);
                    graph = popupView.findViewById(R.id.patientview);

                    patientsteps = popupView.findViewById(R.id.patientstepsmade);
                    date = System.currentTimeMillis();
                    final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    dateString = sdf.format(date);
                    Log.d(TAG, "the date is " + dateString);

                    name.setText(user.getName());
                    surname.setText(user.getSurname());
                    type.setText(user.getType());
                    email.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        profpic.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(mContext).load(user.getImageURL()).into(profpic);
                    }


                    popupWindow.setBackgroundDrawable(new ColorDrawable(
                            android.graphics.Color.TRANSPARENT));
                    popupWindow.setFocusable(true);
                    popupWindow.setBackgroundDrawable(new ColorDrawable());
                    int[] location = new int[2];

                    // Get the View's(the one that was clicked in the Fragment) location
                    view.getLocationOnScreen(location);
                    popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,
                            location[0], location[1] + view.getHeight());
                    popupWindow.setOutsideTouchable(true);


                    datedialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDatePickerDialog();



                        }
                    });


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
                    graph.getGridLabelRenderer().invalidate(false,false);
                    graph.onDataChanged(false,false);
                    Log.d(TAG, "cleared series");





                    final Query query4 = FirebaseFirestore.getInstance().collection("User").document(user.getId()).collection("Steps");
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
                    graph.getGridLabelRenderer().setHumanRounding(false);
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

                    query = FirebaseFirestore.getInstance().collection("User").document(user.getId()).collection("Elevation").document(dateString).collection("Readings").orderBy("timestamp", Query.Direction.ASCENDING);

                    query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            assert queryDocumentSnapshots != null;
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                final MyElevationModel myElevationModel = documentSnapshot.toObject(MyElevationModel.class);
                                allelevations.add(myElevationModel);
                                Log.d(TAG, "The models i got is:" + myElevationModel);
                                assert myElevationModel != null;



//open later on
//                                Elevationline.appendData(new DataPoint(myElevationModel.getTimestamp() , myElevationModel.getStatus()), true, 999999999);
//                                graph.getViewport().setMinX(allelevations.get(0).getTimestamp() );
//                                graph.getViewport().setMaxX(allelevations.get(allelevations.size()-1).getTimestamp() );
//                                graph.onDataChanged(false,false);

                            }

                           reduceelevations();
                            for (int y = 0; y < reducedelevations.size(); y++) {
                                Elevationline.appendData(new DataPoint(reducedelevations.get(y).getTimestamp(), reducedelevations.get(y).getStatus()), true, 999999999);
                                graph.getViewport().setMinX(reducedelevations.get(0).getTimestamp());
                                graph.getViewport().setMaxX(reducedelevations.get(reducedelevations.size() - 1).getTimestamp());
                                graph.onDataChanged(false, false);
                            }







                            for (int y = 0; y < allelevations.size(); y++) {
                                if ((allelevations.get(y).getStatus()) == 1) {
                                    Log.d(TAG, "It got the up");
                                    Long current = allelevations.get(y).getTimestamp();
                                    if (y+1 < allelevations.size()) {
                                        //if (allelevations.contains(allelevations.get(y + 1))) {
                                        if (allelevations.get(y + 1).getStatus() == 1) {
                                            Log.d(TAG, "this is up again " + allelevations.get(y + 1).getTimestamp());
                                            Long next = allelevations.get(y + 1).getTimestamp();

                                            totaltimeinmsecs += (next - current);
                                        } else {
                                            Long down = allelevations.get(y + 1).getTimestamp();
                                            totaltimeinmsecs += down-current;
                                        }

                                    }else {
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

                            totalelevation.setText("Total Elevation in Seconds is: " + String.format("%02d",hours) + ":" +String.format("%02d",mins) + ":" + String.format("%02d",secs)+ "." + String.format("%03d",msecs));



                        }
                    });



                }else {
                    graph.getGridLabelRenderer().invalidate(false,false);
                    totaltimeinmsecs = 0L;
                    popupWindow.dismiss();
                    graph.removeAllSeries();
                    reducedelevations.clear();
                    allelevations.clear();

                    Log.d(TAG, "dismiss called finally");
                    if (Elevationline != null) {
                        Elevationline.resetData(new DataPoint[]{});
                    }
                    graph.removeAllSeries();
                    Log.d(TAG, "cleared series");
                    click = true;
                }
            }
        });
    }





private void reduceelevations(){
    reducedelevations.addAll(allelevations);
        if (reducedelevations != null && reducedelevations.size() > 0) {
        for (int y = 1; y < reducedelevations.size(); y++) {
            if (reducedelevations.get(y).getTimestamp().equals(reducedelevations.get(y - 1).getTimestamp())) {
                reducedelevations.remove(reducedelevations.get(y));
            }

        }
    }

}


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                mContext,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


        // Create a new instance of DatePickerDialog and return it



        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            String date = format.format(calendar.getTime());
            graph.init();

            datechosen.setText(date);
            totaltimeinmsecs = 0L;
            graph.removeAllSeries();
            allelevations.clear();
            reducedelevations.clear();

            Log.d(TAG, "dismiss called finally");
            if (Elevationline != null) {
                Elevationline.resetData(new DataPoint[]{});
            }
            graph.removeAllSeries();
            graph.getGridLabelRenderer().invalidate(false,false);
            graph.onDataChanged(false,false);
            Log.d(TAG, "cleared series");
            dateString = date;





            final Query query4 = FirebaseFirestore.getInstance().collection("User").document(user.getId()).collection("Steps");
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
            graph.getGridLabelRenderer().setHumanRounding(false);
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

            query = FirebaseFirestore.getInstance().collection("User").document(user.getId()).collection("Elevation").document(dateString).collection("Readings").orderBy("timestamp", Query.Direction.ASCENDING);

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        final MyElevationModel myElevationModel = documentSnapshot.toObject(MyElevationModel.class);
                        allelevations.add(myElevationModel);
                        Log.d(TAG, "The models i got is:" + myElevationModel);
                        assert myElevationModel != null;
//                      Elevationline.appendData(new DataPoint(myElevationModel.getTimestamp() , myElevationModel.getStatus()), true, 999999999);
//                      graph.getViewport().setMinX(allelevations.get(0).getTimestamp() );
//                        graph.getViewport().setMaxX(allelevations.get(allelevations.size()-1).getTimestamp() );
//                        graph.onDataChanged(false,false);







                    }

                    reduceelevations();
                    for (int y = 0; y < reducedelevations.size(); y++) {
                        Elevationline.appendData(new DataPoint(reducedelevations.get(y).getTimestamp(), reducedelevations.get(y).getStatus()), true, 999999999);
                        graph.getViewport().setMinX(reducedelevations.get(0).getTimestamp());
                        graph.getViewport().setMaxX(reducedelevations.get(reducedelevations.size() - 1).getTimestamp());
                        graph.onDataChanged(false, false);
                    }

                    //Elevationline = new LineGraphSeries<>(getDataPoint());
//                    for (int y = 0; y < allelevations.size(); y++) {
//                        //Elevationline.appendData(new DataPoint(y , allelevations.get(y).getStatus()), true, 2);
//
//                        Log.d(TAG, "it will append"+ allelevations.get(y).getTimestamp()*1000);
//                        Elevationline.appendData(new DataPoint(allelevations.get(y).getTimestamp() * 1000, allelevations.get(y).getStatus()), true, 999999999);
//                        graph.getViewport().setMinX(allelevations.get(0).getTimestamp() * 1000);
//                        graph.getViewport().setMaxX(allelevations.get(y).getTimestamp() * 1000);
//                    }


                    for (int y = 0; y < allelevations.size(); y++) {
                        if ((allelevations.get(y).getStatus()) == 1) {
                            Log.d(TAG, "It got the up");
                            Long current = allelevations.get(y).getTimestamp();
                            if (y+1 < allelevations.size()) {
                                //if (allelevations.contains(allelevations.get(y + 1))) {
                                if (allelevations.get(y + 1).getStatus() == 1) {
                                    Log.d(TAG, "this is up again " + allelevations.get(y + 1).getTimestamp());
                                    Long next = allelevations.get(y + 1).getTimestamp();

                                    totaltimeinmsecs += (next - current);
                                } else {
                                    Long down = allelevations.get(y + 1).getTimestamp();
                                    totaltimeinmsecs += down-current;
                                }

                            }else {
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

                    totalelevation.setText("Total Elevation in Seconds is: " + String.format("%02d",hours) + ":" +String.format("%02d",mins) + ":" + String.format("%02d",secs)+ "." + String.format("%03d",msecs));



                }
            });



                }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView name;
        public ImageView profile_image;


        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mypatients_name);
            username = itemView.findViewById(R.id.mypatients_email);
            profile_image = itemView.findViewById(R.id.mypatients_prof);

        }

    }

}



