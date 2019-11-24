package com.example.myapplication.Fragments;

import com.example.myapplication.Notifications.MyResponse;
import com.example.myapplication.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAJosWrcc:APA91bEiVaBj6_1ZI-BefXuodT060XxNW2CMqckKN2Y3wLrJnk8gGwndoPytVrZW6i_QfokzBAfPXqzRxSTMhReAniu_bG_Q75E05YUZw28VwmooFOoBT8939G_FkkQ4S4FWF0ZoHWwz"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
