package com.example.soumilchugh.exposocial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SpeechAlarmReceiver extends BroadcastReceiver {

    public static String TAG = "SpeechAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Received in Speech Alarm");
        //Toast.makeText(context, "Alarm", Toast.LENGTH_SHORT).show();
        Intent i = new Intent();
        i.setAction("com.local.receiver1");
        context.sendBroadcast(i);

    }
}
