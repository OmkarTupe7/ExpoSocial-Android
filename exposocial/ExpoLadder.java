package com.example.soumilchugh.exposocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExpoLadder extends AppCompatActivity {

    private BaseActivity baseActs;
    private BottomNavigationView bottomNavigationView;
    private PopupWindow pw;
    private HashMap<String, Integer> ladderMap;
    private int[] fearArray;
    private int[] avoidArray;
    String srcActi;
    String situation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expo_ladder);
        this.setTitle(getResources().getString(R.string.expoLadderTab));

        // Initialize scale arrays
        fearArray = new int[AnxietyLevel.questionList.length];
        avoidArray = new int[AnxietyLevel.questionList.length];

        // Get Intent from previous activity
        Bundle extras = getIntent().getExtras();
        srcActi = extras.getString("FROM");

        // If from AnxietyLevel activity then get LSAS arrays from intent
        // Otherwise, get LSAS from perference
        if (srcActi.equals(AnxietyLevel.class.getSimpleName())){
            fearArray = extras.getIntArray("fearScale");
            avoidArray = extras.getIntArray("avoidScale");
        }else{
            SharedPreferences sharedpreferences = getSharedPreferences("FearScales", 0);
            for (int n = 0; n<AnxietyLevel.questionList.length; n++){
                int fScale = sharedpreferences.getInt("fear_"+n,0);
                int aScale = sharedpreferences.getInt("avoid_"+n,0);
                fearArray[n] = fScale;
                avoidArray[n] = aScale;
            }
            SharedPreferences sharedpreferences2 = getSharedPreferences("currFearSituation", 0);
            situation = sharedpreferences2.getString("fName","");
        }

        // Get bottom bar
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_exposure);
        baseActs = new BaseActivity(ExpoLadder.this, getApplicationContext().getString(R.string.loading));
        baseActs.showProgressDialog();

        // Get sorted HashMap of situation and scale
        ladderMap = getSortedHashMap(ExpoLadder.this, fearArray, avoidArray);

        // Convert to ArrayLists
        final ArrayList<String> fearSituations = new ArrayList<String>(ladderMap.keySet());
        ArrayList<Integer> fearScales = new ArrayList<Integer>(ladderMap.values());

        // Update Fear name if from anxiety level activity
        if (srcActi.equals(AnxietyLevel.class.getSimpleName())){
            situation = fearSituations.get(0);
            // Initial shared preference to store scale arryas
            SharedPreferences sharedpreferences = getSharedPreferences("currFearSituation", 0);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("fName",situation);
            editor.commit();
        }

        // Display in ListView
        fearLadder fl = new fearLadder(fearSituations, fearScales);
        ListAdapter adapter = new ListAdapter(this, fl);
        ListView listView = (ListView) findViewById(R.id.list_ladder);
        listView.setAdapter(adapter);
        baseActs.hideProgressDialog();

        // Listen to click each item of listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int currFear = getCompleteNumExpo(ExpoLadder.this, fearArray.length);
                if (i != currFear){
                    Toast.makeText(getApplicationContext(), "Let's start from the #"+(int)(currFear+1)+" fear.", Toast.LENGTH_SHORT).show();
                }else{
                    if (currFear == fearArray.length - 1) {
                        // Start intent
                        Intent intent = new Intent(getApplicationContext(), SelectLocation.class);
                        intent.putExtra("FEARNAME", situation);
                        intent.putExtra("FROM", ExpoLadder.class.getSimpleName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Start the next Exposure...", Toast.LENGTH_SHORT).show();
                    }
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
                        // Show a dialog to get confirmation
                        new AlertDialog.Builder(ExpoLadder.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Confirm")
                                .setMessage("Are you sure to redo LSAS?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        baseActs.showProgressDialog();
                                        Intent i = new Intent(getApplicationContext(), AnxietyLevel.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        baseActs.hideProgressDialog();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        bottomNavigationView.getMenu().getItem(1).setChecked(true);
                                    }
                                })
                                .show();
                        break;
                    // When click exposure
                    case R.id.menu_exposure:
                        Toast.makeText(getApplicationContext(), R.string.message_already_here, Toast.LENGTH_SHORT).show();
                        break;
                    // When click profile
                    case R.id.profile:
                        // Show a dialog to get confirmation
                        new AlertDialog.Builder(ExpoLadder.this, AlertDialog.THEME_HOLO_LIGHT)
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
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        bottomNavigationView.getMenu().getItem(1).setChecked(true);
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

    // Popup window when the activity if ready
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Popup Window
        if (srcActi.equals(AnxietyLevel.class.getSimpleName())){
            initiatePopupWindow(ExpoLadder.this);
        }
    }

    //////////////////////// Useful Functions ////////////////////////
    // function to sort hashmap by values
    public HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());
        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public HashMap<String, Integer> getSortedHashMap (Context context, int[] arrA, int[] arrB){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i<arrA.length; i++){
            String situation = context.getResources().getString(AnxietyLevel.questionList[i]).split(":")[0];
            float frac = (float)((arrA[i] + arrB[i]) / 6.0 * 10);
            int scale = Math.round(frac) * 10;
            map.put(situation, scale);
        }
        HashMap<String, Integer> sortedMap = sortByValue(map);
        return sortedMap;
    }

    private void initiatePopupWindow(Context context) {
        try {
            // We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            // Inflate the view from a predefined XML layout
            View layout = inflater.inflate(R.layout.expo_ladder_popup,
                    (ViewGroup) findViewById(R.id.popup_element));
            // create a PopupWindow
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // display the popup in the center
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

            View container = (View) pw.getContentView().getParent();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            // add flag
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.6f;
            wm.updateViewLayout(container, p);

            TextView mContentsText = (TextView) layout.findViewById(R.id.txtContents);
            mContentsText.setText(getResources().getText(R.string.expoLadderContent1) + " \"" +
                    ladderMap.keySet().toArray()[0] + "\". " +
                    getResources().getText(R.string.expoLadderContent2)
            );
            Button cancelButton = (Button) layout.findViewById(R.id.btn_dismissPW);
            cancelButton.setOnClickListener(cancel_button_click_listener);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
        }
    };

    private int getCompleteNumExpo(Context context, int lenLadder){
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
        int numE = outData.keySet().size();
        int nowE = lenLadder - (numE/5 + 1);
        return nowE;
    }
}
