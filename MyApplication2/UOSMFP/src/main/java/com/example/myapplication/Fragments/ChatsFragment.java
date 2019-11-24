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

import com.example.myapplication.Adapter.UserAdapter;
import com.example.myapplication.Model.ChatModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.Notifications.Token;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> mUsers;
    private FirebaseUser Fuser;
    private CollectionReference collection;
    private DocumentReference document;
    private List<String> usersList;
    private String TAG = "Something";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        collection = FirebaseFirestore.getInstance().collection("Chats");
                collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        usersList.clear();
                        assert queryDocumentSnapshots != null;
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            ChatModel chatModel = document.toObject(ChatModel.class);
                            assert chatModel != null;
                            if (chatModel.getSender().equals(Fuser.getUid())) {
                                usersList.add(chatModel.getReceiver());
                            }
                            if (chatModel.getReceiver().equals(Fuser.getUid())) {
                                usersList.add(chatModel.getSender());
                            }
                        }

                        chatList();
                    }


                });





        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "get InstanceId failed", task.getException());
                    return;
                }

                String refreshToken = task.getResult().getToken();
                updateToken(refreshToken);
                Log.e("newToken", refreshToken);

            }
        });


        return view;
    }

    private void updateToken(String token) {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Tokens");
        Token token1 = new Token(token);
        reference.document(Fuser.getUid()).set(token1);

    }

    private void chatList() {
        mUsers = new ArrayList<>();

        collection = FirebaseFirestore.getInstance().collection("User");
                collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        mUsers.clear();

                        assert queryDocumentSnapshots != null;
                        for (DocumentSnapshot snapshots : queryDocumentSnapshots.getDocuments()) {
                            UserModel userModel = snapshots.toObject(UserModel.class);
                            for (String id : usersList) {
                                assert userModel != null;
                                if (userModel.getId().equals(id)) {
                                    if (mUsers.size() != 0) {
                                        for (UserModel user1 : mUsers) {
                                            if (!userModel.getId().equals(user1.getId())) {
                                                mUsers.add(userModel);
                                            }
                                        }
                                    } else {
                                        mUsers.add(userModel);
                                    }
                                }
                            }
                        }
                        userAdapter = new UserAdapter(getContext(), mUsers, true);
                        recyclerView.setAdapter(userAdapter);
                    }
                });
            }


}
