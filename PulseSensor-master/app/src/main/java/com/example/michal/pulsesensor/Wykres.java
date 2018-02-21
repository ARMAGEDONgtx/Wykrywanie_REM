package com.example.michal.pulsesensor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Wykres extends AppCompatActivity {

    private LineGraphSeries<DataPoint> seria;   // potrzebne do rysowania wykresu
    private String [] fileText;                 // tekst pobrany z pliku
    private GraphView graph;                    // rysuje wykres
    private Spinner spinner;                    // lista plikkow do wczytania
    private FilesAdapter adapter;               // potrzebne do prawidlowego funkcjonowania spinnera
    private Context context;                    // context aktywnosci
    private boolean czyPierwszyRaz;             // czyPierwszyRaz została uruchomiona aktywnosc
    private String fileName;                    // nazwa pliku

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wykres);
        init();
        setSpinner();

      /*  final TextView helloTextView = (TextView) findViewById(R.id.textView);
        String user_greeting = "Love";

        helloTextView.setText(R.string.user_greeting);*/
       // x=0.0;
        //GraphView graph =(GraphView) findViewById(R.id.graph);

        // for(int i= 0; i <500;i++)
        // {
        // x=x+0.1;
        //y=Math.sin(x);
        /*graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(10.0);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        seria.appendData(new DataPoint(x,y),true, 500);

        graph.addSeries(seria);
        graph.getViewport().setMaxY(200.0);*/
    }

   /* public void buttonOnClick(View v)
    {
        GraphView graph =(GraphView) findViewById(R.id.graph);
        //mText= (EditText)findViewById(R.id.editText);
        Button button=(Button) v;
        ((Button) v).setText("Tetno w dół");
        //wartosc= mText.getText().toString();
        //int adds = Integer.parseInt(mText.getText().toString());
        //hallo = Integer.parseInt("0.1");
        //hallo1 =Integer.parseInt(wartosc);
        //hallo=Integer.valueOf(wartosc);
        x=x+0.1;
        //final TextView helloTextView = (TextView) findViewById(R.id.textView);
        //helloTextView.setText(R.string.cos);
        //wartosc1 =String.valueOf(x);
        //helloTextView.setText(wartosc1);

        y=y-1.0;
        seria.appendData(new DataPoint(x,y),true, 500);
        graph.addSeries(seria);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(10.0);
        //Text Wiew wartosc
        final TextView helloTextView = (TextView) findViewById(R.id.textView);
        helloTextView.setText(R.string.cos);
        wartosc1 =String.valueOf(y);
        helloTextView.setText(wartosc1);


    }
    public void buttonOnClick2(View v)
    {
        GraphView graph =(GraphView) findViewById(R.id.graph);
        Button button=(Button) v;
        ((Button) v).setText("Ustaw tętno");
        mText= (EditText)findViewById(R.id.editText2);
        wartosc=mText.getText().toString();
        hallo = Integer.valueOf(wartosc);



        //dziala \/
        x=x+0.1;
        y=hallo;
        seria.appendData(new DataPoint(x,y),true, 500);
        graph.addSeries(seria);

        final TextView helloTextView = (TextView) findViewById(R.id.textView);
        helloTextView.setText(R.string.cos);
        wartosc1 =String.valueOf(y);
        helloTextView.setText(wartosc1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(10.0);
    }
    public void buttonOnClick3(View v)
    {
        GraphView graph =(GraphView) findViewById(R.id.graph);
        //mText= (EditText)findViewById(R.id.editText);
        Button button=(Button) v;
        ((Button) v).setText("Tetno w góre");
        //wartosc= mText.getText().toString();
        //int adds = Integer.parseInt(mText.getText().toString());
        //hallo = Integer.parseInt("0.1");
        //hallo1 =Integer.parseInt(wartosc);
        //hallo=Integer.valueOf(wartosc);
        x=x+0.1;
        //final TextView helloTextView = (TextView) findViewById(R.id.textView);
        //helloTextView.setText(R.string.cos);
        //wartosc1 =String.valueOf(x);
        //helloTextView.setText(wartosc1);

        y=y+1.0;
        seria.appendData(new DataPoint(x,y),true, 500);
        graph.addSeries(seria);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0.5);
        graph.getViewport().setMaxX(10.0);
        //Text Wiew wartosc
        final TextView helloTextView = (TextView) findViewById(R.id.textView);
        helloTextView.setText(R.string.cos);
        wartosc1 =String.valueOf(y);
        helloTextView.setText(wartosc1);

    }
*/
    public void wczytajWykres(View view){ // wczytuje wykres z wybranego w spinnerze pliku
        final String path = MainActivity.pathToFolder + File.separator+fileName;
        File file = new File(path);
        fileText = Load(file);
        String[] puls = new String[fileText.length];
        puls = separacja(0);
        String [] czasMS = separacja(1);
        //godzina = separacja(2);
        graph.setTitle("Wykres "+fileName);
        Rysuj(strToInt(puls), graph);
    }

    int [] strToInt(String [] str){ // konwersja tablic string do tablic int
        int [] tab = new int[str.length];
        tab[0]=52;
        for (int i=1;i<str.length;i++){

            tab[i]=Integer.parseInt(str[i]);
        }
        return tab;
    }
    String [] separacja(int wersja){    // separacja potrzebnych danych z pliku
        if(wersja > 2){
            return null;
        }
        else{
            String [] str2;
            String [] str = new String[fileText.length];
            for (int i=0;i<fileText.length;i++){
                str2 = fileText[i].split(";");
                str[i] = str2[wersja];
            }
            return str;
        }

    }
    public String[] Load(File file)     //wczytaj dane z pliku
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace(); }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int anzahl=0;
        try
        {
            while ((test=br.readLine()) != null)
            {
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try
        {
            while((line=br.readLine())!=null)
            {
                array[i] = line;
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }
    public void Rysuj(int [] puls, GraphView g){    // Rysowanie wykresu
        int x=0;

        seria = new LineGraphSeries<>();

        g.getViewport().setXAxisBoundsManual(true);
        g.getViewport().setMinX(1);
        g.getViewport().setMaxX(puls.length);

        g.getViewport().setYAxisBoundsManual(true);
        g.getViewport().setMinY(0);
        g.getViewport().setMaxY(200.0);

        g.getViewport().setScrollable(true); // enables horizontal scrolling
        g.getViewport().setScrollableY(true); // enables vertical scrolling
        g.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        g.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        seria.setColor(Color.RED);
        for (int y : puls){
            seria.appendData(new DataPoint(x++,y),true, puls.length);
        }
        g.addSeries(seria);
    }
    public String nazwaPliku(){
        String file =/* mText.getText().toString()*/"ggy";
        file = "/"+file+".txt";
        return file;
    }
    public void init(){ //inicjalizacja zmiennych
        graph =(GraphView) findViewById(R.id.graph);
        spinner = (Spinner) findViewById(R.id.spinner);
        context = this.getApplicationContext();
        czyPierwszyRaz = true;
    }
    public void rysujWykres(String nazwaPliku, GraphView g){    // Rusuj wykres  - potrzebne do innej aktywnosci
        final String path = MainActivity.pathToFolder + File.separator+ nazwaPliku;
        File file = new File(path);
        fileText = Load(file);
        String[] puls = new String[fileText.length];
        puls = separacja(0);
        String [] czasMS = separacja(1);
        //godzina = separacja(2);
        g.setTitle("Wykres " + nazwaPliku);
        Rysuj(strToInt(puls),g);
    }
    public String [] listaPlikow(){ // pobiera liste plikow z foledru
        File folder = new File(MainActivity.pathToFolder);
        File[] listOfFiles = folder.listFiles();
        String [] fileNames = new String[listOfFiles.length];
        for (int i=0;i<listOfFiles.length;i++){
            fileNames[i]=listOfFiles[i].getName();
        }
        return fileNames;
    }
    public void setSpinner(){   // ustawienia listy wyboru plikow spinner
        ArrayList<String> arrayList = new ArrayList<String>();
        String [] lista = listaPlikow();
        for(String x : lista){
            arrayList.add(x);
        }
        adapter = new FilesAdapter(context,arrayList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(!czyPierwszyRaz){
                    String clickedItem = (String)adapterView.getItemAtPosition(position);
                    Toast.makeText(Wykres.this,"Wybrano "+ clickedItem  , Toast.LENGTH_SHORT).show();
                    fileName = clickedItem;
                }
                czyPierwszyRaz=false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}

