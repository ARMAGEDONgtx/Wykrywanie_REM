package com.example.michal.pulsesensor;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;

/**
 * Created by Kuba on 2017-11-24.
 */

public class Alarm extends BroadcastReceiver {
    MediaPlayer music;
    @Override
    public void onReceive(Context context, Intent intent) {
        music = MediaPlayer.create(context, R.raw.akcent);
        music.start();
    }
}
