package com.example.myapplication.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.UserAdapter;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> mUsers;
    private CollectionReference collection;
    private DocumentReference document;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search,container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        readUsers();

        EditText search_users = view.findViewById(R.id.search_users);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }

    private void searchUsers (String s) {
        final FirebaseUser Fuser = FirebaseAuth.getInstance().getCurrentUser();
        final Query query = FirebaseFirestore.getInstance().collection("User").orderBy("search")
                .startAt(s)
                .endAt(s+"\uf8ff");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
               query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                   @Override
                   public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                       mUsers.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshots1: task.getResult()) {
                            UserModel userModel = queryDocumentSnapshots1.toObject(UserModel.class);
                            assert Fuser != null;
                            if (!userModel.getId().equals(Fuser.getUid())&& userModel.getType().equals("Doctor") && userModel.getApproved().equals("approved")) {
                                mUsers.add(userModel);
                            }
                        }


                        userAdapter = new UserAdapter(getContext(), mUsers, false);
                        recyclerView.setAdapter(userAdapter);
                }

                   });
               }
            });
        }




    private void readUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        collection = FirebaseFirestore.getInstance().collection("User");
        collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        mUsers.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot1 : task.getResult()) {
                            UserModel userModel = queryDocumentSnapshot1.toObject(UserModel.class);
                            assert firebaseUser != null;
                            if (!userModel.getId().equals(firebaseUser.getUid())&& userModel.getType().equals("Doctor")&& userModel.getApproved().equals("approved")){
                                mUsers.add(userModel);
                            }
                        }

                        userAdapter = new UserAdapter(getContext(), mUsers, false );
                        recyclerView.setAdapter(userAdapter);
                    }
                    });
                }
            });
        }
}



