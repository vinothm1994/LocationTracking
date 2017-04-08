package com.example.vinoth.googlemap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vinoth.googlemap.model.LocationDatas;
import com.example.vinoth.googlemap.model.Vehicle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10101;
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
        setTitle("Home");
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
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_style));
        LatLng india=new LatLng(20.5937, 78.9629);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(india)      // Sets the center of the map to Mountain View
                .zoom(5)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                String text = "Please allow the application to access the LOCATION permission";

                    Snackbar snackbar = Snackbar
                            .make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                            .setDuration(Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_LOCATION);
                                }
                            });
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(getResources().getColor(R.color.colorAccent));
                    snackbar.show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
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
                prefsEditor.apply();
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
        builder.show();
    }

    protected void markerAdding() {
        try {
            Gson gson = new Gson();
            String json = sharedpreference.getString("vehicleList", "[{\"enable\":true,\"name\":\"Start\"}]");
            Log.i(TAG, "markerAdding: "+json);
             ArrayList<Vehicle> locationdata1 = gson.fromJson(json, new TypeToken<List<Vehicle>>() {
            }.getType());
            if (locationdata1==null||locationdata1.size()==0){
                locationdata1=new ArrayList<>();
                locationdata1.add(new Vehicle("Start",true));
            }
            Log.d("vcvvvvv", "onCreate: " + locationdata1.size());
            for (Vehicle v : locationdata1) {
                Log.d("vcvvvvv", "onCreate: " + v.getName());
                locationDBRef.child(v.getName()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot ds) {

                        try {
                            List<LatLng> latLngslist = new ArrayList<LatLng>();
                            for (DataSnapshot dataSt : ds.getChildren()) {
                                LocationDatas location_data = dataSt.getValue(LocationDatas.class);
                                latLngslist.add(new LatLng(location_data.getLatitude(), location_data.getLongitude()));
                            }
                            DataSnapshot dataSnapshot = ds.child("current");
                            LocationDatas currentlocationdatas = dataSnapshot.getValue(LocationDatas.class);
                            Log.d(TAG, "currentlocationdatas: " + currentlocationdatas.getTimeStamp() + currentlocationdatas.getLatitude() + currentlocationdatas.getLongitude());
                            double latitude = currentlocationdatas.getLatitude();
                            double longitude = currentlocationdatas.getLongitude();
                            String TimeData = currentlocationdatas.getTimeStamp();
                            if ((latitude != 0) && (longitude != 0)) {
                                LatLng vihecalLatlng = new LatLng(latitude, longitude);
                                String nameVihecal = dataSnapshot.getRef().getParent().getKey();
                                removeOldMarker(nameVihecal);
                                removeoldPolyline(nameVihecal);
                              //  MarkerOptions markerOptions = new MarkerOptions().position(vihecalLatlng).title(nameVihecal + " Bus at ").snippet(TimeData).icon(BitmapDescriptorFactory.fromBitmap(iconGen(nameVihecal,TimeData)));

                                MarkerOptions markerOptions=   new MarkerOptions()
                                        .position(vihecalLatlng)
                                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MapsActivity.this,markerView(nameVihecal,TimeData) ))).anchor(0.5f,1f);
                                myMarker = mMap.addMarker(markerOptions);
                                myPolyline = mMap.addPolyline(new PolylineOptions().addAll(latLngslist).width(5)
                                        .color(Color.RED));
                                markerDatas.put(nameVihecal, myMarker);
                                polylineHashMap.put(nameVihecal, myPolyline);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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


    private  View markerView(String name, String timeData){
        View customMarkerViewTitle = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_icon, null, false);
        TextView placeNameTextTitle = (TextView) customMarkerViewTitle.findViewById(R.id.pin_place_name);
        TextView eventNameTextTitle = (TextView) customMarkerViewTitle.findViewById(R.id.pin_event_name);
        RelativeLayout pinFullLayoutTitle = (RelativeLayout) customMarkerViewTitle.findViewById(R.id.map_pin_layout);
        RelativeLayout pinLayoutTitle = (RelativeLayout) customMarkerViewTitle.findViewById(R.id.pin_layout);
        placeNameTextTitle.setText(name);
        eventNameTextTitle.setText(timeData);
        try{
            int fullwidth = pinFullLayoutTitle.getMeasuredWidth();
            pinLayoutTitle.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int pinWidth = pinLayoutTitle.getMeasuredWidth();
            float anchor = ((float) pinWidth / 2) / (float) fullwidth;
        }
        catch (Exception e){
            Log.e(TAG, "error  ", e);
        }


        return customMarkerViewTitle;
    }

    public  Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==MY_PERMISSIONS_REQUEST_LOCATION){
            if (resultCode==RESULT_OK){
                Intent intent = new Intent(getBaseContext(), SigninActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            else
            {
                String text = "Please allow the application to access the LOCATION permission";
                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                        .setDuration(Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        });
                snackbar.show();
            }


        }
    }
}







