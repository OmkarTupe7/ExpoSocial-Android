package com.example.soumilchugh.exposocial;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SpeechRecognizerAlarmReceiver extends BroadcastReceiver {

    public static String TAG = "SpeechRecognizerAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent(context, SpeechRecognizerAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 200, intent1, 0);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, pendingIntent);
        Log.d(TAG, "Received");
        //Toast.makeText(context, "Alarm", Toast.LENGTH_SHORT).show();
        Intent i = new Intent();
        i.setAction("com.local.receiver");
        context.sendBroadcast(i);

    }
}
