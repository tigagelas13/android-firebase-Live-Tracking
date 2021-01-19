package com.example.androidrealtimelocation2019;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidrealtimelocation2019.Interface.IFirebaseLoadDone;
import com.example.androidrealtimelocation2019.Interface.IRecyclerItemClickListener;
import com.example.androidrealtimelocation2019.Model.User;
import com.example.androidrealtimelocation2019.Service.MyLocationReceiver;
import com.example.androidrealtimelocation2019.Utils.Common;
import com.example.androidrealtimelocation2019.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements IFirebaseLoadDone, LocationListener {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_friend_list;
    IFirebaseLoadDone firebaseLoadDone;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    List<String> suggestList = new ArrayList<>();

    FloatingActionMenu materialDesignFAM;
    FloatingActionButton find_people, friend_request, signout, userMenu, userChat;

    //DatabaseReference publicLocation;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    SwitchCompat sw_metric;
    TextView tv_speed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        find_people = (FloatingActionButton) findViewById(R.id.id_find_people);
        friend_request = (FloatingActionButton) findViewById(R.id.id_friend_request);
        signout = (FloatingActionButton) findViewById(R.id.id_signout);
        userMenu = (FloatingActionButton) findViewById(R.id.id_user);
        userChat = (FloatingActionButton) findViewById(R.id.id_chat);


        sw_metric = findViewById(R.id.sw_metric);
        tv_speed = findViewById(R.id.tv_speed);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("UserInformation").child(userId);

        status("online");
        //chack gps permision
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
        }else {
            //start program if the permision
            doStuff();
        }
        this.updateSpeed(null);

        sw_metric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HomeActivity.this.updateSpeed(null);
            }
        });

        recycler_friend_list = (RecyclerView) findViewById(R.id.mt_recycler_friend_list);
        recycler_friend_list.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_friend_list.setLayoutManager(layoutManager);
        recycler_friend_list.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        //Update Location
       // publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        upadateLocation();

        firebaseLoadDone = this;
        loadFriendList();
        loadSearchData();

        userChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ChatActivity.class));
            }
        });
        userMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, UsersActivity.class));
            }
        });

        find_people.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AIIPeopleActivity.class));
            }
        });
        friend_request.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, FriendRequestActivity.class));
            }
        });
        signout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                status("offline");
                finish();
                System.exit(0);
            }
        });
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("UserInformation").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    private void loadSearchData() {
        final List<String> lstUserEmail = new ArrayList<>();
        DatabaseReference userList = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot:dataSnapshot.getChildren()){
                    User user = userSnapShot.getValue(User.class);
                    lstUserEmail.add(user.getEmail());
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());

            }
        });

    }

    private void loadFriendList() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            //kemungkinan di ganti
        //For {
                    @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        //show tracking
                    Common.trackingUser = model;
                       startActivity(new Intent(HomeActivity.this,TrackingActivity.class));



                    }
                });

            }


            @NonNull
            // hapuss
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user,viewGroup,false);
                return new UserViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_friend_list.setAdapter(adapter);

    }
//    private void AllTrackingMap() {
//        Query query = FirebaseDatabase.getInstance()
//                .getReference(Common.USER_INFORMATION)
//                .child(Common.loggedUser.getUid())
//                .child(Common.ACCEPT_LIST);
//
//        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
//                .setQuery(query,User.class)
//                .build();
//
//        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
//            //kemungkinan di ganti
//            //For {
//            @Override
//            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
//                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
//                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
//                    @Override
//                    public void onItemClickListener(View view, int position) {
//                        //show tracking
//                        Common.trackingUser = model;
//                        startActivity(new Intent(HomeActivity.this,TrackingActivity.class));
//
//
//
//                    }
//                });
//
//            }
//
//
//            //aaaaaaaaaaaaaassssssssss
//
//            @NonNull
//            // hapuss
//            @Override
//            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//                View itemView = LayoutInflater.from(viewGroup.getContext())
//                        .inflate(R.layout.layout_user,viewGroup,false);
//                return new UserViewHolder(itemView);
//            }
//        };
//        adapter.startListening();
//        recycler_friend_list.setAdapter(adapter);
//
//    }

    @Override
    protected void onStop() {
        if (adapter != null)
            adapter.stopListening();
        if (searchAdapter != null)
            searchAdapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.startListening();
        if (searchAdapter != null)
            searchAdapter.startListening();
    }

    private void upadateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
 
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(HomeActivity.this,MyLocationReceiver.class);
        intent.setAction(MyLocationReceiver.ACTION);
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setFastestInterval(3000);
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startSearch(String search_value) {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST)
                .orderByChild("name")
                .startAt(search_value);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        Common.trackingUser = model;
                        startActivity(new Intent(HomeActivity.this,TrackingActivity.class));

                    }
                });

            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user,viewGroup,false);
                return new UserViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_friend_list.setAdapter(adapter);
    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) {

    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null){
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @SuppressLint("MissingPermission")
    private void doStuff(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0 ,0, this);
        }
        Toast.makeText(this, "Waiting for GPS connection", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation location){
        float nCurrentSpeed = 0;

        if (location != null){
            location.setUseMericUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }
        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String setCurrenSpeed = fmt.toString();
        setCurrenSpeed = setCurrenSpeed.replace(" ", "0");

        if (this.useMetricUnits()){
            tv_speed.setText(setCurrenSpeed + "km/h");
        }else {
            tv_speed.setText(setCurrenSpeed + "miles/h");
        }

    }
    private boolean useMetricUnits(){
        return sw_metric.isChecked();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                doStuff();
            }else {
                finish();
            }
        }
    }
}