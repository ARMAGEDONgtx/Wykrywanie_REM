package com.example.michal.pulsesensor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.Calendar;
import java.util.Date;


public class Budzik extends AppCompatActivity {
    private TimePicker Time;
    private Button UstawButton;
    private TextView PokazCzas;
    private int hour, minute;
    private int currentTimeHours, currentTimeMinutes;
    MediaPlayer music;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budzik);

        Time=(TimePicker)findViewById(R.id.timePicker);
        UstawButton=(Button)findViewById(R.id.UstawBudzik);
        PokazCzas = (TextView) findViewById(R.id.PokazCzas);
        ShowTime();

    }
    public void ShowTime(){

        UstawButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //UstawAlarm(timeInMilis());
                        hour = Time.getCurrentHour();
                        minute = Time.getCurrentMinute();
                        currentTimeHours = Calendar.getInstance().getTime().getHours();
                        currentTimeMinutes = Calendar.getInstance().getTime().getMinutes();
                        int dlugoscSpaniaHours = czasSpaniaGodziny(hour,minute,currentTimeHours,currentTimeMinutes);
                        int dlugoscSpaniaMinutes =  czasSpaniaMinuty(hour,minute,currentTimeHours,currentTimeMinutes);
                        String TimeText;

                        if(dlugoscSpaniaHours>0){
                            TimeText = ". Długość spania: " + dlugoscSpaniaHours + " godzin " + dlugoscSpaniaMinutes + " minut";
                        }
                        else {
                            TimeText = ". Długość spania: " + dlugoscSpaniaMinutes + " minut";
                        }
                        Toast BudzikToast;
                        BudzikToast = Toast.makeText(Budzik.this, "Budzik ustawiony ", Toast.LENGTH_SHORT);
                        BudzikToast.show();
                        PokazCzas.setText("Budzik ustawiony na " + hour +":"+minute + TimeText);
                        UstawAlarm();
                        //UstawAlarm(czasSpania_ms(hour,minute,currentTimeHours,currentTimeMinutes));

                    }
                }
        );
    }

    private int czasSpaniaGodziny(int Hour, int Minute, int CurHour, int CurMin){
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
    private int czasSpaniaMinuty(int Hour, int Minute, int CurHour, int CurMin){
        int minuty = Minute-CurMin;
        if(minuty>=0){
            return (minuty%60);
        }
        else{
            return ((60+minuty)%60);
        }
    }
    private long czasSpania_ms(int Hour, int Minute, int CurHour, int CurMin){       //Czas spania w ms
        long godziny = (czasSpaniaGodziny( Hour,  Minute,  CurHour,  CurMin)*60);
        long minuty = czasSpaniaMinuty( Hour,  Minute,  CurHour,  CurMin);
        long czas= ((godziny + minuty)*60000);
        Date data=new Date(System.currentTimeMillis()+czas);
        return data.getTime();
        //return ((godziny + minuty)*60000);

    }
    private long timeInMilis(){
        Calendar calendar=Calendar.getInstance();
        /*calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                Time.getCurrentHour(),
                Time.getCurrentMinute(),
                0
        );*/
        calendar.set(Calendar.HOUR_OF_DAY, Time.getCurrentHour());
        calendar.set(Calendar.MINUTE, Time.getCurrentMinute());
        return calendar.getTimeInMillis();
    }
    private void UstawAlarm(){
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Time.getCurrentHour());
        calendar.set(Calendar.MINUTE, Time.getCurrentMinute());
        AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent (this, Alarm.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,intent,0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(),pendingIntent);

    }
}
