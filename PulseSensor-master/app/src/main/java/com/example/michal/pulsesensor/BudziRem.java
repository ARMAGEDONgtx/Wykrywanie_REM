package com.example.michal.pulsesensor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Random;

public class BudziRem extends AppCompatActivity {

    private NumberPicker minGodz = null;
    private NumberPicker minMinuty = null;
    private NumberPicker maxGodz = null;
    private NumberPicker maxMinuty = null;
    private Button ustawButton= null;
    private Button anulujButton= null;
    private TextView textView = null;
    private Random randomNumber = null;
    private Budzik budzik = null;
    private Calendar calendar;
    private  AlarmManager alarmManager;
    private Intent intent;
    private PendingIntent pendingIntent;
    public static int Godz_forBLE = 0;
    public static int Min_forBLE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budzi_rem);

        init();
        initValue();

        onUstaw();

    }

    public void onUstaw(){
        ustawButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Toast.makeText(BudziRem.this, "Budzik ustawiony ", Toast.LENGTH_SHORT).show();
                        anulujButton.setEnabled(true);
                        UstawAlarm();
                        ustawMaxCzasSpania();
                        TimeText();
                        //Intent intent = new Intent(this, blunoactivity.class);
                        //startActivity(intent);
                    }
                }
        );
    }

    private int losowanie(){
        int granicaRandom = (int)budzik.czasSpania_ms(maxGodz.getValue(),
                maxMinuty.getValue(),minGodz.getValue(), minMinuty.getValue());
        int random = randomNumber.nextInt(granicaRandom);
        return random;
    }
    void ustawCzas(){
        calendar=Calendar.getInstance();
        //WROBSI GRZEBAL****************************************************************************
        calendar.set(Calendar.HOUR_OF_DAY, (Calendar.getInstance().getTime().getHours()));
        calendar.set(Calendar.MINUTE,( Calendar.getInstance().getTime().getMinutes()));
        calendar.set(Calendar.SECOND, (Calendar.getInstance().getTime().getSeconds()+10));
        Godz_forBLE = minGodz.getValue();
        Godz_forBLE = minMinuty.getValue();
        //******************************************************************************************
        //calendar.set(Calendar.HOUR_OF_DAY, (minGodz.getValue()));
        //calendar.set(Calendar.MINUTE,( minMinuty.getValue()));
        if((calendar.getTimeInMillis()-System.currentTimeMillis())<0){
            calendar.set(calendar.DAY_OF_YEAR,(calendar.get(calendar.DAY_OF_YEAR)+1));
        }

    }
    private void init(){
        minGodz = (NumberPicker)findViewById(R.id.minGodz);
        minMinuty = (NumberPicker)findViewById(R.id.minMinuty);
        maxGodz = (NumberPicker)findViewById(R.id.maxGodz);
        maxMinuty = (NumberPicker)findViewById(R.id.maxMinuty);
        ustawButton = (Button)findViewById(R.id.ustawButton);
        anulujButton=(Button)findViewById(R.id.Anuluj);
        anulujButton.setEnabled(false);
        textView = (TextView)findViewById(R.id.textView);
        randomNumber = new Random();
        budzik = new Budzik();
        calendar=Calendar.getInstance();
        setGodz(minGodz);
        setMinuty(minMinuty);
        setGodz(maxGodz);
        setMinuty(maxMinuty);

    }
    private  void setGodz(NumberPicker NP){
        NP.setMaxValue(23);
        NP.setMinValue(0);
        NP.setWrapSelectorWheel(true);
    }
    private void setMinuty(NumberPicker NP){
        NP.setMaxValue(59);
        NP.setMinValue(0);
        NP.setWrapSelectorWheel(true);
    }
    private void initValue(){
        minGodz.setValue(Calendar.getInstance().getTime().getHours());
        minMinuty.setValue(Calendar.getInstance().getTime().getMinutes());
        maxGodz.setValue((Calendar.getInstance().getTime().getHours()+1));
        maxMinuty.setValue(Calendar.getInstance().getTime().getMinutes());
    }

    private void UstawAlarm(){
        ustawCzas();
        MainActivity.czyBudzic=0;
        alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        intent = new Intent (this, AlarmReceiverREM.class);
        pendingIntent=PendingIntent.getBroadcast(this,0,intent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
        //Intent serviceIntent = new Intent(getBaseContext(),AlarmReceiverREM.class);
        //getBaseContext().startService(serviceIntent);
    }
    void TimeText(){
        int hour, minute;
        int currentTimeHours, currentTimeMinutes;
        int dlugoscSpaniaHours;
        int dlugoscSpaniaMinutes;
        Budzik budzik2=new Budzik();
        String TimeText;

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        currentTimeHours = Calendar.getInstance().getTime().getHours();
        currentTimeMinutes = Calendar.getInstance().getTime().getMinutes();
        dlugoscSpaniaHours = budzik2.czasSpaniaGodziny(hour,minute,currentTimeHours,currentTimeMinutes);
        dlugoscSpaniaMinutes =  budzik2.czasSpaniaMinuty(hour,minute,currentTimeHours,currentTimeMinutes);

        if(dlugoscSpaniaHours>0){
            TimeText = ". Długość spania: " + dlugoscSpaniaHours + " godzin " + dlugoscSpaniaMinutes + " minut";
        }
        else {
            TimeText = ". Długość spania: " + dlugoscSpaniaMinutes + " minut";
        }

        textView.setText("Budzik ustawiony na " +calendar.getTime() + TimeText);
    }
    public void AnulujBudzik(View view){

        alarmManager.cancel(pendingIntent);
        anulujButton.setEnabled(false);
        textView.setText(null);
        Toast.makeText(BudziRem.this, "Budzik anulowany", Toast.LENGTH_SHORT).show();
    }
    public void ustawMaxCzasSpania(){
        Calendar calendarMx= Calendar.getInstance();
        calendarMx.set(Calendar.HOUR_OF_DAY, (maxGodz.getValue()));
        calendarMx.set(Calendar.MINUTE,( maxMinuty.getValue()));
        AlarmManager alarmManagerMx=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intentMx = new Intent (this, AlarmReceiver.class);
        PendingIntent pendingIntentMx=PendingIntent.getBroadcast(this,0,intentMx,0);
        alarmManagerMx.set( AlarmManager.RTC_WAKEUP, calendarMx.getTimeInMillis(),pendingIntentMx);
    }
}
