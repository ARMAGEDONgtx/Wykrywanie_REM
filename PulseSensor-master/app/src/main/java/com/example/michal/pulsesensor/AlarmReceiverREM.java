package com.example.michal.pulsesensor;

/**
 * Created by kuba on 2018-01-10.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.IOException;



public class AlarmReceiverREM  extends BroadcastReceiver{

    public static int current_numb_probes = 0; // liczba probek ( do odliczania okresu np 5min)
    public static int global_numb_probes = 0; // liczba probek odczytanych od poczatku
    public static int global_mean = 0;  // srednia pulsu od poczatku pomiaru
    public static int current_mean = 0; // srednia z ostatnich (np 5 min)
    public static int numb_probes_to_wait = 1000; // strefa nieczulosci, do obliczenia wiarygodnej sredniej pulsu podczas snu


    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "onRecive REM", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(context,AlarmReceiver.class);
        context.startService(serviceIntent);

        // WROBSI GRZEBAL*********************************************


        Intent intent2 = new Intent(context, blunoactivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent2);
        //budzikREM(context);
        //openAlarm(context);
    }
    public void openAlarm(Context context){
        Intent i = new Intent(context.getApplicationContext(), Alarm2.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
    public void budzikREM(final Context context) {

        new Thread(new Runnable() {
            public void run() {
                // TUTAJ KOD SPRAWDZAJACY CZY REM JEST CZY NIE
                // JEZELI JEST TO WYWOLAC FUNKCJE openAlarm(context)
                // Nie wiem czy nie bedzie potrzebna petla jakas
                // potestowac
                // jeżeli wykryje REM to dodać linijke jak ponizej
                // MainActivity.czyBudzic=-1;
                /*
                global_mean = 50;
                //Toast.makeText(context, "Budzik REM", Toast.LENGTH_SHORT).show();
                while(true) {
                    if (blunoactivity.new_probe) // warunek , ze nowy pomiar
                    {
                        if (current_numb_probes >= 100 && global_numb_probes > numb_probes_to_wait) // zamiast 100 wyliczyc przedzial w jakim ma byc tymaczasowa srednia liczona
                        {
                            if (current_mean >= global_mean * 1.2) // 1.2 wspolczynnik o ile musi byc wieksza tymczasowa srednia od globalnej( z calego pomairu)
                            {
                                MainActivity.czyBudzic = -1;
                                //Toast.makeText(context, "REM", Toast.LENGTH_SHORT).show();
                                openAlarm(context);

                            } else {
                                current_mean = 0;
                                current_numb_probes = 0;
                            }
                        }

                        current_mean = (current_mean + blunoactivity.pulse) / 2;
                        global_mean = (global_mean + blunoactivity.pulse) / 2;
                        current_numb_probes++;
                        global_numb_probes++;
                        blunoactivity.new_probe = false;

                    }
                }


*/
            }
        }).start();

    }

}
