package com.example.myapplication.Notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseIdService extends FirebaseMessagingService {
    public String TAG = "Something";


    @Override
    public void onNewToken(String s) {
       super.onNewToken(s);
       final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

       FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener( new OnCompleteListener<InstanceIdResult>() {
           @Override
           public void onComplete(@NonNull Task<InstanceIdResult> task) {
               if (!task.isSuccessful()) {
                   Log.w(TAG, "getInstanceId failed", task.getException());
                   return;
               }

               String refreshToken = (task.getResult()).getToken();
               //String refreshToken = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();
               if (firebaseUser != null) {
                   updateToken(refreshToken);
                   Log.e("newToken", refreshToken);
               }
           }
       });
           }

           private void updateToken(String refreshToken) {
               FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
               {
                   CollectionReference reference = FirebaseFirestore.getInstance().collection("Tokens");
                   Token token1 = new Token(refreshToken);
                   assert firebaseUser != null;
                   reference.document(firebaseUser.getUid()).set(token1);


               }


           }

       }


