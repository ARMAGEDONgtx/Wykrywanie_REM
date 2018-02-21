package com.example.michal.pulsesensor;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;

public class Alarm2 extends AppCompatActivity {

    private  MediaPlayer music;     // Muzyka Alarmu
    private  AudioManager audio;    // Potrzebne do regulowania głośności
    private  int currentVolume;     // Zmienna do zapisania aktualnej glosnosci
    private Button menu;            // Powrot do menu
    private Button stopAlarm;       // zmienna zatrzymuje alarm
    private GraphView graphView;    // Potrzebna do rysowania wykrsu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm2);

        init();
        setAudio();
        startMusic();

    }

    void init(){// Inicjalizacja zmiennych
        MainActivity.czyBudzic=0;
        audio = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
        music = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        currentVolume=audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        menu=(Button)findViewById(R.id.powrotDoMenu);
        stopAlarm = (Button)findViewById(R.id.stopAlarm);
    }
    void startMusic(){// Start alarmu
        music.setLooping(true);
        music.start();
    }
    public void stopMusic(View view){//stop alarm, rysowanie wykresu
        music.stop();
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,1);
        Wykres wykres = new Wykres();
        graphView = (GraphView) findViewById(R.id.graph2);
        wykres.rysujWykres(MainActivity.fileSaveText,graphView);
        menu.setEnabled(true);
        stopAlarm.setEnabled(false);
    }
    void setAudio(){
        /*Ustawia glosnosc na 0.8 glosnosci maksymalnej, zeby była pewnosfc że
                         muzyka będzie dzwonić*/
        int Volume = (int)(0.8*audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,Volume,1);
    }
    public void powrotDoMenu(View view){// powrot do menu glownego
        this.finish();
        Intent i = new Intent(Alarm2.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Alarm2.this.startActivity(i);
    }
}
