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

import com.example.myapplication.Adapter.ApproveRequestAdapter;
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


public class ApproveRequests extends Fragment  {
    private RecyclerView recyclerView;
    private ApproveRequestAdapter approveRequestAdapter;
    private List<UserModel> mUsers;
    private List<RequestsModel> mReqeusts;
    private CollectionReference collection;
    private DocumentReference document;
    private String requestusers;
    private String requestIDs;
    private ArrayList<String> theids;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    String TAG = "approve_request_frag";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_approve_requests, container, false);
        recyclerView = view.findViewById(R.id.approverequests_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        mReqeusts = new ArrayList<>();

        //approverequestshow();
        //requestmade();
        //requestreceived();
        //requestids();
        //pushthedata();
        updateview();



        return view;
    }
//    private void getpatients() {
//
//        final Query query = FirebaseFirestore.getInstance().collection("User").whereEqualTo("type", "Patient");
//        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                mUsers.clear();
//                assert queryDocumentSnapshots != null;
//                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                    final UserModel userModel = documentSnapshot.toObject(UserModel.class);
//                    Log.d(TAG, "Current data from USER QUERY: " + documentSnapshot.getData());
//                    assert userModel != null;
//                    if (!userModel.getId().equals(fuser.getUid())) {
//                        mUsers.add(userModel);// put all patients in}
//                        removepatients();
//
//                    }
//                }
//            }
//        });
//    }


    private void requestids() {
        theids = new ArrayList<>();
        Query query1 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("ReceivedRequestIDs").whereEqualTo("status", "pending");
        query1.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot queryDocumentSnapshots1 : queryDocumentSnapshots.getDocuments()) {

                    final RequestIDsModel requestIDsModel = queryDocumentSnapshots1.toObject(RequestIDsModel.class);
                    assert requestIDsModel != null;
                    requestusers = requestIDsModel.getSender();
                    requestIDs = requestIDsModel.getRequestID();

                    if (theids.contains(requestusers)){return;} else {
                        theids.add(requestusers);
                        Log.d(TAG, "the ids we got are" + theids);

                    }


                    }
                    gettheusersonrequest();

            }
        });
    }

//

private void gettheusersonrequest(){
        mUsers.clear();
    pushthedata();

    for (int y = 0; y < theids.size();y++){
            document = FirebaseFirestore.getInstance().collection("User").document(theids.get(y));
            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final UserModel userModel = task.getResult().toObject(UserModel.class);
                    assert userModel != null;
                    if (mUsers.contains(userModel)) {
                        return;
                    } else {
                        mUsers.add(userModel);
                        Log.d(TAG, "this is what it passes for display " + userModel.getId());

                    }


                    pushthedata();


                }


            });
                }

            }



    private void pushthedata(){
        approveRequestAdapter = new ApproveRequestAdapter(getContext(), mUsers);
        recyclerView.setAdapter(approveRequestAdapter);
    }

//private void approverequestshow() {
//    Query query1 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("ReceivedRequestIDs").whereEqualTo("status", "pending");
//    query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
//        @Override
//        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//            mUsers.clear();
//            assert queryDocumentSnapshots != null;
//            for (DocumentSnapshot queryDocumentSnapshot1 : queryDocumentSnapshots.getDocuments()) {
//                final RequestIDsModel requestIDsModel = queryDocumentSnapshot1.toObject(RequestIDsModel.class);
//                assert requestIDsModel != null;
//                Log.d(TAG, "Current data from requestIDS QUERY: " + requestIDsModel.getRequestID());
//                String requestids = requestIDsModel.getRequestID();//these are all the request ids received for this doctor that are pending
//                String requestUser = requestIDsModel.getSender();
//
//
//                final Query query2 = FirebaseFirestore.getInstance().collection("User").whereEqualTo("id", requestUser);
//                query2.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                        assert queryDocumentSnapshots != null;
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            final UserModel userModel = documentSnapshot.toObject(UserModel.class);
//                            Log.d(TAG, "Current data from USER QUERY: " + documentSnapshot.getData());
//                            mUsers.add(userModel);// put all doctors in}
//
//
//                        }
//                        approveRequestAdapter = new ApproveRequestAdapter(getContext(), mUsers);
//                        recyclerView.setAdapter(approveRequestAdapter);
//                    }
//
//
//                });
//            }
//        }
//    });
//}

//gets request multiple times because of the forloop
//    private void requestreceived() {
//        Query query2 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("ReceivedRequestIDs").whereEqualTo("status", "pending");
//        query2.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                mUsers.clear();
//                assert queryDocumentSnapshots != null;
//                for (DocumentSnapshot queryDocumentSnapshot1 : queryDocumentSnapshots.getDocuments()) {
//                    final RequestIDsModel requestIDsModel = queryDocumentSnapshot1.toObject(RequestIDsModel.class);
//                    assert requestIDsModel != null;
//                    requestusers = requestIDsModel.getSender();
//                    Log.d(TAG, "Current data from requestIDS QUERY: " + requestIDsModel.getRequestID());
//
//
//                    document = FirebaseFirestore.getInstance().collection("User").document(requestusers);
//                  document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                      @Override
//                      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            final UserModel userModel = task.getResult().toObject(UserModel.class);
//                          assert userModel != null;
//                          if ((userModel.getId().equals(requestusers))&&
//                                    (userModel.getType().equals("Patient"))) {
//
//                                if (mUsers.contains(userModel)) {
//                                    return;
//                                } else {
//                                    mUsers.add(userModel);
//                                    Log.d(TAG, "this is what it passes for display " + userModel.getId());
//
//                                }
//                            }
//
//
//                          approveRequestAdapter = new ApproveRequestAdapter(getContext(), mUsers );
//                          recyclerView.setAdapter(approveRequestAdapter);
//
//
//                        }
//                    });
//
//                }
//            }
//        });
//    }








//    private void requestmade() {
//        Query query2 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("ReceivedRequestIDs").whereEqualTo("status", "pending");
//        query2.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                assert queryDocumentSnapshots != null;
//                for (DocumentSnapshot queryDocumentSnapshot1 : queryDocumentSnapshots.getDocuments()) {
//
//                    final RequestIDsModel requestIDsModel = queryDocumentSnapshot1.toObject(RequestIDsModel.class);
//                    assert requestIDsModel != null;
//                    Log.d(TAG, "Current data from requestIDS QUERY: " + requestIDsModel.getRequestID());
//                     final String requestids = requestIDsModel.getRequestID();//these are all the request ids received for this doctor that are pending
//                     String requestusers = requestIDsModel.getSender();
//
//
//        final Query query1 = FirebaseFirestore.getInstance().collection("User").whereEqualTo("type", "Patient");
//        query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                       @Override
//                                       public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                                           mUsers.clear();
//                                           assert queryDocumentSnapshots != null;
//                                           for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                                               final UserModel userModel = documentSnapshot.toObject(UserModel.class);
//                                               Log.d(TAG, "Current data from USER QUERY: " + documentSnapshot.getData());
//                                               assert userModel != null; // get all patients
//
//
//
//
//
//                                                               Query query3 = FirebaseFirestore.getInstance().collection("Requests").whereEqualTo("doctorId", fuser.getUid()).whereEqualTo("id", requestids); //requests for thsi doctor only
//                                                               query3.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                                                   @Override
//                                                                   public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//
//                                                                       assert queryDocumentSnapshots != null;
//                                                                       for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {//these are all the requests
//                                                                           final RequestsModel requestsModel = documentSnapshot.toObject(RequestsModel.class);
//                                                                           Log.d(TAG, "Current data from approve request QUERY: " + documentSnapshot.getData());
//                                                                           assert requestsModel != null;
//                                                                           if ((requestIDsModel.getRequestID().equals(requestsModel.getID()))
//                                                                                   && (requestsModel.getDoctorId().equals(fuser.getUid()))
//                                                                                   && (requestsModel.getPatientId().equals(userModel.getId()))
//                                                                                   && (requestsModel.getStatus().equals(requestIDsModel.getStatus()))) {
//
//                                                                               if (mUsers.contains(userModel)){return;} else
//                                                                               {
//                                                                                   mUsers.add(userModel);
//                                                                                   Log.d(TAG, "this is what it passes for display " + userModel.getId());
//
//                                                                               }
//
//                                                                           }
//                                                                           approveRequestAdapter = new ApproveRequestAdapter(getContext(), mUsers );
//                                                                           recyclerView.setAdapter(approveRequestAdapter);
//                                                                       }
//
//
//
//                                                                   }
//
//                                                               });
//
//
//                                                           }
//
//                                                       }
//
//
//                                                   });
//
//                                               }
//
//                                           }
//
//
//                                   });
//    }













//this one doesn't updat eon change and imlements the users on top of the other even with the update function.
//
//       Query query1 = FirebaseFirestore.getInstance().collection("Requests").whereEqualTo("status", "pending").whereEqualTo("doctorId", fuser.getUid());
//                    query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                            mUsers.clear();
//                            assert queryDocumentSnapshots != null;
//                            for (final DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            final RequestsModel requestsModel = documentSnapshot.toObject(RequestsModel.class);
//                                assert requestsModel != null;
//
//                                if (requestsModel.getDoctorId().equals(fuser.getUid()) //not the user we picked up before same user
//                                        && (requestsModel.getStatus().equals("pending"))){ //made request
//                                    mReqeusts.add(requestsModel); //these are all the requests
//                                    Log.d(TAG, "Current  Requests is : " + requestsModel.getID());
//
//                                    final String theuserswewant = requestsModel.getPatientId();
//                                    Log.d(TAG, "the user we want is : " + theuserswewant);
//
//                                   document = FirebaseFirestore.getInstance().collection("User").document(theuserswewant);
//                                    document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                            assert documentSnapshot != null;
//                                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
//                                                Log.d(TAG, "Current data from approve USER QUERY: " + documentSnapshot.getData());
//                                                assert userModel != null;
//                                                if (!userModel.getId().equals(fuser.getUid()) && userModel.getType().equals("Patient") && userModel.getId().equals(theuserswewant)) {
//                                                    Log.d(TAG, "this guy goes in the list " + userModel.getId());
//                                                        boolean contains = false;
//                                                        for (int y = 0; y < mUsers.size();y++){
//                                                            if (mUsers.get(y) == userModel){
//                                                                contains= true;
//                                                                break;
//                                                            }
//                                                    }
//                                                        if (contains){return;}
//                                                        else  {mUsers.add(userModel);
//                                                        Log.d(TAG, "this is what it passes for display " + userModel.getId());
//
//                                                        }
//
//
//                                            }
//                                            approveRequestAdapter = new ApproveRequestAdapter(getContext(), mUsers );
//                                            recyclerView.setAdapter(approveRequestAdapter);
//
//                                        }
//
//
//                                    });
//
//                        }
//
//                    }
//                }
//
//        });
//    }


    private void updateview() {

        final Query query4 = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid()).collection("ReceivedRequestIDs");
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



    }

}
