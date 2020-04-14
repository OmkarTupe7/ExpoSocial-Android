package com.example.soumilchugh.exposocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Profile extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedpreferences, sharedpreferences2;
    private TextView lsasScore;
    private TextView chartTitle;
    private TextView barchart_title;
    private LineChart chart;
    private BarChart barChart;
    private FloatingActionButton fab;
    private int[] fearArray;
    private int[] avoidArray;
    private HashMap<String, Integer> ladderMap;
    public static String[] expoDate = {"2019-04-01", "2019-04-02", "2019-04-04", "2019-04-05"};
    public static int[] expoAudioPercent = {35, 50, 45, 60};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.setTitle(getResources().getString(R.string.profile_tab));

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        lsasScore = (TextView) findViewById(R.id.prof_lsas_score);
        chartTitle = (TextView) findViewById(R.id.chart_title);
        barchart_title =  findViewById(R.id.barchart_title);
        sharedpreferences = getSharedPreferences("FearScales", 0);
        sharedpreferences2 = getSharedPreferences("currFearSituation", 0);
        chart = (LineChart) findViewById(R.id.ac_lineChart);
        barChart = findViewById(R.id.ac_barChart);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Initialize scale arrays, set up chart title
        int N = AnxietyLevel.questionList.length;
        int sumScore = 0;
        fearArray = new int[AnxietyLevel.questionList.length];
        avoidArray = new int[AnxietyLevel.questionList.length];
        String situation = sharedpreferences2.getString("fName","");

        // Update scale arrays
        for (int n = 0; n<N; n++){
            int fScale = sharedpreferences.getInt("fear_"+n,0);
            int aScale = sharedpreferences.getInt("avoid_"+n,0);
            fearArray[n] = fScale;
            avoidArray[n] = aScale;
            sumScore = sumScore + fScale + aScale;
        }
        int lsasTotalScore = (sumScore * 100) / ((3 + 3) * N);
        lsasScore.setText(lsasTotalScore+"");

        // Create and Load populated expo data
        HashMap<String, HashMap<Integer, Integer>> data = loadExpoData(Profile.this);
        Map<String, HashMap<Integer, Integer>> treeMap = new TreeMap<String, HashMap<Integer, Integer>>(data);
        // Create and Load populated expo audio data
        HashMap<String, HashMap<Integer, Integer>> dataAudio = loadExpoAudioData(Profile.this);
        Map<String, HashMap<Integer, Integer>> treeMapAudio = new TreeMap<String, HashMap<Integer, Integer>>(dataAudio);
        Log.d("Profile_readHash", "treeMapAudio: "+ treeMapAudio.toString());
        List<ILineDataSet> dataSetsList = new ArrayList<ILineDataSet>();
        BarData barData = new BarData();
        int colorIter = 0;
        int colorIter1 = 0;
        Random rnd = new Random();
        final float barWidth = 0.6f;

        chartTitle.setText(situation + ": " + data.keySet().size()+" completed exposures");
        barchart_title.setText(getResources().getString(R.string.bartitle));

        for (String key : treeMapAudio.keySet()){
            ArrayList<BarEntry> barentries = new ArrayList<>();
            HashMap<Integer, Integer> tmp = treeMapAudio.get(key);
            int x = (int) tmp.keySet().toArray()[0];
            int y = tmp.get(x);
            barentries.add(new BarEntry(x, y));

            BarDataSet barDataSet1 = new BarDataSet(barentries, "Expo-"+(int)(colorIter1+1));
            if (colorIter1<4){
                barDataSet1.setColor(getResources().getIntArray(R.array.array_dot_inactive)[colorIter1]);
            }else{
                int r4 = rnd.nextInt(256);
                int g4 = rnd.nextInt(256);
                int b4 = rnd.nextInt(256);
                barDataSet1.setColor(Color.argb(255, r4,g4,b4));
            }
            barData.addDataSet(barDataSet1);
            colorIter1+=1;
        }
        barData.setBarWidth(barWidth);
        XAxis xAxis1 = barChart.getXAxis();
        xAxis1.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis1.setAxisMaximum(treeMapAudio.keySet().size()+1);
        xAxis1.setAxisMinimum(0);
        xAxis1.setGranularity(1f);

        YAxis leftAxis1 = barChart.getAxisLeft();
        leftAxis1.setAxisMaximum(100);
        leftAxis1.setAxisMinimum(0);
        // Set up Legned
        Legend legend1 = barChart.getLegend();
        legend1.setForm(Legend.LegendForm.SQUARE);
        legend1.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        legend1.setWordWrapEnabled(true);
        legend1.setTextSize(12);
        // Set up display
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawBorders(true);
        barChart.setData(barData);
        barChart.invalidate();

        // Set up the line chart
        for (String key : treeMap.keySet()){
            HashMap<Integer, Integer> expoRec = data.get(key);
            Map<Integer, Integer> treeMap2 = new TreeMap<Integer, Integer>(expoRec);
            // Construct line
            ArrayList<Entry> entries = new ArrayList<>();
            for (int minute : treeMap2.keySet()){
                entries.add(new Entry(minute, treeMap2.get(minute)));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Expo-"+(int)(colorIter+1));
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(5);
            dataSet.setCircleRadius(6);
            //Random rnd = new Random();
            if (colorIter<4){
                dataSet.setColor(getResources().getIntArray(R.array.array_dot_inactive)[colorIter]);
                dataSet.setCircleColor(getResources().getIntArray(R.array.array_dot_active)[colorIter]);
            }else{
                int r4 = rnd.nextInt(256);
                int g4 = rnd.nextInt(256);
                int b4 = rnd.nextInt(256);
                dataSet.setColor(Color.argb(255, r4,g4,b4));
                dataSet.setCircleColor(Color.argb(128, r4,g4,b4));
            }
            dataSetsList.add(dataSet);
            colorIter+=1;
        }
        LineData lineData = new LineData(dataSetsList);
        // Set up Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(30);
        xAxis.setAxisMinimum(0);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(100);
        leftAxis.setAxisMinimum(0);
        // Set up Legned
        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12);
        // Set up display
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(true);
        chart.setData(lineData);
        chart.invalidate();

        // Load exposure text data
        HashMap<String, String> expo_text = loadExpoTextData(Profile.this);
        Map<String, String> treeText = new TreeMap<>(expo_text);
        String showText="";
        TextView tv_Expo_text = findViewById(R.id.expo_text_data);
        int counter = 1;
        for (String Key: treeText.keySet()){
            if (counter==1){
                showText = "Expo-" + counter + ":  " +  Key + "  @  " + expo_text.get(Key);
            }else{
                showText = showText + "\nExpo-" + counter + ":  " +  Key + "  @  " + expo_text.get(Key);
            }
            counter++;
        }
        tv_Expo_text.setText(showText);

        // Setup Table
        createRescheduledTable(Profile.this);

        // When floating button then initiate camera
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentEmail();
                //Toast.makeText(getApplicationContext(), "Share", Toast.LENGTH_SHORT).show();
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
                        new AlertDialog.Builder(Profile.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Confirm")
                                .setMessage("Are you sure to redo LSAS?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getApplicationContext(), AnxietyLevel.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                                    }
                                })
                                .show();
                        break;
                    // When click exposure
                    case R.id.menu_exposure:
                        // Show a dialog to get confirmation
                        new AlertDialog.Builder(Profile.this, AlertDialog.THEME_HOLO_LIGHT)
                                .setIcon(getResources().getDrawable(R.drawable.baseline_assignment_late_24px))
                                .setTitle("Confirm")
                                .setMessage(getString(R.string.message_back_ladder))
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(getApplicationContext(), ExpoLadder.class);
                                        i.putExtra("FROM", Profile.class.getSimpleName());
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                                    }
                                })
                                .show();
                        break;
                    // When click profile
                    case R.id.profile:
                        Toast.makeText(getApplicationContext(), R.string.message_already_here, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    /* FUNCTIONS */
    public static HashMap<String, HashMap<Integer, Integer>> populateExpoData(){
        // Create container
        HashMap<String, HashMap<Integer, Integer>> data = new HashMap<String, HashMap<Integer, Integer>>();
        // Create expo-records
        HashMap<Integer, Integer> record1 = new HashMap<Integer, Integer>();
        record1.put(0, 70);
        record1.put(3, 90);
        record1.put(10, 80);
        record1.put(18, 60);
        record1.put(30, 60);
        HashMap<Integer, Integer> record2 = new HashMap<Integer, Integer>();
        record2.put(0, 60);
        record2.put(7, 50);
        record2.put(19, 40);
        record2.put(25, 30);
        HashMap<Integer, Integer> record3 = new HashMap<Integer, Integer>();
        record3.put(0, 50);
        record3.put(2, 80);
        record3.put(15, 60);
        record3.put(25, 30);
        record1.put(30, 30);
        HashMap<Integer, Integer> record4 = new HashMap<Integer, Integer>();
        record4.put(0, 50);
        record4.put(6, 40);
        record4.put(10, 50);
        record4.put(20, 30);
        record4.put(26, 10);
        // Update to container
        data.put(expoDate[0], record1);
        data.put(expoDate[1], record2);
        data.put(expoDate[2], record3);
        data.put(expoDate[3], record4);
        return data;
    }

    public HashMap<String, String>populateExpoTextData(){
        HashMap<String, String> outData = new HashMap<>();
        outData.put(expoDate[0], "Canoe Restaurant");
        outData.put(expoDate[1], "Scaramouche Restaurant");
        outData.put(expoDate[2], "Alo Restaurant");
        outData.put(expoDate[3], "CANO Restaurant");
        return outData;
    }

    public static HashMap<String, HashMap<Integer, Integer>> populateExpoAudioData(){
        HashMap<String, HashMap<Integer, Integer>> outData = new HashMap<>();
        for(int i=0; i<expoDate.length; i++){
            HashMap<Integer, Integer> tmp = new HashMap<>();
            tmp.put(i+1, expoAudioPercent[i]);
            outData.put(expoDate[i], tmp);
        }
        Log.d("Profile_readHash", "populateExpoAudioData: save "+ outData.toString());
        return outData;
    }

    private HashMap<String, String> loadExpoTextData(Context context){
        HashMap<String, String> outData = new HashMap<>();
        String path = context.getFilesDir().getAbsolutePath() + "/" + "finished_expo_text_data.map";
        File file = new File(path);

        if (file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                outData = (HashMap) objectInputStream.readObject();
                objectInputStream.close();
                Log.d("Profile_readHash", "loadExpoTextData: " + outData.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "FileNotFoundException: "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "IOException: "+ e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "ClassNotFoundException: "+e.getMessage());
            }
        }else{
            outData = populateExpoTextData();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(outData);
                objectOutputStream.close();
                Log.d("Profile_readHash", "loadExpoTextData Save: "+outData.toString() + "to" + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outData;

    }

    private HashMap<String, HashMap<Integer, Integer>> loadExpoData(Context context){
        HashMap<String, HashMap<Integer, Integer>> outData = new HashMap<>();
        String path = context.getFilesDir().getAbsolutePath() + "/" + "finished_expo_data.map";
        Log.d("Profile_readHash", path);
        File file = new File(path);
        if (file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                outData = (HashMap) objectInputStream.readObject();
                objectInputStream.close();
                Log.d("Profile_readHash", "loadExpoData Load file: " + outData.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "FileNotFoundException: "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "IOException: "+ e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "ClassNotFoundException: "+e.getMessage());
            }
        }else{
            outData = populateExpoData();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(outData);
                objectOutputStream.close();
                Log.d("Profile_readHash", "loadExpoData Save: "+outData.toString() + "to" + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outData;
    }


    public HashMap<String, HashMap<Integer, Integer>> loadExpoAudioData(Context context){
        HashMap<String, HashMap<Integer, Integer>> outData = new HashMap<>();
        String path = context.getFilesDir().getAbsolutePath() + "/" + "finished_expo_audio_data.map";
        Log.d("Profile_readHash", path);
        File file = new File(path);
        if (file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                outData = (HashMap) objectInputStream.readObject();
                objectInputStream.close();
                Log.d("Profile_readHash", "loadExpoAudioData Load file: " + outData.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "FileNotFoundException: "+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "IOException: "+ e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_readHash", "ClassNotFoundException: "+e.getMessage());
            }
        }else{
            outData = populateExpoAudioData();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(outData);
                objectOutputStream.close();
                Log.d("Profile_readHash", "loadExpoData Save: "+outData.toString() + "to" + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outData;
    }


    private void createRescheduledTable(Context context){
        String path = context.getFilesDir().getAbsolutePath() + "/" + "later_expo_data.map";
        TextView laterExpoList = findViewById(R.id.later_expo_list);
        String textShow = "";
        try{
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            HashMap<String, String> curr_outData = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
            Log.d("Profile_table", curr_outData.toString());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            if (curr_outData.keySet().size()==0){
                textShow = "N/A";
                laterExpoList.setText(textShow);
            }
            else{
                int i = 1;
                for(String key: curr_outData.keySet()) {
                    Log.d("Profile_table", key+": "+curr_outData.get(key));
                    Date tarDate = dateFormat.parse(curr_outData.get(key));

                    if (System.currentTimeMillis() > tarDate.getTime()){
                        laterExpoList.setTextColor(getResources().getColor(R.color.dot_light_screen1));
                        textShow = "  " + textShow;
                        if (i==1){
                            textShow = "#" + i + ":  [Passed]  " + key + "  &#10132  " + curr_outData.get(key);
                        }else{
                            textShow = textShow+"<br />"+"#" + i + ":  [Passed]  " + key + " &#10132 " + curr_outData.get(key);
                        }
                    }
                    if (System.currentTimeMillis() < tarDate.getTime()){
                        laterExpoList.setTextColor(getResources().getColor(R.color.dot_dark_screen2));
                        if (i==1){
                            textShow = "#" + i + ":  " + key + "  &#10132  " + curr_outData.get(key);
                        }else{
                            textShow = textShow+"<br />"+"#" + i + ":  " + key + " &#10132 " + curr_outData.get(key);
                        }
                    }
                    i++;
                }
                laterExpoList.setText(Html.fromHtml(textShow));
            }
        } catch (FileNotFoundException e) {
            textShow = "N/A";
            laterExpoList.setText(textShow);
            e.printStackTrace();
            Log.d("Profile_table", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Profile_table", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("Profile_table", e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void sentEmail (){
        String dirpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (layoutToImage()){
            try {
                if (imageToPDF()){
                    File filelocation = new File(dirpath+"/myProfile.pdf");
                    //Uri path = Uri.fromFile(filelocation);
                    Uri path = FileProvider.getUriForFile(
                            Profile.this,
                            "com.example.soumilchugh.exposocial.provider", //(use your app signature + ".provider" )
                            filelocation);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    // set the type to 'email'
                    emailIntent .setType("vnd.android.cursor.dir/email");
                    String to[] = {getString(R.string.to_email1), getString(R.string.to_email2), getString(R.string.to_email3)};
                    emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
                    // the attachment
                    emailIntent .putExtra(Intent.EXTRA_STREAM, path);
                    // the mail subject
                    emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Exposure Profile");
                    startActivity(Intent.createChooser(emailIntent , "Send email..."));
                }
                else{
                    Log.d("Profile_PDF", "sentEmail(): PDF returns false");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("Profile_PDF", "sentEmail(): "+e.getMessage());
            }
        }
    }

    public Boolean layoutToImage() {

        String dirpath = Environment.getExternalStorageDirectory().getAbsolutePath();

        LayoutInflater inflater = (LayoutInflater) Profile.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.activity_profile, null);
        root.setDrawingCacheEnabled(true);

        Bitmap bm= getBitmapFromView(this.getWindow().findViewById(R.id.profile_content));
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpg");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(dirpath + File.separator + "profile_image.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            Log.d("Profile_PDF", "layoutToImage(): Done...");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Profile_PDF", "layoutToImage(): "+e.getMessage());
            return false;
        }

    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public Boolean imageToPDF() throws FileNotFoundException {
        String dirpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            // Open new PDF file
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(dirpath + "/myProfile.pdf")); //  Change pdf's name.
            document.open();
            PdfContentByte content = pdfWriter.getDirectContent();
            // Load image from external folder
            Image image = Image.getInstance(dirpath + File.separator + "profile_image.jpg");
            image.scaleAbsolute(PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight());
            image.setAbsolutePosition(0, 0);

            float width = PageSize.LETTER.getWidth();
            float heightRatio = image.getHeight() * width / image.getWidth();
            int nPages = (int) (heightRatio / PageSize.LETTER.getHeight());
            float difference = heightRatio % PageSize.LETTER.getHeight();

            while (nPages >= 0) {
                document.newPage();
                content.addImage(image, width, 0, 0, heightRatio, 0, -((--nPages * PageSize.LETTER.getHeight()) + difference));
            }
            document.close();
            //Toast.makeText(this, "PDF Generated successfully!", Toast.LENGTH_SHORT).show();
            Log.d("Profile_PDF", dirpath + "/myProfile.pdf");
            return true;
        } catch (Exception e) {
            Log.d("Profile_PDF", e.getMessage());
            return false;
        }

    }

}
