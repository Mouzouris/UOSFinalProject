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

import com.example.myapplication.Adapter.MakeRequestAdapter;
import com.example.myapplication.Model.RequestsModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
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


public class MakeRequests extends Fragment {
    private RecyclerView recyclerView;
    private MakeRequestAdapter makeRequestAdapter;
    private List<UserModel> mUsers;
    private CollectionReference collection;
    private DocumentReference document;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    String TAG = "request_frag";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_make_requests, container, false);
        recyclerView = view.findViewById(R.id.makerequests_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        requestmade();
        updateview();


        return view;
    }



    private void requestmade() {

        final Query query = FirebaseFirestore.getInstance().collection("User").whereEqualTo("type", "Doctor").whereEqualTo("approved", "approved");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mUsers.clear();
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    final UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    Log.d(TAG, "Current data from USER QUERY: " + documentSnapshot.getData());
                    assert userModel != null;
                    if (!userModel.getId().equals(fuser.getUid())) {
                        mUsers.add(userModel);// put all doctors in}


                        final Query query = FirebaseFirestore.getInstance().collection("Requests");
                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                assert queryDocumentSnapshots != null;
                                for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots.getDocuments()) {
                                    final RequestsModel requestsModel = documentSnapshot1.toObject(RequestsModel.class);
                                    Log.d(TAG, "Current data from the Requests is : " + documentSnapshot1.getData());

                                    if (requestsModel.getDoctorId().equals(userModel.getId()) && (requestsModel.getPatientId().equals(fuser.getUid())) //not the user we picked up beforesame user
                                            && (((requestsModel.getStatus().equals("pending")) //made request
                                            || (requestsModel.getStatus().equals("approved")))) || (requestsModel.getDoctorId().equals(fuser.getUid()))) {
                                        Log.d(TAG, "it will remove this guy(s) from the model " + userModel.getId());
                                        mUsers.remove(userModel);

                                    }

                                }

                                makeRequestAdapter = new MakeRequestAdapter(getContext(), mUsers);
                                recyclerView.setAdapter(makeRequestAdapter);
                            }
                        });

                    }
                }
            }
        });
    }


    private void updateview() {
        final Query query = FirebaseFirestore.getInstance().collection("Requests");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                requestmade();
            }
        });
        final Query query6 = FirebaseFirestore.getInstance().collection("User").whereEqualTo("approved", "approved");
        query6.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                requestmade();

            }
        });


    }

}
