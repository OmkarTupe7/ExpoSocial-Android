package com.example.soumilchugh.exposocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;
import java.util.HashMap;

public class Debrief extends AppCompatActivity {
    private Button btn_submit;
    private EditText a1,a2,a3;
    private String expo_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debrief);

        btn_submit = findViewById(R.id.btn_submit);
        a1 = findViewById(R.id.tv_a1);
        a2 = findViewById(R.id.tv_a2);
        a3 = findViewById(R.id.tv_a3);
        Bundle extras = getIntent().getExtras();
        expo_date = extras.getString("expo_date");

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] answersTxt = {a1.getText().toString(), a2.getText().toString(), a3.getText().toString()};
                HashMap<String, String[]> answers = new HashMap<>();
                answers.put(expo_date, answersTxt);
                saveAnswerFile(answers);
                if (getCompleteNumExpo(Debrief.this) < 5){
                    Intent i = new Intent(getApplicationContext(), Profile.class);
                    i.putExtra("FROM", ExposureTracking.class.getSimpleName());
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }else{
                    new AlertDialog.Builder(Debrief.this, AlertDialog.THEME_HOLO_LIGHT)
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
            }
        });
    }


    private void saveAnswerFile(HashMap<String, String[]> answers ){
        String path = Debrief.this.getFilesDir().getAbsolutePath() + "/" + "debrief_data.map";
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
}
