package com.example.myapplication.Adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Model.RequestsModel;
import com.example.myapplication.Model.UserModel;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyDoctorsAdapter extends RecyclerView.Adapter<MyDoctorsAdapter.ViewHolder> {
    private Context mContext;
    private List<UserModel> mUsers;
    private CollectionReference collection;
    private DocumentReference document;
    List<RequestsModel> RequestList;
    private FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private Query query;
    String TAG = "mydoctors frag: ";

    public MyDoctorsAdapter(Context mContext, List<UserModel> mUsers) {
        this.mUsers = mUsers;
        this.mContext = mContext;


    }


    @NonNull
    @Override
    public MyDoctorsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_mydoctors, parent, false);
        return new MyDoctorsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyDoctorsAdapter.ViewHolder holder, int position) {
        final UserModel user = mUsers.get(position);
        holder.name.setText(user.getName());
        holder.username.setText(user.getUsername());

        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View popupView = LayoutInflater.from(mContext).inflate(R.layout.popupprofile, null);
                final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                TextView name = popupView.findViewById(R.id.popupname);
                TextView surname = popupView.findViewById(R.id.popupsurname);
                TextView type = popupView.findViewById(R.id.popuptype);
                TextView email = popupView.findViewById(R.id.popupemail);
                CircleImageView profpic = popupView.findViewById(R.id.popup_profile_image);
                name.setText(user.getName());
                surname.setText(user.getSurname());
                type.setText(user.getType());
                email.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profpic.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(mContext).load(user.getImageURL()).into(profpic);
                }
                popupWindow.setBackgroundDrawable(new ColorDrawable(
                        android.graphics.Color.TRANSPARENT));
                popupWindow.setFocusable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable());
                int[] location = new int[2];

                // Get the View's(the one that was clicked in the Fragment) location
                view.getLocationOnScreen(location);
                popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,
                        location[0], location[1] + view.getHeight());





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


        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.mydoctors_name);
            username = itemView.findViewById(R.id.mydoctors_email);
            profile_image = itemView.findViewById(R.id.mydoctors_prof);

        }

    }

}



