package com.example.myapplication.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Adapter.MessageAdapter;
import com.example.myapplication.Fragments.APIService;
import com.example.myapplication.Model.ChatModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.Notifications.Client;
import com.example.myapplication.Notifications.Data;
import com.example.myapplication.Notifications.MyResponse;
import com.example.myapplication.Notifications.Sender;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    TextView type;
    TextView name;
    String TAG = "message act:";


    FirebaseUser fuser;
    private CollectionReference collection;
    private DocumentReference document;
    private com.google.firebase.firestore.Query query;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<ChatModel> mchat;
    RecyclerView recyclerView;
    Intent intent;
    ListenerRegistration seenListener;
    String userid;

    APIService apiService;

    boolean notify = false;
    Long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar_message);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view_chat);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.toolbar_image_profile);
        username = findViewById(R.id.toolbar_username);
        name = findViewById(R.id.toolbar_name);
        type = findViewById(R.id.toolbar_type);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        final List<UserModel> chatview = new ArrayList<>();
        query = FirebaseFirestore.getInstance().collection("User");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        chatview.clear();
                        UserModel userModel = null;
                        for (QueryDocumentSnapshot queryDocumentSnapshot1 : task.getResult()) {
                            userModel = queryDocumentSnapshot1.toObject(UserModel.class);
                            if (userModel.getId().equals(userid)) {
                                chatview.add(userModel);

                                type.setText(userModel.getType());
                                name.setText(userModel.getName());
                                username.setText(userModel.getUsername());
                                if (userModel.getImageURL().equals("default")) {
                                    profile_image.setImageResource(R.mipmap.ic_launcher);
                                } else {
                                    //and this
                                    Glide.with(getApplicationContext()).load(userModel.getImageURL()).into(profile_image);
                                }
                            }

                        }
                        readMesagges(fuser.getUid(), userid, userModel.getImageURL());

                    }
                });
            }
        });
        seenMessage(userid);
    }
//

    private void seenMessage(final String userid) {
        collection = FirebaseFirestore.getInstance().collection("Chats");
                seenListener = collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        assert queryDocumentSnapshots != null;
                        for (DocumentSnapshot queryDocumentSnapshots1 : queryDocumentSnapshots.getDocuments()) {
                            ChatModel chatModel = queryDocumentSnapshots1.toObject(ChatModel.class);
                            assert chatModel != null;
                            if (chatModel.getReceiver().equals(fuser.getUid()) && chatModel.getSender().equals(userid)) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isseen", true);
                                queryDocumentSnapshots1.getReference().update(hashMap);

                            }

                        }
                    }
                });
            }




    private void sendMessage(String sender, final String receiver, String message) {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Chats");

        timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("timestamp", timestamp);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        reference.document().set(hashMap);





        final String msg = message;

        document = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid());
                document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        assert documentSnapshot != null;
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (notify) {
                            assert userModel != null;
                            sendNotification(receiver, userModel.getUsername(), msg);
                            Log.d(TAG,"it apparently sent nots "+"   "+receiver+"   "+userModel.getUsername()+"  "+ msg);
                        }
                        notify = false;

                    }
                });
            }


    private void sendNotification(String receiver, final String username, final String message){
        document =  FirebaseFirestore.getInstance().collection("Tokens").document(receiver);
        document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    Token token = documentSnapshot.toObject(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message", userid);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                            if (response.code() == 200){
                                assert response.body() != null;
                                if (response.body().success != 1){
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {

                        }
                    });
                }

        });

    }








    private void readMesagges(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();
        query = FirebaseFirestore.getInstance().collection("Chats").orderBy("timestamp", Query.Direction.ASCENDING);
                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        mchat.clear();
                        assert queryDocumentSnapshots != null;
                        for (DocumentSnapshot queryDocumentSnapshot1 :queryDocumentSnapshots.getDocuments()) {
                            ChatModel chatModel = queryDocumentSnapshot1.toObject(ChatModel.class);
                            assert chatModel != null;
                            if (chatModel.getReceiver().equals(myid) && chatModel.getSender().equals(userid) ||
                                    chatModel.getReceiver().equals(userid) && chatModel.getSender().equals(myid)) {
                                mchat.add(chatModel);
                                Log.d(TAG, "Current data: " + queryDocumentSnapshot1.getData());
                            }


                            messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(messageAdapter);

                        }
                    }
                });
            }





    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status){

        document = FirebaseFirestore.getInstance().collection("User").document(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        document.update(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);

    }
    @Override
    protected void onStart() {
        super.onStart();
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
       seenListener.remove();
        status("offline");
        currentUser("none");
    }
@Override
        protected void onStop() {
            super.onStop();
            seenListener.remove();
            status("offline");
            currentUser("none");
    }
}