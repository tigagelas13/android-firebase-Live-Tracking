package com.example.androidrealtimelocation2019.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.example.androidrealtimelocation2019.Utils.Common;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

public class MyLocationReceiver extends BroadcastReceiver {
   public static final String ACTION = "com.example.androidrealtimelocation2019.UPDATE_LOCATION";

   DatabaseReference publicLocation;
   String uid;

    public MyLocationReceiver() {
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Paper.init(context);

        uid = Paper.book().read(Common.USER_UID_SAVE_KEY);
        if (intent != null)
        {
            final String action = intent.getAction();
            if (action.equals(ACTION))
            {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null)
                {
                    Location location = result.getLastLocation();
                    if (Common.loggedUser != null) // App in foreground
                    {
                        publicLocation.child(Common.loggedUser.getUid()).setValue(location);
                    }
                    else // App be killd
                    {
                        publicLocation.child(uid).setValue(location);
                    }
                }
            }
        }

    }
}
