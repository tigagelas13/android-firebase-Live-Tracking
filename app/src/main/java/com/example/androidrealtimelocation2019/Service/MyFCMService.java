package com.example.androidrealtimelocation2019.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.androidrealtimelocation2019.Model.User;
import com.example.androidrealtimelocation2019.R;
import com.example.androidrealtimelocation2019.Utils.Common;
import com.example.androidrealtimelocation2019.Utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationWithChannel(remoteMessage);
            else
                sendNotification(remoteMessage);
            
            addRequestToUserInformation(remoteMessage.getData());
        }
    }

    private void addRequestToUserInformation(Map<String, String> data) {
        //Pending request
        DatabaseReference friend_request = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(data.get(Common.TO_UID))
                .child(Common.FRIEND_REQUEST);

        User user = new User();
        user.setUid(data.get(Common.FROM_UID));
        user.setEmail(data.get(Common.FROM_NAME));

        friend_request.child(user.getUid()).setValue(user);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String title = "Firebase Request";
        String content = "New friend request from "+data.get(Common.FROM_NAME);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Notification.Builder builder = new Notification.Builder(this)
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(),builder.build());
    }
@RequiresApi (api = Build.VERSION_CODES.O)
    private void sendNotificationWithChannel(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        String title = "Firebase Request";
        String content = "New friend request from "+data.get(Common.FROM_NAME);

        NotificationHelper helper;
        Notification.Builder builder; //null;

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        helper = new NotificationHelper(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder = helper.getRealtimeTrackingNotification(title,content,defaultSound);
//        }
        builder = helper.getRealtimeTrackingNotification(title,content,defaultSound);
        helper.getManager().notify(new Random().nextInt(), builder.build());


    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            final DatabaseReference tokens = FirebaseDatabase.getInstance()
                    .getReference("Tokens");
            tokens.child(user.getUid()).setValue(s);
        }
    }
}
