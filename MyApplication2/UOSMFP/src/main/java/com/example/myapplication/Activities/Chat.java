package com.example.myapplication.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.myapplication.Fragments.ChatsFragment;
import com.example.myapplication.Fragments.MyDoctorsChat;
import com.example.myapplication.Fragments.MyPatientsChat;
import com.example.myapplication.Model.ChatModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.myapplication.R.layout.activity_chat;

public class Chat extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    private CollectionReference collection;
    private DocumentReference document;
    String TAG = "chat_act";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(activity_chat);

    Toolbar toolbar = findViewById(R.id.chat_activity_toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("");

    profile_image = findViewById(R.id.profile_image);
    username = findViewById(R.id.username);

    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    document = FirebaseFirestore.getInstance().collection("User").document(firebaseUser.getUid());
    document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    assert snapshot != null;
                    UserModel userModel = snapshot.toObject(UserModel.class);
                    Log.w(TAG, "it made it to the object phase");
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (userModel != null) {
                        username.setText(userModel.getUsername());
                        if (userModel.getImageURL().equals("default")) {
                            if (profile_image != null)
                                profile_image.setImageResource(R.mipmap.ic_launcher_round);
                        } else {
                            if (profile_image != null)
                                Glide.with(getApplicationContext()).load(userModel.getImageURL()).into(profile_image);
                        }

                    } else {
                        Log.d(TAG, "Current data: null");
                    }

                }


            });


        }
    });


    final TabLayout tabLayout = findViewById(R.id.tab_layout);
    final ViewPager viewPager = findViewById(R.id.view_pager);



    collection = FirebaseFirestore.getInstance().collection("Chats");

         collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
             @Override
             public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                 final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            int unread = 0;
                 assert queryDocumentSnapshots != null;
                 for (DocumentSnapshot document1 : queryDocumentSnapshots.getDocuments() ){
                     ChatModel chatModel = document1.toObject(ChatModel.class);
                     assert chatModel != null;
                     if (chatModel.getReceiver().equals(firebaseUser.getUid()) && !chatModel.isIsseen()){
                    unread++;
                }
            }

            if (unread == 0 ){
                viewPagerAdapter.AddFragment(new ChatsFragment(), "Chats");
            } else if (unread == 1){
                viewPagerAdapter.AddFragment(new ChatsFragment(), "("+unread+") Chat");
            } else
                {viewPagerAdapter.AddFragment(new ChatsFragment(), "("+unread+") Chats");
            }
           document = FirebaseFirestore.getInstance().collection("User"). document(firebaseUser.getUid());
            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final UserModel userModel = task.getResult().toObject(UserModel.class);
                    assert userModel != null;
                    if(userModel.getType().equals("Patient")){
                        viewPagerAdapter.AddFragment(new MyDoctorsChat(), "Doctors");
                        viewPagerAdapter.notifyDataSetChanged();
                    }else if (userModel.getType().equals("Doctor")) {
                        viewPagerAdapter.AddFragment(new MyPatientsChat(), "Patients");
                        viewPagerAdapter.notifyDataSetChanged();


                    }
                }

            });


            viewPager.setAdapter(viewPagerAdapter);

            tabLayout.setupWithViewPager(viewPager);

             }
         });
        }




class ViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;

    ViewPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();

    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    void AddFragment(Fragment fragment, String title){

        fragments.add(fragment);
        titles.add(title);
    }




    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}

    private void  status (String status) {
        document = FirebaseFirestore.getInstance().collection("User").document(firebaseUser.getUid());
        document.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

            }
        });
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        document.update(hashMap);

    }


protected void onResume() {
    super.onResume();
    status("online");
}
protected void onPause(){
    super.onPause();
    status("offline");
}


}

