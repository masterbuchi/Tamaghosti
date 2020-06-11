package com.google.ar.sceneform.samples.gltf;


import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Thread.sleep;


public class SleepActivity extends AppCompatActivity {

    private int energy;
    private ProgressBar prgEnergy;
    private Calendar currentTime;
    private volatile int safeEnergy;
    private volatile boolean stopThread = false;
    private TextView progress;

    private PersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        persistenceManager = new PersistenceManager(getApplicationContext());

        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        final int slValue = persistenceManager.getInt("sleep",0);

        Log.d("SLEEP", "sleep value: " + slValue);
        this.energy = slValue;
        prgEnergy = findViewById(R.id.progressEnergy2);
        prgEnergy.setProgress(slValue);
        progress = findViewById(R.id.sleepProgress);

        Log.d("SLEEP", "energy first: " + energy);


        //Handler handlerSleep = new Handler();

        currentTime = Calendar.getInstance();
        Calendar updateTime = currentTime;
        updateTime.add(Calendar.SECOND, 1);

        

        Button wakeUp = findViewById(R.id.wkaeUpControl);
        wakeUp.setOnClickListener(v -> {
            stopThread(null);

            Log.d("SLEEP", "safeEnergy end: " + safeEnergy);

            Intent intent = new Intent(SleepActivity.this, ArActivity.class);

            startActivity(intent);
        });

        startThread(null);
    }
    public void startThread(View view){
        stopThread = false;
        ExampleRunnable runnable = new ExampleRunnable(energy);
        new Thread(runnable).start();
    }
    public void stopThread(View view) {
        stopThread = true;
    }
    class ExampleRunnable implements Runnable {
        int energy;
        ExampleRunnable(int energy) {
            this.energy = energy;
        }

        @Override
        public void run() {
            for (int a = energy; a <= 100; a++) {
                if (stopThread)
                    return;
                if (energy > 100)
                    return;
                runOnUiThread(() -> {
                    prgEnergy.setProgress(energy);
                    progress.setText(energy + "%");
                    if (energy % 10 == 0)
                        safeEnergy = energy;
                    Log.d("SLEEP", "progress in 10er steps: " + safeEnergy);
                    Log.d("SLEEP", "progress is growing: " + energy);
                    energy ++;

                    persistenceManager.saveInt("sleep", safeEnergy);

                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}


