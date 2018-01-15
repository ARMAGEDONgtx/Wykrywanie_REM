package com.example.michal.pulsesensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Kuba on 2017-11-24.
 */

public class Alarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaPlayer music;
        AudioManager audio;
        Toast.makeText(context, "Howdy partner", Toast.LENGTH_SHORT).show();
        audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        music = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int Volume = (int)(0.8*audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,Volume,1);
        music.start();
        music.setLooping(true);
        context.startActivity(new Intent(context, MainActivity.class));

    }
}
