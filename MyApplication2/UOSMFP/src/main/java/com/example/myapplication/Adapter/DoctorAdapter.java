package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activities.ChatActivity2;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private Context mContext;
    private List<UserModel> mUsers;
    private CollectionReference collection;
    private DocumentReference document;


    public DoctorAdapter(Context mContext, List<UserModel> mUsers) {
        this.mUsers = mUsers;
        this.mContext = mContext;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_doctor_approval, parent, false);
        return new DoctorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserModel user = mUsers.get(position);
        holder.name.setText(user.getName());
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        holder.approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("approved", "approved");
                FirebaseFirestore.getInstance().collection("User").document(user.getId()).update(hashMap);
                Toast.makeText(mContext, "Doctor Approved", Toast.LENGTH_SHORT).show();


            }
        });

        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("approved", "declined");

                FirebaseFirestore.getInstance().collection("User").document(user.getId()).update(hashMap);
                Toast.makeText(mContext, "Doctor Denied", Toast.LENGTH_SHORT).show();

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity2.class);
                Bundle bundle = new Bundle();
                bundle.putString("USER_ID", (user.getId()));
                bundle.putString("USER_NAME", (user.getName()));
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);


            }

        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Button approve;
        private Button decline;
        public TextView username;
        public TextView name;
        public ImageView profile_image;


        ViewHolder(View itemView) {
            super(itemView);
            approve = itemView.findViewById(R.id.doctor_approve);
            decline = itemView.findViewById(R.id.doctor_decline);
            name = itemView.findViewById(R.id.doctor_name);
            username = itemView.findViewById(R.id.doctor_email);
            profile_image = itemView.findViewById(R.id.doctor_prof);

        }

    }

}



