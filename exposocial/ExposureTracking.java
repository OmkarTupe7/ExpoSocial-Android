package com.example.soumilchugh.exposocial;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import cn.iwgang.countdownview.CountdownView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.triggertrap.seekarc.SeekArc;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class ExposureTracking extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,SoundRecorder.OnVoicePlaybackStateChangedListener, OnMapReadyCallback {

    private Location location;
    Location mCurrentLocation;
    //TextView addressView;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private static final long INTERVAL = 1000 * 10;               // Interval of 10 seconds
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final String TAG = LocationAudioActivity.class.getName();
    String mLastUpdateTime;
    LocationAddress locationAddress;
    private int SUDS;
    private CountdownView countdownView;
    private SeekArc seekArc;
    private TextView txt_suds;
    private Button btn_start;
    private GoogleMap mMap;
    double targetLatitude;
    double targetLongtitude;
    AlertDialog alert;
    double distanceInMeters;
    String expo_date,placeName;
    BroadcastReceiver receiver;
    DroidSpeech droidSpeech;
    Bundle bundle;
    private AudioManager audioManager;
    private PopupWindow debrief_pw;
    private Button btn_submit;
    private EditText a1,a2,a3;
    private int speechDetectInterval = 10;
    private int noSpeechSec = 0;
    private int speechPercentage = 0;


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

    public void startAlarmForSpeechRecognizer() {
        Intent intent1 = new Intent(this, SpeechRecognizerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 200, intent1, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, pendingIntent);
    }

    public void startAlarmForIdleTimeDetection() {
        Intent intent2 = new Intent(this, SpeechAlarmReceiver.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
                this.getApplicationContext(), 201, intent2, 0);
        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager2.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (speechDetectInterval*1000), pendingIntent2);
    }

    void stopAlarmForSpeechRecognizer() {
        Intent intent1 = new Intent(this, SpeechRecognizerAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 200, intent1, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    void stopAlarmForIdleTimeDetection() {
        Intent intent = new Intent(this, ExposureTracking.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 201, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
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
        startAlarmForSpeechRecognizer();
        startAlarmForIdleTimeDetection();
        droidSpeech = new DroidSpeech(this, null);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);


        droidSpeech.setOnDroidSpeechListener(new OnDSListener() {
            @Override
            public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {

            }

            @Override
            public void onDroidSpeechRmsChanged(float rmsChangedValue) {
                float quiet_max = 2f;
                float medium_max = 10f;

                if (rmsChangedValue < quiet_max) {
                    Log.d(TAG,"Quiet" + rmsChangedValue);
                    // quiet
                } else if (rmsChangedValue >= quiet_max && rmsChangedValue < medium_max) {
                    Log.d(TAG,"Medium" + rmsChangedValue);
                    // medium
                } else {
                    Log.d(TAG,"Loud" + rmsChangedValue);
                    // loud
                }

            }

            @Override
            public void onDroidSpeechLiveResult(String liveSpeechResult) {
                Log.d(TAG, liveSpeechResult);
                Toast.makeText(getApplicationContext(),"Speech detected",Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(getApplicationContext(), SpeechAlarmReceiver.class);
                NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                startAlarmForIdleTimeDetection();
                Log.d(TAG,"Starting alarm");

            }

            @Override
            public void onDroidSpeechFinalResult(String finalSpeechResult) {

            }

            @Override
            public void onDroidSpeechClosedByUser() {
                Log.d(TAG,"Closed view");

            }

            @Override
            public void onDroidSpeechError(String errorMsg) {
                Log.d(TAG,errorMsg);

            }
        });
        if (droidSpeech != null)
            droidSpeech.startDroidSpeechRecognition();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expotracking);
        this.setTitle(getResources().getString(R.string.expo_tracking_tab));
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        droidSpeech = new DroidSpeech(this, null);
        // Initialize layout vars
        //addressView = findViewById(R.id.tvAddress);
        txt_suds = (TextView) findViewById(R.id.txt_SUDS);
        countdownView = (CountdownView) findViewById(R.id.countDownView);
        seekArc = (com.triggertrap.seekarc.SeekArc) findViewById(R.id.seekArc);
        btn_start = (Button) findViewById(R.id.btn_start);
        //btn_start.setVisibility(View.GONE);
        btn_start.setEnabled(false);

        // Get SUDS value passing from ExposureStart
        Bundle extras = getIntent().getExtras();
        SUDS = extras.getInt("SUDS");
        expo_date = extras.getString("expo_date");
        placeName = extras.getString("placeName");
        bundle = getIntent().getParcelableExtra("bundle");
        LatLng fromPosition = bundle.getParcelable("from_position");
        targetLatitude = fromPosition.latitude;
        targetLongtitude = fromPosition.longitude;

        txt_suds.setText(SUDS+"");
        seekArc.setProgress(SUDS);

        Log.d("ExposureTracking_data: ", expo_date+", "+SUDS);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        locationAddress = new LocationAddress();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Start counting down
        String from = extras.getString("FROM");
        long millis = 0;
        if (from.equals("notification")){
            millis = extras.getLong("RT");
        }
        else{
            millis = TimeUnit.MINUTES.toMillis(30);
        }
        countdownView.start(millis);
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                /*
                new AlertDialog.Builder(ExposureTracking.this, AlertDialog.THEME_HOLO_LIGHT)
                        .setIcon(getResources().getDrawable(R.drawable.baseline_trending_up_24px))
                        .setTitle("Congratulations!")
                        .setMessage(R.string.congrats2)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Initial shared preference to store scale arryas
                                SharedPreferences sharedpreferences = getSharedPreferences(expo_date, 0);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putInt(30+"", seekArc.getProgress());
                                editor.commit();
                                updateFinishedExpoFile();

                                Intent i = new Intent(getApplicationContext(), Profile.class);
                                i.putExtra("FROM", ExposureTracking.class.getSimpleName());
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        })
                        .show();
                */
                speechPercentage = (30*60 - noSpeechSec) / 30*60 * 100;
                updateFinishedExpoFile();
                //initiateDebriefWindow(ExposureTracking.this);
            }
        });

        int sec = 30;
        Long M = Long.valueOf(sec * 1000);
        countdownView.setOnCountdownIntervalListener(M, new CountdownView.OnCountdownIntervalListener() {
            @Override
            public void onInterval(CountdownView cv, long remainTime) {
                /*long[] pattern = { 500 };
                final Vibrator v = (Vibrator) getSystemService(ExposureTracking.this.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(pattern , 0);
                } else {
                    //deprecated in API 26
                    v.vibrate(pattern , 0);
                }
                */
                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(ExposureTracking.this, AlertDialog.THEME_HOLO_LIGHT);
                builder.setIcon(getResources().getDrawable(R.drawable.baseline_trending_up_24px));
                builder.setTitle("Update your SUDS!");
                builder.setMessage(R.string.remind_update_suds);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        v.cancel();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.getWindow().setType(WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setType(WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                dialog.show();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount=1.0f;
                dialog.getWindow().setAttributes(lp);
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                */
                showNotificationForSUDS(getString(R.string.remind_update_suds));
            }
        });

        // Listen SUDS bar change
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                int nearestTen = ((seekArc.getProgress() + 5) / 10) * 10;
                txt_suds.setText(nearestTen+"");
                btn_start.setEnabled(true);
                //btn_start.setVisibility(View.VISIBLE);
            }
            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                btn_start.setEnabled(false);
                //btn_start.setVisibility(View.GONE);
            }
            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }
        });

        // Listen Set click
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**/
                int secLeft = countdownView.getMinute() * 60 + countdownView.getSecond();
                if (secLeft < 1790){
                    final int nearestTen = ((seekArc.getProgress() + 5) / 10) * 10;
                    final int usedMinute = 30 - secLeft/60;
                    final int usedSeconds = 30*60 - secLeft;

                    Log.d("ExposureTracking_data: ",
                            expo_date+", nearestTen = "+nearestTen+", usedMinute = "+usedMinute+", usedSeconds = "+usedSeconds+
                                    ", noSpeechSec = "+noSpeechSec);
                    if ( (nearestTen <= (SUDS*0.5))){
                        //Toast.makeText(getApplicationContext(), "Im here", Toast.LENGTH_SHORT).show();
                        // Initial shared preference to store scale arryas
                        SharedPreferences sharedpreferences = getSharedPreferences(expo_date, 0);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(usedMinute+"", nearestTen);
                        editor.commit();
                        speechPercentage = (usedSeconds - noSpeechSec) / usedSeconds * 100;
                        updateFinishedExpoFile();

                    }else{
                        Toast.makeText(getApplicationContext(), R.string.message_progress, Toast.LENGTH_LONG).show();
                        // Initial shared preference to store scale arryas
                        SharedPreferences sharedpreferences = getSharedPreferences(expo_date, 0);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(usedMinute+"", nearestTen);
                        editor.commit();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.error_time, Toast.LENGTH_LONG).show();
                }
            }
        });

        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == "com.local.receiver") {
                    droidSpeech.closeDroidSpeechOperations();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            droidSpeech.startDroidSpeechRecognition();
                        }
                    }, 2000);
                }
                if (intent.getAction() == "com.local.receiver1") {
                    //showNotificationForSpeech("Do you want to continue the exposure or reschedule it for later?");
                    noSpeechSec += speechDetectInterval;
                    Log.d("ExposureTracking_data:", "No speech: "+noSpeechSec);
                    startAlarmForIdleTimeDetection();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        // specify the action to which receiver will listen
        filter.addAction("com.local.receiver");
        filter.addAction("com.local.receiver1");
        registerReceiver(receiver,filter);
    }

    public void showNotificationForSpeech(String message) {
        //Vibrator v = (Vibrator) ExposureTracking.this.getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(500);
        //countdownView.pause();
        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        Intent ii = new Intent(getApplicationContext(), ExposureTracking.class);
        ii.putExtra("SUDS", SUDS);
        ii.putExtra("expo_date", expo_date);
        ii.putExtra("placeName", placeName );
        ii.putExtra("bundle", bundle);
        ii.putExtra("FROM", "notification");
        ii.putExtra("RT", countdownView.getRemainTime());
        ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, ii, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        Intent i2 = new Intent(getApplicationContext(), SelectLocation.class);
        i2.putExtra("FROM", "notification");
        i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(getApplicationContext(),
                0, i2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle("Cannot Detect Speech");
        bigText.setSummaryText("Warning");

        mBuilder.setContentIntent(pendingIntent);
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ladder1);
        mBuilder.setLargeIcon(icon);
        mBuilder.setSmallIcon(R.drawable.icon_school);
        //mBuilder.setContentTitle("");
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setAutoCancel(true);
        mBuilder.addAction(R.drawable.check,"Yes",pendingIntent);
        mBuilder.addAction(R.drawable.cross,"No",pendingIntent2);

        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "notify_001";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
        Log.d(TAG,"Notification");
    }

    public void showNotificationForSUDS(String message) {
        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_002");
        Intent ii = new Intent(getApplicationContext(), ExposureTracking.class);
        ii.putExtra("SUDS", SUDS);
        ii.putExtra("expo_date", expo_date);
        ii.putExtra("placeName", placeName );
        ii.putExtra("bundle", bundle);
        ii.putExtra("FROM", "notification");
        ii.putExtra("RT", countdownView.getRemainTime());
        ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, ii, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ladder1);
        mBuilder.setLargeIcon(icon);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle("Update your SUDS");
        bigText.setSummaryText("Warning");

        mBuilder.setContentIntent(pendingIntent);


        mBuilder.setSmallIcon(R.drawable.icon_school);
        //mBuilder.setContentTitle("Warning");
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setAutoCancel(true);

        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "notify_002";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(1, mBuilder.build());
        Log.d(TAG,"Notification");
    }

    void sendAlert() {
        Log.d(TAG,"send Alert");
        if (alert != null) {
            if (alert.isShowing()) {
                Log.d(TAG, "isShowing");
                return;
            }
        }
        alert = new AlertDialog.Builder(ExposureTracking.this, AlertDialog.THEME_HOLO_LIGHT)
                .setIcon(getResources().getDrawable(R.drawable.baseline_trending_up_24px))
                .setTitle("You are not at the location")
                .setMessage(getString(R.string.message_gobackexpo))
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(getApplicationContext(), SelectLocation.class);
                        i.putExtra("FROM", ExposureTracking.class.getSimpleName());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                })
                .create();
        alert.setCancelable(false);
        alert.show();
        countdownView.pause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            Log.d(TAG,"Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
            locationAddress.getAddressFromLocation(location.getLatitude(), location.getLongitude(),
                    getApplicationContext(), new GeocoderHandler());
            Location location2 = new Location("");
            location2.setLatitude(targetLatitude);
            location2.setLongitude(targetLongtitude);
            distanceInMeters = location2.distanceTo(location)/1000;
            if (distanceInMeters > 0.1) {
                sendAlert();
            }
        }

        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition.Builder cameraPosition = new CameraPosition.Builder();
            cameraPosition.target(center);
            cameraPosition.zoom(15);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition.build()));
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Log.d(TAG, "Location update not started ..............: ");

            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"Connection suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Connection Failed");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"Location changed");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        locationAddress.getAddressFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                getApplicationContext(), new GeocoderHandler());
        LatLng center = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        CameraPosition.Builder cameraPosition = new CameraPosition.Builder();
        cameraPosition.target(center);
        cameraPosition.zoom(15);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition.build()));
        Location location2 = new Location("");
        location2.setLatitude(targetLatitude);
        location2.setLongitude(targetLongtitude);
        Log.d (TAG,String.valueOf(targetLatitude));
        Log.d (TAG,String.valueOf(targetLongtitude));

        distanceInMeters = location2.distanceTo(mCurrentLocation)/1000;

        if (distanceInMeters > 0.1 ) {
            sendAlert();
        }
    }

    @Override
    public void onPlaybackStopped() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            Log.d(TAG,locationAddress + "\n" + "At Time: " + mLastUpdateTime);
            //addressView.setText(locationAddress + "\n" + "At Time: " + mLastUpdateTime);
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (droidSpeech != null) {
            droidSpeech.closeDroidSpeechOperations();
            Log.d(TAG, "Closing speech detection");
        }
        stopAlarmForSpeechRecognizer();
        stopAlarmForIdleTimeDetection();
        unregisterReceiver(receiver);

    }


    private void updateFinishedExpoFile(){

        String path = ExposureTracking.this.getFilesDir().getAbsolutePath() + "/" + "finished_expo_data.map";
        HashMap<String, HashMap<Integer, Integer>> outData;
        HashMap<String, HashMap<Integer, Integer>> outSpeechData;
        HashMap<Integer, Integer> tempMap = new HashMap<>();
        HashMap<Integer, Integer> newEntry = new HashMap<>();

        SharedPreferences sharedpreferences = getSharedPreferences(expo_date, 0);
        for(Map.Entry<String,?> entry : sharedpreferences.getAll().entrySet()){
            tempMap.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue().toString()));
            Log.d("ExposureTracking_data",entry.getKey() + ": " + entry.getValue().toString());
        }
        Map<Integer, Integer> treeEntry = new TreeMap<>(tempMap);
        for (Integer Key: treeEntry.keySet()){
            newEntry.put(Key, tempMap.get(Key));
            Log.d("ExposureTracking_data","sorted: "+ Key + ": " + tempMap.get(Key));
        }

        File file = new File(path);
        if (!file.exists()){
            Log.d("ExposureTracking_data", "fileExist: "+file.exists());
            HashMap<String, HashMap<Integer, Integer>> preData = Profile.populateExpoData();

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(preData);
                objectOutputStream.close();
                Log.d("ExposureTracking_data", "pre-populate: "+preData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // Update exposure data
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            outData = (HashMap) objectInputStream.readObject();
            objectInputStream.close();

            Log.d("ExposureTracking_data", outData.toString());
            outData.put(expo_date, newEntry);
            Log.d("ExposureTracking_data", outData.toString());

            FileOutputStream fileOutputStream = new FileOutputStream(path);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(outData);
            objectOutputStream.close();

            // Update speech length data
            Profile prof = new Profile();
            outSpeechData = prof.loadExpoAudioData(ExposureTracking.this);
            Log.d("ExposureTracking_data", outSpeechData.toString());

            HashMap<Integer, Integer> temp = new HashMap<>();
            speechPercentage = 50;
            temp.put(outSpeechData.keySet().size()+1, speechPercentage);
            outSpeechData.put(expo_date, temp);
            Log.d("ExposureTracking_data", outSpeechData.toString());

            String path1 = ExposureTracking.this.getFilesDir().getAbsolutePath() + "/" + "finished_expo_audio_data.map";
            FileOutputStream fileOutputStream1 = new FileOutputStream(path1);
            ObjectOutputStream objectOutputStream1= new ObjectOutputStream(fileOutputStream1);
            objectOutputStream1.writeObject(outSpeechData);
            objectOutputStream1.close();

            // Redirect to debrief page
            Intent i = new Intent(getApplicationContext(), Debrief.class);
            i.putExtra("FROM", ExposureTracking.class.getSimpleName());
            i.putExtra("expo_date", expo_date);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("ExposureTracking_data", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ExposureTracking_data", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("ExposureTracking_data", e.getMessage());
        }

    }

    private int getCompleteNumExpo(Context context){
        HashMap<String, HashMap<Integer, Integer>> outData = new HashMap<>();
        String path = context.getFilesDir().getAbsolutePath() + "/" + "finished_expo_data.map";
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            outData = (HashMap) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outData.keySet().size();
    }


    private void initiateDebriefWindow(Context context) {
        try {
            // We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            // Inflate the view from a predefined XML layout
            View curr_view = inflater.inflate(R.layout.debrief_popup,
                    (ViewGroup) findViewById(R.id.popup_debrief));
            // create a PopupWindow
            debrief_pw = new PopupWindow(curr_view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            // display the popup in the center
            debrief_pw.showAtLocation(curr_view, Gravity.CENTER, 0, 0);
            // Setup view
            View container = (View) debrief_pw.getContentView().getParent();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            // add flag and set dimmed background
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.6f;
            wm.updateViewLayout(container, p);
            // Setup viewPager in the Popup window
            btn_submit = (Button)curr_view.findViewById(R.id.btn_submit);
            a1 = curr_view.findViewById(R.id.tv_a1);
            a2 = curr_view.findViewById(R.id.tv_a2);
            a3 = curr_view.findViewById(R.id.tv_a3);

            // Listen to button click
            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] answersTxt = {a1.getText().toString(), a2.getText().toString(), a3.getText().toString()};
                    HashMap<String, String[]> answers = new HashMap<>();
                    answers.put(expo_date, answersTxt);
                    saveAnswerFile(answers);
                    if (getCompleteNumExpo(ExposureTracking.this) < 5){
                        Intent i = new Intent(getApplicationContext(), Profile.class);
                        i.putExtra("FROM", ExposureTracking.class.getSimpleName());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }else{
                        new AlertDialog.Builder(ExposureTracking.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_trending_up_24px))
                                .setTitle("Congratulations!")
                                .setMessage(R.string.congrats3)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getApplicationContext(), Profile.class);
                                        i.putExtra("FROM", ExposureTracking.class.getSimpleName());
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                })
                                .show();
                    }
                    debrief_pw.dismiss();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveAnswerFile(HashMap<String, String[]> answers ){
        String path = ExposureTracking.this.getFilesDir().getAbsolutePath() + "/" + "debrief_data.map";
        Log.d("saveAnswerFile:", path);
        File file = new File(path);
        FileInputStream fileInputStream = null;
        HashMap<String, String[]> outData;

        if (file.exists()){
            try {
                fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                outData = (HashMap) objectInputStream.readObject();

                Log.d("saveAnswerFile: ", outData.toString());
                outData.putAll(answers);
                Log.d("saveAnswerFile: ", outData.toString());

                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(outData);
                objectOutputStream.close();
                Log.d("saveAnswerFile:", "saved: "+outData.toString());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("saveAnswerFile:", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("saveAnswerFile:", e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("saveAnswerFile:", e.getMessage());
            }

        }
        else{
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(answers);
                objectOutputStream.close();
                Log.d("saveAnswerFile:", "new file added: "+answers.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("saveAnswerFile:", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("saveAnswerFile:", e.getMessage());
            }
        }
    }
}
