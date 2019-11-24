package com.example.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activities.ChatActivity2;
import com.example.myapplication.Model.RequestIDsModel;
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

public class MyRequestsAdapter extends RecyclerView.Adapter<MyRequestsAdapter.ViewHolder> {
    private Context mContext;
    private List ids;
    private List<UserModel> mUsers;
    private CollectionReference collection;
    private DocumentReference document;
    List<RequestsModel> RequestList;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private Query query;
    String TAG = "approve_frag";


    public MyRequestsAdapter(Context mContext, List<UserModel> mUsers, List docids) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        //Collections.reverse(status);
        this.ids = docids;
        //Log.d(TAG, "this is what it gets on the adapter " + status);


    }
//private void getstatus(){
//for (int y = 0; y < ids.size();y++) {
//        assert  document != null;
//        document = FirebaseFirestore.getInstance().collection("User").document(ids.get(y).toString()).collection();
//    //Log.d(TAG, "this is what it passes for display " + userModel.getId());
//
//
//    document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                final UserModel userModel = task.getResult().toObject(UserModel.class);
//                assert userModel != null;
//                if (mUsers.contains(userModel)) {
//                    return;
//                } else {
//                    mUsers.add(userModel);
//                    Log.d(TAG, "this is what it passes for display " + userModel.getId());
//
//                }
//            }
//        });
//}
//}


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_myrequests, parent, false);
        return new MyRequestsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserModel user = mUsers.get(position);
        holder.name.setText(user.getName());
        holder.username.setText(user.getUsername());

        query = FirebaseFirestore.getInstance().collection("User").document(user.getId()).collection("ReceivedRequestIDs").whereEqualTo("receiver", user.getId()).whereEqualTo("sender",fuser.getUid());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                      @Override
                                      public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                          for (DocumentSnapshot queryDocumentSnapshots1 : queryDocumentSnapshots.getDocuments()) {
                                              final RequestIDsModel requestIDsModel = queryDocumentSnapshots1.toObject(RequestIDsModel.class);
                                              if (requestIDsModel.getSender().equals(fuser.getUid())&& requestIDsModel.getReceiver().equals(user.getId())){
                                                  holder.status.setText(requestIDsModel.getStatus());
                                              }
                                          }
                                      }
                                  });
        //holder.status.setText(statuses.get(position).toString());
        Log.d(TAG, "this is what the postion is on the adapter " + position+"with this user "+ user.getName());



        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }





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
        public TextView status;
        public TextView username;
        public TextView name;
        public ImageView profile_image;


        ViewHolder(View itemView) {
            super(itemView);
           status = itemView.findViewById(R.id.myrequests_status);
            name = itemView.findViewById(R.id.myrequests_name);
            username = itemView.findViewById(R.id.myrequests_email);
            profile_image = itemView.findViewById(R.id.myrequests_prof);

        }

    }

}



