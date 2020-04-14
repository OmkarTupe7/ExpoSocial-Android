package com.example.soumilchugh.exposocial;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class AnxietyLevel extends AppCompatActivity {

    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private TextView txtSubmit;
    private BaseActivity baseActs;
    private BottomNavigationView bottomNavigationView;

    // Define LSAS questions
    public static int[] questionList = {
            R.string.lsas1, R.string.lsas2, R.string.lsas3, R.string.lsas4, R.string.lsas5
    };
    // Define images for display with LSAS questions
    private static int[] imgList = {
            R.drawable.phone, R.drawable.group, R.drawable.eat, R.drawable.drink, R.drawable.talk
    };
    // Define scale arrays
    private int[] fearScale  = new int[questionList.length];
    private int[] avoidScale = new int[questionList.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anxiety_level);
        this.setTitle(getResources().getString(R.string.lsasTab));

        // Initialize scale arrays with -1
        Arrays.fill(fearScale, -1);
        Arrays.fill(avoidScale, -1);

        // Initialize ViewPager and CardAdapter
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mCardAdapter = new CardPagerAdapter();
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.lsas);
        baseActs = new BaseActivity(AnxietyLevel.this, getApplicationContext().getString(R.string.loading));

        // Add new card based on given LSAS questions
        for (int i = 0; i<imgList.length; i++){
            mCardAdapter.addCardItem(new CardItem(questionList[i], imgList[i]));
        }


        // Initialize ShadowTransformer and set cardView scale
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);
        mCardShadowTransformer.enableScaling(true);
        bottomNavigationView.setSelectedItemId(R.id.lsas);

        // Listen to page swiping
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}
            @Override
            public void onPageSelected(int i) {
                // Update seekbar progress according to scale arrays
                setSeekbar();
                // Listen to clicking txtSubmit
                if (i == questionList.length-1){
                    setSumitText();
                }
            }
            @Override
            public void onPageScrollStateChanged(int i) {
                // When the user starts dragging the view,
                // then update scale array with current seekbar's values
                if (i == 1){
                    updateScales();
                }
            }
        });

        // Listen select bottom bar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    // When click LSAS
                    case R.id.lsas:
                        Toast.makeText(getApplicationContext(), R.string.message_already_here, Toast.LENGTH_SHORT).show();
                        break;
                    // When click exposure
                    case R.id.menu_exposure:
                        // Check if all pages of LSAS have been gone through once
                        if (!checkScales()){
                            new AlertDialog.Builder(AnxietyLevel.this, AlertDialog.THEME_HOLO_LIGHT)
                                    .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                    .setTitle("Warning")
                                    .setMessage("Please finish LSAS first!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            bottomNavigationView.getMenu().getItem(0).setChecked(true);
                                        }
                                    })
                                    .show();
                        }else{
                            initiateExpoLadder();
                        }
                        break;
                    // When click profile
                    case R.id.profile:
                        new AlertDialog.Builder(AnxietyLevel.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Warning")
                                .setMessage("Please finish LSAS first!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        bottomNavigationView.getMenu().getItem(0).setChecked(true);
                                    }
                                })
                                .show();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    ///////////////////// Functions /////////////////////

    private void updateScales(){
        // Get current view ID
        final int currViewId = mViewPager.getCurrentItem();
        // Get current view with ID and Tag
        final View currView = mViewPager.findViewWithTag(currViewId);
        // Get SeekBars and textView for display the values
        SeekBar sb_fear = (SeekBar) currView.findViewById(R.id.seekBar_fear);
        SeekBar sb_avoid = (SeekBar) currView.findViewById(R.id.seekBar_avoid);
        // Update scale arrays
        fearScale[currViewId]  = sb_fear.getProgress();
        avoidScale[currViewId] = sb_avoid.getProgress();
        //Toast.makeText(getApplicationContext(), currViewId+"___"+fearScale[currViewId]+"+++"+avoidScale[currViewId], Toast.LENGTH_SHORT).show();
    }

    private void setSeekbar(){
        // Get current view ID
        final int currViewId = mViewPager.getCurrentItem();
        // Get current view with ID and Tag
        final View currView = mViewPager.findViewWithTag(currViewId);
        // Get SeekBars and textView for display the values
        SeekBar sb_fear = (SeekBar) currView.findViewById(R.id.seekBar_fear);
        SeekBar sb_avoid = (SeekBar) currView.findViewById(R.id.seekBar_avoid);
        // Set seekbar's value with current array
        sb_fear.setProgress(fearScale[currViewId]);
        sb_avoid.setProgress(avoidScale[currViewId]);
    }

    private Boolean checkScales(){
        for (int i = 0; i<fearScale.length; i++){
            if (fearScale[i] == -1 || avoidScale[i] == -1){
                return false;
            }
        }
        return true;
    }

    private void setSumitText(){
        // Get current view ID
        final int currViewId = mViewPager.getCurrentItem();
        // Get current view with ID and Tag
        final View currView = mViewPager.findViewWithTag(currViewId);
        // Get Submit TextView on the last page
        txtSubmit = (TextView) currView.findViewById(R.id.txtSubmit);
        TextView txtPage = (TextView)currView.findViewById(R.id.txtPage);
        // Set txtPage view blank
        txtPage.setText("");
        // Set txtSubmit
        txtSubmit.setTextSize(15);
        txtSubmit.setBackgroundColor(getResources().getColor(R.color.colorSubmit));
        txtSubmit.setPadding(4,4,4,4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txtSubmit.setLetterSpacing((float) 0.1);
        }
        txtSubmit.setTypeface(null, Typeface.BOLD);
        txtSubmit.setText("Submit");

        // Listen to click submit
        txtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateScales();
                // Show a dialog to get confirmation
                new AlertDialog.Builder(AnxietyLevel.this, AlertDialog.THEME_HOLO_LIGHT)
                        .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                        .setTitle("Confirm")
                        .setMessage("Are you sure to submit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                baseActs.showProgressDialog();
                                if (checkScales()){
                                    initiateExpoLadder();
                                }
                                else
                                    Toast.makeText(getApplicationContext(), "Question(s) not answered yet, please check!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void initiateExpoLadder (){
        // Initial shared preference to store scale arryas
        SharedPreferences sharedpreferences = getSharedPreferences("FearScales", 0);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        // Loop over each entry to store in preference
        for (int n = 0; n < fearScale.length; n++){
            editor.putInt("fear_"+n, fearScale[n]);
            editor.putInt("avoid_"+n, avoidScale[n]);
        }
        editor.commit();
        // Start intent
        Intent i = new Intent(getApplicationContext(), ExpoLadder.class);
        i.putExtra("fearScale", fearScale);
        i.putExtra("avoidScale", avoidScale);
        i.putExtra("FROM", AnxietyLevel.class.getSimpleName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        baseActs.hideProgressDialog();
    }
}
