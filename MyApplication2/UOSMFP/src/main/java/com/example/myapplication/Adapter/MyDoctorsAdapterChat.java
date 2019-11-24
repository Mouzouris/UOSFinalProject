package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activities.MessageActivity;
import com.example.myapplication.Model.ChatModel;
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

import java.util.List;

import javax.annotation.Nullable;

public class MyDoctorsAdapterChat extends RecyclerView.Adapter<MyDoctorsAdapterChat.ViewHolder> {
    private Context mContext;
    private List<UserModel> mUsers;
    private boolean ischat;
    private String TheLastMessage;
    private CollectionReference collection;
    private DocumentReference document;
    List<RequestsModel> RequestList;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private Query query;
    String TAG = "mydoctors frag: ";

    public MyDoctorsAdapterChat(Context mContext, List<UserModel> mUsers, boolean ischat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;



    }


    @NonNull
    @Override
    public MyDoctorsAdapterChat.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_mydoctorschat, parent, false);
        return new MyDoctorsAdapterChat.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyDoctorsAdapterChat.ViewHolder holder, int position) {
        final UserModel user = mUsers.get(position);
        holder.name.setText(user.getName());
        holder.username.setText(user.getUsername());

        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }
        if (ischat) {
            lastMessage(user.getId(), holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (ischat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else if (user.getStatus().equals("offline")) {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }

        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }

        });
    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView name;
        public ImageView profile_image;
        private ImageView img_off;
        private ImageView img_on;
        private TextView last_msg;



        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mydoctorschat_name);
            username = itemView.findViewById(R.id.mydoctorschat_email);
            profile_image = itemView.findViewById(R.id.mydoctorschat_prof);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);


        }

    }
    private void lastMessage(final String userid, final TextView last_msg) {
        TheLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        query = FirebaseFirestore.getInstance().collection("Chats").orderBy("timestamp", Query.Direction.ASCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    ChatModel chatModel = document.toObject(ChatModel.class);
                    assert firebaseUser != null;
                    assert chatModel != null;
                    if (chatModel.getReceiver().equals(firebaseUser.getUid()) && chatModel.getSender().equals(userid)||
                            chatModel.getReceiver().equals(userid) && chatModel.getSender().equals(firebaseUser.getUid())){
                        TheLastMessage = chatModel.getMessage();
                    }
                }

                if ("default".equals(TheLastMessage)) {
                    last_msg.setText("No Message");
                } else {
                    last_msg.setText(TheLastMessage);
                }
                TheLastMessage = "default";

            }
        });
    }


}



