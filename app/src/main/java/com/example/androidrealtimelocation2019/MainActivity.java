package com.example.androidrealtimelocation2019;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.androidrealtimelocation2019.Model.Token;
import com.example.androidrealtimelocation2019.Model.User;
import com.example.androidrealtimelocation2019.Utils.Common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    DatabaseReference user_information, detail_info_user;
    private static final int MY_REQUEST_CODE = 7117; // any number you want
    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Paper.init(this);

        //Init provider
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        //Request permission location
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        showSignInOptions();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept permission to use app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
        .build(),MY_REQUEST_CODE);
    }

    //Ctrl+o


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(requestCode == 7117)
            {
                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                //Init firebase
                user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
                //Check if user exists Database
                user_information.orderByKey()
                        .equalTo(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() == null) {
                                    if (!dataSnapshot.child(firebaseUser.getUid()).exists()) {

                                        String userId = firebaseUser.getUid();
                                        String email = firebaseUser.getEmail();
                                        String[] username = email.split("@");
                                        //Add to database
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("uid",userId);
                                        hashMap.put("username",username[0]);
                                        hashMap.put("imageURL","default");
                                        hashMap.put("status", "offline");
                                        hashMap.put("search",username[0].toLowerCase());
                                        hashMap.put("email",email);

                                        user_information.child(userId).setValue(hashMap);
                                        Common.loggedUser = new User(firebaseUser.getUid(),username[0],"default","offline",username[0].toLowerCase(),firebaseUser.getEmail());
                                    }
                                } else {
                                    Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                                }

                                //save UID to storage to update location from bacgrtound
                                String a = Common.loggedUser.getUid();
                                Paper.book().write(Common.USER_UID_SAVE_KEY,Common.loggedUser.getUid());
                                String refreshToken = FirebaseInstanceId.getInstance().getToken();
                                if (firebaseUser != null) {
                                    updateToken(refreshToken);
                                }
                                updateToken(refreshToken);
                                setupUI();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }

    }

    private void setupUI() {
        //Navigate Home
        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();
    }


    private void updateToken(String refreshToken) {


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token ttoken = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(ttoken);

    }

//    private void updateToken(final FirebaseUser firebaseUser) {
//        final DatabaseReference tokens = FirebaseDatabase.getInstance()
//                .getReference(Common.TOKENS);
//
//        //Get Token
//        FirebaseInstanceId.getInstance() .getInstanceId()
//                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
//                    @Override
//                    public void onSuccess(InstanceIdResult instanceIdResult) {
//                        tokens.child(firebaseUser.getUid())
//                                .setValue(instanceIdResult.getToken());
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MainActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
}