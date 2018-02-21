package com.example.michal.pulsesensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * Created by kuba on 2017-12-14.
 */

// Serwis towrzy intent, który włącza alarm. Wykorzystywany przy zwyklym budziku

public class AlarmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context,AlarmReceiver.class);
        context.startService(serviceIntent);
        Intent i = new Intent(context.getApplicationContext(), Alarm2.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
