package com.example.vinoth.googlemap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.vinoth.googlemap.model.LocationDatas;
import com.example.vinoth.googlemap.model.Vehicle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    SharedPreferences sharedpreference;
    HashMap<String, Marker> markerDatas;
    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private DatabaseReference locationDBRef;
    private TextView tvTime;
    private DatabaseReference vicleDBRef;
    private ArrayList<String> list;
    private String[] vehicleNos;
    private ArrayList<Vehicle> vehicleList;
    private Marker myMarker;
    private HashMap<String, Polyline> polylineHashMap;
    private Polyline myPolyline;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ValueEventListener vicleDBRefValEvl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Intent intent = new Intent(getBaseContext(), SigninActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        vicleDBRef = databaseReference.child("vehicle");
        locationDBRef = databaseReference.child("Location");
        sharedpreference = getSharedPreferences("mydata", Context.MODE_PRIVATE);
        tvTime = (TextView) findViewById(R.id.tvResult);
        markerDatas = new HashMap<String, Marker>();
        polylineHashMap = new HashMap<String, Polyline>();
        vehicleList = new ArrayList<Vehicle>();
        list=new ArrayList<String>();

         vicleDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot);

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String s1 = ds.getValue(String.class);
                    list.add(s1);
                }
               // String [] gg= (String[]) list.toArray();
                vehicleNos = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    vehicleNos[i] = list.get(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d(TAG, "onCancelled:"+databaseError);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.isMyLocationEnabled();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setBuildingsEnabled(true);
        markerAdding();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        mAuth.addAuthStateListener(mAuthListener);



    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.select:
                showDialogBox();
                break;
            case R.id.logout:
                mAuth.signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select vehicle ");
        builder.setMultiChoiceItems(vehicleNos, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Vehicle vehicleSelect = new Vehicle(vehicleNos[which], isChecked);
                vehicleList.add(vehicleSelect);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor prefsEditor = sharedpreference.edit();
                String json = new Gson().toJson(vehicleList);
                prefsEditor.putString("vehicleList", json);
                prefsEditor.commit();
            }
        });
        builder.show();
    }

    protected void markerAdding() {

        try {
            Gson gson = new Gson();
            String json = sharedpreference.getString("vehicleList", "E1");
            final ArrayList<Vehicle> locationdata1 = gson.fromJson(json, new TypeToken<List<Vehicle>>() {
            }.getType());
            Log.d("vcvvvvv", "onCreate: " + locationdata1.size());
            for (Vehicle v : locationdata1) {
                Log.d("vcvvvvv", "onCreate: " + v.getName());
                locationDBRef.child(v.getName()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot ds) {

                        List<LatLng> latLngslist = new ArrayList<LatLng>();
                        for (DataSnapshot dataSt : ds.getChildren()) {
                            LocationDatas location_data = dataSt.getValue(LocationDatas.class);
                            latLngslist.add(new LatLng(location_data.getLatitude(), location_data.getLongitude()));
                        }
                        DataSnapshot dataSnapshot = ds.child("current");
                        LocationDatas currentlocationdatas = dataSnapshot.getValue(LocationDatas.class);
                        Log.d("hggduyfdufdd", "currentlocationdatas: " + currentlocationdatas.getTimeStamp() + currentlocationdatas.getLatitude() + currentlocationdatas.getLongitude());
                        double latitude = currentlocationdatas.getLatitude();
                        double longitude = currentlocationdatas.getLongitude();
                        String TimeData = currentlocationdatas.getTimeStamp();
                        if ((latitude != 0) && (longitude != 0)) {
                            LatLng vihecalLatlng = new LatLng(latitude, longitude);
                            String nameVihecal = dataSnapshot.getRef().getParent().getKey();
                            removeOldMarker(nameVihecal);
                            removeoldPolyline(nameVihecal);
                            MarkerOptions markerOptions = new MarkerOptions().position(vihecalLatlng).title(nameVihecal + " Bus at ").snippet(TimeData).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
                            myMarker = mMap.addMarker(markerOptions);
                            myPolyline = mMap.addPolyline(new PolylineOptions().addAll(latLngslist).width(5)
                                    .color(Color.RED));

                            markerDatas.put(nameVihecal, myMarker);
                            polylineHashMap.put(nameVihecal, myPolyline);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeoldPolyline(String nameVihecal) {
        Polyline polyLine = polylineHashMap.get(nameVihecal);

        if (polyLine != null) {
            polyLine.remove();
        }

    }

    private void removeOldMarker(String name) {
        Marker marker = markerDatas.get(name);
        if (marker != null) {
            marker.remove();
        }
    }


}







