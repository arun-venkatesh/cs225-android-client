package com.arun.cs225.sixfeettracing.firebase;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.arun.cs225.sixfeettracing.R;
import com.arun.cs225.sixfeettracing.TrackerActivity;
import com.arun.cs225.sixfeettracing.api.VolleyImpl;
import com.arun.cs225.sixfeettracing.database.DBStore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class Handler extends FirebaseMessagingService {
    private static final String TAG = "Handler";
    private Context context;

    public Handler() {

    }
    public Handler(Context context) {
        this.context = context;
    }

    public void currentRegistrationToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        updateServer(token);

                        Toast.makeText(context,
                                "Current Token = " +token, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        Toast.makeText(Handler.this,
                "Refreshed Token = " +token, Toast.LENGTH_SHORT).show();

        updateServer(token);

    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Data = " + remoteMessage.getData());

        TrackerActivity.notifyUsers(remoteMessage.getData());
    }


    private void updateServer(String token) {
        try
        {
            VolleyImpl volley = new VolleyImpl(context, Request.Method.PUT,
                    TrackerActivity.BASE_URL + TrackerActivity.UPDATE_USER_API_URL);
            JSONObject obj = new JSONObject();
            obj.put("user_name",TrackerActivity.getDeviceName());
            JSONObject data = new JSONObject();
            data.put("registration_token", token);
            data.put("status", "-1");
            obj.put("data",data);
            volley.postJSONObject(obj.toString());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
