package com.example.michal.pulsesensor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class Budzik extends AppCompatActivity {

    private TimePicker Time;
    private Button UstawButton;
    private Button stopButton;
    private TextView PokazCzas;
    private Calendar calendar;
    AlarmManager alarmManager;
    Intent intent;
    PendingIntent pendingIntent;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budzik);

        init();

        ShowTime();


    }
    public void ShowTime(){

        UstawButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        UstawAlarm();
                        TimeText();
                        Toast.makeText(Budzik.this, "Budzik ustawiony", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent( context, MainActivity.class);
                        startActivity(intent);

                    }
                }
        );
    }
    void ustawCzas(){
        calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Time.getCurrentHour());
        calendar.set(Calendar.MINUTE, Time.getCurrentMinute());
        if((calendar.getTimeInMillis()-System.currentTimeMillis())<0){
            calendar.set(calendar.DAY_OF_YEAR,(calendar.get(calendar.DAY_OF_YEAR)+1));
        }
    }
    void TimeText(){
        int hour, minute;
        int currentTimeHours, currentTimeMinutes;
        int dlugoscSpaniaHours;
        int dlugoscSpaniaMinutes;
        String TimeText;

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        currentTimeHours = Calendar.getInstance().getTime().getHours();
        currentTimeMinutes = Calendar.getInstance().getTime().getMinutes();
        dlugoscSpaniaHours = czasSpaniaGodziny(hour,minute,currentTimeHours,currentTimeMinutes);
        dlugoscSpaniaMinutes =  czasSpaniaMinuty(hour,minute,currentTimeHours,currentTimeMinutes);

        if(dlugoscSpaniaHours>0){
            TimeText = ". Długość spania: " + dlugoscSpaniaHours + " godzin " + dlugoscSpaniaMinutes + " minut";
        }
        else {
            TimeText = ". Długość spania: " + dlugoscSpaniaMinutes + " minut";
        }

        PokazCzas.setText("Budzik ustawiony na " +calendar.getTime() + TimeText);
    }
    private void init(){
        Time=(TimePicker)findViewById(R.id.timePicker);
        UstawButton=(Button)findViewById(R.id.UstawBudzik);
        PokazCzas = (TextView) findViewById(R.id.PokazCzas);
        stopButton=(Button)findViewById(R.id.stop);
        stopButton.setEnabled(false);
        calendar=Calendar.getInstance();
        context=this.getApplicationContext();

    }
    int czasSpaniaGodziny(int Hour, int Minute, int CurHour, int CurMin){
        int godziny = (24-CurHour+Hour)%24;
        if((Hour==CurHour)&&(Minute<CurMin))
        {
            return 23;
        }
        if(Minute>=CurMin){
            return godziny;
        }
        else{
            return (godziny-1);
        }
    }
    int czasSpaniaMinuty(int Hour, int Minute, int CurHour, int CurMin){
        int minuty = Minute-CurMin;
        if(minuty>=0){
            return (minuty%60);
        }
        else{
            return ((60+minuty)%60);
        }
    }
    long czasSpania_ms(int Hour, int Minute, int CurHour, int CurMin){       //Czas spania w ms
        long godziny = (czasSpaniaGodziny( Hour,  Minute,  CurHour,  CurMin)*60);
        long minuty = czasSpaniaMinuty( Hour,  Minute,  CurHour,  CurMin);
        long czas= ((godziny + minuty)*60000);
        return czas;
    }
    private void UstawAlarm(){
        ustawCzas();
        MainActivity.czyBudzic=1;
        stopButton.setEnabled(true);
        intent = new Intent (this, Alarm.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);
        alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
    }
    public void StopButton(View view){
        MainActivity.czyBudzic=0;
        alarmManager.cancel(pendingIntent);
        stopButton.setEnabled(false);
        PokazCzas.setText(null);
        Toast.makeText(Budzik.this, "Budzik anulowany", Toast.LENGTH_SHORT).show();
    }
}
