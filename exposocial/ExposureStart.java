package com.example.soumilchugh.exposocial;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.triggertrap.seekarc.SeekArc;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ExposureStart extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private PopupWindow pw;
    private int SUDS;
    private Button btn_start;
    private TextView txt_exposure_title;
    private TextView txt_suds;
    private SeekArc seekArc;
    private BottomNavigationView bottomNavigationView;
    String fearName;
    public static String TAG = "ExposureStart__check_loc";
    double targetLatitude;
    double targetLongtiude;
    Location mCurrentlocation; // location
    double latitude; // latitude
    double longitude; // longitude
    protected LocationManager locationManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private static final long INTERVAL = 1000 * 10;               // Interval of 10 seconds
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LatLng fromPosition;
    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    double distanceInMeters = 1000;
    String placeName;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exposure_start);
        this.setTitle("SUDS Estimation");
        if (!isGooglePlayServicesAvailable()) {
            Log.d(TAG, "isGooglePlayServicesAvailable(): " + isGooglePlayServicesAvailable());
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(ExposureStart.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(ExposureStart.this)
                .addOnConnectionFailedListener(ExposureStart.this)
                .build();

        // Get fear name
        Bundle extras = getIntent().getExtras();
        fearName = extras.getString("FEARNAME");
        placeName = extras.getString("placeName");
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        fromPosition = bundle.getParcelable("from_position");
        targetLatitude = fromPosition.latitude;
        targetLongtiude = fromPosition.longitude;
        // Initialize vars
        txt_exposure_title = findViewById(R.id.txt_title);
        //tvDistanceDuration = findViewById(R.id.tv_distance_time);
        txt_exposure_title.setText(fearName);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.getMenu().getItem(1).setCheckable(false);
        bottomNavigationView.getMenu().getItem(2).setCheckable(false);
        txt_suds = (TextView) findViewById(R.id.txt_SUDS);
        seekArc = (com.triggertrap.seekarc.SeekArc) findViewById(R.id.seekArc);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setEnabled(false);
        txt_exposure_title.setText(fearName);
        txt_suds.setText(seekArc.getProgress() + "");

        // Show message
        new AlertDialog.Builder(ExposureStart.this, AlertDialog.THEME_HOLO_LIGHT)
                .setIcon(getResources().getDrawable(R.drawable.baseline_trending_up_24px))
                .setTitle("SUDS Estimation")
                .setMessage(getString(R.string.suds_predict))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Check current location and target location
                        checkIsAtLocation();
                    }
                })
                .show();

        // Listen SUDS bar change
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                SUDS = ((seekArc.getProgress() + 5) / 10) * 10;
                txt_suds.setText(SUDS + "");
                btn_start.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }
        });

        // Listen to click start button
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date now = Calendar.getInstance().getTime();
                String today = dateFormat.format(now);
                // Initial shared preference to store scale arryas
                SharedPreferences sharedpreferences = getSharedPreferences(today, 0);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                //editor.putString("Location", placeName);
                editor.putInt("0", SUDS);
                editor.commit();

                updateToExpoTextFile(ExposureStart.this);

                Intent i = new Intent(getApplicationContext(), ExposureTracking.class);
                i.putExtra("FROM", ExposureTracking.class.getSimpleName());
                Bundle args = new Bundle();
                args.putParcelable("from_position", fromPosition);
                i.putExtra("bundle", args);
                i.putExtra("expo_date", today);
                i.putExtra("SUDS", SUDS);
                i.putExtra("placeName", placeName);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });

        // Listen select bottom bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    // When click LSAS
                    case R.id.lsas:
                        // Show a dialog to get confirmation
                        new AlertDialog.Builder(ExposureStart.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Confirm")
                                .setMessage("Are you sure to redo LSAS?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getApplicationContext(), AnxietyLevel.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        break;
                    // When click exposure
                    case R.id.menu_exposure:
                        // Show a dialog to get confirmation
                        new AlertDialog.Builder(ExposureStart.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Confirm")
                                .setMessage("Are you sure back to fear hierarchy?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getApplicationContext(), ExpoLadder.class);
                                        i.putExtra("FROM", ExposureStart.class.getSimpleName());
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        break;
                    // When click profile
                    case R.id.profile:
                        // Show a dialog to get confirmation
                        new AlertDialog.Builder(ExposureStart.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Confirm")
                                .setMessage("Are you sure to check your profile now?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getApplicationContext(), Profile.class);
                                        i.putExtra("FROM", ExposureStart.class.getSimpleName());
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void checkIsAtLocation() {
        Log.d(TAG, "checkIsAtLocation(): " + mGoogleApiClient.isConnected() + "");
        Log.d(TAG, "checkIsAtLocation(): latitude is " + String.valueOf(latitude));
        Log.d(TAG, "checkIsAtLocation(): longitude is " + String.valueOf(longitude));
        Log.d(TAG, "checkIsAtLocation(): target latitude is " + String.valueOf(targetLatitude));
        Log.d(TAG, "checkIsAtLocation(): target longitude is " + String.valueOf(targetLongtiude));

        Location location2 = new Location("");
        location2.setLatitude(targetLatitude);
        location2.setLongitude(targetLongtiude);
        distanceInMeters = location2.distanceTo(mCurrentlocation) / 1000;
        Log.d(TAG, "checkIsAtLocation(): distanceInMeters is " + distanceInMeters);

        if (distanceInMeters > 0.1) {
            Log.d(TAG, "checkIsAtLocation(): distanceInMeters is  > 0.1");
            new AlertDialog.Builder(ExposureStart.this, AlertDialog.THEME_HOLO_LIGHT)
                    .setIcon(getResources().getDrawable(R.drawable.error_face))
                    .setTitle("Oops...Check your location")
                    .setMessage(getString(R.string.error_location))
                    .setPositiveButton("FIND PATH", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initiateLocationPopupWindow(ExposureStart.this);
                        }
                    })
                    .setNegativeButton("START ANYWAY", null)
                    .show();
        }
    }

    private void initiateLocationPopupWindow(Context context) {
        try {
            Log.d(TAG, "initiateLocationPopupWindow() starts....");
            // We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            // Inflate the view from a predefined XML layout
            View layout = inflater.inflate(R.layout.expo_start_location_popup,
                    (ViewGroup) findViewById(R.id.popup_element));

            // create a PopupWindow
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, true);

            // display the popup in the center
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

            View container = (View) pw.getContentView().getParent();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            // add flag
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.6f;
            wm.updateViewLayout(container, p);

            // Get button
            Button btn_imhere = (Button) layout.findViewById(R.id.btn_imhere);
            Button btn_back = (Button) layout.findViewById(R.id.btn_back);
            final TextView routeInfo = (TextView) layout.findViewById(R.id.routeInfo);
            btn_imhere.setOnClickListener(imhere_button_click_listener);
            btn_back.setOnClickListener(back_button_click_listener);

            // Initializing Map
            markerPoints = new ArrayList<LatLng>();
            final LatLng tar_latlng = new LatLng(targetLatitude, targetLongtiude);
            final LatLng curr_latlng = new LatLng(latitude, longitude);
            markerPoints.add(curr_latlng);
            markerPoints.add(tar_latlng);

            // Getting reference to SupportMapFragment of the activity_main
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
            Log.d(TAG, "initiateLocationPopupWindow() fm: " + fm.toString());
            // Getting Map for the SupportMapFragment
            fm.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    if (ActivityCompat.checkSelfPermission(ExposureStart.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(ExposureStart.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    map.setMyLocationEnabled(true);
                    map.clear();
                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();
                    options.position(curr_latlng);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    map.addMarker(options);
                    options.position(tar_latlng);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    map.addMarker(options);
                    Log.d(TAG, "initiateLocationPopupWindow(): map options OK...............");
                    // Set up camera
                    CameraPosition.Builder cameraPosition = new CameraPosition.Builder();
                    cameraPosition.target(curr_latlng);
                    cameraPosition.zoom(15);
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition.build()));
                    Log.d(TAG, "initiateLocationPopupWindow(): animateCamera OK...............");

                    // Set up route
                    GoogleDirection.withServerKey(getString(R.string.map_key))
                            .from(curr_latlng)
                            .to(tar_latlng)
                            .transportMode(TransportMode.WALKING)
                            .execute(new DirectionCallback() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onDirectionSuccess(Direction direction, String rawBody) {
                                    String status = direction.getStatus();
                                    if(status.equals(RequestResult.OK)) {
                                        Route route = direction.getRouteList().get(0);
                                        Leg leg = route.getLegList().get(0);
                                        Info distanceInfo = leg.getDistance();
                                        Info durationInfo = leg.getDuration();
                                        String distance = distanceInfo.getText();
                                        String duration = durationInfo.getText();
                                        String rInfo = "Your destination is "+distance+" away from you." +
                                                "\nIt's about to "+duration+" mins walk from here.";
                                        routeInfo.setText(rInfo);

                                        ArrayList<LatLng> pointList = leg.getDirectionPoint();
                                        PolylineOptions polylineOptions = DirectionConverter.createPolyline(ExposureStart.this, pointList,
                                                5, getColor(R.color.seek_bar_progress));
                                        map.addPolyline(polylineOptions);
                                    } else if(status.equals(RequestResult.NOT_FOUND)) {
                                        Log.d(TAG, "initiateLocationPopupWindow(): Route Not Found");
                                    }
                                }
                                @Override
                                public void onDirectionFailure(Throwable t) {
                                    Log.d(TAG, "initiateLocationPopupWindow(): " + t.getMessage());
                                }
                            });

                }
            });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private View.OnClickListener imhere_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
        }
    };

    private View.OnClickListener back_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getApplicationContext(), SelectLocation.class);
            i.putExtra("FROM", ExposureStart.class.getSimpleName());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onConnected permission denied");
            return;
        }
        mCurrentlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "mCurrentlocation OK...............");
        if (mCurrentlocation != null) {
            latitude = mCurrentlocation.getLatitude();
            longitude = mCurrentlocation.getLongitude();
            Log.d(TAG, "latitude & longitude OK...............");
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Location location2 = new Location("");
        location2.setLatitude(targetLatitude);
        location2.setLongitude(targetLongtiude);
        distanceInMeters = location2.distanceTo(mCurrentlocation)/1000;
        Log.d(TAG, "distanceInMeters OK...............");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"Location changed");
        mCurrentlocation = location;
        latitude =  mCurrentlocation.getLatitude();
        longitude =  mCurrentlocation.getLongitude();
        Location location2 = new Location("");
        location2.setLatitude(targetLatitude);
        location2.setLongitude(targetLongtiude);
        distanceInMeters = location2.distanceTo(mCurrentlocation)/1000;
        //tvDistanceDuration.setText("Distance in meters: "+distanceInMeters);
    }

    private void updateToExpoTextFile(Context context){
        HashMap<String, String> outData = new HashMap<>();
        String path = context.getFilesDir().getAbsolutePath() + "/" + "finished_expo_text_data.map";
        File file = new File(path);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = Calendar.getInstance().getTime();
        String today = dateFormat.format(now);

        if (file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                outData = (HashMap) objectInputStream.readObject();
                objectInputStream.close();
                Log.d("ExposureStart_location", "Load file: " + outData.toString());
                outData.put(today, placeName);
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(outData);
                objectOutputStream.close();
                Log.d("ExposureStart_location", "Save: "+outData.toString() + "to" + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("ExposureStart_location", "FileNotFoundException: "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ExposureStart_location", "IOException: "+ e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("ExposureStart_location", "ClassNotFoundException: "+e.getMessage());
            }
        }else {
            Profile prof = new Profile();
            outData = prof.populateExpoTextData();
            outData.put(today, placeName);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(outData);
                objectOutputStream.close();
                Log.d("ExposureStart_location", "Save: "+outData.toString() + "to" + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
