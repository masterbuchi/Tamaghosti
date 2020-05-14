package com.google.ar.sceneform.samples.gltf;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;


public class SleepActivity extends AppCompatActivity {
    private int energy;
    ProgressBar prgEnergy;
    private Calendar currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        Intent in = getIntent();
        final int hValue = in.getIntExtra("hungerValue", 0);
        final int slValue = in.getIntExtra("sleepValue", 0);
        final int soValue = in.getIntExtra("socialValue", 0);
        final int tValue = in.getIntExtra("trainingValue", 0);
        Log.d("NeedsDebug", "sleep value: " + slValue);
        this.energy = slValue;
        prgEnergy = (ProgressBar) findViewById(R.id.progressEnergy2);
        prgEnergy.setProgress((int) slValue);

        Log.d("NeedsDebug", "energy first: " + energy);


        Handler handler1 = new Handler();

        currentTime = Calendar.getInstance();
        Calendar updateTime = currentTime;
        updateTime.add(Calendar.SECOND, 1);



        handler1.post(new Runnable() {
            @Override
            public void run() {
                Calendar currentTime;

                while (energy<=100) {
                    currentTime = Calendar.getInstance();
                    if (currentTime.getTimeInMillis() >= updateTime.getTimeInMillis()) {
                        energy ++;
                        prgEnergy.setProgress(energy);
                        updateTime.add(Calendar.SECOND, 1);
                        Log.d("Time", sdf.format(updateTime.getTime()));
                    }
                }

                handler1.removeCallbacksAndMessages(null);
            }
        });


        Button wakeUp = (Button) findViewById(R.id.wkaeUpControl);
        wakeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler1.removeCallbacksAndMessages(null);
                Intent intent = new Intent(SleepActivity.this, GltfActivity.class);
                intent.putExtra("hungerValue", hValue);
                intent.putExtra("sleepValue", energy);
                intent.putExtra("socialValue", soValue);
                intent.putExtra("trainingValue", tValue);
                startActivity(intent);
            }
        });


    }


}


