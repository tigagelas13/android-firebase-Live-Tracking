package com.example.androidrealtimelocation2019.Model;

import com.example.androidrealtimelocation2019.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAV1zGp0Q:APA91bGU9Cl5Cy9yo4NTOxja1jXQdU-hwvuTG1-d80u1YHpmK8vHnWZd6B61Xu3fyVe5STqqfTw2Jd9QC2q1OuekLYy5i7ls03Im96uerFU5gQPeUH90fxkpfVSmPOdJ0n-ievLJATyk"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
