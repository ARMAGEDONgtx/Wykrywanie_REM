package com.example.michal.pulsesensor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ChooseAlarm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_alarm);
    }

    public void Budzik(View view)
    {
        Intent intent = new Intent(this, Budzik.class);
        startActivity(intent);
    }
    public void BudzikREM(View view)
    {
        Intent intent = new Intent(this, BudziRem.class);
        startActivity(intent);
    }
}