package com.example.myapplication.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.MyDoctorsAdapterChat;
import com.example.myapplication.Model.RequestIDsModel;
import com.example.myapplication.Model.RequestsModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class MyDoctorsChat extends Fragment  {
    private RecyclerView recyclerView;
    private MyDoctorsAdapterChat myDoctorsAdapterChat;
    private List<UserModel> mUsers;
    private List<RequestsModel> mReqeusts;
    private CollectionReference collection;
    private DocumentReference document;
    private String mydoctors;
    private ArrayList<String> theids;
    private ArrayList<String> thestatuses;

    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    String TAG = "show_request_frag";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_doctorschat, container, false);
        recyclerView = view.findViewById(R.id.mydoctorschat_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        mReqeusts = new ArrayList<>();


        updateview();

        return view;
    }



    private void requestids() {
        theids = new ArrayList<>();
        thestatuses = new ArrayList<>();
        Query query1 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("RequestIDs").whereEqualTo("status", "approved");
        query1.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot queryDocumentSnapshots1 : queryDocumentSnapshots.getDocuments()) {

                    final RequestIDsModel requestIDsModel = queryDocumentSnapshots1.toObject(RequestIDsModel.class);
                    assert requestIDsModel != null;
                    if (requestIDsModel.getStatus().equals("approved")) {
                        mydoctors = requestIDsModel.getReceiver();
                    }

                    if (theids.contains(mydoctors)){return;} else {
                        theids.add(mydoctors);
                        Log.d(TAG, "the ids we got are" + theids);
                        Log.d(TAG, "the statuses we got are" + thestatuses);


                    }


                }
                gettheusersonrequest();

            }
        });
    }

//

    private void gettheusersonrequest(){
        mUsers.clear();
        myDoctorsAdapterChat = new MyDoctorsAdapterChat(getContext(), mUsers, false);
        recyclerView.setAdapter(myDoctorsAdapterChat);
        for (int y = 0; y < theids.size();y++) {
            assert  document != null;
            document = FirebaseFirestore.getInstance().collection("User").document(theids.get(y));


            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final UserModel userModel = task.getResult().toObject(UserModel.class);
                    assert userModel != null;
                    if (mUsers.contains(userModel)|| userModel.getApproved().equals("False") ||  userModel.getType().equals("Patient")) {
                        return;
                    } else {
                        mUsers.add(userModel);
                        Log.d(TAG, "this is what it passes for display " + userModel.getId());

                    }

                    myDoctorsAdapterChat = new MyDoctorsAdapterChat(getContext(), mUsers, false );

                    recyclerView.setAdapter(myDoctorsAdapterChat);


                }


            });
        }


    }




    private void updateview() {

        final Query query4 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("RequestIDs");
        query4.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //requestreceived();
                //getpatients();
                requestids();
                //requestmade();

            }
        });
        final Query query5 = FirebaseFirestore.getInstance().collection("Requests");
        query5.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                requestids();

            }
        });
        final Query query6 = FirebaseFirestore.getInstance().collection("User").whereEqualTo("approved", "approved");
        query6.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                requestids();

            }
        });


    }

}
