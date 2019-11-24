package com.example.myapplication.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.Activities.Chat;
import com.example.myapplication.Activities.MainActivity;
import com.example.myapplication.Activities.MessageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingFirebase extends FirebaseMessagingService {

    private static final  String CHANNEL_ID = "com.example.myapplication";
    private String TAG = "onmessagereceived: ";


    @Override
    public void  onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentuser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (sented != null){
        if ( firebaseUser != null && sented.equals(firebaseUser.getUid())){
            if(!currentUser.equals(user)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendSDK26Notifications(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }

                }

            }
        }else {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                Log.d("FCM", remoteMessage.getData().toString());
                if (remoteMessage.getData().size() > 0) {
                    Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                }
            }
        }






    private void sendSDK26Notifications(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int k = Integer.parseInt(user.replaceAll( "[\\D]",""));
        Intent[] intents = new Intent[3];
        intents[0] = Intent.makeMainActivity(new ComponentName(this, MainActivity.class));
        intents[1] = Intent.makeMainActivity(new ComponentName(this, Chat.class));
        intents[2] = Intent.makeMainActivity(new ComponentName(this, MessageActivity.class));
        //Intent intent= new Intent (this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intents[2].putExtras(bundle);
        intents[2].addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, k, intents, PendingIntent.FLAG_ONE_SHOT);
//        Intent intent = new Intent(this, MessageActivity.class);
//
//        Bundle bundle = new Bundle();
//        bundle.putString("userid", user);
//        intent.putExtras(bundle);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, k, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        SDK26Notifications sdk26Notifications = new SDK26Notifications(this);
        Notification.Builder builder = sdk26Notifications.getSDK26Notifications(title,body, pendingIntent,defaultSound,icon);
        int i= 0;
        if(k>0){
            i=k;
        }
        sdk26Notifications.getManager().notify(i,builder.build());


    }

    private void sendNotification(RemoteMessage remoteMessage){
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int k = Integer.parseInt(user.replaceAll( "[\\D]",""));
        Intent[] intents = new Intent[3];
        intents[0] = Intent.makeMainActivity(new ComponentName(this, MainActivity.class));
        intents[1] = Intent.makeMainActivity(new ComponentName(this, Chat.class));
        intents[2] = Intent.makeMainActivity(new ComponentName(this, MessageActivity.class));
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intents[2].putExtras(bundle);
        intents[2].addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, k, intents, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int i= 0;
        if(k>0){
            i=k;
        }
        notificationManager.notify(i,builder.build());


               }
}
