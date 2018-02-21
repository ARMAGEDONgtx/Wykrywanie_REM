package com.example.michal.pulsesensor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String pathToFolder =   //sciezka do zapisu folderu
            Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"PulseSensor" ;
    public static int czyBudzic = 0;        // zmienna okreslajaca czy budzic czy nie
    TextView puls;                          // wyswietla aktualny puls
    TextView stanPolaczenia;                // wyswietla aktualny stan polaczenia
    Random random;                          // potrzebna do testownia
    static String fileSaveText;             // zmienna statyczna nazywy pliku do zapisu
    static boolean czyZapisywac = true;     // warunek czy plik powinien sie zapisac

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        showPulse(true);

    }
    void init(){    //inicjalizacja zmiennych
        puls = (TextView)findViewById(R.id.Puls);
        stanPolaczenia = (TextView)findViewById(R.id.StanPolaczenia);
        random = new Random();
        fileSaveText=saveFormat();
        stworzFolder();
    }
    public void sendMessage(View view)  // otwiera aktywnosc polacz
    {
        Intent intent = new Intent(this, blunoactivity.class);
        startActivity(intent);
    }

    public void chooseAlarm(View view)  // otwiera aktywnosc wybor budzika
    {
        Intent intent = new Intent(this, ChooseAlarm.class);
        startActivity(intent);
    }
    public void wykres(View view)       // otwiera aktywnosc wybor wykresu do wczytania
    {
        Intent intent = new Intent(this, Wykres.class);
        startActivity(intent);
    }
    public void zamknijAplikacje(View view) // zamyka aplikacje
    {
        finish();
        System.exit(0);
    }
    public void showPulse(boolean czyPolaczono){    // pokazuje puls w oknie
        if (czyPolaczono){

            Thread t=new Thread(){
                @Override
                public void run(){

                    while(!isInterrupted()){

                        try {
                            Thread.sleep(1000);  //1000ms = 1 sec

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if(blunoactivity.czyPolaczono){
                                        int pulsInt= blunoactivity.pulse;
                                        //int pulsInt = 0;
                                        puls.setText(String.valueOf(pulsInt));
                                        stanPolaczenia.setTextColor(Color.GREEN);
                                        stanPolaczenia.setText("Połączono");
                                        stanPolaczenia.setGravity(11);  //center
                                        if(czyZapisywac){
                                            writeToFile(String.valueOf(pulsInt));
                                        }
                                    }
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };

            t.start();
        }
    }
    public static void stworzFolder(){      // sprawdza czy jest stworzony folder do pliku
        File directory = new File(MainActivity.pathToFolder);
        if(!directory.exists()){
            directory.mkdirs();
        }
    }
    private void writeToFile(String data) {   // zapisuje do pliku
        final String filename = MainActivity.pathToFolder + File.separator+fileSaveText;
        try {
            File filepath = new File(filename);
            FileWriter writer = new FileWriter(filepath, true);
            writer.append(data+";"+Calendar.getInstance().getTimeInMillis()+"\n");
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(getBaseContext(), "File save failed!",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public String saveFormat(){     // format zapisu - nazwa pliku
        String years = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String months = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);
        String days = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String hours = String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        String minutes = String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
        String all = years+"_"+months+"_"+days+"_"+hours+"_"+minutes+".txt";
        return all;
    }
}

