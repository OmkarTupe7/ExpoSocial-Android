package com.example.soumilchugh.exposocial;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SelectLocation extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private BottomNavigationView bottomNavigationView;
    private WebView web_exposure_content;
    private TextView txt_exposure_title;
    private Button startNowButton;
    private Button startLaterButton;
    private PopupWindow pw;
    private PopupWindow tip_pw;
    private int PLACE_PICKER_REQUEST = 1;
    private BaseActivity baseActs;
    private TextView[] dots;
    private int[] layouts;
    private LinearLayout dotsLayout;
    private Button btnSkip;
    private Button btnNext;
    private ViewPager viewPager;

    GoogleApiClient mGoogleApiClient;
    String fearName;
    String srcActi;
    public static String TAG = SelectLocation.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        // Get Intent from previous activity
        Bundle extras = getIntent().getExtras();
        srcActi = extras.getString("FROM");
        SharedPreferences sharedpreferences2 = getSharedPreferences("currFearSituation", 0);
        fearName = sharedpreferences2.getString("fName","");

        // Get vars
        baseActs = new BaseActivity(SelectLocation.this, getApplicationContext().getString(R.string.loading));
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.getMenu().getItem(1).setCheckable(false);
        bottomNavigationView.getMenu().getItem(2).setCheckable(false);

        txt_exposure_title = (TextView) findViewById(R.id.txt_exposure_title);
        txt_exposure_title.setText(fearName);
        web_exposure_content = (WebView) findViewById(R.id.txt_expoStartContent);
        web_exposure_content.setVerticalScrollBarEnabled(false);
        web_exposure_content.loadData(getResources().getString(R.string.exposure_start2),
                "text/html; charset=utf-8", "utf-8");
        startNowButton = findViewById(R.id.startNow);
        startLaterButton = findViewById(R.id.startLater);
        startLaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiateCalendarPopupWindow(SelectLocation.this);
            }
        });
        startNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
                        .setIcon(getResources().getDrawable(R.drawable.baseline_trending_up_24px))
                        .setTitle("Select Location for exposure!")
                        .setMessage(R.string.select_location)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                                try {
                                    startActivityForResult(builder.build(SelectLocation.this), PLACE_PICKER_REQUEST);
                                } catch (GooglePlayServicesRepairableException e) {
                                    e.printStackTrace();
                                } catch (GooglePlayServicesNotAvailableException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
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
                        new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
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
                        new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
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
                        new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
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
    // Popup window when the activity if ready
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Popup Window
        if (srcActi.equals(ExpoLadder.class.getSimpleName())){
            initiatePopupExpoIntroWindow(SelectLocation.this);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String placeName = place.getName().toString();
                Log.d("SelectLocation_place: ", placeName);
                //.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(), ExposureStart.class);
                i.putExtra("SUDS", 1);
                LatLng queriedLocation = place.getLatLng();
                Log.v("Latitude is", "" + (queriedLocation.latitude));
                Log.v("Longitude is", "" + queriedLocation.longitude);
                Bundle args = new Bundle();
                args.putParcelable("from_position", queriedLocation);
                i.putExtra("bundle", args);

                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra("FEARNAME", fearName);
                i.putExtra("placeName", placeName);
                startActivity(i);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void initiateCalendarPopupWindow(Context context) {
        try {
            // We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            // Inflate the view from a predefined XML layout
            View layout = inflater.inflate(R.layout.expo_calender_popup,
                    (ViewGroup) findViewById(R.id.popup_element));

            // create a PopupWindow
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.setHeight(1300);
            // display the popup in the center
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

            View container = (View) pw.getContentView().getParent();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            // add flag
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.6f;
            wm.updateViewLayout(container, p);

            // Get CalendarView
            CalendarView calendarView = (CalendarView) layout.findViewById(R.id.calendarView);
            Button btn_back = (Button) layout.findViewById(R.id.btn_back);

            btn_back.setOnClickListener(back_button_click_listener);
            calendarView.setOnDateChangeListener(data_change_listener);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener back_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
        }
    };

    private CalendarView.OnDateChangeListener data_change_listener = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(CalendarView calendarView, final int i, final int i1, final int i2) {
            //Toast.makeText(getApplicationContext(), "Selected Date:\n" + "Day = " + i2 + "\n" + "Month = " + i1 + "\n" + "Year = " + i, Toast.LENGTH_LONG).show();
            final Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            final int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(new ContextThemeWrapper(SelectLocation.this, R.style.CalenderViewCustom),
                    new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, final int selectedHour, final int selectedMinute) {
                    int m = i1+1;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Log.d("SelectLoc_date", selectedHour+":"+selectedMinute);
                    final String targetDate = i+"-"+m+"-"+i2+" "+selectedHour+":"+selectedMinute;
                    Log.d("SelectLoc_date", targetDate);
                    try {
                        Date tarDate = dateFormat.parse(targetDate);
                        if (System.currentTimeMillis() > tarDate.getTime()){
                            Log.d("SelectLoc_date", "Past date");
                            new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
                                    .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                    .setTitle("Date Error")
                                    .setMessage(R.string.past_date)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }else{
                            // Show a dialog to get confirmation
                            new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
                                    .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                    .setTitle("Confirm")
                                    .setMessage(getString(R.string.expo_reschedule))
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            int m = mcurrentTime.get(Calendar.MONTH) + 1;
                                            String currDate = mcurrentTime.get(Calendar.YEAR) + "-" +
                                                    m + "-" + mcurrentTime.get(Calendar.DATE);
                                            String tarH = "";
                                            String tarM = "";
                                            if (selectedHour == 0) {
                                                tarH = "00";
                                            }else{
                                                tarH = selectedHour+"";
                                            }
                                            if (selectedMinute == 0) {
                                                tarM = "00";
                                            }else{
                                                tarM = selectedMinute+"";
                                            }
                                            String tarDate = i + "-" + (int) (i1 + 1) + "-" + i2 + " " + tarH + ":" + tarM;
                                            Log.d("SelectLocation_data", "tarDate: " + tarDate + " currDate: " + currDate);
                                            addLaterExpoToFile(SelectLocation.this, currDate, tarDate);
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        }
    };

    private void addLaterExpoToFile(Context context, String currDate, String laterDate){

        String path = context.getFilesDir().getAbsolutePath() + "/" + "later_expo_data.map";
        Log.d("SelectLocation_data", path);
        File file = new File(path);

        if (file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                HashMap<String, String> curr_outData = (HashMap) objectInputStream.readObject();
                Log.d("SelectLocation_data", "Load current map: " + curr_outData.toString());
                if (curr_outData.containsKey(currDate)){
                    new AlertDialog.Builder(SelectLocation.this, AlertDialog.THEME_HOLO_LIGHT)
                            .setIcon(getResources().getDrawable(R.drawable.error_face))
                            .setTitle("Oops...")
                            .setMessage(getResources().getString(R.string.error_dupli_reschedule))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    pw.dismiss();
                                }
                            })
                            .show();
                    objectInputStream.close();
                }
                else{
                    //addToCalendar(laterDate);
                    setAlarm(laterDate);
                    curr_outData.put(currDate, laterDate);
                    writeHashMapToFile(curr_outData, path);
                    objectInputStream.close();
                    Toast.makeText(getApplicationContext(), "Exposure rescheduled to "+laterDate, Toast.LENGTH_SHORT).show();
                    pw.dismiss();
                    Intent i = new Intent(getApplicationContext(), Profile.class);
                    i.putExtra("FROM", SelectLocation.class.getSimpleName());
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("SelectLocation_data", "FileNotFoundException: "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SelectLocation_data", "IOException: "+ e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("SelectLocation_data", "ClassNotFoundException: "+e.getMessage());
            }
        }else{
            //addToCalendar(laterDate);
            setAlarm(laterDate);
            HashMap<String, String> mapToAdd = new HashMap<>();
            mapToAdd.put(currDate, laterDate);
            writeHashMapToFile(mapToAdd, path);
            Toast.makeText(getApplicationContext(), "Exposure rescheduled to "+laterDate, Toast.LENGTH_SHORT).show();
            pw.dismiss();
            Intent i = new Intent(getApplicationContext(), Profile.class);
            i.putExtra("FROM", SelectLocation.class.getSimpleName());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    private void setAlarm(String targetDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calNow = Calendar.getInstance();
        Calendar targetCal = (Calendar) calNow.clone();
        int RQS_1 = 1;

        try {
            Date tarDate = dateFormat.parse(targetDate);
            targetCal.setTime(tarDate);
            Intent intent = new Intent(SelectLocation.this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(SelectLocation.this, RQS_1, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
            Log.d("setAlarm", "setAlarm: Done...");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("setAlarm", e.getMessage());
        }
    }

    private void addToCalendar(String targetDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date tarDate = dateFormat.parse(targetDate);
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(tarDate);
            long start = cal1.getTimeInMillis();
            long end = start + 30 * 60 * 1000;

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .setType("vnd.android.cursor.item/event")
                    .putExtra(CalendarContract.Events.TITLE, "Rescheduled Exposure")
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Rescheduled Exposure")
                    // to specify start time use "beginTime" instead of "dtstart"
                    //.putExtra(Events.DTSTART, calendar.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                    // if you want to go from 6pm to 9pm, don't specify all day
                    //.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                    .putExtra(CalendarContract.Events.HAS_ALARM, 1)
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            startActivity(intent);

            Log.d("addToCalendar", "addToCalendar(): good");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("addToCalendar", e.getMessage());
        }
    }


    private void writeHashMapToFile(HashMap<String, String>file, String filePath){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(file);
            objectOutputStream.close();
            Log.d("SelectLocation_data", "writeHashMapToFile(): "+file.toString() + "to" + filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ///////////////////// FUNCTIONS Regarding popup message when page is loaded /////////////////////
    private void initiatePopupExpoIntroWindow(Context context) {
        try {
            // We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            // Inflate the view from a predefined XML layout
            View curr_view = inflater.inflate(R.layout.exposure_tips,
                    (ViewGroup) findViewById(R.id.popup_tips));
            // create a PopupWindow
            tip_pw = new PopupWindow(curr_view, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // display the popup in the center
            tip_pw.showAtLocation(curr_view, Gravity.CENTER, 0, 0);
            // Setup view
            View container = (View) tip_pw.getContentView().getParent();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            // add flag and set dimmed background
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.6f;
            wm.updateViewLayout(container, p);
            // Setup viewPager in the Popup window
            viewPager = (ViewPager) curr_view.findViewById(R.id.view_pager_exposureTips);
            btnSkip = (Button) curr_view.findViewById(R.id.btn_skip);
            btnNext = (Button) curr_view.findViewById(R.id.btn_next);
            dotsLayout = (LinearLayout) curr_view.findViewById(R.id.layoutDots);
            MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
            // layouts of all sliders
            layouts = new int[]{
                    R.layout.expo_intro_1,
                    R.layout.expo_intro_2,
                    R.layout.expo_intro_3
            };
            // adding bottom dots
            addBottomDots(0);
            // Show viewpagers
            viewPager.setAdapter(myViewPagerAdapter);
            // Listen to swipe
            viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

            // Listen to button click
            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tip_pw.dismiss();
                }
            });
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currItem = viewPager.getCurrentItem()+1;
                    if ( currItem < layouts.length){
                        viewPager.setCurrentItem(currItem);
                    }else{
                        tip_pw.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Add bottom dots
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
        //int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        //int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);
        int colorInactive = getResources().getColor(R.color.dot_dark_screen3);
        int colorActive = getResources().getColor(R.color.dot_light_screen3);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(40);
            dots[i].setTextColor(colorActive);
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(colorInactive);
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to Start it!
                btnNext.setText(R.string.start);
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    //View pager adapter
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        public MyViewPagerAdapter() {
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }
        @Override
        public int getCount() {
            return layouts.length;
        }
        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}

