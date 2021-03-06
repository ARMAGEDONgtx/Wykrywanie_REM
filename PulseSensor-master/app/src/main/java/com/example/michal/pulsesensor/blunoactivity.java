package com.example.michal.pulsesensor;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.michal.pulsesensor.BudziRem.alarmManagerMax;
import static com.example.michal.pulsesensor.BudziRem.pendingIntentMax;

public class blunoactivity  extends BlunoLibrary {
    private Button buttonScan; //referencja do przycisku 'scan'
    private Button buttonSerialSend; //uzywane w poprzednich wersjach
    //private EditText serialSendText; //uzywane w poprzednich wersjach
    private TextView serialReceivedText; //referencja do danych odczytanych
    private TextView TextPulse; //referencja do wyswietlacza pulsu
    private Calendar m_calendar; //referencja do kalendarza, aby uzyskiwac aktualny czas
    public static Integer pulse = 0; //zmienna do algorytmu
    public static boolean obudzono = false; //wykrycie czy budzik zadzwonil
    //************************************************************************************************
    public static double current_numb_probes = 0; // liczba probek ( do odliczania okresu np 5min)
    public static int global_numb_probes = 0; // liczba probek odczytanych od poczatku
    public static double global_mean = 50;  // srednia pulsu od poczatku pomiaru
    public static double current_mean = 50; // srednia z ostatnich (np 5 min)
    public static int numb_probes_to_wait = 100; // strefa nieczulosci, do obliczenia wiarygodnej sredniej pulsu podczas snu
    //************************************************************************************************
    //PAWLAK*****************************************************************************************
    public static boolean czyPolaczono = false;
    //***********************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blunoactivity);
        onCreateProcess();														//onCreate Process w BlunoLibrary

        m_calendar = Calendar.getInstance();
        serialBegin(115200);													//ustawienie baudratu

        TextPulse = (TextView)findViewById(R.id.TextView_Pulse);

        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//połaczenie ui z referencja
        //serialSendText=(EditText) findViewById(R.id.serialSendText);			//połaczenie ui z referencja

   /*     buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//połaczenie ui z referencja

        //obsluga przicsku , uzywana w poprzednich wersjach
        buttonSerialSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                serialSend("a");				//wysylanie 'a' do BLUNO, jako znak , ze chcemy dane
            }
        });*/

        buttonScan = (Button) findViewById(R.id.buttonScan);					//połaczenie ui z referencja

        //dialog do skanowania urządzeń
        buttonScan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                buttonScanOnClickProcess();

            }
        });
    }


    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoLibrary
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //onPauseProcess();														//onPause Process by BlunoLibrary
        Toast.makeText(getBaseContext(), "onPause", Toast.LENGTH_SHORT).show();
    }

    protected void onStop() {
        super.onStop();
        //onStopProcess();														//onStop Process by
        Toast.makeText(getBaseContext(), "onStop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //onDestroyProcess();													//onDestroy Process by BlunoLibrary
        Toast.makeText(getBaseContext(), "onDestroy" , Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                blunoactivity.czyPolaczono = true;
                buttonScan.setText("Connected");
                break;
            case isConnecting:
                buttonScan.setText("Connecting");
                break;
            case isToScan:
                buttonScan.setText("Scan");
                break;
            case isScanning:
                buttonScan.setText("Scanning");
                break;
            case isDisconnecting:
                buttonScan.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    //uzywana w poprzednich wersjach
    private void writeToFile(String data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        String filename = sdf.format(m_calendar.getTime());
        try {
            File root = new File("mi_drive");
            if (!root.exists()) {
                root.mkdirs(); // this will create folder.
            }
            File filepath = new File(root, filename + ".txt");
            FileWriter writer = new FileWriter(filepath);

            writer.append(data);
            writer.flush();
            writer.close();
            Toast.makeText(getBaseContext(), "File save succeded!",
                    Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(getBaseContext(), "File save failed!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override //kiedy otrzymamy dane ta funkcja jest wywolywana
    public void onSerialReceived(String theString) {
        /*m_calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String data = sdf.format(m_calendar.getTime());
        writeToFile(data+"-"+theString+"\r\n");
         */

        // TODO Auto-generated method stub
        theString = theString.replace(theString.substring(theString.length() - 2), "");
        serialReceivedText.append(theString+", "+ global_numb_probes + ", " + current_numb_probes + ", " + current_mean + ", " + global_mean+ "\r\n");
        ((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
        try {
            pulse = Integer.parseInt(theString);
        }
        catch(NumberFormatException e)
        {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }

        TextPulse.setText(theString);

        //ALGORYTM**********************************************************************************

        boolean time_to_wake = false; // sprawdzanie czy jest juz godzina do obudzenia

        if(BudziRem.Godz_forBLE < Calendar.getInstance().getTime().getHours())
        {
            time_to_wake = true;
        }
        else if(BudziRem.Godz_forBLE == Calendar.getInstance().getTime().getHours())
        {
            if(BudziRem.Min_forBLE <= Calendar.getInstance().getTime().getMinutes())
            {
                time_to_wake = true;
            }
        }

        boolean time_to_start_couting = false; // sprawdzanie czy jest juz godzina do obliczania sredniej

        if(BudziRem.Godz_forBLE < (Calendar.getInstance().getTime().getHours() - 1))
        {
            time_to_start_couting = true;
        }
        else if(BudziRem.Godz_forBLE == (Calendar.getInstance().getTime().getHours() - 1))
        {
            if(BudziRem.Min_forBLE <= Calendar.getInstance().getTime().getMinutes())
            {
                time_to_start_couting = true;
            }
        }



        if (!obudzono && time_to_start_couting) // warunek , ze budzik juz zadzwonil

        {
            if (current_numb_probes >= 50 && global_numb_probes >= numb_probes_to_wait) // zamiast 100 wyliczyc przedzial w jakim ma byc tymaczasowa srednia liczona
            {
                if ((current_mean >= global_mean * 1.2) && time_to_wake) // 1.2 wspolczynnik o ile musi byc wieksza tymczasowa srednia od globalnej( z calego pomairu)
                {
                    MainActivity.czyBudzic = -1;
                    //Toast.makeText(context, "REM", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getBaseContext().getApplicationContext(), Alarm2.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(i);
                    obudzono = true;
                    blunoactivity.current_numb_probes = 0;
                    blunoactivity.current_mean = 0;
                    blunoactivity.global_numb_probes = 0;
                    blunoactivity.global_mean = 0;
                    // PAWLAK GRZEBAL*************************************
                    BudziRem.alarmManagerMax.cancel(BudziRem.pendingIntentMax);
                    // ***************************************************
                } else {
                    current_mean = 0;
                    current_numb_probes = 0;
                }
            }
            current_numb_probes++;
            global_numb_probes++;
            current_mean = (current_mean*(current_numb_probes-1)+ pulse) / current_numb_probes;
            global_mean = (global_mean*(global_numb_probes-1) + pulse) / global_numb_probes;

        }
    }

    public void backToMenu(View v){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}